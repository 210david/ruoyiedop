package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 发票付款记录 pms_invoice_payment
 *
 * @author ruoyi
 */
public class PmsInvoicePayment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 付款记录ID */
    private Long paymentId;

    /** 付款编号 */
    @Excel(name = "付款编号")
    private String paymentNo;

    /** 发票ID */
    private Long invoiceId;

    /** 合同ID */
    private Long contractId;

    /** 订单ID */
    private Long orderId;

    /** 供应商ID */
    private Long supplierId;

    /** 收款单位（供应商名称） */
    @Excel(name = "收款单位")
    private String supplierName;

    /** 本次付款金额 */
    @Excel(name = "付款金额")
    private BigDecimal paymentAmount;

    /** 付款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "付款日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date paymentDate;

    /** 付款方式（字典 wms_payment_method） */
    @Excel(name = "付款方式", readConverterExp = "0=现结,1=月结,2=预付")
    private String paymentMethod;

    /** 开户银行 */
    @Excel(name = "开户银行")
    private String bankName;

    /** 银行账号 */
    @Excel(name = "银行账号")
    private String bankAccount;

    /** 付款人 */
    @Excel(name = "付款人")
    private String payer;

    /** 付款凭证附件 */
    private String attachment;

    /** 删除标志 */
    private String delFlag;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getPayer() { return payer; }
    public void setPayer(String payer) { this.payer = payer; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
