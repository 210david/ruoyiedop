package com.ruoyi.pms.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 价格比价视图对象
 *
 * @author ruoyi
 */
public class PmsPriceCompareVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 报价明细ID */
    private Long detailId;

    /** 询价单号 */
    @Excel(name = "询价单号")
    private String inquiryNo;

    /** 供应商编号 */
    @Excel(name = "供应商编号")
    private String supplierCode;

    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;

    /** 物料编号 */
    @Excel(name = "物料编号")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specification;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 数量 */
    @Excel(name = "数量")
    private BigDecimal quantity;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal price;

    /** 报价日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "报价日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date quotationDate;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date validDate;

    /** 税率（%） */
    @Excel(name = "税率(%)")
    private BigDecimal taxRate;

    /** 交货期（天） */
    @Excel(name = "交货期(天)")
    private Integer deliveryDays;

    /** 付款方式 */
    @Excel(name = "付款方式")
    private String paymentTerms;

    /** 是否中标（0-未中标 1-已中标） */
    @Excel(name = "中标标识", readConverterExp = "0=未中标,1=已中标")
    private String isAward;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public String getInquiryNo() { return inquiryNo; }
    public void setInquiryNo(String inquiryNo) { this.inquiryNo = inquiryNo; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Date getQuotationDate() { return quotationDate; }
    public void setQuotationDate(Date quotationDate) { this.quotationDate = quotationDate; }
    public Date getValidDate() { return validDate; }
    public void setValidDate(Date validDate) { this.validDate = validDate; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public Integer getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(Integer deliveryDays) { this.deliveryDays = deliveryDays; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getIsAward() { return isAward; }
    public void setIsAward(String isAward) { this.isAward = isAward; }
}
