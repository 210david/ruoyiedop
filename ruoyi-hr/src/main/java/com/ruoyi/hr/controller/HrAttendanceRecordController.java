package com.ruoyi.hr.controller;

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
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.service.IHrAttendanceRecordService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/attendance")
public class HrAttendanceRecordController extends BaseController
{
    @Autowired
    private IHrAttendanceRecordService hrAttendanceRecordService;

    @PreAuthorize("@ss.hasPermi('hr:attendance:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrAttendanceRecord hrAttendanceRecord)
    {
        startPage();
        List<HrAttendanceRecord> list = hrAttendanceRecordService.selectHrAttendanceRecordList(hrAttendanceRecord);
        return getDataTable(list);
    }

    @Log(title = "考勤记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:attendance:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrAttendanceRecord hrAttendanceRecord)
    {
        List<HrAttendanceRecord> list = hrAttendanceRecordService.selectHrAttendanceRecordList(hrAttendanceRecord);
        ExcelUtil<HrAttendanceRecord> util = new ExcelUtil<>(HrAttendanceRecord.class);
        util.exportExcel(response, list, "考勤记录");
    }

    @PreAuthorize("@ss.hasPermi('hr:attendance:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(hrAttendanceRecordService.selectHrAttendanceRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('hr:attendance:add')")
    @Log(title = "考勤记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrAttendanceRecord hrAttendanceRecord)
    {
        return toAjax(hrAttendanceRecordService.insertHrAttendanceRecord(hrAttendanceRecord));
    }

    @PreAuthorize("@ss.hasPermi('hr:attendance:edit')")
    @Log(title = "考勤记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrAttendanceRecord hrAttendanceRecord)
    {
        return toAjax(hrAttendanceRecordService.updateHrAttendanceRecord(hrAttendanceRecord));
    }

    @PreAuthorize("@ss.hasPermi('hr:attendance:remove')")
    @Log(title = "考勤记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(hrAttendanceRecordService.deleteHrAttendanceRecordByIds(recordIds));
    }
}
