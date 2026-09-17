package com.ruoyi.flowable.controller;

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
import com.ruoyi.flowable.domain.FlowCategory;
import com.ruoyi.flowable.service.IFlowCategoryService;

/**
 * 流程分类 Controller（后台：系统管理→流程管理→流程分类管理）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/category")
public class FlowCategoryController extends BaseController
{
    @Autowired
    private IFlowCategoryService flowCategoryService;

    /**
     * 查询流程分类列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowCategory query)
    {
        List<FlowCategory> list = flowCategoryService.selectFlowCategoryList(query);
        return getDataTable(list);
    }

    /**
     * 获取流程分类详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:category:query')")
    @GetMapping("/{dictCode}")
    public AjaxResult getInfo(@PathVariable Long dictCode)
    {
        return success(flowCategoryService.selectFlowCategoryByCode(dictCode));
    }

    /**
     * 新增流程分类
     */
    @Log(title = "流程分类", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:category:add')")
    @PostMapping
    public AjaxResult add(@RequestBody FlowCategory category)
    {
        return toAjax(flowCategoryService.insertFlowCategory(category));
    }

    /**
     * 修改流程分类
     */
    @Log(title = "流程分类", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:category:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody FlowCategory category)
    {
        return toAjax(flowCategoryService.updateFlowCategory(category));
    }

    /**
     * 删除流程分类
     */
    @Log(title = "流程分类", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:category:remove')")
    @DeleteMapping("/{dictCodes}")
    public AjaxResult remove(@PathVariable Long[] dictCodes)
    {
        return toAjax(flowCategoryService.deleteFlowCategoryByCodes(dictCodes));
    }
}
