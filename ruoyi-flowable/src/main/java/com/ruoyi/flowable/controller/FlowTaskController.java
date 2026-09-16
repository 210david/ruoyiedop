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
