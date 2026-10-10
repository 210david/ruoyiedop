package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;

/**
 * 跨模块只读视图对象 —— 出库明细（成本结转数据源）
 *
 * <p>来自 wms_outbound_order_detail + wms_outbound_order + wms_material。</p>
 *
 * @author ruoyi
 */
public class OutboundCostRefVO
{
    /** 出库明细ID */
    private Long detailId;

    /** 出库单ID */
    private Long outboundOrderId;

    /** 出库单号 */
    private String outboundOrderNo;

    /** 出库类型（字典 wms_outbound_type） */
    private String orderType;

    /** 出库日期 */
    private java.util.Date outboundDate;

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

    /** 实际复核数量 */
    private BigDecimal actualQty;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }

    public Long getOutboundOrderId() { return outboundOrderId; }
    public void setOutboundOrderId(Long outboundOrderId) { this.outboundOrderId = outboundOrderId; }

    public String getOutboundOrderNo() { return outboundOrderNo; }
    public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public java.util.Date getOutboundDate() { return outboundDate; }
    public void setOutboundDate(java.util.Date outboundDate) { this.outboundDate = outboundDate; }

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

    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }
}