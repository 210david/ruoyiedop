package com.ruoyi.flowable.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowModel;
import com.ruoyi.flowable.service.IFlowModelService;

/**
 * 流程模型 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FlowModelServiceImpl implements IFlowModelService
{
    @Autowired
    private RepositoryService repositoryService;

    /** 空模型 BPMN XML 模板 */
    private static final String EMPTY_BPMN_TEMPLATE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "xmlns:flowable=\"http://flowable.org/bpmn\" " +
        "xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" " +
        "xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" " +
        "xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" " +
        "targetNamespace=\"http://flowable.org/bpmn\">\n" +
        "  <process id=\"%s\" name=\"%s\" isExecutable=\"true\">\n" +
        "    <startEvent id=\"StartEvent_1\"/>\n" +
        "  </process>\n" +
        "</definitions>";

    @Override
    public List<FlowModel> selectModelList(FlowModel query)
    {
        ModelQuery modelQuery = repositoryService.createModelQuery().orderByCreateTime().desc();
        if (StringUtils.isNotEmpty(query.getModelName()))
        {
            modelQuery.modelNameLike("%" + query.getModelName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getModelKey()))
        {
            modelQuery.modelKey(query.getModelKey());
        }
        if (StringUtils.isNotEmpty(query.getCategory()))
        {
            modelQuery.modelCategory(query.getCategory());
        }
        List<Model> models = modelQuery.list();
        List<FlowModel> list = new ArrayList<>();
        for (Model model : models)
        {
            FlowModel fm = convert(model);
            // 判断是否已部署
            long count = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(model.getKey()).count();
            fm.setDeployed(count > 0 ? "1" : "0");
            list.add(fm);
        }
        return list;
    }

    @Override
    public FlowModel selectModelById(String modelId)
    {
        Model model = repositoryService.getModel(modelId);
        if (model == null)
        {
            throw new ServiceException("流程模型不存在");
        }
        FlowModel fm = convert(model);
        byte[] bytes = repositoryService.getModelEditorSource(modelId);
        if (bytes != null)
        {
            fm.setBpmnXml(new String(bytes, StandardCharsets.UTF_8));
        }
        return fm;
    }

    @Override
    @Transactional
    public String insertModel(FlowModel flowModel)
    {
        if (StringUtils.isEmpty(flowModel.getModelKey()))
        {
            throw new ServiceException("模型Key不能为空");
        }
        // Key 唯一性校验
        long count = repositoryService.createModelQuery().modelKey(flowModel.getModelKey()).count();
        if (count > 0)
        {
            throw new ServiceException("模型Key已存在：" + flowModel.getModelKey());
        }
        Model model = repositoryService.newModel();
        model.setName(flowModel.getModelName());
        model.setKey(flowModel.getModelKey());
        model.setCategory(flowModel.getCategory());
        model.setMetaInfo(buildMetaInfo(flowModel.getDescription()));
        repositoryService.saveModel(model);
        // 初始化空 BPMN XML
        String xml = String.format(EMPTY_BPMN_TEMPLATE, flowModel.getModelKey(), flowModel.getModelName());
        repositoryService.addModelEditorSource(model.getId(), xml.getBytes(StandardCharsets.UTF_8));
        return model.getId();
    }

    @Override
    @Transactional
    public int updateModel(FlowModel flowModel)
    {
        Model model = repositoryService.getModel(flowModel.getModelId());
        if (model == null)
        {
            throw new ServiceException("流程模型不存在");
        }
        if (StringUtils.isNotEmpty(flowModel.getModelName()))
        {
            model.setName(flowModel.getModelName());
        }
        if (StringUtils.isNotEmpty(flowModel.getCategory()))
        {
            model.setCategory(flowModel.getCategory());
        }
        model.setMetaInfo(buildMetaInfo(flowModel.getDescription()));
        repositoryService.saveModel(model);
        if (StringUtils.isNotEmpty(flowModel.getBpmnXml()))
        {
            repositoryService.addModelEditorSource(model.getId(),
                    flowModel.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        }
        return 1;
    }

    @Override
    @Transactional
    public int deleteModelByIds(String[] modelIds)
    {
        int rows = 0;
        for (String modelId : modelIds)
        {
            Model model = repositoryService.getModel(modelId);
            if (model == null)
            {
                continue;
            }
            // 删除该模型对应的所有部署及流程定义
            List<Deployment> deployments = repositoryService.createDeploymentQuery()
                    .deploymentName(model.getName()).list();
            repositoryService.deleteModel(modelId);
            rows++;
        }
        return rows;
    }

    @Override
    @Transactional
    public int deployModel(String modelId)
    {
        Model model = repositoryService.getModel(modelId);
        if (model == null)
        {
            throw new ServiceException("流程模型不存在");
        }
        byte[] bpmnBytes = repositoryService.getModelEditorSource(modelId);
        if (bpmnBytes == null || bpmnBytes.length == 0)
        {
            throw new ServiceException("模型尚未设计流程图，请先保存BPMN XML");
        }
        String resourceName = model.getKey() + ".bpmn20.xml";
        repositoryService.createDeployment()
                .name(model.getName())
                .key(model.getKey())
                .category(model.getCategory())
                .addBytes(resourceName, bpmnBytes)
                .deploy();
        return 1;
    }

    @Override
    public String getDefinitionBpmnXml(String processDefinitionId)
    {
        ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在");
        }
        try
        {
            return IOUtils.toString(repositoryService.getResourceAsStream(
                    definition.getDeploymentId(), definition.getResourceName()), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException("读取流程定义XML失败：" + e.getMessage());
        }
    }

    @Override
    public List<FlowModel> selectDefinitionList(FlowModel query)
    {
        ProcessDefinitionQuery defQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion().orderByProcessDefinitionKey().asc();
        if (StringUtils.isNotEmpty(query.getModelName()))
        {
            defQuery.processDefinitionNameLike("%" + query.getModelName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getModelKey()))
        {
            defQuery.processDefinitionKey(query.getModelKey());
        }
        if (StringUtils.isNotEmpty(query.getCategory()))
        {
            defQuery.processDefinitionCategory(query.getCategory());
        }
        List<ProcessDefinition> definitions = defQuery.list();
        List<FlowModel> list = new ArrayList<>();
        for (ProcessDefinition def : definitions)
        {
            FlowModel fm = new FlowModel();
            fm.setModelId(def.getId());
            fm.setModelKey(def.getKey());
            fm.setModelName(def.getName());
            fm.setCategory(def.getCategory());
            fm.setVersion(def.getVersion());
            fm.setDeploymentId(def.getDeploymentId());
            fm.setSuspended(def.isSuspended());
            fm.setDeployed("1");
            try
            {
                Deployment deployment = repositoryService.createDeploymentQuery()
                        .deploymentId(def.getDeploymentId()).singleResult();
                if (deployment != null)
                {
                    fm.setDeploymentTime(deployment.getDeploymentTime());
                }
            }
            catch (Exception ignored) { }
            list.add(fm);
        }
        return list;
    }

    @Override
    public int changeDefinitionState(String processDefinitionId, boolean suspend)
    {
        ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在");
        }
        if (suspend)
        {
            repositoryService.suspendProcessDefinitionById(processDefinitionId);
        }
        else
        {
            repositoryService.activateProcessDefinitionById(processDefinitionId);
        }
        return 1;
    }

    private FlowModel convert(Model model)
    {
        FlowModel fm = new FlowModel();
        fm.setModelId(model.getId());
        fm.setModelName(model.getName());
        fm.setModelKey(model.getKey());
        fm.setCategory(model.getCategory());
        fm.setVersion(model.getVersion());
        fm.setCreateTime(model.getCreateTime() != null ? model.getCreateTime() : new Date());
        return fm;
    }

    private String buildMetaInfo(String description)
    {
        return "{\"description\":\"" + (description == null ? "" : description.replace("\"", "'")) + "\"}";
    }
}
