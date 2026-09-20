package com.ruoyi.bid.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 保证金台账对象 bid_deposit
 */
public class BidDeposit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long depositId;
    private Long projectId;
    @Excel(name = "投标编号")
    private String bidNo;
    @Excel(name = "项目名称")
    private String projectName;
    /** 保证金类型（字典 bid_deposit_type） */
    @Excel(name = "保证金类型", dictType = "bid_deposit_type")
    private String depositType;
    /** 缴纳形式（字典 bid_pay_type） */
    @Excel(name = "缴纳形式", dictType = "bid_pay_type")
    private String payType;
    @Excel(name = "缴纳金额")
    private BigDecimal amount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "缴纳时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "应退回时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expectRefundTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "实际退回时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date actualRefundTime;
    /** 退回状态（字典 bid_refund_status） */
    @Excel(name = "退回状态", dictType = "bid_refund_status")
    private String refundStatus;
    private String voucherUrl;
    private Long deptId;
    private String delFlag;

    public Long getDepositId() { return depositId; }
    public void setDepositId(Long depositId) { this.depositId = depositId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getBidNo() { return bidNo; }
    public void setBidNo(String bidNo) { this.bidNo = bidNo; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getDepositType() { return depositType; }
    public void setDepositType(String depositType) { this.depositType = depositType; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }
    public Date getExpectRefundTime() { return expectRefundTime; }
    public void setExpectRefundTime(Date expectRefundTime) { this.expectRefundTime = expectRefundTime; }
    public Date getActualRefundTime() { return actualRefundTime; }
    public void setActualRefundTime(Date actualRefundTime) { this.actualRefundTime = actualRefundTime; }
    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
    public String getVoucherUrl() { return voucherUrl; }
    public void setVoucherUrl(String voucherUrl) { this.voucherUrl = voucherUrl; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
