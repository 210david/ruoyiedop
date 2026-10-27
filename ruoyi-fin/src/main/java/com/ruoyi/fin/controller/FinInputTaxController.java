package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinInputTax;
import com.ruoyi.fin.domain.FinInventoryValuation;
import com.ruoyi.fin.service.IFinInputTaxService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 进项税抵扣与存货跌价准备 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/taxProvision")
public class FinInputTaxController extends BaseController
{
    @Autowired
    private IFinInputTaxService service;

    // ==================== 进项税抵扣台账 ====================

    /**
     * 查询进项税列表
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:list')")
    @GetMapping("/inputTax/list")
    public TableDataInfo inputTaxList(FinInputTax tax)
    {
        startPage();
        return getDataTable(service.selectInputTaxList(tax));
    }

    /**
     * 由应付单生成进项税台账
     */
    @Log(title = "进项税台账", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ap:inputTax')")
    @PostMapping("/inputTax/generate/{apId}")
    public AjaxResult generate(@PathVariable("apId") Long apId)
    {
        return AjaxResult.success(service.generateFromAp(apId));
    }

    /**
     * 标记进项税已抵扣
     */
    @Log(title = "进项税抵扣", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ap:inputTax')")
    @PutMapping("/inputTax/deduct/{taxId}")
    public AjaxResult deduct(@PathVariable("taxId") Long taxId,
                             @RequestParam String deductPeriod)
    {
        return toAjax(service.markDeducted(taxId, deductPeriod));
    }

    /**
     * 标记进项税作废
     */
    @Log(title = "进项税作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:ap:inputTax')")
    @PutMapping("/inputTax/void/{taxId}")
    public AjaxResult voidTax(@PathVariable("taxId") Long taxId)
    {
        return toAjax(service.markVoid(taxId));
    }

    /**
     * 批量标记超期未抵扣的进项税为已过期
     */
    @Log(title = "进项税过期处理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:ap:inputTax')")
    @PostMapping("/inputTax/markExpired")
    public AjaxResult markExpired()
    {
        return AjaxResult.success(service.markExpired());
    }

    /**
     * 进项税汇总
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:query')")
    @GetMapping("/inputTax/summary")
    public AjaxResult inputTaxSummary(@RequestParam(required = false) String deductStatus)
    {
        return AjaxResult.success(service.summary(deductStatus));
    }

    /**
     * 即将过期的进项税预警
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:query')")
    @GetMapping("/inputTax/expiring")
    public AjaxResult expiring(@RequestParam(required = false) Integer days)
    {
        return AjaxResult.success(service.expiringSoon(days));
    }

    /**
     * 导出进项税台账
     */
    @Log(title = "进项税台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:ap:export')")
    @PostMapping("/inputTax/export")
    public void exportInputTax(HttpServletResponse response, FinInputTax tax)
    {
        ExcelUtil<FinInputTax> util = new ExcelUtil<>(FinInputTax.class);
        util.exportExcel(response, service.selectInputTaxList(tax), "进项税抵扣台账");
    }

    // ==================== 存货跌价准备 ====================

    /**
     * 查询跌价准备列表
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:list')")
    @GetMapping("/provision/list")
    public TableDataInfo provisionList(FinInventoryValuation val)
    {
        startPage();
        return getDataTable(service.selectValuationList(val));
    }

    /**
     * 待计提跌价准备的存货清单
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:valuation')")
    @GetMapping("/provision/pending")
    public AjaxResult pending(@RequestParam String period)
    {
        return AjaxResult.success(service.pendingProvision(period));
    }

    /**
     * 执行跌价计提
     */
    @Log(title = "存货跌价准备", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:valuation')")
    @PostMapping("/provision/do")
    public AjaxResult doProvision(@RequestParam String period,
                                  @RequestParam Long materialId,
                                  @RequestParam BigDecimal ratio)
    {
        return AjaxResult.success(service.provision(period, materialId, ratio));
    }

    /**
     * 冲回跌价准备
     */
    @Log(title = "存货跌价准备冲回", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:valuation')")
    @PutMapping("/provision/reverse/{valId}")
    public AjaxResult reverse(@PathVariable("valId") Long valId)
    {
        return toAjax(service.reverseProvision(valId));
    }

    /**
     * 期间跌价准备汇总
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:valuation')")
    @GetMapping("/provision/summary")
    public AjaxResult provisionSummary(@RequestParam String period)
    {
        return AjaxResult.success(service.provisionSummary(period));
    }

    /**
     * 导出跌价准备
     */
    @Log(title = "存货跌价准备", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:cost:export')")
    @PostMapping("/provision/export")
    public void exportProvision(HttpServletResponse response, FinInventoryValuation val)
    {
        ExcelUtil<FinInventoryValuation> util = new ExcelUtil<>(FinInventoryValuation.class);
        util.exportExcel(response, service.selectValuationList(val), "存货跌价准备");
    }
}