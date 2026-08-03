package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商 wms_supplier（WMS/PMS共用）
 *
 * @author ruoyi
 */
public class WmsSupplier extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商编码 */
    @Excel(name = "供应商编码")
    private String supplierCode;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 供应商简称 */
    @Excel(name = "供应商简称")
    private String supplierShortName;

    /** 供应商类型（字典 wms_supplier_type） */
    @Excel(name = "供应商类型", readConverterExp = "0=厂商,1=经销商,2=代理商,3=服务商")
    private String supplierType;

    /** 供应商等级（字典 wms_supplier_level） */
    @Excel(name = "供应商等级")
    private String supplierLevel;

    /** 统一社会信用代码 */
    @Excel(name = "统一社会信用代码")
    private String unifiedCreditCode;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 传真 */
    private String fax;

    /** 网址 */
    private String website;

    /** 邮政编码 */
    private String postcode;

    /** 地址 */
    @Excel(name = "地址")
    private String address;

    /** 法人代表 */
    @Excel(name = "法人代表")
    private String legalPerson;

    /** 注册资本（万元） */
    @Excel(name = "注册资本(万元)")
    private BigDecimal registeredCapital;

    /** 企业性质（字典 wms_enterprise_nature） */
    @Excel(name = "企业性质", readConverterExp = "0=国有企业,1=民营企业,2=外资企业,3=合资企业,4=个体工商户")
    private String enterpriseNature;

    /** 经营范围 */
    private String businessScope;

    /** 成立日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "成立日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date establishedDate;

    /** 开户银行 */
    @Excel(name = "开户银行")
    private String bankName;

    /** 开户支行 */
    private String bankBranch;

    /** 银行账号 */
    @Excel(name = "银行账号")
    private String bankAccount;

    /** 税号 */
    @Excel(name = "税号")
    private String taxNumber;

    /** 发票抬头 */
    @Excel(name = "发票抬头")
    private String invoiceTitle;

    /** 发票地址 */
    private String invoiceAddress;

    /** 发票联系电话 */
    private String invoicePhone;

    /** 付款方式（字典 wms_payment_method） */
    @Excel(name = "付款方式", readConverterExp = "0=现结,1=月结,2=预付")
    private String paymentMethod;

    /** 账期天数 */
    @Excel(name = "账期天数")
    private Integer paymentDays;

    /** 结算方式（字典 wms_settlement_type） */
    @Excel(name = "结算方式", readConverterExp = "0=银行转账,1=承兑汇票,2=现金")
    private String settlementType;

    /** 结算币种（字典 wms_currency） */
    @Excel(name = "结算币种")
    private String currency;

    /** 交货周期（天） */
    @Excel(name = "交货周期(天)")
    private Integer deliveryCycle;

    /** 最小订单金额 */
    @Excel(name = "最小订单金额")
    private BigDecimal minOrderAmount;

    /** 合作开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合作开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cooperationDate;

    /** 资质状态（0未审核 1已审核 2审核不通过） */
    @Excel(name = "资质状态", readConverterExp = "0=未审核,1=已审核,2=审核不通过")
    private String qualificationStatus;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierShortName() { return supplierShortName; }
    public void setSupplierShortName(String supplierShortName) { this.supplierShortName = supplierShortName; }
    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }
    public String getSupplierLevel() { return supplierLevel; }
    public void setSupplierLevel(String supplierLevel) { this.supplierLevel = supplierLevel; }
    public String getUnifiedCreditCode() { return unifiedCreditCode; }
    public void setUnifiedCreditCode(String unifiedCreditCode) { this.unifiedCreditCode = unifiedCreditCode; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLegalPerson() { return legalPerson; }
    public void setLegalPerson(String legalPerson) { this.legalPerson = legalPerson; }
    public BigDecimal getRegisteredCapital() { return registeredCapital; }
    public void setRegisteredCapital(BigDecimal registeredCapital) { this.registeredCapital = registeredCapital; }
    public String getEnterpriseNature() { return enterpriseNature; }
    public void setEnterpriseNature(String enterpriseNature) { this.enterpriseNature = enterpriseNature; }
    public String getBusinessScope() { return businessScope; }
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }
    public Date getEstablishedDate() { return establishedDate; }
    public void setEstablishedDate(Date establishedDate) { this.establishedDate = establishedDate; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }
    public String getInvoiceAddress() { return invoiceAddress; }
    public void setInvoiceAddress(String invoiceAddress) { this.invoiceAddress = invoiceAddress; }
    public String getInvoicePhone() { return invoicePhone; }
    public void setInvoicePhone(String invoicePhone) { this.invoicePhone = invoicePhone; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Integer getPaymentDays() { return paymentDays; }
    public void setPaymentDays(Integer paymentDays) { this.paymentDays = paymentDays; }
    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getDeliveryCycle() { return deliveryCycle; }
    public void setDeliveryCycle(Integer deliveryCycle) { this.deliveryCycle = deliveryCycle; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public Date getCooperationDate() { return cooperationDate; }
    public void setCooperationDate(Date cooperationDate) { this.cooperationDate = cooperationDate; }
    public String getQualificationStatus() { return qualificationStatus; }
    public void setQualificationStatus(String qualificationStatus) { this.qualificationStatus = qualificationStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
