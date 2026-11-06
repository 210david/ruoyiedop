package com.ruoyi.pms.domain;

import java.math.BigDecimal;

/**
 * 零散采购耗用明细 pms_consumption_detail
 *
 * @author ruoyi
 */
public class PmsConsumptionDetail
{
    private static final long serialVersionUID = 1L;

    private Long detailId;
    private Long consumptionId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String specModel;
    private String unit;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getConsumptionId() { return consumptionId; }
    public void setConsumptionId(Long consumptionId) { this.consumptionId = consumptionId; }
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
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
