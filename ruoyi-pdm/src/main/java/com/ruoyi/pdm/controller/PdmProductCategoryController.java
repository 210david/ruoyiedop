package com.ruoyi.pdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pdm.domain.PdmProductCategory;
import com.ruoyi.pdm.service.IPdmProductCategoryService;

/**
 * 产品分类 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/category")
public class PdmProductCategoryController extends BaseController
{
    @Autowired
    private IPdmProductCategoryService categoryService;

    /**
     * 查询产品分类列表（平铺）
     */
    @PreAuthorize("@ss.hasPermi('pdm:category:list')")
    @GetMapping("/list")
    public AjaxResult list(PdmProductCategory category)
    {
        return success(categoryService.selectCategoryList(category));
    }

    /**
     * 查询产品分类树
     */
    @PreAuthorize("@ss.hasPermi('pdm:category:list')")
    @GetMapping("/tree")
    public AjaxResult tree(PdmProductCategory category)
    {
        return success(categoryService.buildCategoryTree(categoryService.selectCategoryList(category)));
    }

    /**
     * 获取分类详情
     */
    @PreAuthorize("@ss.hasPermi('pdm:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 新增分类（分类编码自动生成）
     */
    @Log(title = "产品分类", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:category:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PdmProductCategory category)
    {
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改分类
     */
    @Log(title = "产品分类", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:category:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody PdmProductCategory category)
    {
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除分类（校验子分类与产品引用）
     */
    @Log(title = "产品分类", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:category:remove')")
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryService.deleteCategoryByIds(categoryIds));
    }
}
