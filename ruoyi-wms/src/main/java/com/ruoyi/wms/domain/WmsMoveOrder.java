package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 移库单表 wms_move_order
 *
 * @author ruoyi
 */
public class WmsMoveOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 移库单ID */
    private Long moveId;

    /** 移库单号 */
    @Excel(name = "移库单号")
    private String moveNo;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    @Excel(name = "仓库")
    private String warehouseName;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 源库位ID */
    private Long fromLocationId;

    /** 源库位编码 */
    @Excel(name = "源库位编码")
    private String fromLocationCode;

    /** 源库位名称 */
    @Excel(name = "源库位")
    private String fromLocationName;

    /** 目标库位ID */
    private Long toLocationId;

    /** 目标库位编码 */
    @Excel(name = "目标库位编码")
    private String toLocationCode;

    /** 目标库位名称 */
    @Excel(name = "目标库位")
    private String toLocationName;

    /** 移库数量 */
    @Excel(name = "移库数量")
    private BigDecimal moveQty;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=待审批,1=已批准,2=已完成,3=已作废")
    private String status;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    /** 删除标志 */
    private String delFlag;

    public Long getMoveId() { return moveId; }
    public void setMoveId(Long moveId) { this.moveId = moveId; }
    public String getMoveNo() { return moveNo; }
    public void setMoveNo(String moveNo) { this.moveNo = moveNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getFromLocationId() { return fromLocationId; }
    public void setFromLocationId(Long fromLocationId) { this.fromLocationId = fromLocationId; }
    public String getFromLocationCode() { return fromLocationCode; }
    public void setFromLocationCode(String fromLocationCode) { this.fromLocationCode = fromLocationCode; }
    public String getFromLocationName() { return fromLocationName; }
    public void setFromLocationName(String fromLocationName) { this.fromLocationName = fromLocationName; }
    public Long getToLocationId() { return toLocationId; }
    public void setToLocationId(Long toLocationId) { this.toLocationId = toLocationId; }
    public String getToLocationCode() { return toLocationCode; }
    public void setToLocationCode(String toLocationCode) { this.toLocationCode = toLocationCode; }
    public String getToLocationName() { return toLocationName; }
    public void setToLocationName(String toLocationName) { this.toLocationName = toLocationName; }
    public BigDecimal getMoveQty() { return moveQty; }
    public void setMoveQty(BigDecimal moveQty) { this.moveQty = moveQty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
