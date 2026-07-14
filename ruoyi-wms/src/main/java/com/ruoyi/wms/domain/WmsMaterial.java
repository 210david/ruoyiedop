package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物料主数据 wms_material
 *
 * @author ruoyi
 */
public class WmsMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 物料类型 */
    @Excel(name = "物料类型", readConverterExp = "0=原材料,1=半成品,2=成品,3=辅料")
    private String materialType;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 基本计量单位 */
    @Excel(name = "计量单位", readConverterExp = "0=个,1=件,2=箱,3=kg,4=吨,5=米")
    private String unit;

    /** 保质期天数 */
    @Excel(name = "保质期天数")
    private Integer shelfLifeDays;

    /** 临期预警天数 */
    @Excel(name = "临期预警天数")
    private Integer expiryAlertDays;

    /** 效期管理（0否 1是） */
    @Excel(name = "效期管理", readConverterExp = "0=否,1=是")
    private String isExpiryManage;

    /** 安全库存下限 */
    @Excel(name = "安全库存下限")
    private BigDecimal safetyStockMin;

    /** 安全库存上限 */
    @Excel(name = "安全库存上限")
    private BigDecimal safetyStockMax;

    /** 是否批次管理（0否 1是） */
    private String isBatchManage;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }
    public Integer getExpiryAlertDays() { return expiryAlertDays; }
    public void setExpiryAlertDays(Integer expiryAlertDays) { this.expiryAlertDays = expiryAlertDays; }
    public String getIsExpiryManage() { return isExpiryManage; }
    public void setIsExpiryManage(String isExpiryManage) { this.isExpiryManage = isExpiryManage; }
    public BigDecimal getSafetyStockMin() { return safetyStockMin; }
    public void setSafetyStockMin(BigDecimal safetyStockMin) { this.safetyStockMin = safetyStockMin; }
    public BigDecimal getSafetyStockMax() { return safetyStockMax; }
    public void setSafetyStockMax(BigDecimal safetyStockMax) { this.safetyStockMax = safetyStockMax; }
    public String getIsBatchManage() { return isBatchManage; }
    public void setIsBatchManage(String isBatchManage) { this.isBatchManage = isBatchManage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
