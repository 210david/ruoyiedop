package com.ruoyi.pms.domain.dto;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 发票付款请求DTO
 *
 * @author ruoyi
 */
public class InvoicePaymentDTO
{
    /** 发票ID */
    private Long invoiceId;

    /** 本次付款金额 */
    private BigDecimal paymentAmount;

    /** 付款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date paymentDate;

    /** 付款方式 */
    private String paymentMethod;

    /** 开户银行 */
    private String bankName;

    /** 银行账号 */
    private String bankAccount;

    /** 付款人 */
    private String payer;

    /** 付款凭证附件 */
    private String attachment;

    /** 备注 */
    private String remark;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
