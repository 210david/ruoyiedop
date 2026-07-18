package com.ruoyi.dms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.dms.domain.DmsEquipmentCategory;
import com.ruoyi.dms.service.IDmsEquipmentCategoryService;

@RestController
@RequestMapping("/dms/category")
public class DmsEquipmentCategoryController extends BaseController
{
    @Autowired
    private IDmsEquipmentCategoryService dmsEquipmentCategoryService;

    @PreAuthorize("@ss.hasPermi('dms:category:list')")
    @GetMapping("/list")
    public AjaxResult list(DmsEquipmentCategory category)
    {
        List<DmsEquipmentCategory> list = dmsEquipmentCategoryService.selectCategoryList(category);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('dms:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return AjaxResult.success(dmsEquipmentCategoryService.selectCategoryById(categoryId));
    }

    @Log(title = "设备分类管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:category:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsEquipmentCategory category)
    {
        return toAjax(dmsEquipmentCategoryService.insertCategory(category));
    }

    @Log(title = "设备分类管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:category:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsEquipmentCategory category)
    {
        return toAjax(dmsEquipmentCategoryService.updateCategory(category));
    }

    @Log(title = "设备分类管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:category:remove')")
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(dmsEquipmentCategoryService.deleteCategoryByIds(categoryIds));
    }
}
