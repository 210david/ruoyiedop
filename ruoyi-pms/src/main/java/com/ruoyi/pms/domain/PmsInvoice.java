package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 发票结算表 pms_invoice
 *
 * @author ruoyi
 */
public class PmsInvoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 发票ID */
    private Long invoiceId;

    /** 结算单号 */
    @Excel(name = "结算单号")
    private String invoiceNo;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 采购订单ID */
    private Long orderId;

    /** 采购单号（冗余） */
    @Excel(name = "采购单号")
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 状态（字典 pms_invoice_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审核,2=已审核,3=已付款,4=已作废")
    private String status;

    /** 发票类型（字典 pms_invoice_type） */
    @Excel(name = "发票类型", readConverterExp = "0=增值税专用发票,1=增值税普通发票,2=收据")
    private String invoiceType;

    /** 发票号码 */
    @Excel(name = "发票号码")
    private String invoiceNumber;

    /** 开票日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开票日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date invoiceDate;

    /** 发票抬头 */
    @Excel(name = "发票抬头")
    private String invoiceTitle;

    /** 纳税人识别号 */
    @Excel(name = "纳税人识别号")
    private String taxNumber;

    /** 发票图片URL */
    private String invoiceImageUrl;

    /** 发票附件URL */
    private String invoiceAttachment;

    /** 不含税金额 */
    @Excel(name = "不含税金额")
    private BigDecimal invoiceAmount;

    /** 税额 */
    @Excel(name = "税额")
    private BigDecimal taxAmount;

    /** 税率(%) */
    @Excel(name = "税率(%)")
    private BigDecimal taxRate;

    /** 开票金额（价税合计） */
    @Excel(name = "开票金额")
    private BigDecimal totalAmount;

    /** 已付金额（自动计算） */
    @Excel(name = "已付金额")
    private BigDecimal paymentAmount;

    /** 付款金额 */
    @Excel(name = "付款金额")
    private BigDecimal payAmount;

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

    /** 删除标志 */
    private String delFlag;

    /** 发票明细列表 */
    private List<PmsInvoiceDetail> detailList;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 审核日志列表 */
    private List<PmsInvoiceAuditLog> auditLogList;

    /** 付款记录列表 */
    private List<PmsInvoicePayment> paymentLogList;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Date getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(Date invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public String getInvoiceImageUrl() { return invoiceImageUrl; }
    public void setInvoiceImageUrl(String invoiceImageUrl) { this.invoiceImageUrl = invoiceImageUrl; }
    public String getInvoiceAttachment() { return invoiceAttachment; }
    public void setInvoiceAttachment(String invoiceAttachment) { this.invoiceAttachment = invoiceAttachment; }
    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsInvoiceDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsInvoiceDetail> detailList) { this.detailList = detailList; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public List<PmsInvoiceAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsInvoiceAuditLog> auditLogList) { this.auditLogList = auditLogList; }
    public List<PmsInvoicePayment> getPaymentLogList() { return paymentLogList; }
    public void setPaymentLogList(List<PmsInvoicePayment> paymentLogList) { this.paymentLogList = paymentLogList; }
}
