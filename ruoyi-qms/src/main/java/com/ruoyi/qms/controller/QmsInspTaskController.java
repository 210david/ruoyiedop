package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsInspTask;
import com.ruoyi.qms.service.IQmsInspTaskService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 检验任务 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/task")
public class QmsInspTaskController extends BaseController
{
    @Autowired
    private IQmsInspTaskService qmsInspTaskService;

    @PreAuthorize("@ss.hasPermi('qms:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsInspTask inspTask)
    {
        startPage();
        List<QmsInspTask> list = qmsInspTaskService.selectInspTaskList(inspTask);
        return getDataTable(list);
    }

    @Log(title = "检验任务", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:task:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsInspTask inspTask)
    {
        List<QmsInspTask> list = qmsInspTaskService.selectInspTaskList(inspTask);
        ExcelUtil<QmsInspTask> util = new ExcelUtil<>(QmsInspTask.class);
        util.exportExcel(response, list, "检验任务数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return AjaxResult.success(qmsInspTaskService.selectInspTaskById(taskId));
    }

    @PreAuthorize("@ss.hasPermi('qms:task:query')")
    @GetMapping("/report/{taskId}")
    public AjaxResult getReport(@PathVariable Long taskId)
    {
        QmsInspTask task = qmsInspTaskService.selectInspTaskById(taskId);
        if (task != null && "2".equals(task.getTaskStatus()))
        {
            task = qmsInspTaskService.getReportData(taskId);
        }
        return AjaxResult.success(task);
    }

    @Log(title = "检验任务", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:task:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsInspTask inspTask)
    {
        return toAjax(qmsInspTaskService.insertInspTask(inspTask));
    }

    @Log(title = "检验任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:task:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsInspTask inspTask)
    {
        return toAjax(qmsInspTaskService.updateInspTask(inspTask));
    }

    @Log(title = "检验任务", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:task:remove')")
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(qmsInspTaskService.deleteInspTaskByIds(taskIds));
    }

    @Log(title = "检验结果录入", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:task:inspect')")
    @PutMapping("/saveResult")
    public AjaxResult saveInspectResult(@RequestBody QmsInspTask inspTask)
    {
        return toAjax(qmsInspTaskService.saveInspectResult(inspTask));
    }

    @Log(title = "发起复检", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:task:add')")
    @PostMapping("/recheck/{taskId}")
    public AjaxResult createRecheckTask(@PathVariable Long taskId)
    {
        return AjaxResult.success(qmsInspTaskService.createRecheckTask(taskId));
    }
}
