package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 入库单明细表 wms_inbound_order_detail
 *
 * @author ruoyi
 */
public class WmsInboundOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 入库单ID */
    private Long orderId;

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
    private String specModel;

    /** 计量单位 */
    @Excel(name = "单位")
    private String unit;

    /** 计划数量 */
    @Excel(name = "计划数量")
    private BigDecimal planQty;

    /** 已收数量 */
    @Excel(name = "已收数量")
    private BigDecimal receivedQty;

    /** 已上架数量 */
    @Excel(name = "已上架数量")
    private BigDecimal putawayQty;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;

    /** 保质期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;

    /** 上架库位ID */
    private Long locationId;

    /** 库位编码 */
    @Excel(name = "上架库位")
    private String locationCode;

    /** 库位名称 */
    private String locationName;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
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
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }
    public BigDecimal getPutawayQty() { return putawayQty; }
    public void setPutawayQty(BigDecimal putawayQty) { this.putawayQty = putawayQty; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
