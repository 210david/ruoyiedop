package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinMaterialTax;
import com.ruoyi.fin.service.IFinMaterialTaxService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料税率档案 Controller
 *
 * <p><b>为什么单独提供接口</b>：FinCostEngine 取税率时查不到档案会直接抛 10008，
 * 不做默认回落。若提供"批量按13% 初始化"接口，
 * 须明确告知风险——零税率物料会被错按 13% 计税且难以察觉。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/materialTax")
public class FinMaterialTaxController extends BaseController
{
    @Autowired
    private IFinMaterialTaxService taxService;

    /**
     * 查询税率档案列表
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinMaterialTax tax)
    {
        startPage();
        return getDataTable(taxService.selectTaxList(tax));
    }

    /**
     * 按物料编码查生效税率
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:query')")
    @GetMapping("/effective/{materialCode}")
    public AjaxResult effective(@PathVariable("materialCode") String materialCode)
    {
        return AjaxResult.success(taxService.getEffectiveTax(materialCode));
    }

    /**
     * 新增税率档案
     */
    @Log(title = "物料税率档案", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:tax')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FinMaterialTax tax)
    {
        return toAjax(taxService.insertTax(tax));
    }

    /**
     * 修改税率档案
     */
    @Log(title = "物料税率档案", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:tax')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FinMaterialTax tax)
    {
        return toAjax(taxService.updateTax(tax));
    }

    /**
     * 删除税率档案
     */
    @Log(title = "物料税率档案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:cost:tax')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(taxService.deleteTaxByIds(ids));
    }

    /**
     * 未建税率档案的物料（提示补录）
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:list')")
    @GetMapping("/untaxed")
    public AjaxResult untaxed(@RequestParam(defaultValue = "100") int limit)
    {
        return AjaxResult.success(taxService.untaxedMaterials(limit));
    }

    /**
     * 批量按默认税率初始化（慎用）
     */
    @Log(title = "物料税率批量初始化", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:tax')")
    @PostMapping("/batchInit")
    public AjaxResult batchInit(@RequestParam String taxRate)
    {
        return AjaxResult.success(taxService.batchInit(taxRate));
    }

    /**
     * 税率档案统计
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:query')")
    @GetMapping("/count")
    public AjaxResult count()
    {
        return AjaxResult.success(taxService.countEffective());
    }

    /**
     * 导出税率档案
     */
    @Log(title = "物料税率档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:cost:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinMaterialTax tax)
    {
        ExcelUtil<FinMaterialTax> util = new ExcelUtil<>(FinMaterialTax.class);
        util.exportExcel(response, taxService.selectTaxList(tax), "物料税率档案数据");
    }
}