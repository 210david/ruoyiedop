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
import com.ruoyi.food.domain.FoodBatchRule;
import com.ruoyi.food.service.IFoodBatchRuleService;

/**
 * 食品批次号规则Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/rule")
public class FoodBatchRuleController extends BaseController
{
    @Autowired
    private IFoodBatchRuleService ruleService;

    /**
     * 查询批次规则列表
     */
    @PreAuthorize("@ss.hasPermi('food:rule:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodBatchRule rule)
    {
        startPage();
        List<FoodBatchRule> list = ruleService.selectRuleList(rule);
        return getDataTable(list);
    }

    /**
     * 规则详情
     */
    @PreAuthorize("@ss.hasPermi('food:rule:query')")
    @GetMapping(value = "/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId)
    {
        return success(ruleService.selectRuleById(ruleId));
    }

    /**
     * 新增规则
     */
    @PreAuthorize("@ss.hasPermi('food:rule:add')")
    @Log(title = "批次号规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodBatchRule rule)
    {
        rule.setCreateBy(getUsername());
        return toAjax(ruleService.insertRule(rule));
    }

    /**
     * 修改规则
     */
    @PreAuthorize("@ss.hasPermi('food:rule:edit')")
    @Log(title = "批次号规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodBatchRule rule)
    {
        rule.setUpdateBy(getUsername());
        return toAjax(ruleService.updateRule(rule));
    }

    /**
     * 删除规则
     */
    @PreAuthorize("@ss.hasPermi('food:rule:remove')")
    @Log(title = "批次号规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(ruleService.deleteRuleByIds(ruleIds));
    }

    /**
     * 规则试算预览（不消耗流水号）
     */
    @PreAuthorize("@ss.hasPermi('food:rule:query')")
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody FoodBatchRule rule)
    {
        return AjaxResult.success("操作成功", ruleService.previewPattern(rule.getRulePattern(), rule.getPreviewCode()));
    }
}
