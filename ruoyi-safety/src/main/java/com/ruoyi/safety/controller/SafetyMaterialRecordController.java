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
import com.ruoyi.safety.domain.SafetyMaterialRecord;
import com.ruoyi.safety.service.ISafetyMaterialRecordService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/materialRecord")
public class SafetyMaterialRecordController extends BaseController
{
    @Autowired
    private ISafetyMaterialRecordService safetyMaterialRecordService;

    @PreAuthorize("@ss.hasPermi('safety:materialRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyMaterialRecord record) {
        startPage();
        List<SafetyMaterialRecord> list = safetyMaterialRecordService.selectSafetyMaterialRecordList(record);
        return getDataTable(list);
    }

    @Log(title = "出入库记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:materialRecord:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyMaterialRecord record) {
        List<SafetyMaterialRecord> list = safetyMaterialRecordService.selectSafetyMaterialRecordList(record);
        ExcelUtil<SafetyMaterialRecord> util = new ExcelUtil<>(SafetyMaterialRecord.class);
        util.exportExcel(response, list, "出入库记录");
    }

    @PreAuthorize("@ss.hasPermi('safety:materialRecord:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        return AjaxResult.success(safetyMaterialRecordService.selectSafetyMaterialRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('safety:materialRecord:add')")
    @Log(title = "出入库记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyMaterialRecord record) {
        record.setOperator(getUsername());
        return toAjax(safetyMaterialRecordService.insertSafetyMaterialRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('safety:materialRecord:edit')")
    @Log(title = "出入库记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyMaterialRecord record) {
        return toAjax(safetyMaterialRecordService.updateSafetyMaterialRecord(record));
    }

    @PreAuthorize("@ss.hasPermi('safety:materialRecord:remove')")
    @Log(title = "出入库记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        return toAjax(safetyMaterialRecordService.deleteSafetyMaterialRecordByIds(recordIds));
    }
}
