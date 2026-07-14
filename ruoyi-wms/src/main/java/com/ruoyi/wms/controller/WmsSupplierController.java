package com.ruoyi.wms.controller;

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
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.service.IWmsSupplierService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/supplier")
public class WmsSupplierController extends BaseController
{
    @Autowired
    private IWmsSupplierService wmsSupplierService;

    @PreAuthorize("@ss.hasPermi('wms:supplier:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsSupplier supplier)
    {
        startPage();
        List<WmsSupplier> list = wmsSupplierService.selectSupplierList(supplier);
        return getDataTable(list);
    }

    @Log(title = "供应商管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:supplier:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsSupplier supplier)
    {
        List<WmsSupplier> list = wmsSupplierService.selectSupplierList(supplier);
        ExcelUtil<WmsSupplier> util = new ExcelUtil<>(WmsSupplier.class);
        util.exportExcel(response, list, "供应商数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:supplier:query')")
    @GetMapping(value = "/{supplierId}")
    public AjaxResult getInfo(@PathVariable("supplierId") Long supplierId)
    {
        return AjaxResult.success(wmsSupplierService.selectSupplierById(supplierId));
    }

    @Log(title = "供应商管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:supplier:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsSupplier supplier)
    {
        return toAjax(wmsSupplierService.insertSupplier(supplier));
    }

    @Log(title = "供应商管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:supplier:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsSupplier supplier)
    {
        return toAjax(wmsSupplierService.updateSupplier(supplier));
    }

    @Log(title = "供应商管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:supplier:remove')")
    @DeleteMapping("/{supplierIds}")
    public AjaxResult remove(@PathVariable Long[] supplierIds)
    {
        return toAjax(wmsSupplierService.deleteSupplierByIds(supplierIds));
    }
}
