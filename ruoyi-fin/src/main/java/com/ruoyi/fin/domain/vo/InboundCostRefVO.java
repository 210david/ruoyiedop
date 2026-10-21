package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;

/**
 * 跨模块只读视图对象 —— 入库明细（计价数据源）
 *
 * <p>来自 wms_inbound_order_detail + wms_inbound_order + wms_material。</p>
 *
 * <p>【注意】实测生产库中 101 条明细里有 43 条 unit_price 为 0 或 NULL，
 * 计价时须显式校验单价有效性，不能假定都有值。</p>
 *
 * @author ruoyi
 */
public class InboundCostRefVO
{
    /** 入库明细ID */
    private Long detailId;

    /** 入库单ID */
    private Long inboundOrderId;

    /** 入库单号 */
    private String inboundOrderNo;

    /** 入库日期 */
    private java.util.Date inboundDate;

    /** 仓库ID */
    private Long warehouseId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 批次号 */
    private String batchNo;

    /** 库位ID */
    private Long locationId;

    /** 实收数量 */
    private BigDecimal receivedQty;

    /** 入库单价（Q4 决策：含税价） */
    private BigDecimal unitPrice;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public Long getInboundOrderId() { return inboundOrderId; }
    public void setInboundOrderId(Long inboundOrderId) { this.inboundOrderId = inboundOrderId; }

    public String getInboundOrderNo() { return inboundOrderNo; }
    public void setInboundOrderNo(String inboundOrderNo) { this.inboundOrderNo = inboundOrderNo; }

    public java.util.Date getInboundDate() { return inboundDate; }
    public void setInboundDate(java.util.Date inboundDate) { this.inboundDate = inboundDate; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}