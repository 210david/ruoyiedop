package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 报价明细表 pms_quotation_detail
 *
 * @author ruoyi
 */
public class PmsQuotationDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 报价ID */
    private Long quotationId;

    /** 询价明细ID */
    private Long inquiryDetailId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
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

    /** 采购数量 */
    @Excel(name = "数量")
    private BigDecimal quantity;

    /** 报价单价 */
    @Excel(name = "报价单价")
    private BigDecimal price;

    /** 报价金额 */
    @Excel(name = "报价金额")
    private BigDecimal amount;

    /** 税率（%） */
    @Excel(name = "税率(%)")
    private BigDecimal taxRate;

    /** 含税单价 */
    @Excel(name = "含税单价")
    private BigDecimal taxPrice;

    /** 含税金额 */
    @Excel(name = "含税金额")
    private BigDecimal taxAmount;

    /** 交货日期 */
    private String deliveryDate;

    /** 备注 */
    private String remark;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public Long getQuotationId() { return quotationId; }
    public void setQuotationId(Long quotationId) { this.quotationId = quotationId; }

    public Long getInquiryDetailId() { return inquiryDetailId; }
    public void setInquiryDetailId(Long inquiryDetailId) { this.inquiryDetailId = inquiryDetailId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

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

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTaxPrice() { return taxPrice; }
    public void setTaxPrice(BigDecimal taxPrice) { this.taxPrice = taxPrice; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    @Override
    public String getRemark() { return remark; }
    @Override
    public void setRemark(String remark) { this.remark = remark; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return "PmsQuotationDetail{" +
                "detailId=" + detailId +
                ", quotationId=" + quotationId +
                ", inquiryDetailId=" + inquiryDetailId +
                ", materialId=" + materialId +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", specification='" + specification + '\'' +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", amount=" + amount +
                ", taxRate=" + taxRate +
                ", taxPrice=" + taxPrice +
                ", taxAmount=" + taxAmount +
                ", deliveryDate='" + deliveryDate + '\'' +
                ", remark='" + remark + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
