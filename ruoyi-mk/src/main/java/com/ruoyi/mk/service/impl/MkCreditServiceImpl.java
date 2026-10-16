package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.mapper.MkCreditMapper;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.service.IMkCreditService;

/**
 * 客户信用控制 Service实现
 *
 * 欠款占用 = 应收未收（fin_ar_order OPEN：total-received-bad_debt）
 *          + 已审核未开票订单（mk_order 状态2/3 且无 mk_invoice）
 * 信用控制：credit_status=1 冻结禁止下单；credit_limit>0 时 欠款占用+本单金额 超额拦截
 *
 * @author ruoyi
 */
@Service
public class MkCreditServiceImpl implements IMkCreditService
{
    @Autowired
    private MkCreditMapper mkCreditMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Override
    public Map<String, Object> selectCreditUsage(Long customerId)
    {
        MkCustomer customer = mkCustomerMapper.selectCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }
        BigDecimal arOutstanding = mkCreditMapper.selectArOutstanding(customerId);
        BigDecimal uninvoiced = mkCreditMapper.selectUninvoicedOrderAmount(customerId);
        BigDecimal used = arOutstanding.add(uninvoiced);
        BigDecimal limit = customer.getCreditLimit() != null ? customer.getCreditLimit() : BigDecimal.ZERO;
        BigDecimal available = limit.subtract(used);

        Map<String, Object> result = new HashMap<>();
        result.put("customerId", customerId);
        result.put("customerName", customer.getCustomerName());
        result.put("creditLimit", limit);
        result.put("creditDays", customer.getCreditDays());
        result.put("creditStatus", customer.getCreditStatus());
        result.put("arOutstanding", arOutstanding);
        result.put("uninvoicedAmount", uninvoiced);
        result.put("used", used);
        result.put("available", available);
        return result;
    }

    @Override
    public void checkCreditForOrder(Long customerId, BigDecimal orderAmount)
    {
        if (customerId == null)
        {
            throw new ServiceException("订单必须指定客户");
        }
        MkCustomer customer = mkCustomerMapper.selectCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在");
        }
        if ("1".equals(customer.getCreditStatus()))
        {
            throw new ServiceException("客户【" + customer.getCustomerName() + "】信用已冻结，禁止下单");
        }
        BigDecimal limit = customer.getCreditLimit() != null ? customer.getCreditLimit() : BigDecimal.ZERO;
        if (limit.compareTo(BigDecimal.ZERO) <= 0)
        {
            // 未启用信用额度控制
            return;
        }
        BigDecimal amount = orderAmount != null ? orderAmount : BigDecimal.ZERO;
        BigDecimal used = mkCreditMapper.selectArOutstanding(customerId)
                .add(mkCreditMapper.selectUninvoicedOrderAmount(customerId));
        if (used.add(amount).compareTo(limit) > 0)
        {
            throw new ServiceException(String.format(
                    "客户【%s】超出信用额度：已占用欠款 %s 元 + 本单 %s 元 > 信用额度 %s 元，禁止下单",
                    customer.getCustomerName(), used.toPlainString(), amount.toPlainString(), limit.toPlainString()));
        }
    }
}
