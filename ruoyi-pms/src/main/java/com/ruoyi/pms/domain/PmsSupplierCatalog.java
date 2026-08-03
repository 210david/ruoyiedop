package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商供货清单表 pms_supplier_catalog
 *
 * @author ruoyi
 */
public class PmsSupplierCatalog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 清单ID */
    private Long catalogId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称", type = Excel.Type.EXPORT)
    private String materialName;

    /** 规格型号 */
    @Excel(name = "规格型号", type = Excel.Type.EXPORT)
    private String specModel;

    /** 计量单位 */
    @Excel(name = "单位", type = Excel.Type.EXPORT)
    private String unit;

    /** 供货单价（不含税） */
    @Excel(name = "供货单价")
    private BigDecimal unitPrice;

    /** 税率 */
    @Excel(name = "税率(%)")
    private BigDecimal taxRate;

    /** 最小起订量 */
    @Excel(name = "最小起订量")
    private BigDecimal minOrderQty;

    /** 交货周期（天） */
    @Excel(name = "交货周期(天)")
    private Integer deliveryCycle;

    /** 货币类型 */
    @Excel(name = "货币类型", type = Excel.Type.EXPORT)
    private String currency;

    /** 价格生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "价格生效日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date priceEffectiveDate;

    /** 价格失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "价格失效日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date priceExpireDate;

    /** 状态（字典 pms_catalog_status） */
    @Excel(name = "状态", readConverterExp = "1=正常供货,2=暂停供货,3=停止供货")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getCatalogId() { return catalogId; }
    public void setCatalogId(Long catalogId) { this.catalogId = catalogId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    public BigDecimal getMinOrderQty() { return minOrderQty; }
    public void setMinOrderQty(BigDecimal minOrderQty) { this.minOrderQty = minOrderQty; }
    public Integer getDeliveryCycle() { return deliveryCycle; }
    public void setDeliveryCycle(Integer deliveryCycle) { this.deliveryCycle = deliveryCycle; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Date getPriceEffectiveDate() { return priceEffectiveDate; }
    public void setPriceEffectiveDate(Date priceEffectiveDate) { this.priceEffectiveDate = priceEffectiveDate; }
    public Date getPriceExpireDate() { return priceExpireDate; }
    public void setPriceExpireDate(Date priceExpireDate) { this.priceExpireDate = priceExpireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
