package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 出库单明细表 wms_outbound_order_detail
 *
 * @author ruoyi
 */
public class WmsOutboundOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 出库单ID */
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
    @Excel(name = "单位", dictType = "wms_unit")
    private String unit;

    /** 指定批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 批次ID（food_batch） */
    private Long batchId;

    /** FEFO推荐批次号 */
    private String fefoBatchNo;

    /** 换批原因（与FEFO推荐不一致时必填） */
    private String changeReason;

    /** 到期日期（FEFO关联查询） */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private java.util.Date expireDate;

    /** 批次剩余天数（FEFO关联查询） */
    private Long daysRemain;

    /** 批次状态（FEFO关联查询：0待检 1合格 2冻结 3报废） */
    private String batchStatus;

    /** 计划数量 */
    @Excel(name = "计划数量")
    private BigDecimal planQty;

    /** 实际复核数量 */
    @Excel(name = "实际数量")
    private BigDecimal actualQty;

    /** 拣货库位ID */
    private Long locationId;

    /** 库位编码 */
    @Excel(name = "拣货库位")
    private String locationCode;

    /** 库位名称 */
    private String locationName;

    /** 已拣货数量 */
    @Excel(name = "已拣货数量")
    private BigDecimal pickQty;

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
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getFefoBatchNo() { return fefoBatchNo; }
    public void setFefoBatchNo(String fefoBatchNo) { this.fefoBatchNo = fefoBatchNo; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public java.util.Date getExpireDate() { return expireDate; }
    public void setExpireDate(java.util.Date expireDate) { this.expireDate = expireDate; }
    public Long getDaysRemain() { return daysRemain; }
    public void setDaysRemain(Long daysRemain) { this.daysRemain = daysRemain; }
    public String getBatchStatus() { return batchStatus; }
    public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public BigDecimal getPickQty() { return pickQty; }
    public void setPickQty(BigDecimal pickQty) { this.pickQty = pickQty; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
