package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 应收核销记录 fin_ar_writeoff
 *
 * <p>核销 = 财务经理确认「这笔应收可以销账」的动作，
 * 与出纳/MK 侧的实际收款（mk_payment_record）是两个动作，分别留痕。</p>
 *
 * @author ruoyi
 */
public class FinArWriteoff extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 核销ID */
    private Long writeoffId;

    /** 应收单ID */
    private Long arId;

    /** 收款记录ID（mk_payment_record.record_id） */
    private Long receiptId;

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

    public Long getArId() { return arId; }
    public void setArId(Long arId) { this.arId = arId; }

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }

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
