package com.ruoyi.flowable.service.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.stream.XMLStreamReader;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.apache.commons.io.IOUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.flowable.domain.FlowModel;
import com.ruoyi.flowable.domain.FlowProcdefExt;
import com.ruoyi.flowable.config.FlowReturnConstants;
import com.ruoyi.flowable.mapper.FlowFormMapper;
import com.ruoyi.flowable.mapper.FlowProcdefExtMapper;
import com.ruoyi.flowable.service.IFlowModelService;

/**
 * 流程模型 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FlowModelServiceImpl implements IFlowModelService
{
    private static final Logger log = LoggerFactory.getLogger(FlowModelServiceImpl.class);
    @Autowired
    private RepositoryService repositoryService;

    /** 删除模型前判定「是否已产生流程数据」需要查历史实例 */
    @Autowired
    private HistoryService historyService;

    @Autowired
    private FlowFormMapper flowFormMapper;

    @Autowired
    private FlowProcdefExtMapper flowProcdefExtMapper;

    @Autowired
    private org.flowable.engine.ProcessEngineConfiguration processEngineConfiguration;

    /** 空模型 BPMN XML 模板（含 BPMNDiagram 图形段，避免 bpmn-js 报 no diagram to display） */
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
        "    <startEvent id=\"StartEvent_1\" name=\"开始\"/>\n" +
        "  </process>\n" +
        "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
        "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"%s\">\n" +
        "      <bpmndi:BPMNShape id=\"StartEvent_1_di\" bpmnElement=\"StartEvent_1\">\n" +
        "        <omgdc:Bounds x=\"173\" y=\"102\" width=\"36\" height=\"36\"/>\n" +
        "      </bpmndi:BPMNShape>\n" +
        "    </bpmndi:BPMNPlane>\n" +
        "  </bpmndi:BPMNDiagram>\n" +
        "</definitions>";

    /** BPMN 根节点 targetNamespace 属性匹配（部署前按模型分类改写） */
    private static final java.util.regex.Pattern TARGET_NAMESPACE_PATTERN =
            java.util.regex.Pattern.compile("targetNamespace\\s*=\\s*\"([^\"]*)\"");

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
            // 已产生的流程实例数（含已办结）：>0 时前端标注「已发起 N 单」，并禁止删除
            fm.setInstanceCount(countInstances(model.getKey()));
            // 判断是否已部署，并取「最新部署版本」作为列表展示版本
            // 注意：act_re_model.VERSION_ 是模型编辑修订号，不等于流程定义部署版本，
            // 因此列表版本以 act_re_procdef 的最高版本为准（未部署时清空，前端显示为 -）
            List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(model.getKey())
                    .orderByProcessDefinitionVersion().desc()
                    .list();
            if (definitions.isEmpty())
            {
                fm.setDeployed("0");
                fm.setVersion(null);
            }
            else
            {
                ProcessDefinition newest = definitions.get(0);
                fm.setDeployed("1");
                fm.setVersion(newest.getVersion());
                fm.setDeploymentId(newest.getDeploymentId());
                // 挂起状态：与版本编辑弹框口径统一，列表可直接标识该部署版本是否可发起
                fm.setSuspended(newest.isSuspended());
                // 活动版本标志（扩展表）：列表可直接标识当前生效版本
                FlowProcdefExt ext = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(newest.getId());
                if (ext != null)
                {
                    fm.setActiveFlag(ext.getActiveFlag());
                }
            }
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
        model.setMetaInfo(buildMetaInfo(flowModel.getDescription(), flowModel.getFormId()));
        repositoryService.saveModel(model);
        // 初始化空 BPMN XML
        String xml = String.format(EMPTY_BPMN_TEMPLATE, flowModel.getModelKey(), flowModel.getModelName(),
                flowModel.getModelKey());
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
        JSONObject oldMeta = parseMetaInfo(model.getMetaInfo());
        String desc = flowModel.getDescription() != null ? flowModel.getDescription()
                : oldMeta.getString("description");
        // 前端传 formId 覆盖；unbind=true 解除绑定；未传则沿用原绑定
        Long formId;
        if (Boolean.TRUE.equals(flowModel.getUnbind()))
        {
            formId = null;
        }
        else
        {
            formId = flowModel.getFormId() != null ? flowModel.getFormId() : oldMeta.getLong("formId");
        }
        // 高级设置：前端传值覆盖，未传沿用原值（存于 metaInfo）
        String deployMode = flowModel.getDeployMode() != null ? flowModel.getDeployMode()
                : oldMeta.getString("deployMode");
        Boolean locked = flowModel.getLocked() != null ? flowModel.getLocked()
                : oldMeta.getBoolean("locked");
        String helpDoc = flowModel.getHelpDoc() != null ? flowModel.getHelpDoc()
                : oldMeta.getString("helpDoc");
        Integer orderNum = flowModel.getOrderNum() != null ? flowModel.getOrderNum()
                : oldMeta.getInteger("orderNum");
        // 流程编号规则：前端传 JSON 字符串覆盖，未传沿用原规则
        String processNoRule = flowModel.getProcessNoRule() != null ? flowModel.getProcessNoRule()
                : oldMeta.getString("processNoRule");
        model.setMetaInfo(buildMetaInfo(desc, formId, deployMode, locked, helpDoc, orderNum, processNoRule));
        repositoryService.saveModel(model);
        // 名称同步：模型改名后，已部署的流程定义（act_re_procdef.NAME_）仍是部署时的快照，
        // 不同步会导致前台/流程定义列表显示旧名。此处按 key 同步所有已部署定义名。
        if (StringUtils.isNotEmpty(flowModel.getModelName()))
        {
            syncDefinitionName(model.getKey(), model.getName());
        }
        if (StringUtils.isNotEmpty(flowModel.getBpmnXml()))
        {
            repositoryService.addModelEditorSource(model.getId(),
                    flowModel.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        }
        return 1;
    }

    /**
     * 同步模型名称到该 Key 下所有已部署的流程定义。
     *
     * 背景：Flowable 部署时会把模型的 NAME_ 快照写入 act_re_procdef.NAME_，
     * 之后修改模型名不会自动同步，导致前台流程发起/流程定义列表仍显示部署时的旧名。
     * Flowable 7.2 的 RepositoryService 未提供"重命名流程定义"的 API
     * （只有 setProcessDefinitionCategory），故按 Key 定向更新 act_re_procdef.NAME_。
     * 仅更新名称字段的 DML，不改表结构、不改引擎行为。
     *
     * @param processKey 流程 Key
     * @param newName    新的流程名称
     */
    private void syncDefinitionName(String processKey, String newName)
    {
        if (StringUtils.isEmpty(processKey) || StringUtils.isEmpty(newName))
        {
            return;
        }
        flowProcdefExtMapper.updateDefinitionNameByKey(processKey, newName);
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
            String processKey = model.getKey();
            // 防误删：已经跑过的流程（有实例，含已办结）不允许删模型。
            // 否则 deleteDeployment(cascade=true) 会把该流程的单据、审批意见、附件一起删掉，不可恢复。
            long instances = countInstances(processKey);
            if (instances > 0)
            {
                throw new ServiceException("流程《" + model.getName() + "》已产生 " + instances
                        + " 个流程实例（含已办结），删除会一并删掉这些单据与审批记录，因此已阻止本次删除。"
                        + "如果只是不再启用该流程，请在设计器的「版本管理」中挂起该版本（挂起后不能发起新流程，历史数据全部保留）。");
            }
            // 只删该 Key 下的部署：原来按「部署名 = 模型名」匹配，同名流程会误删其它模型的部署
            Set<String> deploymentIds = new LinkedHashSet<>();
            if (StringUtils.isNotEmpty(processKey))
            {
                List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processKey).list();
                for (ProcessDefinition definition : definitions)
                {
                    deploymentIds.add(definition.getDeploymentId());
                }
            }
            for (String deploymentId : deploymentIds)
            {
                // cascade=false：即使有残留运行实例也不允许连带删除，异常向上抛，避免静默丢数据
                repositoryService.deleteDeployment(deploymentId, false);
            }
            repositoryService.deleteModel(modelId);
            rows++;
        }
        return rows;
    }

    /**
     * 统计某个流程 Key 已产生的实例数（含已办结）。
     *
     * <p>用于「有数据就不允许删模型」的校验，也是列表上「已发起 N 单」标识的数据源。</p>
     */
    private long countInstances(String processKey)
    {
        if (StringUtils.isEmpty(processKey))
        {
            return 0L;
        }
        try
        {
            return historyService.createHistoricProcessInstanceQuery()
                    .processDefinitionKey(processKey).count();
        }
        catch (Exception e)
        {
            log.warn("统计流程实例数失败（{}）：{}", processKey, e.getMessage());
            return 0L;
        }
    }

    @Override
    @Transactional
    public int deployModel(String modelId, String mode, String remark, Boolean setActive)
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
        // 部署前把 BPMN targetNamespace 改写为模型真实分类：
        // Flowable 部署时 act_re_procdef.CATEGORY_ 取自 BPMN 的 targetNamespace，
        // 若不改写会写入 Flowable 默认命名空间 http://flowable.org/bpmn，导致前台分类显示为 URL
        byte[] deployBytes = normalizeBpmnNamespace(bpmnBytes, model.getCategory());
        // 在 <process> 上记录本次部署绑定的数据表表单ID，供后续「结构对比」判断是否更换数据表
        deployBytes = injectFormId(deployBytes, parseMetaInfo(model.getMetaInfo()).getLong("formId"));
        // "提交申请"发起人节点：让「驳回 → 退回发起人」有落点（无入线，正常流转不经过）
        deployBytes = injectInitiatorNode(deployBytes);
        repositoryService.createDeployment()
                .name(model.getName())
                .key(model.getKey())
                .category(model.getCategory())
                .addBytes(resourceName, deployBytes)
                .deploy();
        // 刚部署的新版本 = 该Key下最高版本
        ProcessDefinition newest = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(model.getKey())
                .orderByProcessDefinitionVersion().desc()
                .list().get(0);
        // replace 模式：覆盖当前版本——部署成功后停用该Key下所有旧版本定义并停用其活动标志，
        // 运行中实例继续按原版本走完，新发起实例落到新版本；新版本自动设为活动版本
        boolean active = Boolean.TRUE.equals(setActive) || "replace".equals(mode);
        if ("replace".equals(mode))
        {
            List<ProcessDefinition> olds = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(model.getKey())
                    .orderByProcessDefinitionVersion().asc()
                    .list();
            // 最高版本是刚部署的新版本，只停用其余旧版本
            for (int i = 0; i < olds.size() - 1; i++)
            {
                ProcessDefinition old = olds.get(i);
                if (!old.isSuspended())
                {
                    repositoryService.suspendProcessDefinitionById(old.getId());
                }
                clearActiveFlag(old.getId());
            }
        }
        // 版本备注 + 活动版本标志写入扩展表
        saveProcdefExt(newest.getId(), newest.getKey(), newest.getVersion(), remark, active);
        return 1;
    }

    /**
     * 部署前改写 BPMN 的 targetNamespace 为模型真实分类。
     *
     * 背景：Flowable 部署时 act_re_procdef.CATEGORY_ 取自 BPMN 根节点 targetNamespace，
     * 而非 deployment 的 category。模型侧分类为 oa/hr/fin 等业务值，
     * 若不改写，流程定义分类会被写成 Flowable 默认命名空间 http://flowable.org/bpmn，
     * 导致前台流程发起页分类标签显示为 URL。
     *
     * @param bpmnBytes 原始 BPMN XML 字节
     * @param category  模型分类（为空时不改写）
     * @return 改写后的字节；无法解析或无需改写时返回原字节
     */
    private byte[] normalizeBpmnNamespace(byte[] bpmnBytes, String category)
    {
        if (bpmnBytes == null || bpmnBytes.length == 0 || StringUtils.isEmpty(category))
        {
            return bpmnBytes;
        }
        try
        {
            String xml = new String(bpmnBytes, StandardCharsets.UTF_8);
            java.util.regex.Matcher matcher = TARGET_NAMESPACE_PATTERN.matcher(xml);
            if (!matcher.find())
            {
                return bpmnBytes;
            }
            String current = matcher.group(1);
            if (category.equals(current))
            {
                return bpmnBytes;
            }
            String replaced = matcher.replaceFirst("targetNamespace=\"" + category + "\"");
            return replaced.getBytes(StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            // 改写失败不阻断部署，退回原始内容
            return bpmnBytes;
        }
    }

    @Override
    @Transactional
    public int updateDefinitionRemark(String processDefinitionId, String remark)
    {
        ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在");
        }
        saveProcdefExt(processDefinitionId, definition.getKey(), definition.getVersion(), remark, false);
        return 1;
    }

    @Override
    @Transactional
    public int setActiveDefinition(String processDefinitionId)
    {
        ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在");
        }
        if (definition.isSuspended())
        {
            throw new ServiceException("该版本已挂起，请先激活后再设为当前版本");
        }
        // 同Key其他版本取消活动标志，本版本设为活动版本
        List<ProcessDefinition> versions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(definition.getKey())
                .list();
        for (ProcessDefinition def : versions)
        {
            clearActiveFlag(def.getId());
        }
        saveProcdefExt(processDefinitionId, definition.getKey(), definition.getVersion(), null, true);
        return 1;
    }

    /** 扩展表 upsert：存在则按需更新备注/活动标志，不存在则插入 */
    private void saveProcdefExt(String procdefId, String procKey, Integer version, String remark, boolean active)
    {
        FlowProcdefExt exist = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(procdefId);
        Date now = new Date();
        if (exist == null)
        {
            FlowProcdefExt ext = new FlowProcdefExt();
            ext.setProcdefId(procdefId);
            ext.setProcKey(procKey);
            ext.setVersion(version);
            ext.setRemark(remark == null ? "" : remark);
            ext.setActiveFlag(active ? "1" : "0");
            ext.setCreateTime(now);
            ext.setUpdateTime(now);
            flowProcdefExtMapper.insertFlowProcdefExt(ext);
        }
        else
        {
            FlowProcdefExt update = new FlowProcdefExt();
            update.setProcdefId(procdefId);
            update.setUpdateTime(now);
            if (remark != null)
            {
                update.setRemark(remark);
            }
            if (active)
            {
                update.setActiveFlag("1");
            }
            flowProcdefExtMapper.updateFlowProcdefExt(update);
        }
    }

    /** 清除指定定义的活动版本标志 */
    private void clearActiveFlag(String procdefId)
    {
        FlowProcdefExt exist = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(procdefId);
        if (exist != null && "1".equals(exist.getActiveFlag()))
        {
            FlowProcdefExt update = new FlowProcdefExt();
            update.setProcdefId(procdefId);
            update.setActiveFlag("0");
            update.setUpdateTime(new Date());
            flowProcdefExtMapper.updateFlowProcdefExt(update);
        }
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
    public String getDefinitionBpmnXmlByKey(String processKey)
    {
        ProcessDefinition target = resolveDefinitionByKey(processKey);
        try
        {
            return IOUtils.toString(repositoryService.getResourceAsStream(
                    target.getDeploymentId(), target.getResourceName()), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException("读取流程定义XML失败：" + e.getMessage());
        }
    }

    /**
     * 按流程 Key 定位发起时使用的流程定义：优先活动版本，无活动标志时回退最高版本
     */
    private ProcessDefinition resolveDefinitionByKey(String processKey)
    {
        // 取当前活动版本；无活动标志时回退到最高版本（与发起路由口径一致）
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion().desc()
                .list();
        if (defs.isEmpty())
        {
            throw new ServiceException("流程[" + processKey + "]尚未部署");
        }
        ProcessDefinition target = defs.get(0);
        for (ProcessDefinition def : defs)
        {
            FlowProcdefExt ext = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(def.getId());
            if (ext != null && "1".equals(ext.getActiveFlag()) && !def.isSuspended())
            {
                target = def;
                break;
            }
        }
        return target;
    }

    @Override
    public byte[] generateDefinitionDiagram(String processKey)
    {
        ProcessDefinition target = resolveDefinitionByKey(processKey);
        // 1. 优先取部署时自带的流程图资源（部分历史版本未生成，DGRM_RESOURCE_NAME_ 为空）
        try (java.io.InputStream in = repositoryService.getProcessDiagram(target.getId()))
        {
            if (in != null)
            {
                return in.readAllBytes();
            }
        }
        catch (Exception ignored)
        {
            // 资源缺失时走下方动态生成
        }
        // 2. 兜底：由 BPMN 模型动态生成流程图（无实例，不做节点高亮）
        try
        {
            org.flowable.bpmn.model.BpmnModel bpmnModel =
                    repositoryService.getBpmnModel(target.getId());
            if (bpmnModel == null)
            {
                throw new ServiceException("该流程定义缺少 BPMN 模型，无法生成流程图");
            }
            org.flowable.image.ProcessDiagramGenerator generator =
                    processEngineConfiguration.getProcessDiagramGenerator();
            try (java.io.InputStream in = generator.generateDiagram(bpmnModel, "png",
                    java.util.Collections.emptyList(), java.util.Collections.emptyList(),
                    "宋体", "宋体", "宋体", null, 1.0, true))
            {
                return in.readAllBytes();
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("生成流程图失败：" + e.getMessage());
        }
    }

    @Override
    public List<FlowModel> selectDefinitionList(FlowModel query)
    {
        ProcessDefinitionQuery defQuery = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().asc();
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
            // 版本备注 + 活动版本标志（扩展表）
            FlowProcdefExt ext = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(def.getId());
            if (ext != null)
            {
                fm.setRemark(ext.getRemark());
                fm.setActiveFlag(ext.getActiveFlag());
            }
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
        // 按流程 key 聚合：每个流程只保留 1 条，避免同一流程的历史版本被平铺
        // （前台流程发起页据此展示，与后台流程模型数量口径一致）
        if (Boolean.TRUE.equals(query.getOnlyActive()) || query.getOnlyActive() == null)
        {
            list = aggregateByProcessKey(list);
        }
        return list;
    }

    /**
     * 按流程 key 聚合，每个 key 只保留 1 条：
     * 优先取活动版本（activeFlag=1），无活动版本时取最高版本；
     * 多个活动版本时取版本号最高者。返回顺序保持不变。
     */
    private List<FlowModel> aggregateByProcessKey(List<FlowModel> source)
    {
        Map<String, FlowModel> picked = new LinkedHashMap<>();
        for (FlowModel fm : source)
        {
            String key = fm.getModelKey();
            FlowModel exist = picked.get(key);
            if (exist == null || isBetterVersion(fm, exist))
            {
                picked.put(key, fm);
            }
        }
        return new ArrayList<>(picked.values());
    }

    /** fm 是否比 exist 更适合作为该 key 的代表版本 */
    private boolean isBetterVersion(FlowModel fm, FlowModel exist)
    {
        boolean fmActive = "1".equals(fm.getActiveFlag());
        boolean existActive = "1".equals(exist.getActiveFlag());
        if (fmActive != existActive)
        {
            return fmActive;
        }
        int fmVer = fm.getVersion() == null ? 0 : fm.getVersion();
        int existVer = exist.getVersion() == null ? 0 : exist.getVersion();
        return fmVer > existVer;
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
        JSONObject meta = parseMetaInfo(model.getMetaInfo());
        fm.setDescription(meta.getString("description"));
        Long formId = meta.getLong("formId");
        fm.setFormId(formId);
        // 高级设置（存于 metaInfo）
        fm.setDeployMode(meta.getString("deployMode"));
        fm.setLocked(meta.getBoolean("locked"));
        fm.setHelpDoc(meta.getString("helpDoc"));
        fm.setOrderNum(meta.getInteger("orderNum"));
        fm.setProcessNoRule(meta.getString("processNoRule"));
        if (formId != null)
        {
            try
            {
                com.ruoyi.flowable.domain.FlowForm f = flowFormMapper.selectFlowFormByFormId(formId);
                if (f != null)
                {
                    fm.setFormName(f.getFormName());
                    fm.setFormTableName(f.getTableName());
                    // 字段数：解析 fieldsJson 数组长度（解析失败留空，前端可再取）
                    try
                    {
                        com.alibaba.fastjson2.JSONArray fields = com.alibaba.fastjson2.JSON.parseArray(f.getFieldsJson());
                        fm.setFormFieldCount(fields == null ? 0 : fields.size());
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return fm;
    }

    private String buildMetaInfo(String description, Long formId)
    {
        return buildMetaInfo(description, formId, null, null, null, null, null);
    }

    private String buildMetaInfo(String description, Long formId, String deployMode, Boolean locked, String helpDoc, Integer orderNum,
            String processNoRule)
    {
        JSONObject meta = new JSONObject();
        meta.put("description", description == null ? "" : description.replace("\"", "'"));
        if (formId != null)
        {
            meta.put("formId", formId);
        }
        if (deployMode != null)
        {
            meta.put("deployMode", deployMode);
        }
        if (locked != null)
        {
            meta.put("locked", locked);
        }
        if (helpDoc != null)
        {
            meta.put("helpDoc", helpDoc);
        }
        if (orderNum != null)
        {
            meta.put("orderNum", orderNum);
        }
        if (processNoRule != null)
        {
            meta.put("processNoRule", processNoRule);
        }
        return meta.toJSONString();
    }

    private JSONObject parseMetaInfo(String metaInfo)
    {
        try
        {
            return JSONObject.parseObject(metaInfo == null ? "{}" : metaInfo);
        }
        catch (Exception e)
        {
            return new JSONObject();
        }
    }

    /** 匹配 <process> 开始标签，用于注入 flowable:formId 部署标记 */
    private static final java.util.regex.Pattern PROCESS_TAG_PATTERN =
            java.util.regex.Pattern.compile("<process(?:\\s|>)");

    /** 匹配 BPMN 中部署时写入的 flowable:formId 标记 */
    private static final java.util.regex.Pattern FORM_ID_PATTERN =
            java.util.regex.Pattern.compile("flowable:formId\\s*=\\s*\"(\\d+)\"");

    /** 部署前在 <process> 上记录绑定的数据表表单ID（已有/无 flowable 命名空间时不处理） */
    private byte[] injectFormId(byte[] bpmnBytes, Long formId)
    {
        if (bpmnBytes == null || bpmnBytes.length == 0 || formId == null)
        {
            return bpmnBytes;
        }
        try
        {
            String xml = new String(bpmnBytes, StandardCharsets.UTF_8);
            if (xml.contains("flowable:formId=") || !xml.contains("xmlns:flowable="))
            {
                return bpmnBytes;
            }
            java.util.regex.Matcher matcher = PROCESS_TAG_PATTERN.matcher(xml);
            if (!matcher.find())
            {
                return bpmnBytes;
            }
            String hit = matcher.group(0);
            String injected = "<process flowable:formId=\"" + formId + "\"" + hit.substring("<process".length());
            String replaced = xml.substring(0, matcher.start()) + injected + xml.substring(matcher.end());
            return replaced.getBytes(StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            return bpmnBytes;
        }
    }

    /** 开始事件标签（属性部分） */
    private static final java.util.regex.Pattern START_EVENT_TAG_PATTERN =
            java.util.regex.Pattern.compile("<(?:\\w+:)?startEvent\\b([^>]*)>");

    /** 顺序流标签（属性部分） */
    private static final java.util.regex.Pattern SEQUENCE_FLOW_TAG_PATTERN =
            java.util.regex.Pattern.compile("<(?:\\w+:)?sequenceFlow\\b([^>]*)>");

    /** 流程图节点（DI）标签 */
    private static final java.util.regex.Pattern BPMN_SHAPE_PATTERN =
            java.util.regex.Pattern.compile("<bpmndi:BPMNShape\\b([^>]*)>(.*?)</bpmndi:BPMNShape>",
                    java.util.regex.Pattern.DOTALL);

    /**
     * 部署前注入「提交申请」发起人节点。
     *
     * 背景：Flowable 不能把执行流停在开始事件上（跳过去会立刻沿出线继续往下走），
     * 所以「驳回 → 退回发起人」没有落点。这里在部署时补一个只做出线的用户任务（发起人节点）：
     * 没有入线 → 正常发起/流转永远不会经过它；退回时把执行流挪到它上面，
     * 发起人就在待办里收到「提交申请」，改完表单提交后沿它的出线回到第一审批环节。
     *
     * 安全兜底：注入后的 XML 必须能被引擎解析，且原有节点/连线保持不变，
     * 任何异常都退回原始 XML，不影响正常部署。
     */
    private byte[] injectInitiatorNode(byte[] bpmnBytes)
    {
        if (bpmnBytes == null || bpmnBytes.length == 0)
        {
            return bpmnBytes;
        }
        String xml = new String(bpmnBytes, StandardCharsets.UTF_8);
        try
        {
            // 已注入过（或模型里本来就有同名节点）：直接返回，保证幂等
            if (xml.contains("id=\"" + FlowReturnConstants.INITIATOR_NODE_ID + "\""))
            {
                return bpmnBytes;
            }
            java.util.regex.Matcher startMatcher = START_EVENT_TAG_PATTERN.matcher(xml);
            if (!startMatcher.find())
            {
                log.warn("注入发起人节点跳过：BPMN 中未找到开始事件");
                return bpmnBytes;
            }
            String startAttrs = startMatcher.group(1);
            String startId = attrValue(startAttrs, "id");
            if (StringUtils.isEmpty(startId))
            {
                log.warn("注入发起人节点跳过：开始事件缺少 id 属性");
                return bpmnBytes;
            }
            // 开始事件必须只有一条出线：多出线（并行分支）注入后会改变正常流转语义，直接放弃注入
            String firstTarget = null;
            int outCount = 0;
            java.util.regex.Matcher flowMatcher = SEQUENCE_FLOW_TAG_PATTERN.matcher(xml);
            while (flowMatcher.find())
            {
                String attrs = flowMatcher.group(1);
                if (startId.equals(attrValue(attrs, "sourceRef")))
                {
                    outCount++;
                    firstTarget = attrValue(attrs, "targetRef");
                }
            }
            if (outCount != 1 || StringUtils.isEmpty(firstTarget))
            {
                log.warn("注入发起人节点跳过：开始事件出线数={}（只支持单一出线）", outCount);
                return bpmnBytes;
            }
            // 发起人节点：名称沿用开始事件（如「提交申请」），表单属性一并带过去，
            // 退回后发起人在办理页看到的还是发起时那张表单
            StringBuilder node = new StringBuilder();
            node.append("\n    <userTask id=\"").append(FlowReturnConstants.INITIATOR_NODE_ID).append("\" ");
            String rawName = rawAttr(startAttrs, "name");
            node.append(StringUtils.isNotEmpty(rawName) ? rawName : "name=\"提交申请\"");
            node.append(" flowable:").append(FlowReturnConstants.ATTR_INITIATOR_NODE).append("=\"true\"");
            appendRawAttr(startAttrs, "flowable:formKey", node);
            appendRawAttr(startAttrs, "flowable:nodeFormName", node);
            appendRawAttr(startAttrs, "flowable:nodeFormJson", node);
            node.append("/>");
            String flowId = "flowApplyTo_" + firstTarget;
            node.append("\n    <sequenceFlow id=\"").append(flowId)
                    .append("\" sourceRef=\"").append(FlowReturnConstants.INITIATOR_NODE_ID)
                    .append("\" targetRef=\"").append(firstTarget).append("\"/>");
            int processEnd = xml.indexOf("</process>", startMatcher.end());
            if (processEnd < 0)
            {
                log.warn("注入发起人节点跳过：未找到 </process> 结束标签");
                return bpmnBytes;
            }
            String injected = xml.substring(0, processEnd) + node + xml.substring(processEnd);
            injected = injectInitiatorDiagram(injected, startId, firstTarget, flowId);
            // 校验：能被引擎解析才采用；解析不出开始事件说明结构不对，回退原 XML
            BpmnModel check = new BpmnXMLConverter().convertToBpmnModel(
                    javax.xml.stream.XMLInputFactory.newInstance()
                            .createXMLStreamReader(new ByteArrayInputStream(injected.getBytes(StandardCharsets.UTF_8))));
            if (check == null || check.getMainProcess() == null
                    || check.getMainProcess().getFlowElement(FlowReturnConstants.INITIATOR_NODE_ID) == null)
            {
                log.warn("注入发起人节点跳过：注入后的 BPMN 校验不通过，按原始内容部署");
                return bpmnBytes;
            }
            return injected.getBytes(StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            // 注入失败不阻断部署，退回原始内容
            log.warn("注入发起人节点失败，按原始 BPMN 部署：{}", e.getMessage(), e);
            return bpmnBytes;
        }
    }

    /** 给发起人节点补图形信息（形状 + 连线），否则流程图里看不到这个节点 */
    private String injectInitiatorDiagram(String xml, String startId, String firstTarget, String flowId)
    {
        try
        {
            int planeEnd = xml.indexOf("</bpmndi:BPMNPlane>");
            if (planeEnd < 0)
            {
                return xml;
            }
            double[] startBox = shapeBox(xml, startId);
            double[] targetBox = shapeBox(xml, firstTarget);
            double x;
            double y;
            if (targetBox != null)
            {
                // 放在第一审批环节上方，箭头指向第一审批环节：读起来就是「发起人节点也能流到这里」
                x = targetBox[0];
                y = targetBox[1] - 110;
            }
            else if (startBox != null)
            {
                x = startBox[0];
                y = startBox[1] - 110;
            }
            else
            {
                x = 200;
                y = 40;
            }
            double width = 100;
            double height = 60;
            // 图形元素前缀跟随文档已有声明：设计器（bpmn-js）导出为 dc/di，引擎内置模板为 omgdc/omgdi。
            // 写死前缀会导致注入后的 XML 解析失败，发起人节点因此被整体回退（退回发起人不可用）。
            String dcPrefix = namespacePrefix(xml, "dc");
            String diPrefix = namespacePrefix(xml, "di");
            if (dcPrefix == null || diPrefix == null)
            {
                // 文档没有声明图形命名空间：跳过图形注入，正文节点仍可正常工作
                return xml;
            }
            StringBuilder di = new StringBuilder();
            di.append("\n      <bpmndi:BPMNShape id=\"").append(FlowReturnConstants.INITIATOR_NODE_ID)
                    .append("_di\" bpmnElement=\"").append(FlowReturnConstants.INITIATOR_NODE_ID).append("\">")
                    .append("<").append(dcPrefix).append(":Bounds x=\"").append(fmt(x)).append("\" y=\"").append(fmt(y))
                    .append("\" width=\"").append(fmt(width)).append("\" height=\"").append(fmt(height))
                    .append("\"/></bpmndi:BPMNShape>");
            double[] endBox = targetBox != null ? targetBox : startBox;
            StringBuilder edge = new StringBuilder();
            edge.append("\n      <bpmndi:BPMNEdge id=\"").append(flowId).append("_di\" bpmnElement=\"")
                    .append(flowId).append("\">")
                    .append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(x + width / 2)).append("\" y=\"").append(fmt(y + height))
                    .append("\"/>");
            if (endBox != null)
            {
                edge.append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(endBox[0] + endBox[4] / 2)).append("\" y=\"")
                        .append(fmt(endBox[1])).append("\"/>");
            }
            else
            {
                edge.append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(x + width / 2)).append("\" y=\"").append(fmt(y + height + 40))
                        .append("\"/>");
            }
            edge.append("</bpmndi:BPMNEdge>");
            return xml.substring(0, planeEnd) + di + edge + xml.substring(planeEnd);
        }
        catch (Exception e)
        {
            return xml;
        }
    }

    /**
     * 探测文档中已声明的图形命名空间前缀（Bounds 用 dc，连线用 di）
     *
     * 设计器（bpmn-js）导出为 {@code xmlns:dc / xmlns:di}，
     * 引擎内置模板使用 {@code xmlns:omgdc / xmlns:omgdi}，两种都要兼容。
     *
     * @return 可用前缀；未声明返回 null（此时不注入图形，避免产生非法 XML）
     */
    private String namespacePrefix(String xml, String kind)
    {
        String[] candidates = "dc".equals(kind) ? new String[] { "dc", "omgdc" } : new String[] { "di", "omgdi" };
        for (String prefix : candidates)
        {
            if (xml.contains("xmlns:" + prefix + "="))
            {
                return prefix;
            }
        }
        return null;
    }

    /** 读取流程图节点坐标 [x, y, width, height, 中心x]，取不到返回 null */
    private double[] shapeBox(String xml, String bpmnElement)
    {
        java.util.regex.Matcher matcher = BPMN_SHAPE_PATTERN.matcher(xml);
        while (matcher.find())
        {
            if (!bpmnElement.equals(attrValue(matcher.group(1), "bpmnElement")))
            {
                continue;
            }
            String bounds = matcher.group(2);
            String x = boundsAttr(bounds, "x");
            String y = boundsAttr(bounds, "y");
            String w = boundsAttr(bounds, "width");
            String h = boundsAttr(bounds, "height");
            if (x == null || y == null)
            {
                return null;
            }
            try
            {
                double dx = Double.parseDouble(x);
                double dy = Double.parseDouble(y);
                double dw = w == null ? 36 : Double.parseDouble(w);
                double dh = h == null ? 36 : Double.parseDouble(h);
                return new double[] { dx, dy, dw, dh, dx + dw / 2 };
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
        return null;
    }

    private String boundsAttr(String bounds, String name)
    {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\b" + name + "\\s*=\\s*\"([^\"]*)\"").matcher(bounds);
        return m.find() ? m.group(1) : null;
    }

    private String fmt(double value)
    {
        return String.valueOf(Math.round(value * 10) / 10.0).replace(".0", "");
    }

    /** 读取标签属性值（属性名大小写敏感，支持 flowable:xxx 形式） */
    private String attrValue(String attrs, String name)
    {
        if (attrs == null)
        {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:^|\\s)" + java.util.regex.Pattern.quote(name) + "\\s*=\\s*\"([^\"]*)\"")
                .matcher(attrs);
        return m.find() ? m.group(1) : null;
    }

    /** 原样取属性片段（形如 name="提交申请"），用于在注入节点上复刻开始事件的属性 */
    private String rawAttr(String attrs, String name)
    {
        if (attrs == null)
        {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:^|\\s)(" + java.util.regex.Pattern.quote(name) + "\\s*=\\s*\"[^\"]*\")")
                .matcher(attrs);
        return m.find() ? m.group(1) : null;
    }

    /** 把开始事件上的某个属性原样追加到注入节点上（不存在则跳过） */
    private void appendRawAttr(String attrs, String name, StringBuilder sb)
    {
        String raw = rawAttr(attrs, name);
        if (StringUtils.isNotEmpty(raw))
        {
            sb.append(' ').append(raw);
        }
    }

    @Override
    public Map<String, Object> checkDeployStructure(String modelId)
    {
        Model model = repositoryService.getModel(modelId);
        if (model == null)
        {
            throw new ServiceException("流程模型不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(model.getKey())
                .orderByProcessDefinitionVersion().desc()
                .list();
        result.put("deployed", !defs.isEmpty());
        result.put("activeVersion", null);
        result.put("structureChanged", false);
        result.put("formChanged", false);
        result.put("changes", new ArrayList<String>());
        if (defs.isEmpty())
        {
            return result;
        }
        // 对比目标：当前活动版本（无活动标志时取最高版本）
        ProcessDefinition target = defs.get(0);
        for (ProcessDefinition def : defs)
        {
            FlowProcdefExt ext = flowProcdefExtMapper.selectFlowProcdefExtByProcdefId(def.getId());
            if (ext != null && "1".equals(ext.getActiveFlag()) && !def.isSuspended())
            {
                target = def;
                break;
            }
        }
        result.put("activeVersion", target.getVersion());
        TreeSet<String> current = fingerprintOfModel(model);
        TreeSet<String> deployed = fingerprintOfDefinition(target);
        java.util.Set<String> added = new TreeSet<>(current);
        added.removeAll(deployed);
        java.util.Set<String> removed = new TreeSet<>(deployed);
        removed.removeAll(current);
        boolean structureChanged = !added.isEmpty() || !removed.isEmpty();
        result.put("structureChanged", structureChanged);
        // 绑定数据表对比（部署时写入 flowable:formId 标记；历史版本无标记时不做判断）
        Long currentFormId = parseMetaInfo(model.getMetaInfo()).getLong("formId");
        Long deployedFormId = readDeployedFormId(target);
        boolean formChanged = deployedFormId != null && !deployedFormId.equals(currentFormId);
        result.put("formChanged", formChanged);
        // 变更明细（节点名优先取当前模型，删除节点取已部署模型）
        Map<String, String> currentNames = nodeNamesOfModel(model);
        Map<String, String> deployedNames = nodeNamesOfDefinition(target);
        List<String> changes = new ArrayList<>();
        for (String fp : added)
        {
            changes.add(describeFingerprint(fp, "新增", currentNames));
        }
        for (String fp : removed)
        {
            changes.add(describeFingerprint(fp, "删除", deployedNames));
        }
        if (formChanged)
        {
            changes.add("更换绑定数据表（表单ID " + (deployedFormId == null ? "无" : deployedFormId) + " → "
                    + (currentFormId == null ? "无" : currentFormId) + "）");
        }
        result.put("changes", changes);
        return result;
    }

    private String describeFingerprint(String fp, String action, Map<String, String> names)
    {
        String[] parts = fp.split("\\|");
        if (parts.length < 2)
        {
            return action + "：" + fp;
        }
        if ("N".equals(parts[0]))
        {
            String name = names.get(parts[1]);
            return action + "节点：" + (name == null ? parts[1] : name);
        }
        return action + "流转线：" + parts[1];
    }

    private TreeSet<String> fingerprintOfModel(Model model)
    {
        byte[] xml = repositoryService.getModelEditorSource(model.getId());
        if (xml == null || xml.length == 0)
        {
            return new TreeSet<>();
        }
        try
        {
            BpmnXMLConverter converter = new BpmnXMLConverter();
            XMLStreamReader reader = javax.xml.stream.XMLInputFactory.newInstance()
                    .createXMLStreamReader(new ByteArrayInputStream(xml));
            return fingerprint(converter.convertToBpmnModel(reader));
        }
        catch (Exception e)
        {
            return new TreeSet<>();
        }
    }

    private TreeSet<String> fingerprintOfDefinition(ProcessDefinition definition)
    {
        try
        {
            return fingerprint(repositoryService.getBpmnModel(definition.getId()));
        }
        catch (Exception e)
        {
            return new TreeSet<>();
        }
    }

    private TreeSet<String> fingerprint(BpmnModel bpmnModel)
    {
        TreeSet<String> set = new TreeSet<>();
        if (bpmnModel == null || bpmnModel.getMainProcess() == null)
        {
            return set;
        }
        collectFingerprint(bpmnModel.getMainProcess().getFlowElements(), set);
        return set;
    }

    private void collectFingerprint(java.util.Collection<FlowElement> elements, TreeSet<String> set)
    {
        if (elements == null)
        {
            return;
        }
        for (FlowElement el : elements)
        {
            if (el instanceof SequenceFlow)
            {
                SequenceFlow sf = (SequenceFlow) el;
                set.add("F|" + sf.getSourceRef() + "->" + sf.getTargetRef());
            }
            else if (el instanceof FlowNode)
            {
                set.add("N|" + el.getId() + "|" + el.getClass().getSimpleName());
                if (el instanceof org.flowable.bpmn.model.SubProcess)
                {
                    collectFingerprint(((org.flowable.bpmn.model.SubProcess) el).getFlowElements(), set);
                }
            }
        }
    }

    private Map<String, String> nodeNamesOfModel(Model model)
    {
        byte[] xml = repositoryService.getModelEditorSource(model.getId());
        if (xml == null || xml.length == 0)
        {
            return new java.util.HashMap<>();
        }
        try
        {
            BpmnXMLConverter converter = new BpmnXMLConverter();
            XMLStreamReader reader = javax.xml.stream.XMLInputFactory.newInstance()
                    .createXMLStreamReader(new ByteArrayInputStream(xml));
            return nodeNames(converter.convertToBpmnModel(reader));
        }
        catch (Exception e)
        {
            return new java.util.HashMap<>();
        }
    }

    private Map<String, String> nodeNamesOfDefinition(ProcessDefinition definition)
    {
        try
        {
            return nodeNames(repositoryService.getBpmnModel(definition.getId()));
        }
        catch (Exception e)
        {
            return new java.util.HashMap<>();
        }
    }

    private Map<String, String> nodeNames(BpmnModel bpmnModel)
    {
        Map<String, String> names = new java.util.HashMap<>();
        if (bpmnModel == null || bpmnModel.getMainProcess() == null)
        {
            return names;
        }
        for (FlowElement el : bpmnModel.getMainProcess().getFlowElements())
        {
            if (el instanceof FlowNode)
            {
                names.put(el.getId(), StringUtils.isEmpty(el.getName()) ? el.getId() : el.getName());
            }
        }
        return names;
    }

    private Long readDeployedFormId(ProcessDefinition definition)
    {
        try (java.io.InputStream in = repositoryService.getResourceAsStream(
                definition.getDeploymentId(), definition.getResourceName()))
        {
            String xml = IOUtils.toString(in, StandardCharsets.UTF_8);
            java.util.regex.Matcher matcher = FORM_ID_PATTERN.matcher(xml);
            if (matcher.find())
            {
                return Long.valueOf(matcher.group(1));
            }
        }
        catch (Exception ignored)
        {
            // 历史部署无标记或资源读取失败：不做数据表变更判断
        }
        return null;
    }
}
