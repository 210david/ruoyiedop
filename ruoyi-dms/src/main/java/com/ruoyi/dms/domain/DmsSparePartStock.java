package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 备件库存（按仓库维度） dms_spare_part_stock
 *
 * @author ruoyi
 */
public class DmsSparePartStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 库存ID */
    private Long stockId;

    /** 备件ID */
    private Long partId;

    /** 仓库名称 */
    @Excel(name = "仓库")
    private String warehouseName;

    /** 存放位置 */
    @Excel(name = "存放位置")
    private String storageLocation;

    /** 当前库存 */
    @Excel(name = "当前库存")
    private BigDecimal currentStock;

    /** 锁定库存 */
    private BigDecimal lockQty;

    /** 库存下限 */
    @Excel(name = "库存下限")
    private BigDecimal stockMin;

    /** 库存上限 */
    @Excel(name = "库存上限")
    private BigDecimal stockMax;

    /** 安全库存 */
    private BigDecimal safetyStock;

    /** 乐观锁版本号 */
    private Integer version;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 删除标志 */
    private String delFlag;

    // ======== 以下为关联查询带出的备件主数据字段 ========

    /** 备件编号 */
    @Excel(name = "备件编号")
    private String partCode;

    /** 备件名称 */
    @Excel(name = "备件名称")
    private String partName;

    /** 备件类别 */
    @Excel(name = "备件类别", dictType = "dms_part_type")
    private String partType;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 单位 */
    @Excel(name = "单位", dictType = "wms_unit")
    private String unit;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplier;

    /** 备件状态（主数据表的） */
    private String partStatus;

    public Long getStockId() { return stockId; }
    public void setStockId(Long stockId) { this.stockId = stockId; }
    public Long getPartId() { return partId; }
    public void setPartId(Long partId) { this.partId = partId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public BigDecimal getLockQty() { return lockQty; }
    public void setLockQty(BigDecimal lockQty) { this.lockQty = lockQty; }
    public BigDecimal getStockMin() { return stockMin; }
    public void setStockMin(BigDecimal stockMin) { this.stockMin = stockMin; }
    public BigDecimal getStockMax() { return stockMax; }
    public void setStockMax(BigDecimal stockMax) { this.stockMax = stockMax; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getPartCode() { return partCode; }
    public void setPartCode(String partCode) { this.partCode = partCode; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getPartType() { return partType; }
    public void setPartType(String partType) { this.partType = partType; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getPartStatus() { return partStatus; }
    public void setPartStatus(String partStatus) { this.partStatus = partStatus; }
}
