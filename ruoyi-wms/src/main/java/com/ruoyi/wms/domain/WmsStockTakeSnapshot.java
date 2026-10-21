package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 盘点快照表 wms_stock_take_snapshot
 *
 * @author ruoyi
 */
public class WmsStockTakeSnapshot
{
    private static final long serialVersionUID = 1L;

    /** 快照ID */
    private Long snapshotId;

    /** 盘点单ID */
    private Long takeId;

    /** 物料ID */
    private Long materialId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 快照数量 */
    private BigDecimal snapshotQty;

    /** 快照时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date snapshotTime;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getTakeId() { return takeId; }
    public void setTakeId(Long takeId) { this.takeId = takeId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getSnapshotQty() { return snapshotQty; }
    public void setSnapshotQty(BigDecimal snapshotQty) { this.snapshotQty = snapshotQty; }
    public Date getSnapshotTime() { return snapshotTime; }
    public void setSnapshotTime(Date snapshotTime) { this.snapshotTime = snapshotTime; }
}
