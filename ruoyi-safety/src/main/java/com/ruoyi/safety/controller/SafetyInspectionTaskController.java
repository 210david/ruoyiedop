package com.ruoyi.safety.controller;

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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.safety.domain.SafetyInspectionTask;
import com.ruoyi.safety.service.ISafetyInspectionTaskService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/task")
public class SafetyInspectionTaskController extends BaseController
{
    @Autowired
    private ISafetyInspectionTaskService safetyInspectionTaskService;

    @PreAuthorize("@ss.hasPermi('safety:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyInspectionTask task)
    {
        startPage();
        List<SafetyInspectionTask> list = safetyInspectionTaskService.selectSafetyInspectionTaskList(task);
        return getDataTable(list);
    }

    @Log(title = "排查任务", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:task:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyInspectionTask task)
    {
        List<SafetyInspectionTask> list = safetyInspectionTaskService.selectSafetyInspectionTaskList(task);
        ExcelUtil<SafetyInspectionTask> util = new ExcelUtil<>(SafetyInspectionTask.class);
        util.exportExcel(response, list, "排查任务");
    }

    @PreAuthorize("@ss.hasPermi('safety:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return AjaxResult.success(safetyInspectionTaskService.selectSafetyInspectionTaskById(taskId));
    }

    @PreAuthorize("@ss.hasPermi('safety:task:add')")
    @Log(title = "排查任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyInspectionTask task)
    {
        return toAjax(safetyInspectionTaskService.insertSafetyInspectionTask(task));
    }

    @PreAuthorize("@ss.hasPermi('safety:task:edit')")
    @Log(title = "排查任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyInspectionTask task)
    {
        return toAjax(safetyInspectionTaskService.updateSafetyInspectionTask(task));
    }

    @PreAuthorize("@ss.hasPermi('safety:task:remove')")
    @Log(title = "排查任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(safetyInspectionTaskService.deleteSafetyInspectionTaskByIds(taskIds));
    }

    /**
     * 提交执行反馈
     */
    @PreAuthorize("@ss.hasPermi('safety:task:edit')")
    @Log(title = "排查任务-执行反馈", businessType = BusinessType.UPDATE)
    @PutMapping("/feedback")
    public AjaxResult feedback(@RequestBody SafetyInspectionTask task)
    {
        return toAjax(safetyInspectionTaskService.submitFeedback(task));
    }

    /**
     * 作废任务
     */
    @PreAuthorize("@ss.hasPermi('safety:task:edit')")
    @Log(title = "排查任务-作废", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{taskId}")
    public AjaxResult cancel(@PathVariable("taskId") Long taskId)
    {
        return toAjax(safetyInspectionTaskService.cancelTask(taskId));
    }
}