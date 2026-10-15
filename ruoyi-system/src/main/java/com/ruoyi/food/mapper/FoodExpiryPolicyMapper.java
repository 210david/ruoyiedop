package com.ruoyi.food.mapper;

import java.util.List;
import com.ruoyi.food.domain.FoodExpiryPolicy;

/**
 * 食品效期策略Mapper接口
 *
 * @author ruoyi
 */
public interface FoodExpiryPolicyMapper
{
    public FoodExpiryPolicy selectPolicyById(Long policyId);

    public List<FoodExpiryPolicy> selectPolicyList(FoodExpiryPolicy policy);

    /** 按物料精确匹配 */
    public FoodExpiryPolicy selectPolicyByMaterial(Long materialId);

    /** 按物料类别匹配 */
    public FoodExpiryPolicy selectPolicyByMaterialType(String materialType);

    /** 默认策略 */
    public FoodExpiryPolicy selectDefaultPolicy();

    public int insertPolicy(FoodExpiryPolicy policy);

    public int updatePolicy(FoodExpiryPolicy policy);

    public int deletePolicyByIds(Long[] policyIds);
}
