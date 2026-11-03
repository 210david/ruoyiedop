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
    public void checkCreditForOrder(Long customerId, BigDecimal orderAmount, String settleType)
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
        // 个人客户强制现结（下单+审核双节点拦截，settle_type=0现结 1账期）
        if (MkCustomer.TYPE_PERSONAL.equals(customer.getCustomerType()) && !"0".equals(settleType))
        {
            throw new ServiceException("个人客户【" + customer.getCustomerName() + "】强制现结，不允许账期结算");
        }
        // 现结订单不占用信用额度，跳过额度校验
        if ("0".equals(settleType))
        {
            return;
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
                    "客户【%s】超出信用额度：已占用欠款 %s 元 + 本单 %s 元 > 信用额度 %s 元（当前可用额度 %s 元），禁止下单",
                    customer.getCustomerName(), used.toPlainString(), amount.toPlainString(), limit.toPlainString(),
                    limit.subtract(used).toPlainString()));
        }
    }
}
