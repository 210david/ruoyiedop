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
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.service.IHrShiftService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/shift")
public class HrShiftController extends BaseController
{
    @Autowired
    private IHrShiftService hrShiftService;

    @PreAuthorize("@ss.hasPermi('hr:shift:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrShift hrShift)
    {
        startPage();
        List<HrShift> list = hrShiftService.selectHrShiftList(hrShift);
        return getDataTable(list);
    }

    @Log(title = "班次", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:shift:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrShift hrShift)
    {
        List<HrShift> list = hrShiftService.selectHrShiftList(hrShift);
        ExcelUtil<HrShift> util = new ExcelUtil<>(HrShift.class);
        util.exportExcel(response, list, "班次");
    }

    @PreAuthorize("@ss.hasPermi('hr:shift:query')")
    @GetMapping(value = "/{shiftId}")
    public AjaxResult getInfo(@PathVariable("shiftId") Long shiftId)
    {
        return AjaxResult.success(hrShiftService.selectHrShiftById(shiftId));
    }

    @PreAuthorize("@ss.hasPermi('hr:shift:add')")
    @Log(title = "班次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrShift hrShift)
    {
        return toAjax(hrShiftService.insertHrShift(hrShift));
    }

    @PreAuthorize("@ss.hasPermi('hr:shift:edit')")
    @Log(title = "班次", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrShift hrShift)
    {
        return toAjax(hrShiftService.updateHrShift(hrShift));
    }

    @PreAuthorize("@ss.hasPermi('hr:shift:remove')")
    @Log(title = "班次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{shiftIds}")
    public AjaxResult remove(@PathVariable Long[] shiftIds)
    {
        return toAjax(hrShiftService.deleteHrShiftByIds(shiftIds));
    }
}
