package com.ruoyi.mk.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 客户信用控制 Service接口
 *
 * @author ruoyi
 */
public interface IMkCreditService
{
    /** 信用占用视图：额度/账期/状态/应收未收/未开票订单/已占用/可用 */
    public Map<String, Object> selectCreditUsage(Long customerId);

    /** 下单信用校验：冻结拦截 + 额度拦截（credit_limit<=0 视为不启用额度控制） */
    public void checkCreditForOrder(Long customerId, BigDecimal orderAmount);
}
