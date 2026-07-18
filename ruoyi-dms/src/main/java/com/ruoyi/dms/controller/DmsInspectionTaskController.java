package com.ruoyi.dms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.dms.domain.DmsInspectionTask;
import com.ruoyi.dms.service.IDmsInspectionTaskService;

@RestController
@RequestMapping("/dms/inspection/task")
public class DmsInspectionTaskController extends BaseController
{
    @Autowired
    private IDmsInspectionTaskService dmsInspectionTaskService;

    @PreAuthorize("@ss.hasPermi('dms:inspection:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsInspectionTask task)
    {
        startPage();
        List<DmsInspectionTask> list = dmsInspectionTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('dms:inspection:task:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return AjaxResult.success(dmsInspectionTaskService.selectTaskById(taskId));
    }

    @Log(title = "点检任务", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:inspection:task:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsInspectionTask task)
    {
        return toAjax(dmsInspectionTaskService.insertTask(task));
    }

    @Log(title = "点检任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:inspection:task:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsInspectionTask task)
    {
        return toAjax(dmsInspectionTaskService.updateTask(task));
    }

    @Log(title = "点检任务", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:inspection:task:remove')")
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(dmsInspectionTaskService.deleteTaskByIds(taskIds));
    }

    /** 开始执行点检任务（记录开始时间） */
    @Log(title = "点检任务开始", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:inspection:task:edit')")
    @PutMapping("/start/{taskId}")
    public AjaxResult start(@PathVariable Long taskId)
    {
        return toAjax(dmsInspectionTaskService.startTask(taskId));
    }

    /** 完成点检任务（异常项自动转工单） */
    @Log(title = "点检任务完成", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:inspection:task:edit')")
    @PutMapping("/complete")
    public AjaxResult complete(@RequestBody DmsInspectionTask task)
    {
        return toAjax(dmsInspectionTaskService.completeTask(task));
    }
}
