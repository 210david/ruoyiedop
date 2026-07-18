package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 备件管理 dms_spare_part
 *
 * @author ruoyi
 */
public class DmsSparePart extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 备件ID */
    private Long partId;

    /** 备件编号 */
    @Excel(name = "备件编号")
    private String partCode;

    /** 备件名称 */
    @Excel(name = "备件名称")
    private String partName;

    /** 备件类别（字典 dms_part_type） */
    @Excel(name = "备件类别", readConverterExp = "0=轴承,1=紧固件,2=密封件,3=传动件,4=电气元件,5=液压气动,6=刀具刃具,7=其他")
    private String partType;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 适用设备分类（JSON数组） */
    private String applicableCategory;

    /** 库存下限 */
    @Excel(name = "库存下限")
    private BigDecimal stockMin;

    /** 库存上限 */
    @Excel(name = "库存上限")
    private BigDecimal stockMax;

    /** 安全库存 */
    private BigDecimal safetyStock;

    /** 当前库存 */
    @Excel(name = "当前库存")
    private BigDecimal currentStock;

    /** 存放位置 */
    @Excel(name = "存放位置")
    private String storageLocation;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplier;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getPartId() { return partId; }
    public void setPartId(Long partId) { this.partId = partId; }
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
    public String getApplicableCategory() { return applicableCategory; }
    public void setApplicableCategory(String applicableCategory) { this.applicableCategory = applicableCategory; }
    public BigDecimal getStockMin() { return stockMin; }
    public void setStockMin(BigDecimal stockMin) { this.stockMin = stockMin; }
    public BigDecimal getStockMax() { return stockMax; }
    public void setStockMax(BigDecimal stockMax) { this.stockMax = stockMax; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
