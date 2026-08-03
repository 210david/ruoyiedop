package com.ruoyi.pms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pms.domain.PmsSupplierCatalog;
import com.ruoyi.pms.service.IPmsSupplierCatalogService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 供应商供货清单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/catalog")
public class PmsSupplierCatalogController extends BaseController
{
    @Autowired
    private IPmsSupplierCatalogService pmsSupplierCatalogService;

    @PreAuthorize("@ss.hasPermi('pms:catalog:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsSupplierCatalog catalog)
    {
        startPage();
        List<PmsSupplierCatalog> list = pmsSupplierCatalogService.selectCatalogList(catalog);
        return getDataTable(list);
    }

    @Log(title = "供货清单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:catalog:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsSupplierCatalog catalog)
    {
        List<PmsSupplierCatalog> list = pmsSupplierCatalogService.selectCatalogList(catalog);
        ExcelUtil<PmsSupplierCatalog> util = new ExcelUtil<>(PmsSupplierCatalog.class);
        util.exportExcel(response, list, "供应商供货清单数据");
    }

    @Log(title = "供货清单", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('pms:catalog:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<PmsSupplierCatalog> util = new ExcelUtil<>(PmsSupplierCatalog.class);
        List<PmsSupplierCatalog> list = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = pmsSupplierCatalogService.importCatalog(list, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<PmsSupplierCatalog> util = new ExcelUtil<>(PmsSupplierCatalog.class);
        // 排除仅导出字段，使导入模板与新建表单字段一致
        util.excludeFields = new String[]{"materialName", "specModel", "unit", "currency"};
        util.importTemplateExcel(response, "供货清单数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:catalog:query')")
    @GetMapping(value = "/{catalogId}")
    public AjaxResult getInfo(@PathVariable("catalogId") Long catalogId)
    {
        return AjaxResult.success(pmsSupplierCatalogService.selectCatalogById(catalogId));
    }

    @Log(title = "供货清单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:catalog:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsSupplierCatalog catalog)
    {
        return toAjax(pmsSupplierCatalogService.insertCatalog(catalog));
    }

    @Log(title = "供货清单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:catalog:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsSupplierCatalog catalog)
    {
        return toAjax(pmsSupplierCatalogService.updateCatalog(catalog));
    }

    @Log(title = "供货清单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:catalog:remove')")
    @DeleteMapping("/{catalogIds}")
    public AjaxResult remove(@PathVariable Long[] catalogIds)
    {
        return toAjax(pmsSupplierCatalogService.deleteCatalogByIds(catalogIds));
    }
}
