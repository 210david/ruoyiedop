package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;

/**
 * 跨模块只读视图对象 —— 发票与供应商通用字段
 *
 * <p>字段命名与 pms_invoice / wms_supplier 保持一致，仅做只读投影，
 * 不含任何写操作所需字段。</p>
 *
 * @author ruoyi
 */
public class InvoiceRefVO
{
    /** 发票ID（或供应商ID / 物料ID，按查询来源区分） */
    private Long id;

    /** 单据号 */
    private String billNo;

    /** 往来单位ID */
    private Long partnerId;

    /** 往来单位名称 */
    private String partnerName;

    /** 不含税金额 */
    private BigDecimal amountExclTax;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 价税合计 */
    private BigDecimal totalAmount;

    /** 单据日期 */
    private java.util.Date billDate;

    /** 账期天数（供应商场景用） */
    private Integer paymentDays;

    /** 结算方式（供应商场景用） */
    private String settlementType;

    /** 物料编码（物料场景用） */
    private String materialCode;

    /** 物料名称（物料场景用） */
    private String materialName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public BigDecimal getAmountExclTax() { return amountExclTax; }
    public void setAmountExclTax(BigDecimal amountExclTax) { this.amountExclTax = amountExclTax; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public java.util.Date getBillDate() { return billDate; }
    public void setBillDate(java.util.Date billDate) { this.billDate = billDate; }

    public Integer getPaymentDays() { return paymentDays; }
    public void setPaymentDays(Integer paymentDays) { this.paymentDays = paymentDays; }

    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
}
