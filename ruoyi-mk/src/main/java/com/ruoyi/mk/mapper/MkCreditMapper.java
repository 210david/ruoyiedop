package com.ruoyi.mk.mapper;

import java.math.BigDecimal;
import org.apache.ibatis.annotations.Param;

/**
 * 客户信用占用 Mapper（直查 fin_ar_order / mk_order / mk_invoice，不加模块依赖）
 *
 * 欠款占用 = 应收未收（fin_ar_order OPEN） + 已审核未开票订单金额（mk_order）
 *
 * @author ruoyi
 */
public interface MkCreditMapper
{
    /** 应收未收余额（status=OPEN） */
    public BigDecimal selectArOutstanding(@Param("customerId") Long customerId);

    /** 已审核（含部分发货/完成）且未开票的订单金额合计 */
    public BigDecimal selectUninvoicedOrderAmount(@Param("customerId") Long customerId);
}
