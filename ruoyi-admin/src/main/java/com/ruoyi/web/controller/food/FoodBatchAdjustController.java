package com.ruoyi.web.controller.food;

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
import com.ruoyi.food.domain.FoodBatchAdjust;
import com.ruoyi.food.service.IFoodBatchAdjustService;

/**
 * 食品批次调整单Controller（冻结/解冻/报废/拆批/合批/召回/效期调整）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/adjust")
public class FoodBatchAdjustController extends BaseController
{
    @Autowired
    private IFoodBatchAdjustService adjustService;

    /**
     * 查询调整单列表
     */
    @PreAuthorize("@ss.hasPermi('food:adjust:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodBatchAdjust adjust)
    {
        startPage();
        List<FoodBatchAdjust> list = adjustService.selectAdjustList(adjust);
        return getDataTable(list);
    }

    /**
     * 调整单详情（含明细）
     */
    @PreAuthorize("@ss.hasPermi('food:adjust:query')")
    @GetMapping(value = "/{adjustId}")
    public AjaxResult getInfo(@PathVariable("adjustId") Long adjustId)
    {
        return success(adjustService.selectAdjustById(adjustId));
    }

    /**
     * 新增调整单（待执行）
     */
    @PreAuthorize("@ss.hasPermi('food:adjust:add')")
    @Log(title = "批次调整单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodBatchAdjust adjust)
    {
        adjust.setCreateBy(getUsername());
        return toAjax(adjustService.insertAdjust(adjust));
    }

    /**
     * 执行调整单
     */
    @PreAuthorize("@ss.hasPermi('food:adjust:execute')")
    @Log(title = "批次调整单", businessType = BusinessType.UPDATE)
    @PutMapping("/execute/{adjustId}")
    public AjaxResult execute(@PathVariable Long adjustId)
    {
        return toAjax(adjustService.executeAdjust(adjustId));
    }

    /**
     * 取消调整单
     */
    @PreAuthorize("@ss.hasPermi('food:adjust:cancel')")
    @Log(title = "批次调整单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{adjustId}")
    public AjaxResult cancel(@PathVariable Long adjustId)
    {
        return toAjax(adjustService.cancelAdjust(adjustId));
    }

    /**
     * 删除调整单
     */
    @PreAuthorize("@ss.hasPermi('food:adjust:remove')")
    @Log(title = "批次调整单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{adjustIds}")
    public AjaxResult remove(@PathVariable Long[] adjustIds)
    {
        return toAjax(adjustService.deleteAdjustByIds(adjustIds));
    }
}
