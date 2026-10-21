package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商报价表 pms_quotation
 *
 * @author ruoyi
 */
public class PmsQuotation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 报价ID */
    private Long quotationId;

    /** 询价ID */
    private Long inquiryId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 报价总金额 */
    @Excel(name = "报价金额")
    private BigDecimal totalAmount;

    /** 税率（%） */
    @Excel(name = "税率(%)")
    private BigDecimal taxRate;

    /** 交货期（天） */
    @Excel(name = "交货期(天)")
    private Integer deliveryDays;

    /** 付款方式 */
    @Excel(name = "付款方式")
    private String paymentTerms;

    /** 报价有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date validDate;

    /** 报价状态（0-待报价 1-已报价 2-已采纳 3-已拒绝） */
    @Excel(name = "状态", readConverterExp = "0=待报价,1=已报价,2=已采纳,3=已拒绝")
    private String status;

    /** 报价备注 */
    private String remark;

    /** 删除标志 */
    private String delFlag;

    /** 报价明细列表 */
    private java.util.List<PmsQuotationDetail> detailList;

    public Long getQuotationId() { return quotationId; }
    public void setQuotationId(Long quotationId) { this.quotationId = quotationId; }

    public Long getInquiryId() { return inquiryId; }
    public void setInquiryId(Long inquiryId) { this.inquiryId = inquiryId; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public Integer getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public Date getValidDate() { return validDate; }
    public void setValidDate(Date validDate) { this.validDate = validDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String getRemark() { return remark; }
    @Override
    public void setRemark(String remark) { this.remark = remark; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public java.util.List<PmsQuotationDetail> getDetailList() { return detailList; }
    public void setDetailList(java.util.List<PmsQuotationDetail> detailList) { this.detailList = detailList; }

    @Override
    public String toString() {
        return "PmsQuotation{" +
                "quotationId=" + quotationId +
                ", inquiryId=" + inquiryId +
                ", supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", totalAmount=" + totalAmount +
                ", taxRate=" + taxRate +
                ", deliveryDays=" + deliveryDays +
                ", paymentTerms='" + paymentTerms + '\'' +
                ", validDate=" + validDate +
                ", status='" + status + '\'' +
                ", remark='" + remark + '\'' +
                ", delFlag='" + delFlag + '\'' +
                ", detailList=" + detailList +
                '}';
    }
}
