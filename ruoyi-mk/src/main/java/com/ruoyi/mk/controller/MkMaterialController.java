package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkMaterial;
import com.ruoyi.mk.service.IMkMaterialService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 素材库 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/material")
public class MkMaterialController extends BaseController
{
    @Autowired
    private IMkMaterialService mkMaterialService;

    @PreAuthorize("@ss.hasPermi('marketing:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkMaterial material)
    {
        startPage();
        List<MkMaterial> list = mkMaterialService.selectMaterialList(material);
        return getDataTable(list);
    }

    @Log(title = "素材库", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:material:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkMaterial material)
    {
        List<MkMaterial> list = mkMaterialService.selectMaterialList(material);
        ExcelUtil<MkMaterial> util = new ExcelUtil<>(MkMaterial.class);
        util.exportExcel(response, list, "素材数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return AjaxResult.success(mkMaterialService.selectMaterialById(materialId));
    }

    @Log(title = "素材库", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:material:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkMaterial material)
    {
        return toAjax(mkMaterialService.insertMaterial(material));
    }

    @Log(title = "素材库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:material:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkMaterial material)
    {
        return toAjax(mkMaterialService.updateMaterial(material));
    }

    @Log(title = "素材库", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:material:remove')")
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(mkMaterialService.deleteMaterialByIds(materialIds));
    }
}
