package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkPricePolicy;

/**
 * 价格政策 Service接口
 *
 * @author ruoyi
 */
public interface IMkPricePolicyService
{
    public List<MkPricePolicy> selectPolicyList(MkPricePolicy policy);

    public MkPricePolicy selectPolicyById(Long policyId);

    public int insertPolicy(MkPricePolicy policy);

    public int updatePolicy(MkPricePolicy policy);

    public int deletePolicyByIds(Long[] policyIds);

    /** 下单取价：客户专价 → 等级价 → 无政策；返回 {unitPrice, source, policyNo} */
    public Map<String, Object> applyPrice(Long customerId, Long materialId);
}
