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

    /** 版本解析：与发起路由保持同一口径（活动版本优先） */
    @Autowired
    private com.ruoyi.flowable.service.FlowDefinitionResolver flowDefinitionResolver;

    @Autowired
    private org.flowable.engine.ProcessEngineConfiguration processEngineConfiguration;

    /**
     * 空模型 BPMN XML 模板（含 BPMNDiagram 图形段，避免 bpmn-js 报 no diagram to display）。
     *
     * 新建即预置「开始 → 结束」一对事件并连好线：
     *  - 只给开始节点的老模板，用户新建完发现只有起点，还得自己拖结束节点，流程也画不完整；
     *  - 事件尺寸直接给 80x80（= 前端 WM_EVENT_SIZE / bpmnSizes.DEFAULT_EVENT_SIZE），
     *    导入画布时 normalizeDiagramSizes 幂等、不再二次缩放，位置就是所见即所得；
     *  - 中间留出横向空档（240 → 400），用户往流程线上插审批节点时不用先挪开两个事件。
     */
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
        "    <endEvent id=\"EndEvent_1\" name=\"结束\"/>\n" +
        "    <sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\"/>\n" +
        "  </process>\n" +
        "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
        "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"%s\">\n" +
        "      <bpmndi:BPMNShape id=\"StartEvent_1_di\" bpmnElement=\"StartEvent_1\">\n" +
        "        <omgdc:Bounds x=\"160\" y=\"102\" width=\"80\" height=\"80\"/>\n" +
        "      </bpmndi:BPMNShape>\n" +
        "      <bpmndi:BPMNShape id=\"EndEvent_1_di\" bpmnElement=\"EndEvent_1\">\n" +
        "        <omgdc:Bounds x=\"400\" y=\"102\" width=\"80\" height=\"80\"/>\n" +
        "      </bpmndi:BPMNShape>\n" +
        "      <bpmndi:BPMNEdge id=\"Flow_1_di\" bpmnElement=\"Flow_1\">\n" +
        "        <omgdi:waypoint x=\"240\" y=\"142\"/>\n" +
        "        <omgdi:waypoint x=\"400\" y=\"142\"/>\n" +
        "      </bpmndi:BPMNEdge>\n" +
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
                // 列表展示版本与发起口径保持一致：优先「当前活动版本」，没有活动版本才回退到最高版本
                List<FlowProcdefExt> extList = flowProcdefExtMapper.selectFlowProcdefExtByProcKey(model.getKey());
                Map<String, FlowProcdefExt> extMap = new java.util.HashMap<>();
                if (extList != null)
                {
                    for (FlowProcdefExt ext : extList)
                    {
                        extMap.put(ext.getProcdefId(), ext);
                    }
                }
                ProcessDefinition shown = definitions.get(0);
                for (ProcessDefinition def : definitions)
                {
                    FlowProcdefExt ext = extMap.get(def.getId());
                    if (ext != null && "1".equals(ext.getActiveFlag()))
                    {
                        shown = def;
                        break;
                    }
                }
                fm.setDeployed("1");
                fm.setVersion(shown.getVersion());
                fm.setDeploymentId(shown.getDeploymentId());
                // 挂起状态：与版本编辑弹框口径统一，列表可直接标识该部署版本是否可发起
                fm.setSuspended(shown.isSuspended());
                // 活动版本标志（扩展表）：列表可直接标识当前生效版本
                FlowProcdefExt shownExt = extMap.get(shown.getId());
                if (shownExt != null)
                {
                    fm.setActiveFlag(shownExt.getActiveFlag());
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
        // 跳过相同审批人：未传沿用原值
        Boolean skipSameApprover = flowModel.getSkipSameApprover() != null ? flowModel.getSkipSameApprover()
                : oldMeta.getBoolean("skipSameApprover");
        model.setMetaInfo(buildMetaInfo(desc, formId, deployMode, locked, helpDoc, orderNum, processNoRule, skipSameApprover));
        repositoryService.saveModel(model);
        // 分类同步：流程分类在部署时会快照到 act_re_procdef.CATEGORY_，
        // 之后仅修改模型分类不会自动生效，导致流程发起页仍按旧分类展示。
        // 此处按 Key 同步到该流程的所有已部署版本。
        if (StringUtils.isNotEmpty(flowModel.getCategory()))
        {
            syncDefinitionCategory(model.getKey(), model.getCategory());
        }
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

    /**
     * 同步模型分类到该 Key 下所有已部署的流程定义。
     *
     * 背景：流程分类在部署时会快照写入 act_re_procdef.CATEGORY_，
     * 修改模型分类不会自动同步，导致流程发起页的分类页签仍显示旧分类。
     *
     * @param processKey 流程 Key
     * @param category   新的流程分类
     */
    private void syncDefinitionCategory(String processKey, String category)
    {
        if (StringUtils.isEmpty(processKey) || StringUtils.isEmpty(category))
        {
            return;
        }
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .list();
        for (ProcessDefinition definition : definitions)
        {
            if (!category.equals(definition.getCategory()))
            {
                repositoryService.setProcessDefinitionCategory(definition.getId(), category);
            }
        }
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
            // 统计失败时不能当作“没有业务数据”放行，否则可能误删单据与审批记录
            throw new ServiceException("校验流程《" + processKey + "》的实例数据失败，为避免误删已阻止本次删除：" + e.getMessage());
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
        List<ProcessDefinition> olds = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(model.getKey())
                .orderByProcessDefinitionVersion().asc()
                .list();
        // 最高版本是刚部署的新版本，只处理其余旧版本
        for (int i = 0; i < olds.size() - 1; i++)
        {
            ProcessDefinition old = olds.get(i);
            if ("replace".equals(mode) && !old.isSuspended())
            {
                repositoryService.suspendProcessDefinitionById(old.getId());
            }
            // 新版本要当活动版本时，旧版本的活动标志必须先清掉：
            // 否则「新建版本 + 勾选设为活动版本」会让同一 Key 出现两个 active_flag='1'，
            // 界面上显示两个「当前版本」，发起时到底用哪个也说不清
            if (active)
            {
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
        // 与 FlowDefinitionResolver 共用同一口径，避免预览版本与实际发起版本不一致
        ProcessDefinition target = flowDefinitionResolver.resolve(processKey);
        if (target == null)
        {
            throw new ServiceException("流程[" + processKey + "]尚未部署");
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
        fm.setSkipSameApprover(Boolean.TRUE.equals(meta.getBoolean("skipSameApprover")));
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
        return buildMetaInfo(description, formId, null, null, null, null, null, null);
    }

    private String buildMetaInfo(String description, Long formId, String deployMode, Boolean locked, String helpDoc, Integer orderNum,
            String processNoRule, Boolean skipSameApprover)
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
        if (skipSameApprover != null)
        {
            meta.put("skipSameApprover", skipSameApprover);
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
     * 发起人就在待办里收到「重新提交」任务，改完表单提交后沿它的出线回到第一审批环节。
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
            // 发起人节点：名称固定为「重新提交」，不再沿用开始事件名 ——
            // 开始事件通常叫「提交申请」，沿用会让流程图里出现两个同名节点，看着像多画了一个。
            // 表单属性一并带过去，退回后发起人在办理页看到的还是发起时那张表单
            StringBuilder node = new StringBuilder();
            node.append("\n    <userTask id=\"").append(FlowReturnConstants.INITIATOR_NODE_ID).append("\" ");
            node.append("name=\"").append(FlowReturnConstants.INITIATOR_NODE_NAME).append("\"");
            node.append(" flowable:").append(FlowReturnConstants.ATTR_INITIATOR_NODE).append("=\"true\"");
            appendRawAttr(startAttrs, "flowable:formKey", node);
            appendRawAttr(startAttrs, "flowable:nodeFormName", node);
            appendRawAttr(startAttrs, "flowable:nodeFormJson", node);
            // 发起人节点就是流程开始节点：节点配置（字段权限/操作菜单/签字意见/收回开关/抄送）一并带过去
            appendRawAttr(startAttrs, "flowable:fieldPerms", node);
            appendRawAttr(startAttrs, "flowable:operationMenus", node);
            appendRawAttr(startAttrs, "flowable:signOpinion", node);
            appendRawAttr(startAttrs, "flowable:allowRecall", node);
            appendRawAttr(startAttrs, "flowable:ccConfig", node);
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
            // 尺寸与设计器口径一致（处理节点 100x80）：原来写 100x60，画出来比相邻节点矮一截
            double width = 100;
            double height = 80;
            // 与第一审批环节留 40px 垂直间距（height + 40），避免和它的连线糊在一起
            double gap = 40;
            double x;
            double y;
            if (targetBox != null)
            {
                // 放在第一审批环节正上方，箭头垂直指向它：读起来就是「发起人节点也能流到这里」
                x = targetBox[0];
                y = targetBox[1] - height - gap;
            }
            else if (startBox != null)
            {
                x = startBox[0];
                y = startBox[1] - height - gap;
            }
            else
            {
                x = 200;
                y = 40;
            }
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
            // 连线走正交折线：先垂直落到中间高度，再横向对齐目标中心，最后垂直进入目标顶部。
            // 原来只给首尾两点，目标与发起人节点宽度不同时会画成斜线，看着像随手连的。
            double sx = x + width / 2;
            double sy = y + height;
            // 注意：shapeBox 的第 5 位已经是「左边界 + 半宽」= 中心 X，不能再除以 2，
            // 否则连线会落到目标节点左上角而不是正上方（V7 部署后实测过）
            double ex = endBox != null ? endBox[4] : sx;
            double ey = endBox != null ? endBox[1] : sy + 40;
            StringBuilder edge = new StringBuilder();
            edge.append("\n      <bpmndi:BPMNEdge id=\"").append(flowId).append("_di\" bpmnElement=\"")
                    .append(flowId).append("\">")
                    .append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(sx)).append("\" y=\"").append(fmt(sy))
                    .append("\"/>");
            if (Math.abs(sx - ex) > 0.01d)
            {
                double midY = (sy + ey) / 2;
                edge.append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(sx)).append("\" y=\"").append(fmt(midY))
                        .append("\"/>");
                edge.append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(ex)).append("\" y=\"").append(fmt(midY))
                        .append("\"/>");
            }
            edge.append("<").append(diPrefix).append(":waypoint x=\"").append(fmt(ex)).append("\" y=\"").append(fmt(ey))
                    .append("\"/>");
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
            // 部署时注入的「重新提交」发起人节点只在部署产物里存在，模型草稿里没有；
            // 若不排除，结构对比会永远报“新增节点：重新提交 + 新增流转线”，永远建议存为新版本。
            if (isInjectedInitiator(el))
            {
                continue;
            }
            if (el instanceof SequenceFlow)
            {
                SequenceFlow sf = (SequenceFlow) el;
                if (FlowReturnConstants.INITIATOR_NODE_ID.equals(sf.getSourceRef())
                        || FlowReturnConstants.INITIATOR_NODE_ID.equals(sf.getTargetRef()))
                {
                    continue;
                }
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

    /** 是否为部署时注入的发起人（重新提交）技术节点 */
    private boolean isInjectedInitiator(FlowElement el)
    {
        if (el == null)
        {
            return false;
        }
        if (FlowReturnConstants.INITIATOR_NODE_ID.equals(el.getId()))
        {
            return true;
        }
        // 自定义扩展属性（flowable:initiatorNode）在扩展属性表里按属性名存放
        java.util.Map<String, java.util.List<org.flowable.bpmn.model.ExtensionAttribute>> attrs = el.getAttributes();
        java.util.List<org.flowable.bpmn.model.ExtensionAttribute> flags = attrs == null ? null
                : attrs.get(FlowReturnConstants.ATTR_INITIATOR_NODE);
        if (flags != null)
        {
            for (org.flowable.bpmn.model.ExtensionAttribute flag : flags)
            {
                if ("true".equalsIgnoreCase(flag.getValue()))
                {
                    return true;
                }
            }
        }
        return false;
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
