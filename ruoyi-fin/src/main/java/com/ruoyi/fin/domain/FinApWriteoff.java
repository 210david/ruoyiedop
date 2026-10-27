package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 应付核销记录 fin_ap_writeoff
 *
 * <p>核销 = 财务经理确认「这笔应付可以销账」的动作，
 * 与出纳实际付钱（pms_invoice_payment）是两个动作，分别留痕。</p>
 *
 * @author ruoyi
 */
public class FinApWriteoff extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 核销ID */
    private Long writeoffId;

    /** 应付单ID */
    private Long apId;

    /** 付款流水ID */
    private Long paymentId;

    /** 本次核销金额 */
    private BigDecimal writeoffAmount;

    /** 核销日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date writeoffDate;

    /** 会计期间 */
    private String period;

    /** 归属部门ID */
    private Long deptId;

    /** 核销人ID */
    private Long userId;

    public Long getWriteoffId() { return writeoffId; }
    public void setWriteoffId(Long writeoffId) { this.writeoffId = writeoffId; }

    public Long getApId() { return apId; }
    public void setApId(Long apId) { this.apId = apId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public BigDecimal getWriteoffAmount() { return writeoffAmount; }
    public void setWriteoffAmount(BigDecimal writeoffAmount) { this.writeoffAmount = writeoffAmount; }

    public Date getWriteoffDate() { return writeoffDate; }
    public void setWriteoffDate(Date writeoffDate) { this.writeoffDate = writeoffDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}