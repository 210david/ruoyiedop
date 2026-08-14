package com.ruoyi.qms.controller;

import java.util.List;
import java.util.Map;
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

    @Log(title = "检验结果临时保存", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:task:inspect')")
    @PutMapping("/saveDraft")
    public AjaxResult saveInspectDraft(@RequestBody QmsInspTask inspTask)
    {
        return toAjax(qmsInspTaskService.saveInspectDraft(inspTask));
    }

    @Log(title = "发起复检", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:task:add')")
    @PostMapping("/recheck/{taskId}")
    public AjaxResult createRecheckTask(@PathVariable Long taskId)
    {
        return AjaxResult.success(qmsInspTaskService.createRecheckTask(taskId));
    }

    @Log(title = "开始检验", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:task:inspect')")
    @PutMapping("/start/{taskId}")
    public AjaxResult startInspect(@PathVariable Long taskId)
    {
        return toAjax(qmsInspTaskService.startInspect(taskId));
    }

    @Log(title = "作废检验任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:task:edit')")
    @PutMapping("/void/{taskId}")
    public AjaxResult voidTask(@PathVariable Long taskId, @RequestBody(required = false) Map<String, String> body)
    {
        String reason = body != null ? body.get("reason") : null;
        String voidType = body != null ? body.get("voidType") : null;
        return toAjax(qmsInspTaskService.voidTask(taskId, reason, voidType));
    }

    @Log(title = "批量分配检验员", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:task:edit')")
    @PutMapping("/assignInspector")
    @SuppressWarnings("unchecked")
    public AjaxResult assignInspector(@RequestBody Map<String, Object> body)
    {
        List<Integer> rawIds = (List<Integer>) body.get("taskIds");
        Long[] taskIds = rawIds.stream().map(Integer::longValue).toArray(Long[]::new);
        Long inspectorId = body.get("inspectorId") != null ? Long.valueOf(body.get("inspectorId").toString()) : null;
        String inspectorName = (String) body.get("inspectorName");
        return AjaxResult.success(qmsInspTaskService.assignInspector(taskIds, inspectorId, inspectorName));
    }

    @PreAuthorize("@ss.hasPermi('qms:task:list')")
    @GetMapping("/statusCounts")
    public AjaxResult statusCounts()
    {
        return AjaxResult.success(qmsInspTaskService.selectStatusCounts());
    }
}
