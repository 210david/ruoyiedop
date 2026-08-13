package com.ruoyi.safety.controller;

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
import com.ruoyi.safety.domain.SafetyMaterial;
import com.ruoyi.safety.service.ISafetyMaterialService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/material")
public class SafetyMaterialController extends BaseController
{
    @Autowired
    private ISafetyMaterialService safetyMaterialService;

    @PreAuthorize("@ss.hasPermi('safety:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyMaterial material)
    {
        startPage();
        List<SafetyMaterial> list = safetyMaterialService.selectSafetyMaterialList(material);
        return getDataTable(list);
    }

    @Log(title = "安全物料", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:material:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyMaterial material)
    {
        List<SafetyMaterial> list = safetyMaterialService.selectSafetyMaterialList(material);
        ExcelUtil<SafetyMaterial> util = new ExcelUtil<>(SafetyMaterial.class);
        util.exportExcel(response, list, "安全物料");
    }

    /** 库存预警查询 - 必须放在/{materialId}之前，避免路径变量优先匹配 */
    @PreAuthorize("@ss.hasPermi('safety:material:list')")
    @GetMapping("/stockAlert")
    public AjaxResult stockAlert()
    {
        return AjaxResult.success(safetyMaterialService.selectStockAlertList());
    }

    @PreAuthorize("@ss.hasPermi('safety:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return AjaxResult.success(safetyMaterialService.selectSafetyMaterialById(materialId));
    }

    @PreAuthorize("@ss.hasPermi('safety:material:add')")
    @Log(title = "安全物料", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyMaterial material)
    {
        return toAjax(safetyMaterialService.insertSafetyMaterial(material));
    }

    @PreAuthorize("@ss.hasPermi('safety:material:edit')")
    @Log(title = "安全物料", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyMaterial material)
    {
        return toAjax(safetyMaterialService.updateSafetyMaterial(material));
    }

    @PreAuthorize("@ss.hasPermi('safety:material:remove')")
    @Log(title = "安全物料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(safetyMaterialService.deleteSafetyMaterialByIds(materialIds));
    }
}