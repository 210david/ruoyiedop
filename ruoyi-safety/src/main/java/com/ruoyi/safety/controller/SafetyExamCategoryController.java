package com.ruoyi.safety.controller;

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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.safety.domain.SafetyExamCategory;
import com.ruoyi.safety.service.ISafetyExamCategoryService;

/**
 * 考试题库分类 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/safety/exam/category")
public class SafetyExamCategoryController extends BaseController
{
    @Autowired
    private ISafetyExamCategoryService safetyExamCategoryService;

    /**
     * 查询分类列表
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyExamCategory query)
    {
        startPage();
        List<SafetyExamCategory> list = safetyExamCategoryService.selectSafetyExamCategoryList(query);
        return getDataTable(list);
    }

    /**
     * 分类下拉/树数据（不分页，题库页左侧分类树用）
     */
    @GetMapping("/options")
    public AjaxResult options(SafetyExamCategory query)
    {
        query.setStatus("0");
        return AjaxResult.success(safetyExamCategoryService.selectSafetyExamCategoryList(query));
    }

    /**
     * 分类详情
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:question:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return AjaxResult.success(safetyExamCategoryService.selectSafetyExamCategoryById(categoryId));
    }

    /**
     * 新增分类
     */
    @Log(title = "考试题库分类", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SafetyExamCategory category)
    {
        return toAjax(safetyExamCategoryService.insertSafetyExamCategory(category));
    }

    /**
     * 修改分类
     */
    @Log(title = "考试题库分类", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SafetyExamCategory category)
    {
        return toAjax(safetyExamCategoryService.updateSafetyExamCategory(category));
    }

    /**
     * 删除分类（分类下有题目时拒绝删除）
     */
    @Log(title = "考试题库分类", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:remove')")
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(safetyExamCategoryService.deleteSafetyExamCategoryByIds(categoryIds));
    }
}
