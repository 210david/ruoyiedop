package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;

/**
 * 跨模块只读视图对象 —— 盘点差异（成本调整数据源）
 *
 * <p>来自 wms_stock_take_detail + wms_stock_take，用于财务侧把盘盈/盘亏
 * 落到成本台账，解决「盘点只改数量、台账数量不减导致总成本虚高」的问题。</p>
 *
 * <p>【为什么由财务侧扫描而不是 WMS 直接写台账】
 * ruoyi-fin 依赖 ruoyi-wms，WMS 不能反向写 fin_* 表（会破坏依赖方向与财务结转口径）。
 * 沿用本项目既有的旁路同步模式：WMS 只产出业务事实，财务侧负责计价与落账。</p>
 *
 * @author ruoyi
 */
public class StockTakeDiffRefVO
{
    /** 盘点明细ID（幂等键） */
    private Long detailId;

    /** 盘点单ID */
    private Long takeId;

    /** 盘点单号 */
    private String takeNo;

    /** 仓库ID */
    private Long warehouseId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 差异数量（正数=盘盈，负数=盘亏） */
    private BigDecimal diffQty;

    /** 账面数量 */
    private BigDecimal bookQty;

    /** 实盘数量 */
    private BigDecimal actualQty;

    /** 完成时间（取盘点单完成时间，用于会计期间） */
    private java.util.Date completeTime;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getTakeId() { return takeId; }
    public void setTakeId(Long takeId) { this.takeId = takeId; }
    public String getTakeNo() { return takeNo; }
    public void setTakeNo(String takeNo) { this.takeNo = takeNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getDiffQty() { return diffQty; }
    public void setDiffQty(BigDecimal diffQty) { this.diffQty = diffQty; }
    public BigDecimal getBookQty() { return bookQty; }
    public void setBookQty(BigDecimal bookQty) { this.bookQty = bookQty; }
    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }
    public java.util.Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(java.util.Date completeTime) { this.completeTime = completeTime; }
}
