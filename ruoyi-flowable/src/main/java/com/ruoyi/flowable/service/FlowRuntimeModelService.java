package com.ruoyi.flowable.service;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.api.io.InputStreamProvider;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 运行时节点配置模型。
 *
 * <p>操作者、操作按钮、字段权限、抄送、签字意见等节点配置都写在流程 XML 里。
 * 为了让这些配置「保存即生效」，运行时优先读取设计器模型（ACT_RE_MODEL）中最新保存的 XML，
 * 而不是已部署版本的 XML——部署只决定流程结构（节点、连线、条件），
 * 节点上的配置类调整无需重新发版。</p>
 *
 * <p>模型 XML 解析结果按「模型ID + 最后更新时间」缓存，保存后自动失效；
 * 模型不存在或解析失败时回退到已部署的流程定义 XML。</p>
 */
@Component
public class FlowRuntimeModelService
{
    @Autowired
    private RepositoryService repositoryService;

    /** 模型解析缓存：key=模型ID */
    private final Map<String, CachedModel> cache = new ConcurrentHashMap<>();

    /**
     * 按流程定义ID获取运行时配置模型：取该流程 Key 对应的设计器模型最新 XML
     */
    public BpmnModel getByDefinitionId(String processDefinitionId)
    {
        if (processDefinitionId == null || processDefinitionId.isEmpty())
        {
            return null;
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (definition == null)
        {
            return repositoryService.getBpmnModel(processDefinitionId);
        }
        BpmnModel model = loadModelXml(definition.getKey());
        return model != null ? model : repositoryService.getBpmnModel(processDefinitionId);
    }

    /** 按流程 Key 获取运行时配置模型（发起预览等无流程定义实例的场景） */
    public BpmnModel getByProcessKey(String processKey)
    {
        if (processKey == null || processKey.isEmpty())
        {
            return null;
        }
        return loadModelXml(processKey);
    }

    /** 读取模型最新保存的 XML 并解析；失败返回 null，由调用方回退部署版本 */
    private BpmnModel loadModelXml(String processKey)
    {
        try
        {
            List<Model> models = repositoryService.createModelQuery()
                    .modelKey(processKey)
                    .orderByModelVersion().desc()
                    .listPage(0, 1);
            if (models.isEmpty())
            {
                return null;
            }
            Model model = models.get(0);
            Date updated = model.getLastUpdateTime();
            String cacheKey = model.getId();
            CachedModel cached = cache.get(cacheKey);
            if (cached != null && cached.updateTime != null && updated != null && cached.updateTime.equals(updated))
            {
                return cached.model;
            }
            byte[] xml = repositoryService.getModelEditorSource(model.getId());
            if (xml == null || xml.length == 0)
            {
                return null;
            }
            BpmnModel parsed = new BpmnXMLConverter().convertToBpmnModel(new ByteArrayInput(xml), false, true);
            if (parsed == null || parsed.getMainProcess() == null)
            {
                return null;
            }
            cache.put(cacheKey, new CachedModel(updated, parsed));
            return parsed;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 缓存项：模型最后更新时间 + 解析结果 */
    private static final class CachedModel
    {
        private final Date updateTime;
        private final BpmnModel model;

        private CachedModel(Date updateTime, BpmnModel model)
        {
            this.updateTime = updateTime;
            this.model = model;
        }
    }

    /** 模型 XML 字节流（BpmnXMLConverter 入参） */
    private static final class ByteArrayInput implements InputStreamProvider
    {
        private final byte[] bytes;

        private ByteArrayInput(byte[] bytes)
        {
            this.bytes = bytes;
        }

        @Override
        public java.io.InputStream getInputStream()
        {
            return new ByteArrayInputStream(bytes);
        }
    }
}
