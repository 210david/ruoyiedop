package com.ruoyi.flowable.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.FlowTask;
import com.ruoyi.flowable.domain.FlowDraft;
import com.ruoyi.flowable.domain.FlowCc;
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.domain.FlowNodeForm;
import com.ruoyi.flowable.domain.dto.FlowCcSendDTO;
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
import com.ruoyi.flowable.service.IFlowProcessService;
import com.ruoyi.flowable.service.IFlowDraftService;
import com.ruoyi.flowable.service.IFlowCcService;
import com.ruoyi.flowable.service.IFlowFormService;
import com.ruoyi.flowable.service.IFlowNodeFormService;
import com.ruoyi.flowable.service.IFlowNodePrintService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.task.api.Task;

/**
 * 流程任务 Controller（待办/已办/办理/转办）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/task")
public class FlowTaskController extends BaseController
{
    @Autowired
    private IFlowProcessService flowProcessService;

    @Autowired
    private IFlowFormService flowFormService;

    @Autowired
    private IFlowDraftService flowDraftService;

    @Autowired
    private IFlowCcService flowCcService;

    @Autowired
    private IFlowNodeFormService flowNodeFormService;

    @Autowired
    private IFlowNodePrintService flowNodePrintService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    /**
     * 查询我的待办（支持流程标题/创建人/创建时间等条件，内存过滤后手动分页）
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:todo')")
    @GetMapping("/todoList")
    public TableDataInfo todoList(FlowTask query)
    {
        return buildPage(flowProcessService.selectTodoList(query));
    }

    /**
     * 待办数量（角标）
     */
    @GetMapping("/todoCount")
    public AjaxResult todoCount()
    {
        return success(flowProcessService.countTodo());
    }

    /**
     * 待办数量拆分（all 全部 / returned 被退回），供待办页页签角标使用
     */
    @GetMapping("/todoCounts")
    public AjaxResult todoCounts()
    {
        return success(flowProcessService.countTodoMap());
    }

    /**
     * 当前任务的可退回节点（办理页「驳回」弹窗选节点）
     *
     * 返回本实例已经走过的用户任务节点 + 发起人（申请）节点，当前节点不返回。
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @GetMapping("/prevNodes/{taskId}")
    public AjaxResult prevNodes(@PathVariable("taskId") String taskId)
    {
        return success(flowProcessService.selectReturnableNodes(taskId));
    }

    /**
     * 查询我的已办（statusScope=all/running/finished 对应「全部/未结束/已结束」页签）
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:done')")
    @GetMapping("/doneList")
    public TableDataInfo doneList(FlowTask query)
    {
        return buildPage(flowProcessService.selectDoneList(query));
    }

    /**
     * 已办页签数量（全部 / 未结束 / 已结束）
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:done')")
    @GetMapping("/doneCount")
    public AjaxResult doneCount()
    {
        return success(flowProcessService.countDone());
    }

    /**
     * 我的全部未结事项（待办任务 + 草稿）
     *
     * 对标泛微「待办事宜 → 全部」：一个列表看全当前用户已收到待处理的任务与未提交的草稿，
     * 行上用 bizType 区分（task=待办任务，draft=草稿），前端据 bizType 决定操作按钮。
     */
    @GetMapping("/allList")
    public TableDataInfo allList(FlowTask query)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (FlowTask t : flowProcessService.selectTodoList(query))
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("bizType", "task");
            m.put("taskId", t.getTaskId());
            m.put("instanceId", t.getInstanceId());
            m.put("title", t.getTitle());
            m.put("startUserName", t.getStartUserName());
            m.put("processName", t.getProcessDefinitionName());
            m.put("processKey", t.getProcessDefinitionKey());
            m.put("taskName", t.getTaskName());
            m.put("urgency", t.getUrgency());
            m.put("returned", t.getReturned());
            m.put("returnFrom", t.getReturnFrom());
            m.put("statusText", Boolean.TRUE.equals(t.getReturned()) ? "退回" : "待办");
            m.put("time", formatDateTime(t.getCreateTime()));
            m.put("sortTime", t.getCreateTime() == null ? 0L : t.getCreateTime().getTime());
            rows.add(m);
        }
        // 草稿：仅当创建人筛选条件命中当前用户时才并入
        if (matchDraftCreator(query.getStartUserName()))
        {
            FlowDraft draftQuery = new FlowDraft();
            draftQuery.setTitle(query.getTitle());
            draftQuery.setProcessKey(query.getProcessDefinitionKey());
            draftQuery.setParams(query.getParams());
            for (FlowDraft d : flowDraftService.selectMyDraftList(draftQuery))
            {
                Date time = d.getUpdateTime() != null ? d.getUpdateTime() : d.getCreateTime();
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("bizType", "draft");
                m.put("draftId", d.getDraftId());
                m.put("title", d.getTitle());
                m.put("startUserName", StringUtils.isNotEmpty(d.getStartNickName()) ? d.getStartNickName() : d.getStartUserName());
                m.put("processName", d.getProcessName());
                m.put("processKey", d.getProcessKey());
                m.put("taskName", null);
                m.put("statusText", "草稿");
                m.put("time", formatDateTime(time));
                m.put("sortTime", time == null ? 0L : time.getTime());
                rows.add(m);
            }
        }
        // 待阅：未读抄送一并并入「全部」（已读的不再占用全部列表；创建人筛选对抄送行不适用，仅在未填时并入）
        if (StringUtils.isEmpty(query.getStartUserName()))
        {
            FlowCc ccQuery = new FlowCc();
            ccQuery.setTitle(query.getTitle());
            ccQuery.setProcessKey(query.getProcessDefinitionKey());
            ccQuery.setReadFlag("0");
            ccQuery.setParams(query.getParams());
            for (FlowCc cc : flowCcService.selectMyCcList(ccQuery))
            {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("bizType", "cc");
                m.put("ccId", cc.getCcId());
                m.put("instanceId", cc.getInstanceId());
                m.put("taskId", cc.getTaskId());
                m.put("title", cc.getTitle());
                m.put("startUserName", cc.getSenderName());
                m.put("processName", cc.getProcessName());
                m.put("processKey", cc.getProcessKey());
                m.put("taskName", cc.getTaskName());
                m.put("readFlag", cc.getReadFlag());
                m.put("statusText", "待阅");
                m.put("time", formatDateTime(cc.getCreateTime()));
                m.put("sortTime", cc.getCreateTime() == null ? 0L : cc.getCreateTime().getTime());
                rows.add(m);
            }
        }
        // 统一按时间倒序（最新动过的排前面）
        rows.sort((a, b) -> Long.compare((Long) b.get("sortTime"), (Long) a.get("sortTime")));
        return slicePage(rows);
    }

    /** 创建人筛选：草稿都属于当前用户，条件不命中自己则整批不并入 */
    private boolean matchDraftCreator(String keyword)
    {
        if (StringUtils.isEmpty(keyword))
        {
            return true;
        }
        SysUser user = getLoginUser().getUser();
        String nick = user == null ? null : user.getNickName();
        String name = user == null ? null : user.getUserName();
        return (nick != null && nick.contains(keyword)) || (name != null && name.contains(keyword));
    }

    private String formatDateTime(Date date)
    {
        return date == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /** 对已过滤的完整列表手动分页 */
    private TableDataInfo buildPage(List<FlowTask> list)
    {
        return slicePage(list);
    }

    /** 对内存列表分页（待办/已办/全部共用） */
    private TableDataInfo slicePage(List<?> list)
    {
        com.ruoyi.common.core.page.PageDomain page = com.ruoyi.common.core.page.TableSupport.buildPageRequest();
        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(200);
        rsp.setMsg("查询成功");
        rsp.setTotal(list.size());
        int pageNum = page.getPageNum() == null ? 1 : page.getPageNum();
        int pageSize = page.getPageSize() == null ? 10 : page.getPageSize();
        int from = Math.min((pageNum - 1) * pageSize, list.size());
        int to = Math.min(from + pageSize, list.size());
        rsp.setRows(list.subList(from, to));
        return rsp;
    }

    /**
     * 办理任务（同意/驳回）
     */
    @Log(title = "流程任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PutMapping("/complete")
    public AjaxResult complete(@Validated @RequestBody FlowCompleteDTO dto)
    {
        // 加签（委派）任务办理后只回到原办理人，流程不推进，需单独提示前端
        Task current = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        boolean delegated = current != null
                && org.flowable.task.api.DelegationState.PENDING.equals(current.getDelegationState());
        int rows = flowProcessService.completeTask(dto);
        if (delegated)
        {
            AjaxResult ajax = AjaxResult.success();
            ajax.put("delegated", true);
            ajax.put("msg", "加签办理完成，任务已回到原办理人");
            return ajax;
        }
        return toAjax(rows);
    }

    /**
     * 任务办理页：节点字段权限 + 绑定表单 + 当前值
     * 权限来源：流程定义 BPMN 中该节点（taskDefinitionKey）的 flowable:fieldPerms 扩展属性
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:todo')")
    @GetMapping("/fieldPerms/{taskId}")
    public AjaxResult fieldPerms(@PathVariable("taskId") String taskId)
    {
        org.flowable.task.api.Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            return AjaxResult.error("任务不存在或已办理");
        }
        // 流程定义 BPMN → 节点 fieldPerms（字段权限）与 operationMenus（操作菜单）扩展属性
        String permJson = "";
        String opMenuJson = "";
        String signOpinionAttr = "";
        // 发起人（申请）节点：退回后由发起人修改表单重新提交，字段默认全部可编辑
        boolean initiatorNode = false;
        try
        {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement el = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            if (el instanceof UserTask)
            {
                UserTask ut = (UserTask) el;
                permJson = readNodeAttr(ut, "fieldPerms");
                opMenuJson = readNodeAttr(ut, "operationMenus");
                signOpinionAttr = readNodeAttr(ut, "signOpinion");
                String initiatorAttr = readNodeAttr(ut, com.ruoyi.flowable.config.FlowReturnConstants.ATTR_INITIATOR_NODE);
                initiatorNode = "true".equalsIgnoreCase(initiatorAttr) || "1".equals(initiatorAttr);
            }
        }
        catch (Exception e)
        {
            permJson = "";
            opMenuJson = "";
            signOpinionAttr = "";
            initiatorNode = false;
        }
        // 绑定表单 + 流程变量当前值
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId()).singleResult();
        // 节点模板：只覆盖「显示布局」与模板名称；字段定义始终取表单管理里的那一份，
        // 这样表单增删字段后所有节点实时同步（对标泛微：节点引用同一份表单字段，不存在字段快照）
        FlowNodeForm nodeForm = pd == null ? null
                : flowNodeFormService.selectByModelKeyAndNodeId(pd.getKey(), task.getTaskDefinitionKey());
        FlowForm form = pd == null ? null : flowFormService.selectFlowFormByProcessKey(pd.getKey());
        java.util.Map<String, Object> variables = taskService.getVariables(taskId);
        com.alibaba.fastjson2.JSONObject data = new com.alibaba.fastjson2.JSONObject();
        data.put("fieldPerms", permJson);
        data.put("editableAll", initiatorNode);
        if (nodeForm != null)
        {
            // 字段：表单为准（表单缺失时才退回节点模板里的历史快照，避免老数据整页空白）
            data.put("fieldsJson", resolveNodeFieldsJson(nodeForm, form));
            // 布局：节点布局优先（节点可裁剪显示哪些字段），没有则用表单布局
            data.put("layoutJson", StringUtils.isNotEmpty(nodeForm.getLayoutJson())
                    ? nodeForm.getLayoutJson() : (form == null ? "" : (form.getLayoutJson() == null ? "" : form.getLayoutJson())));
            data.put("nodeForm", true);
            data.put("nodeFormName", nodeForm.getFormName());
        }
        else
        {
            data.put("fieldsJson", form == null ? "" : (form.getFieldsJson() == null ? "" : form.getFieldsJson()));
            data.put("layoutJson", form == null ? "" : (form.getLayoutJson() == null ? "" : form.getLayoutJson()));
        }
        data.put("variables", variables == null ? new java.util.HashMap<>() : variables);
        // 节点操作菜单：设计器勾选的按钮（提交/退回/转办/加签/终止/签字意见/附件上传/打印/抄送），
        // 未配置时返回空数组，由前端回退到默认菜单
        java.util.List<String> menus = parseMenuConfig(opMenuJson);
        data.put("opMenus", menus);
        // 节点是否启用签字意见（默认启用；表单里已放「签字意见」字段时由前端自行去重）
        data.put("signOpinion", resolveSignOpinion(signOpinionAttr, menus));
        data.put("assignee", task.getAssignee());
        return AjaxResult.success(data);
    }

    /**
     * 节点模板场景下的字段定义：以表单管理里的表单为准（字段只有一份），
     * 仅当表单不存在或字段为空时，退回节点模板里的历史快照，避免老数据整页空白。
     */
    private String resolveNodeFieldsJson(FlowNodeForm nodeForm, FlowForm form)
    {
        if (form != null && StringUtils.isNotEmpty(form.getFieldsJson()))
        {
            return form.getFieldsJson();
        }
        return nodeForm == null || nodeForm.getFieldsJson() == null ? "" : nodeForm.getFieldsJson();
    }

    /**
     * 办理页「打印」用的模板。
     *
     * <p>对标泛微：节点可以在设计器里单独配置打印模板，打印时优先用它；
     * 该节点没有配置时返回 configured=false，由前端自动回退到「当前节点显示模板」。</p>
     *
     * <p>模板内容（字段 + 布局）在配置时就已快照落库，这里只负责按流程定义Key + 节点ID 取出来，
     * 并附上打印抬头需要的信息。</p>
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @GetMapping("/printTemplate/{taskId}")
    public AjaxResult printTemplate(@PathVariable("taskId") String taskId)
    {
        org.flowable.task.api.Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            return AjaxResult.error("任务不存在或已办理");
        }
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId()).singleResult();
        String modelKey = pd == null ? null : pd.getKey();
        com.ruoyi.flowable.domain.FlowNodePrint print =
                flowNodePrintService.selectByModelKeyAndNodeId(modelKey, task.getTaskDefinitionKey());

        com.alibaba.fastjson2.JSONObject data = new com.alibaba.fastjson2.JSONObject();
        boolean configured = print != null && StringUtils.isNotEmpty(print.getLayoutJson());
        data.put("configured", configured);
        if (configured)
        {
            data.put("templateName", print.getTemplateName());
            data.put("fieldsJson", print.getFieldsJson());
            data.put("layoutJson", print.getLayoutJson());
        }

        // 打印抬头信息
        com.alibaba.fastjson2.JSONObject meta = new com.alibaba.fastjson2.JSONObject();
        meta.put("processName", pd == null ? "" : pd.getName());
        meta.put("nodeName", task.getName());
        meta.put("taskId", taskId);
        meta.put("instanceId", task.getProcessInstanceId());
        try
        {
            org.flowable.engine.history.HistoricProcessInstance hpi = historyService
                    .createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            meta.put("title", hpi == null || hpi.getName() == null ? "" : hpi.getName());
        }
        catch (Exception e)
        {
            meta.put("title", "");
        }
        try
        {
            com.ruoyi.common.core.domain.entity.SysUser user = getLoginUser().getUser();
            meta.put("printUser", user == null ? getUsername()
                    : (StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName()));
        }
        catch (Exception e)
        {
            meta.put("printUser", getUsername());
        }
        data.put("meta", meta);
        return AjaxResult.success(data);
    }

    /**
     * 读取节点上的扩展属性值（BPMN 中形如 flowable:fieldPerms / flowable:operationMenus）
     */
    private String readNodeAttr(UserTask task, String name)
    {
        Map<String, java.util.List<ExtensionAttribute>> attrs = task.getAttributes();
        if (attrs == null)
        {
            return "";
        }
        java.util.List<ExtensionAttribute> list = attrs.get(name);
        if (list == null || list.isEmpty())
        {
            return "";
        }
        String value = list.get(0).getValue();
        return value == null ? "" : value;
    }

    /** 解析操作菜单 JSON 数组（解析失败按未配置处理） */
    private java.util.List<String> parseMenuConfig(String json)
    {
        java.util.List<String> menus = new ArrayList<>();
        if (StringUtils.isEmpty(json))
        {
            return menus;
        }
        try
        {
            com.alibaba.fastjson2.JSONArray arr = com.alibaba.fastjson2.JSON.parseArray(json);
            for (int i = 0; i < arr.size(); i++)
            {
                String v = arr.getString(i);
                if (StringUtils.isNotEmpty(v))
                {
                    menus.add(v);
                }
            }
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
        return menus;
    }

    /**
     * 节点是否启用签字意见：
     * 1) 节点属性 flowable:signOpinion 明确写了值 → 以其为准（false/0/否 为关闭）；
     * 2) 未配置（老流程）→ 跟随操作菜单：菜单未配置视为默认全开，配置了则看是否勾选「签字意见」。
     */
    private boolean resolveSignOpinion(String attr, java.util.List<String> menus)
    {
        if (StringUtils.isNotEmpty(attr))
        {
            String v = attr.trim();
            return !("false".equalsIgnoreCase(v) || "0".equals(v) || "否".equals(v));
        }
        return menus.isEmpty() || menus.contains("comment");
    }

    /**
     * 任务转办
     */
    @Log(title = "流程任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PutMapping("/delegate/{taskId}")
    public AjaxResult delegate(@PathVariable("taskId") String taskId,
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "comment", required = false) String comment)
    {
        return toAjax(flowProcessService.delegateTask(taskId, targetUserId, comment));
    }

    /**
     * 办理页「抄送」：办理人手动把当前流程抄送给指定人员
     *
     * 与节点级自动抄送共用 flow_cc，接收人在「待办流程-待阅」查看。
     */
    @Log(title = "流程抄送", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PostMapping("/cc")
    public AjaxResult sendCc(@Validated @RequestBody FlowCcSendDTO dto)
    {
        int count = flowCcService.sendCc(dto.getTaskId(), dto.getUserIds(), dto.getComment());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("count", count);
        ajax.put("msg", count > 0 ? "已抄送给 " + count + " 人" : "所选人员已在抄送列表中，无需重复抄送");
        return ajax;
    }

    /**
     * 办理页「加签」：把任务先委派给他人办理，其办理完成后回到当前办理人
     *
     * 采用 Flowable 委派（delegate）语义：owner 保留为当前办理人，被加签人办理后任务回到 owner。
     */
    @Log(title = "流程任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PutMapping("/addSign/{taskId}")
    public AjaxResult addSign(@PathVariable("taskId") String taskId,
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "comment", required = false) String comment)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            return AjaxResult.error("任务不存在或已办理");
        }
        String currentUserId = String.valueOf(com.ruoyi.common.utils.SecurityUtils.getUserId());
        if (StringUtils.isEmpty(task.getAssignee()))
        {
            // 候选任务（候选池未认领）：当前用户是候选人时先认领，再加签
            long candidate = taskService.createTaskQuery().taskId(taskId).taskCandidateUser(currentUserId).count();
            if (candidate <= 0)
            {
                return AjaxResult.error("您不是该任务的办理人");
            }
            taskService.claim(taskId, currentUserId);
            task = taskService.createTaskQuery().taskId(taskId).singleResult();
        }
        else if (!currentUserId.equals(task.getAssignee()))
        {
            return AjaxResult.error("您不是该任务的办理人");
        }
        if (StringUtils.isNotEmpty(task.getOwner()) && !task.getOwner().equals(task.getAssignee()))
        {
            return AjaxResult.error("该任务已加签给他人，待其办理后将回到您这里");
        }
        if (StringUtils.isNotEmpty(comment))
        {
            taskService.addComment(taskId, task.getProcessInstanceId(), "comment", "【addSign】" + comment);
        }
        taskService.delegateTask(taskId, String.valueOf(targetUserId));
        AjaxResult ajax = AjaxResult.success();
        ajax.put("msg", "已加签，被加签人办理完成后任务回到您这里");
        return ajax;
    }

    /**
     * 办理页「终止」：办理人强制结束当前流程实例
     */
    @Log(title = "流程任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PutMapping("/terminate/{taskId}")
    public AjaxResult terminate(@PathVariable("taskId") String taskId,
            @RequestParam(value = "reason", required = false) String reason)
    {
        flowProcessService.terminateTask(taskId, reason);
        return AjaxResult.success("流程已终止");
    }
}
