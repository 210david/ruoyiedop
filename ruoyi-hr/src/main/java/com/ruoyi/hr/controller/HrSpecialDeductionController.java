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
import com.ruoyi.hr.domain.HrSpecialDeduction;
import com.ruoyi.hr.service.IHrSpecialDeductionService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/specialDeduction")
public class HrSpecialDeductionController extends BaseController
{
    @Autowired
    private IHrSpecialDeductionService hrSpecialDeductionService;

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSpecialDeduction hrSpecialDeduction)
    {
        startPage();
        List<HrSpecialDeduction> list = hrSpecialDeductionService.selectHrSpecialDeductionList(hrSpecialDeduction);
        return getDataTable(list);
    }

    @Log(title = "专项附加扣除", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSpecialDeduction hrSpecialDeduction)
    {
        List<HrSpecialDeduction> list = hrSpecialDeductionService.selectHrSpecialDeductionList(hrSpecialDeduction);
        ExcelUtil<HrSpecialDeduction> util = new ExcelUtil<>(HrSpecialDeduction.class);
        util.exportExcel(response, list, "专项附加扣除");
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:query')")
    @GetMapping(value = "/{sdId}")
    public AjaxResult getInfo(@PathVariable("sdId") Long sdId)
    {
        return AjaxResult.success(hrSpecialDeductionService.selectHrSpecialDeductionById(sdId));
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:add')")
    @Log(title = "专项附加扣除", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSpecialDeduction hrSpecialDeduction)
    {
        return toAjax(hrSpecialDeductionService.insertHrSpecialDeduction(hrSpecialDeduction));
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:edit')")
    @Log(title = "专项附加扣除", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSpecialDeduction hrSpecialDeduction)
    {
        return toAjax(hrSpecialDeductionService.updateHrSpecialDeduction(hrSpecialDeduction));
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:remove')")
    @Log(title = "专项附加扣除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sdIds}")
    public AjaxResult remove(@PathVariable Long[] sdIds)
    {
        return toAjax(hrSpecialDeductionService.deleteHrSpecialDeductionByIds(sdIds));
    }
}
