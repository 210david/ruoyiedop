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
import com.ruoyi.safety.domain.SafetyWorker;
import com.ruoyi.safety.service.ISafetyWorkerService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/worker")
public class SafetyWorkerController extends BaseController
{
    @Autowired
    private ISafetyWorkerService safetyWorkerService;

    @PreAuthorize("@ss.hasPermi('safety:worker:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyWorker worker)
    {
        startPage();
        List<SafetyWorker> list = safetyWorkerService.selectSafetyWorkerList(worker);
        return getDataTable(list);
    }

    @Log(title = "特种人员", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:worker:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyWorker worker)
    {
        List<SafetyWorker> list = safetyWorkerService.selectSafetyWorkerList(worker);
        ExcelUtil<SafetyWorker> util = new ExcelUtil<>(SafetyWorker.class);
        util.exportExcel(response, list, "特种人员");
    }

    @PreAuthorize("@ss.hasPermi('safety:worker:query')")
    @GetMapping(value = "/{workerId}")
    public AjaxResult getInfo(@PathVariable("workerId") Long workerId)
    {
        return AjaxResult.success(safetyWorkerService.selectSafetyWorkerById(workerId));
    }

    @PreAuthorize("@ss.hasPermi('safety:worker:add')")
    @Log(title = "特种人员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyWorker worker)
    {
        return toAjax(safetyWorkerService.insertSafetyWorker(worker));
    }

    @PreAuthorize("@ss.hasPermi('safety:worker:edit')")
    @Log(title = "特种人员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyWorker worker)
    {
        return toAjax(safetyWorkerService.updateSafetyWorker(worker));
    }

    @PreAuthorize("@ss.hasPermi('safety:worker:remove')")
    @Log(title = "特种人员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workerIds}")
    public AjaxResult remove(@PathVariable Long[] workerIds)
    {
        return toAjax(safetyWorkerService.deleteSafetyWorkerByIds(workerIds));
    }
}