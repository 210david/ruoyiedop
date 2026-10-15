package com.ruoyi.mk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkPricePolicy;

/**
 * 价格政策 Mapper
 *
 * @author ruoyi
 */
public interface MkPricePolicyMapper
{
    public List<MkPricePolicy> selectPolicyList(MkPricePolicy policy);

    public MkPricePolicy selectPolicyById(Long policyId);

    /** 取价：客户专价（启用+在有效期，取最新一条） */
    public MkPricePolicy selectCustomerPrice(@Param("customerId") Long customerId, @Param("materialId") Long materialId);

    /** 取价：等级价（启用+在有效期，取最新一条） */
    public MkPricePolicy selectLevelPrice(@Param("customerLevel") String customerLevel, @Param("materialId") Long materialId);

    public int insertPolicy(MkPricePolicy policy);

    public int updatePolicy(MkPricePolicy policy);

    public int deletePolicyByIds(Long[] policyIds);
}
