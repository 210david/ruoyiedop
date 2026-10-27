package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 库存按物料+仓库汇总视图对象（非表实体，聚合查询结果）
 *
 * @author cfsp
 */
public class WmsInventorySummary
{
    /** 物料ID */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 仓库名称 */
    @Excel(name = "仓库")
    private String warehouseName;

    /** 可用数量合计 */
    @Excel(name = "可用数量")
    private BigDecimal totalQty;

    /** 锁定数量合计 */
    @Excel(name = "锁定数量")
    private BigDecimal totalLockQty;

    /** 批次数 */
    @Excel(name = "批次数")
    private Integer batchCount;

    /** 最早有效期（FEFO 首出批次） */
    @Excel(name = "最早有效期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date minExpiryDate;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalLockQty() { return totalLockQty; }
    public void setTotalLockQty(BigDecimal totalLockQty) { this.totalLockQty = totalLockQty; }
    public Integer getBatchCount() { return batchCount; }
    public void setBatchCount(Integer batchCount) { this.batchCount = batchCount; }
    public Date getMinExpiryDate() { return minExpiryDate; }
    public void setMinExpiryDate(Date minExpiryDate) { this.minExpiryDate = minExpiryDate; }
}
