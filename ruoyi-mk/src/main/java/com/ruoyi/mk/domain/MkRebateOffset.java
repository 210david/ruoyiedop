package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 返利冲抵台账 mk_rebate_offset（财务性质按销售折扣处理，红票由财务据待办开具）
 *
 * @author ruoyi
 */
public class MkRebateOffset extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审批状态：待财务审核 */
    public static final String AUDIT_PENDING = "0";
    /** 审批状态：审核通过 */
    public static final String AUDIT_PASS = "1";
    /** 审批状态：驳回 */
    public static final String AUDIT_REJECT = "2";

    /** 冲抵ID */
    private Long offsetId;

    /** 返利结算单ID */
    private Long settleId;

    /** 本次冲抵金额 */
    @Excel(name = "本次冲抵金额")
    private BigDecimal offsetAmount;

    /** 冲抵日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "冲抵日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date offsetDate;

    /** 审批状态（0待财务审核 1审核通过 2驳回） */
    @Excel(name = "审批状态", dictType = "mk_offset_audit_status")
    private String auditStatus;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    @Excel(name = "审核意见")
    private String auditOpinion;

    /** 财务待办消息ID */
    private Long msgId;

    /** 结算单号（关联字段） */
    @Excel(name = "结算单号")
    private String settleNo;

    /** 客户ID（关联字段） */
    private Long customerId;

    /** 客户名称（关联字段） */
    @Excel(name = "客户")
    private String customerName;

    /** 结算单返利总额（关联字段） */
    private BigDecimal rebateAmount;

    /** 已冲抵合计（关联字段，审核通过口径） */
    private BigDecimal offsetTotal;

    public Long getOffsetId() { return offsetId; }
    public void setOffsetId(Long offsetId) { this.offsetId = offsetId; }
    public Long getSettleId() { return settleId; }
    public void setSettleId(Long settleId) { this.settleId = settleId; }
    public BigDecimal getOffsetAmount() { return offsetAmount; }
    public void setOffsetAmount(BigDecimal offsetAmount) { this.offsetAmount = offsetAmount; }
    public Date getOffsetDate() { return offsetDate; }
    public void setOffsetDate(Date offsetDate) { this.offsetDate = offsetDate; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public Long getMsgId() { return msgId; }
    public void setMsgId(Long msgId) { this.msgId = msgId; }
    public String getSettleNo() { return settleNo; }
    public void setSettleNo(String settleNo) { this.settleNo = settleNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getRebateAmount() { return rebateAmount; }
    public void setRebateAmount(BigDecimal rebateAmount) { this.rebateAmount = rebateAmount; }
    public BigDecimal getOffsetTotal() { return offsetTotal; }
    public void setOffsetTotal(BigDecimal offsetTotal) { this.offsetTotal = offsetTotal; }
}
