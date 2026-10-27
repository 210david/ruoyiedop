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
import com.ruoyi.food.domain.FoodExpiryPolicy;
import com.ruoyi.food.service.IFoodExpiryPolicyService;

/**
 * 食品效期策略Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/food/policy")
public class FoodExpiryPolicyController extends BaseController
{
    @Autowired
    private IFoodExpiryPolicyService policyService;

    /**
     * 查询效期策略列表
     */
    @PreAuthorize("@ss.hasPermi('food:policy:list')")
    @GetMapping("/list")
    public TableDataInfo list(FoodExpiryPolicy policy)
    {
        startPage();
        List<FoodExpiryPolicy> list = policyService.selectPolicyList(policy);
        return getDataTable(list);
    }

    /**
     * 策略详情
     */
    @PreAuthorize("@ss.hasPermi('food:policy:query')")
    @GetMapping(value = "/{policyId}")
    public AjaxResult getInfo(@PathVariable("policyId") Long policyId)
    {
        return success(policyService.selectPolicyById(policyId));
    }

    /**
     * 新增策略
     */
    @PreAuthorize("@ss.hasPermi('food:policy:add')")
    @Log(title = "效期策略", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FoodExpiryPolicy policy)
    {
        policy.setCreateBy(getUsername());
        return toAjax(policyService.insertPolicy(policy));
    }

    /**
     * 修改策略
     */
    @PreAuthorize("@ss.hasPermi('food:policy:edit')")
    @Log(title = "效期策略", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FoodExpiryPolicy policy)
    {
        policy.setUpdateBy(getUsername());
        return toAjax(policyService.updatePolicy(policy));
    }

    /**
     * 删除策略
     */
    @PreAuthorize("@ss.hasPermi('food:policy:remove')")
    @Log(title = "效期策略", businessType = BusinessType.DELETE)
    @DeleteMapping("/{policyIds}")
    public AjaxResult remove(@PathVariable Long[] policyIds)
    {
        return toAjax(policyService.deletePolicyByIds(policyIds));
    }

    /**
     * 三级匹配测试
     */
    @PreAuthorize("@ss.hasPermi('food:policy:query')")
    @GetMapping("/match")
    public AjaxResult match(Long materialId, String materialType)
    {
        return success(policyService.matchPolicy(materialId, materialType));
    }

    /**
     * 批量刷新批次效期状态
     */
    @PreAuthorize("@ss.hasPermi('food:policy:edit')")
    @Log(title = "效期策略", businessType = BusinessType.UPDATE)
    @PutMapping("/refresh")
    public AjaxResult refresh()
    {
        int rows = policyService.refreshExpireStatus();
        return success("已刷新 " + rows + " 个批次的效期状态");
    }
}
