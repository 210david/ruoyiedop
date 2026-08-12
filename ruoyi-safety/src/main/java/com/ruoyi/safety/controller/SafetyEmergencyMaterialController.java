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
import com.ruoyi.safety.domain.SafetyEmergencyMaterial;
import com.ruoyi.safety.service.ISafetyEmergencyMaterialService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/emergency/material")
public class SafetyEmergencyMaterialController extends BaseController
{
    @Autowired
    private ISafetyEmergencyMaterialService safetyEmergencyMaterialService;

    @PreAuthorize("@ss.hasPermi('safety:emergency:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyEmergencyMaterial material)
    {
        startPage();
        List<SafetyEmergencyMaterial> list = safetyEmergencyMaterialService.selectSafetyEmergencyMaterialList(material);
        return getDataTable(list);
    }

    @Log(title = "应急物资", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:emergency:material:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyEmergencyMaterial material)
    {
        List<SafetyEmergencyMaterial> list = safetyEmergencyMaterialService.selectSafetyEmergencyMaterialList(material);
        ExcelUtil<SafetyEmergencyMaterial> util = new ExcelUtil<>(SafetyEmergencyMaterial.class);
        util.exportExcel(response, list, "应急物资");
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return AjaxResult.success(safetyEmergencyMaterialService.selectSafetyEmergencyMaterialById(materialId));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:material:add')")
    @Log(title = "应急物资", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyEmergencyMaterial material)
    {
        return toAjax(safetyEmergencyMaterialService.insertSafetyEmergencyMaterial(material));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:material:edit')")
    @Log(title = "应急物资", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyEmergencyMaterial material)
    {
        return toAjax(safetyEmergencyMaterialService.updateSafetyEmergencyMaterial(material));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:material:remove')")
    @Log(title = "应急物资", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(safetyEmergencyMaterialService.deleteSafetyEmergencyMaterialByIds(materialIds));
    }
}