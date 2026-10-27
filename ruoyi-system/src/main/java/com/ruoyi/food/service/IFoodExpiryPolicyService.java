package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodExpiryPolicy;

/**
 * 食品效期策略Service接口
 *
 * @author ruoyi
 */
public interface IFoodExpiryPolicyService
{
    public FoodExpiryPolicy selectPolicyById(Long policyId);

    public List<FoodExpiryPolicy> selectPolicyList(FoodExpiryPolicy policy);

    public int insertPolicy(FoodExpiryPolicy policy);

    public int updatePolicy(FoodExpiryPolicy policy);

    public int deletePolicyByIds(Long[] policyIds);

    /**
     * 三级匹配效期策略：物料 → 物料类别 → 默认
     *
     * @param materialId 物料ID
     * @param materialType 物料类别
     * @return 命中策略，未命中返回null
     */
    public FoodExpiryPolicy matchPolicy(Long materialId, String materialType);

    /**
     * 批量刷新全部批次效期状态（临期/过期）
     *
     * @return 影响行数
     */
    public int refreshExpireStatus();
}
