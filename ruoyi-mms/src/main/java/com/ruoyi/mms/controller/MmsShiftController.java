package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsShift;
import com.ruoyi.mms.service.IMmsShiftService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Shift Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/shift")
public class MmsShiftController extends BaseController
{
    @Autowired
    private IMmsShiftService mmsShiftService;

    @PreAuthorize("@ss.hasPermi('mms:shift:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsShift shift)
    {
        startPage();
        List<MmsShift> list = mmsShiftService.selectShiftList(shift);
        return getDataTable(list);
    }

    @Log(title = "Shift", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:shift:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsShift shift)
    {
        List<MmsShift> list = mmsShiftService.selectShiftList(shift);
        ExcelUtil<MmsShift> util = new ExcelUtil<>(MmsShift.class);
        util.exportExcel(response, list, "Shift");
    }

    @PreAuthorize("@ss.hasPermi('mms:shift:query')")
    @GetMapping(value = "/{ShiftId}")
    public AjaxResult getInfo(@PathVariable("ShiftId") Long ShiftId)
    {
        return AjaxResult.success(mmsShiftService.selectShiftById(ShiftId));
    }

    @Log(title = "Shift", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:shift:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsShift shift)
    {
        return toAjax(mmsShiftService.insertShift(shift));
    }

    @Log(title = "Shift", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:shift:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsShift shift)
    {
        return toAjax(mmsShiftService.updateShift(shift));
    }

    @Log(title = "Shift", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:shift:remove')")
    @DeleteMapping("/{ShiftIds}")
    public AjaxResult remove(@PathVariable Long[] ShiftIds)
    {
        return toAjax(mmsShiftService.deleteShiftByIds(ShiftIds));
    }
}
