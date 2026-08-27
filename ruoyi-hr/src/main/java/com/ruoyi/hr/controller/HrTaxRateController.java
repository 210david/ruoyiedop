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
import com.ruoyi.hr.domain.HrTaxRate;
import com.ruoyi.hr.service.IHrTaxRateService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/taxRate")
public class HrTaxRateController extends BaseController
{
    @Autowired
    private IHrTaxRateService hrTaxRateService;

    @PreAuthorize("@ss.hasPermi('hr:taxRate:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrTaxRate hrTaxRate)
    {
        startPage();
        List<HrTaxRate> list = hrTaxRateService.selectHrTaxRateList(hrTaxRate);
        return getDataTable(list);
    }

    @Log(title = "个税税率", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:taxRate:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrTaxRate hrTaxRate)
    {
        List<HrTaxRate> list = hrTaxRateService.selectHrTaxRateList(hrTaxRate);
        ExcelUtil<HrTaxRate> util = new ExcelUtil<>(HrTaxRate.class);
        util.exportExcel(response, list, "个税税率");
    }

    @PreAuthorize("@ss.hasPermi('hr:taxRate:query')")
    @GetMapping(value = "/{taxRateId}")
    public AjaxResult getInfo(@PathVariable("taxRateId") Long taxRateId)
    {
        return AjaxResult.success(hrTaxRateService.selectHrTaxRateById(taxRateId));
    }

    @PreAuthorize("@ss.hasPermi('hr:taxRate:add')")
    @Log(title = "个税税率", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrTaxRate hrTaxRate)
    {
        return toAjax(hrTaxRateService.insertHrTaxRate(hrTaxRate));
    }

    @PreAuthorize("@ss.hasPermi('hr:taxRate:edit')")
    @Log(title = "个税税率", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrTaxRate hrTaxRate)
    {
        return toAjax(hrTaxRateService.updateHrTaxRate(hrTaxRate));
    }

    @PreAuthorize("@ss.hasPermi('hr:taxRate:remove')")
    @Log(title = "个税税率", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taxRateIds}")
    public AjaxResult remove(@PathVariable Long[] taxRateIds)
    {
        return toAjax(hrTaxRateService.deleteHrTaxRateByIds(taxRateIds));
    }
}
