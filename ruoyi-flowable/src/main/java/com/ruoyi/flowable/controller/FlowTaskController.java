package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
import com.ruoyi.flowable.service.IFlowProcessService;
import com.ruoyi.flowable.service.IFlowFormService;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;

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
     * 查询我的已办
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:done')")
    @GetMapping("/doneList")
    public TableDataInfo doneList(FlowTask query)
    {
        return buildPage(flowProcessService.selectDoneList(query));
    }

    /** 对已过滤的完整列表手动分页 */
    private TableDataInfo buildPage(List<FlowTask> list)
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
        return toAjax(flowProcessService.completeTask(dto));
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
        // 流程定义 BPMN → 节点 fieldPerms 属性
        String permJson = "";
        try
        {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            FlowElement el = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            if (el instanceof UserTask)
            {
                UserTask ut = (UserTask) el;
                permJson = ut.getAttributeValue("http://flowable.org/bpmn", "fieldPerms");
                if (permJson == null) permJson = "";
            }
        }
        catch (Exception e)
        {
            permJson = "";
        }
        // 绑定表单 + 流程变量当前值
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId()).singleResult();
        FlowForm form = pd == null ? null : flowFormService.selectFlowFormByProcessKey(pd.getKey());
        java.util.Map<String, Object> variables = taskService.getVariables(taskId);
        com.alibaba.fastjson2.JSONObject data = new com.alibaba.fastjson2.JSONObject();
        data.put("fieldPerms", permJson);
        data.put("fieldsJson", form == null ? "" : (form.getFieldsJson() == null ? "" : form.getFieldsJson()));
        data.put("layoutJson", form == null ? "" : (form.getLayoutJson() == null ? "" : form.getLayoutJson()));
        data.put("variables", variables == null ? new java.util.HashMap<>() : variables);
        return AjaxResult.success(data);
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
}
