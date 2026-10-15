package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 返利结算 mk_rebate_settle
 *
 * @author ruoyi
 */
public class MkRebateSettle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 结算ID */
    private Long settleId;

    /** 结算单号 */
    @Excel(name = "结算单号")
    private String settleNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 核算期间yyyyMM */
    @Excel(name = "核算期间")
    private String period;

    /** 政策编号 */
    @Excel(name = "政策编号")
    private String policyNo;

    /** 政策名称 */
    @Excel(name = "政策名称")
    private String policyName;

    /** 达成额 */
    @Excel(name = "达成额")
    private BigDecimal achieveAmount;

    /** 返利比例% */
    @Excel(name = "返利比例%")
    private BigDecimal rebateRate;

    /** 返利金额 */
    @Excel(name = "返利金额")
    private BigDecimal rebateAmount;

    /** 状态（0待兑现 1已兑现） */
    @Excel(name = "状态", dictType = "mk_rebate_settle_status")
    private String status;

    /** 兑现方式 */
    @Excel(name = "兑现方式")
    private String redeemType;

    /** 兑现时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date redeemDate;

    /** 兑现人 */
    private String redeemBy;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getSettleId() { return settleId; }
    public void setSettleId(Long settleId) { this.settleId = settleId; }
    public String getSettleNo() { return settleNo; }
    public void setSettleNo(String settleNo) { this.settleNo = settleNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getPolicyNo() { return policyNo; }
    public void setPolicyNo(String policyNo) { this.policyNo = policyNo; }
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public BigDecimal getAchieveAmount() { return achieveAmount; }
    public void setAchieveAmount(BigDecimal achieveAmount) { this.achieveAmount = achieveAmount; }
    public BigDecimal getRebateRate() { return rebateRate; }
    public void setRebateRate(BigDecimal rebateRate) { this.rebateRate = rebateRate; }
    public BigDecimal getRebateAmount() { return rebateAmount; }
    public void setRebateAmount(BigDecimal rebateAmount) { this.rebateAmount = rebateAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRedeemType() { return redeemType; }
    public void setRedeemType(String redeemType) { this.redeemType = redeemType; }
    public Date getRedeemDate() { return redeemDate; }
    public void setRedeemDate(Date redeemDate) { this.redeemDate = redeemDate; }
    public String getRedeemBy() { return redeemBy; }
    public void setRedeemBy(String redeemBy) { this.redeemBy = redeemBy; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
