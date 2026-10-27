package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 回款计划 mk_payment_plan
 *
 * @author ruoyi
 */
public class MkPaymentPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计划ID */
    private Long planId;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 期次 */
    @Excel(name = "期次")
    private Integer periodNo;

    /** 计划回款金额 */
    @Excel(name = "计划回款金额")
    private BigDecimal planAmount;

    /** 计划回款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划回款日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date planDate;

    /** 实际回款金额 */
    @Excel(name = "实际回款金额")
    private BigDecimal actualAmount;

    /** 实际回款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际回款日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date actualDate;

    /** 回款状态 */
    @Excel(name = "回款状态", dictType = "marketing_payment_status")
    private String paymentStatus;

    /** 关联订单ID（独立创建时使用） */
    private Long orderId;

    /** 回款方式（0银行转账 1承兑汇票 2现金 3其他） */
    @Excel(name = "回款方式", dictType = "marketing_payment_method")
    private String paymentMethod;

    /** 收款银行账户 */
    private String bankAccount;

    /** 收款凭证附件 */
    private String receiptAttachment;

    /** 逾期标志（0未逾期 1已逾期） */
    @Excel(name = "是否逾期", readConverterExp = "0=否,1=是")
    private String overdueFlag;

    /** 回款记录列表 */
    private java.util.List<MkPaymentRecord> recordList;

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
    public Integer getPeriodNo() { return periodNo; }
    public void setPeriodNo(Integer periodNo) { this.periodNo = periodNo; }
    public BigDecimal getPlanAmount() { return planAmount; }
    public void setPlanAmount(BigDecimal planAmount) { this.planAmount = planAmount; }
    public Date getPlanDate() { return planDate; }
    public void setPlanDate(Date planDate) { this.planDate = planDate; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public Date getActualDate() { return actualDate; }
    public void setActualDate(Date actualDate) { this.actualDate = actualDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getReceiptAttachment() { return receiptAttachment; }
    public void setReceiptAttachment(String receiptAttachment) { this.receiptAttachment = receiptAttachment; }
    public String getOverdueFlag() { return overdueFlag; }
    public void setOverdueFlag(String overdueFlag) { this.overdueFlag = overdueFlag; }
    public java.util.List<MkPaymentRecord> getRecordList() { return recordList; }
    public void setRecordList(java.util.List<MkPaymentRecord> recordList) { this.recordList = recordList; }
}
