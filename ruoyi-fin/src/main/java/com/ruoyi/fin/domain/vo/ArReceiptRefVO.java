package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;

/**
 * 跨模块只读视图对象 —— 客户收款汇总
 *
 * <p>来自 mk_payment_record（明细）+ mk_payment_plan（关联到客户）。</p>
 *
 * <p><b>注意</b>：mk_payment_record 无 del_flag（物理删除），
 * 故此处不按 del_flag 过滤。收款与应收是「按客户」的对应关系——
 * mk 侧按收款计划核销，财务侧按客户维度汇总已收金额。</p>
 *
 * @author ruoyi
 */
public class ArReceiptRefVO
{
    /** 客户ID */
    private Long customerId;

    /** 已收金额合计 */
    private BigDecimal receivedAmount;

    /** 收款笔数 */
    private Integer receiptCount;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }

    public Integer getReceiptCount() { return receiptCount; }
    public void setReceiptCount(Integer receiptCount) { this.receiptCount = receiptCount; }
}