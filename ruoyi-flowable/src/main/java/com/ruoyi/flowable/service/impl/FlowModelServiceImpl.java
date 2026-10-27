package com.ruoyi.flowable.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.flowable.domain.FlowModel;
import com.ruoyi.flowable.domain.FlowProcdefExt;
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
    @Autowired
    private RepositoryService repositoryService;

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
        model.setMetaInfo(buildMetaInfo(desc, formId, deployMode, locked, helpDoc, orderNum));
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
            // 删除该模型对应的所有部署（级联删除其流程定义及运行中实例）
            List<Deployment> deployments = repositoryService.createDeploymentQuery()
                    .deploymentName(model.getName()).list();
            for (Deployment deployment : deployments)
            {
                repositoryService.deleteDeployment(deployment.getId(), true);
            }
            repositoryService.deleteModel(modelId);
            rows++;
        }
        return rows;
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
    public byte[] generateDefinitionDiagram(String processKey)
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
        return buildMetaInfo(description, formId, null, null, null, null);
    }

    private String buildMetaInfo(String description, Long formId, String deployMode, Boolean locked, String helpDoc, Integer orderNum)
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
}
