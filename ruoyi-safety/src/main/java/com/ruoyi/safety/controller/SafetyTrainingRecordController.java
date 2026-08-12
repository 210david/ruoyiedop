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
import com.ruoyi.safety.domain.SafetyTrainingRecord;
import com.ruoyi.safety.service.ISafetyTrainingRecordService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/training/record")
public class SafetyTrainingRecordController extends BaseController
{
    @Autowired
    private ISafetyTrainingRecordService safetyTrainingRecordService;

    @PreAuthorize("@ss.hasPermi('safety:training:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyTrainingRecord record)
    {
        startPage();
        List<SafetyTrainingRecord> list = safetyTrainingRecordService.selectSafetyTrainingRecordList(record);
        return getDataTable(list);
    }

    @Log(title = "培训记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:record:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyTrainingRecord record)
    {
        List<SafetyTrainingRecord> list = safetyTrainingRecordService.selectSafetyTrainingRecordList(record);
        ExcelUtil<SafetyTrainingRecord> util = new ExcelUtil<>(SafetyTrainingRecord.class);
        util.exportExcel(response, list, "培训记录");
    }

    @PreAuthorize("@ss.hasPermi('safety:training:record:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(safetyTrainingRecordService.selectSafetyTrainingRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:record:add')")
    @Log(title = "培训记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyTrainingRecord record)
    {
        safetyTrainingRecordService.insertSafetyTrainingRecord(record);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("recordId", record.getRecordId());
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('safety:training:record:edit')")
    @Log(title = "培训记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyTrainingRecord record)
    {
        return toAjax(safetyTrainingRecordService.updateSafetyTrainingRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:record:remove')")
    @Log(title = "培训记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(safetyTrainingRecordService.deleteSafetyTrainingRecordByIds(recordIds));
    }
}