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
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
import com.ruoyi.flowable.service.IFlowProcessService;

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

    /**
     * 查询我的待办
     */
    @PreAuthorize("@ss.hasPermi('flowable:task:todo')")
    @GetMapping("/todoList")
    public TableDataInfo todoList(FlowTask query)
    {
        startPage();
        List<FlowTask> list = flowProcessService.selectTodoList(query);
        return getDataTable(list);
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
        startPage();
        List<FlowTask> list = flowProcessService.selectDoneList(query);
        return getDataTable(list);
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
