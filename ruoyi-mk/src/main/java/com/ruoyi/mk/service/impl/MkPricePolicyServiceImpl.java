package com.ruoyi.mk.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkPricePolicy;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.mapper.MkPricePolicyMapper;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mk.service.IMkPricePolicyService;

/**
 * 价格政策 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkPricePolicyServiceImpl implements IMkPricePolicyService
{
    @Autowired
    private MkPricePolicyMapper mkPricePolicyMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkPricePolicy> selectPolicyList(MkPricePolicy policy)
    {
        return mkPricePolicyMapper.selectPolicyList(policy);
    }

    @Override
    public MkPricePolicy selectPolicyById(Long policyId)
    {
        return mkPricePolicyMapper.selectPolicyById(policyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPolicy(MkPricePolicy policy)
    {
        validatePolicy(policy);
        if (StringUtils.isEmpty(policy.getPolicyNo()))
        {
            policy.setPolicyNo(mkNumberRuleService.generateNumber("mk_price_policy"));
        }
        if (policy.getStatus() == null)
        {
            policy.setStatus("0");
        }
        policy.setCreateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkPricePolicyMapper.insertPolicy(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePolicy(MkPricePolicy policy)
    {
        validatePolicy(policy);
        policy.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkPricePolicyMapper.updatePolicy(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePolicyByIds(Long[] policyIds)
    {
        return mkPricePolicyMapper.deletePolicyByIds(policyIds);
    }

    @Override
    public Map<String, Object> applyPrice(Long customerId, Long materialId)
    {
        Map<String, Object> result = new HashMap<>();
        if (materialId == null)
        {
            throw new ServiceException("物料不能为空");
        }
        // 1. 客户专价
        if (customerId != null)
        {
            MkPricePolicy custPolicy = mkPricePolicyMapper.selectCustomerPrice(customerId, materialId);
            if (custPolicy != null)
            {
                result.put("unitPrice", custPolicy.getUnitPrice());
                result.put("source", "客户专价");
                result.put("policyNo", custPolicy.getPolicyNo());
                return result;
            }
            MkCustomer customer = mkCustomerMapper.selectCustomerById(customerId);
            // 2. 经销商供货价（仅经销商客户，按经销层级匹配，插在客户专价与等级价之间）
            if (customer != null && MkCustomer.TYPE_DEALER.equals(customer.getCustomerType()))
            {
                // 经销层级随上级推导：无上级=一级(1)，有上级=二级(2)
                String dealerLevel = customer.getParentCustomerId() == null ? "1" : "2";
                MkPricePolicy dealerPolicy = mkPricePolicyMapper.selectDealerPrice(dealerLevel, materialId);
                if (dealerPolicy != null)
                {
                    result.put("unitPrice", dealerPolicy.getUnitPrice());
                    result.put("source", "经销商供货价");
                    result.put("policyNo", dealerPolicy.getPolicyNo());
                    return result;
                }
            }
            // 3. 等级价（按客户等级匹配）
            if (customer != null && StringUtils.isNotEmpty(customer.getCustomerLevel()))
            {
                MkPricePolicy levelPolicy = mkPricePolicyMapper.selectLevelPrice(customer.getCustomerLevel(), materialId);
                if (levelPolicy != null)
                {
                    result.put("unitPrice", levelPolicy.getUnitPrice());
                    result.put("source", "等级价");
                    result.put("policyNo", levelPolicy.getPolicyNo());
                    return result;
                }
            }
        }
        // 4. 无政策
        result.put("unitPrice", null);
        result.put("source", "");
        result.put("policyNo", null);
        return result;
    }

    private void validatePolicy(MkPricePolicy policy)
    {
        if (policy.getMaterialId() == null)
        {
            throw new ServiceException("物料不能为空");
        }
        if (policy.getUnitPrice() == null || policy.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("政策单价必须大于0");
        }
        if (policy.getStartDate() == null || policy.getEndDate() == null)
        {
            throw new ServiceException("生效/失效日期不能为空");
        }
        if (policy.getEndDate().before(policy.getStartDate()))
        {
            throw new ServiceException("失效日期不能早于生效日期");
        }
        if ("1".equals(policy.getPriceType()) && policy.getCustomerId() == null)
        {
            throw new ServiceException("客户专价必须指定客户");
        }
        if ("2".equals(policy.getPriceType()) && StringUtils.isEmpty(policy.getCustomerLevel()))
        {
            throw new ServiceException("等级价必须指定客户等级");
        }
        // 经销商供货价：customer_level 复用为经销层级（1一级 2二级）
        if ("3".equals(policy.getPriceType()) && StringUtils.isEmpty(policy.getCustomerLevel()))
        {
            throw new ServiceException("经销商供货价必须指定经销层级");
        }
        if (policy.getPriceType() == null || policy.getPriceType().isEmpty())
        {
            policy.setPriceType("1");
        }
    }
}
