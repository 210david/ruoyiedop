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
import com.ruoyi.hr.domain.HrAttendanceMonthly;
import com.ruoyi.hr.service.IHrAttendanceMonthlyService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/monthly")
public class HrAttendanceMonthlyController extends BaseController
{
    @Autowired
    private IHrAttendanceMonthlyService hrAttendanceMonthlyService;

    @PreAuthorize("@ss.hasPermi('hr:monthly:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrAttendanceMonthly hrAttendanceMonthly)
    {
        startPage();
        List<HrAttendanceMonthly> list = hrAttendanceMonthlyService.selectHrAttendanceMonthlyList(hrAttendanceMonthly);
        return getDataTable(list);
    }

    @Log(title = "考勤月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:monthly:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrAttendanceMonthly hrAttendanceMonthly)
    {
        List<HrAttendanceMonthly> list = hrAttendanceMonthlyService.selectHrAttendanceMonthlyList(hrAttendanceMonthly);
        ExcelUtil<HrAttendanceMonthly> util = new ExcelUtil<>(HrAttendanceMonthly.class);
        util.exportExcel(response, list, "考勤月报");
    }

    @PreAuthorize("@ss.hasPermi('hr:monthly:query')")
    @GetMapping(value = "/{monthlyId}")
    public AjaxResult getInfo(@PathVariable("monthlyId") Long monthlyId)
    {
        return AjaxResult.success(hrAttendanceMonthlyService.selectHrAttendanceMonthlyById(monthlyId));
    }

    @PreAuthorize("@ss.hasPermi('hr:monthly:add')")
    @Log(title = "考勤月报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrAttendanceMonthly hrAttendanceMonthly)
    {
        return toAjax(hrAttendanceMonthlyService.insertHrAttendanceMonthly(hrAttendanceMonthly));
    }

    @PreAuthorize("@ss.hasPermi('hr:monthly:edit')")
    @Log(title = "考勤月报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrAttendanceMonthly hrAttendanceMonthly)
    {
        return toAjax(hrAttendanceMonthlyService.updateHrAttendanceMonthly(hrAttendanceMonthly));
    }

    @PreAuthorize("@ss.hasPermi('hr:monthly:remove')")
    @Log(title = "考勤月报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{monthlyIds}")
    public AjaxResult remove(@PathVariable Long[] monthlyIds)
    {
        return toAjax(hrAttendanceMonthlyService.deleteHrAttendanceMonthlyByIds(monthlyIds));
    }
}
