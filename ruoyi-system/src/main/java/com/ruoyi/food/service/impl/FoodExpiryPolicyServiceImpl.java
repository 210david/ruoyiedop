package com.ruoyi.food.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.food.domain.FoodExpiryPolicy;
import com.ruoyi.food.mapper.FoodBatchMapper;
import com.ruoyi.food.mapper.FoodExpiryPolicyMapper;
import com.ruoyi.food.service.IFoodExpiryPolicyService;

/**
 * 食品效期策略Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class FoodExpiryPolicyServiceImpl implements IFoodExpiryPolicyService
{
    @Autowired
    private FoodExpiryPolicyMapper policyMapper;

    @Autowired
    private FoodBatchMapper batchMapper;

    @Override
    public FoodExpiryPolicy selectPolicyById(Long policyId)
    {
        return policyMapper.selectPolicyById(policyId);
    }

    @Override
    public List<FoodExpiryPolicy> selectPolicyList(FoodExpiryPolicy policy)
    {
        return policyMapper.selectPolicyList(policy);
    }

    @Override
    public int insertPolicy(FoodExpiryPolicy policy)
    {
        if (policy.getMaterialId() == null)
        {
            policy.setMaterialId(0L);
        }
        if (policy.getMaterialType() == null)
        {
            policy.setMaterialType("");
        }
        if (policy.getStatus() == null)
        {
            policy.setStatus("0");
        }
        if (policy.getFefoEnabled() == null)
        {
            policy.setFefoEnabled("1");
        }
        policy.setCreateBy(policy.getCreateBy() == null ? "system" : policy.getCreateBy());
        return policyMapper.insertPolicy(policy);
    }

    @Override
    public int updatePolicy(FoodExpiryPolicy policy)
    {
        policy.setUpdateBy(policy.getUpdateBy() == null ? "system" : policy.getUpdateBy());
        return policyMapper.updatePolicy(policy);
    }

    @Override
    public int deletePolicyByIds(Long[] policyIds)
    {
        return policyMapper.deletePolicyByIds(policyIds);
    }

    /**
     * 三级匹配：物料 → 物料类别 → 默认
     */
    @Override
    public FoodExpiryPolicy matchPolicy(Long materialId, String materialType)
    {
        FoodExpiryPolicy policy = null;
        if (materialId != null && materialId > 0)
        {
            policy = policyMapper.selectPolicyByMaterial(materialId);
        }
        if (policy == null && materialType != null && !materialType.isEmpty())
        {
            policy = policyMapper.selectPolicyByMaterialType(materialType);
        }
        if (policy == null)
        {
            policy = policyMapper.selectDefaultPolicy();
        }
        return policy;
    }

    @Override
    public int refreshExpireStatus()
    {
        return batchMapper.refreshExpireStatus();
    }
}
