package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 回款记录 mk_payment_record
 * 支持一个回款计划分多次回款
 *
 * @author ruoyi
 */
public class MkPaymentRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 回款计划ID */
    private Long planId;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    private String contractNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 本次回款金额 */
    @Excel(name = "本次回款金额")
    private BigDecimal thisAmount;

    /** 回款方式 */
    @Excel(name = "回款方式", dictType = "marketing_payment_method")
    private String paymentMethod;

    /** 收款银行账户 */
    private String bankAccount;

    /** 收款凭证附件 */
    private String receiptAttachment;

    /** 回款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "回款日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date paymentDate;

    /** 确认状态（0待确认 1已确认 2已驳回） */
    @Excel(name = "确认状态", dictType = "marketing_payment_status")
    private String confirmStatus;

    /** 确认人 */
    private String confirmBy;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 计划回款金额（关联查询） */
    private BigDecimal planAmount;

    /** 期次（关联查询） */
    private Integer periodNo;

    /** 备注（显式声明，确保 MyBatis 正确映射） */
    @Excel(name = "备注")
    private String remark;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getThisAmount() { return thisAmount; }
    public void setThisAmount(BigDecimal thisAmount) { this.thisAmount = thisAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getReceiptAttachment() { return receiptAttachment; }
    public void setReceiptAttachment(String receiptAttachment) { this.receiptAttachment = receiptAttachment; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public String getConfirmStatus() { return confirmStatus; }
    public void setConfirmStatus(String confirmStatus) { this.confirmStatus = confirmStatus; }
    public String getConfirmBy() { return confirmBy; }
    public void setConfirmBy(String confirmBy) { this.confirmBy = confirmBy; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public BigDecimal getPlanAmount() { return planAmount; }
    public void setPlanAmount(BigDecimal planAmount) { this.planAmount = planAmount; }
    public Integer getPeriodNo() { return periodNo; }
    public void setPeriodNo(Integer periodNo) { this.periodNo = periodNo; }
    
    @Override
    public String getRemark() { return this.remark; }
    @Override
    public void setRemark(String remark) { this.remark = remark; }
}
