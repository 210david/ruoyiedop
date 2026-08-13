package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long materialId;
    @Excel(name = "危化品编号")
    private String materialCode;
    @Excel(name = "危化品名称")
    private String materialName;
    @Excel(name = "危化品分类")
    private String hazardClass;
    @Excel(name = "CAS号")
    private String casNo;
    @Excel(name = "最大储存量")
    private BigDecimal maxStorage;
    @Excel(name = "单位")
    private String storageUnit;
    private Long storageAreaId;
    @Excel(name = "储存位置")
    private String storageAreaName;
    private String sdsAttachment;
    @Excel(name = "剧毒/易制爆")
    private String toxicFlag;
    @Excel(name = "当前库存")
    private BigDecimal currentStock;
    private Long enterpriseId;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    @Excel(name = "安全库存下限")
    private BigDecimal safetyStockMin;
    @Excel(name = "安全库存上限")
    private BigDecimal safetyStockMax;
    private Long defaultSupplierId;
    @Excel(name = "闪点")
    private String flashPoint;
    @Excel(name = "爆炸极限")
    private String explosionLimit;
    private String physicalProperties;
    private String hazardSummary;
    private String firstAidMeasures;
    private String fireFighting;
    private String leakHandling;
    private String storagePrecaution;
    private String exposureControl;
    private String stabilityReactivity;
    private String toxicologicalInfo;
    private String ecologicalInfo;
    private String disposalInfo;
    private String transportInfo;
    private String delFlag;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getHazardClass() { return hazardClass; }
    public void setHazardClass(String hazardClass) { this.hazardClass = hazardClass; }
    public String getCasNo() { return casNo; }
    public void setCasNo(String casNo) { this.casNo = casNo; }
    public BigDecimal getMaxStorage() { return maxStorage; }
    public void setMaxStorage(BigDecimal maxStorage) { this.maxStorage = maxStorage; }
    public String getStorageUnit() { return storageUnit; }
    public void setStorageUnit(String storageUnit) { this.storageUnit = storageUnit; }
    public Long getStorageAreaId() { return storageAreaId; }
    public void setStorageAreaId(Long storageAreaId) { this.storageAreaId = storageAreaId; }
    public String getStorageAreaName() { return storageAreaName; }
    public void setStorageAreaName(String storageAreaName) { this.storageAreaName = storageAreaName; }
    public String getSdsAttachment() { return sdsAttachment; }
    public void setSdsAttachment(String sdsAttachment) { this.sdsAttachment = sdsAttachment; }
    public String getToxicFlag() { return toxicFlag; }
    public void setToxicFlag(String toxicFlag) { this.toxicFlag = toxicFlag; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public BigDecimal getSafetyStockMin() { return safetyStockMin; }
    public void setSafetyStockMin(BigDecimal safetyStockMin) { this.safetyStockMin = safetyStockMin; }
    public BigDecimal getSafetyStockMax() { return safetyStockMax; }
    public void setSafetyStockMax(BigDecimal safetyStockMax) { this.safetyStockMax = safetyStockMax; }
    public Long getDefaultSupplierId() { return defaultSupplierId; }
    public void setDefaultSupplierId(Long defaultSupplierId) { this.defaultSupplierId = defaultSupplierId; }
    public String getFlashPoint() { return flashPoint; }
    public void setFlashPoint(String flashPoint) { this.flashPoint = flashPoint; }
    public String getExplosionLimit() { return explosionLimit; }
    public void setExplosionLimit(String explosionLimit) { this.explosionLimit = explosionLimit; }
    public String getPhysicalProperties() { return physicalProperties; }
    public void setPhysicalProperties(String physicalProperties) { this.physicalProperties = physicalProperties; }
    public String getHazardSummary() { return hazardSummary; }
    public void setHazardSummary(String hazardSummary) { this.hazardSummary = hazardSummary; }
    public String getFirstAidMeasures() { return firstAidMeasures; }
    public void setFirstAidMeasures(String firstAidMeasures) { this.firstAidMeasures = firstAidMeasures; }
    public String getFireFighting() { return fireFighting; }
    public void setFireFighting(String fireFighting) { this.fireFighting = fireFighting; }
    public String getLeakHandling() { return leakHandling; }
    public void setLeakHandling(String leakHandling) { this.leakHandling = leakHandling; }
    public String getStoragePrecaution() { return storagePrecaution; }
    public void setStoragePrecaution(String storagePrecaution) { this.storagePrecaution = storagePrecaution; }
    public String getExposureControl() { return exposureControl; }
    public void setExposureControl(String exposureControl) { this.exposureControl = exposureControl; }
    public String getStabilityReactivity() { return stabilityReactivity; }
    public void setStabilityReactivity(String stabilityReactivity) { this.stabilityReactivity = stabilityReactivity; }
    public String getToxicologicalInfo() { return toxicologicalInfo; }
    public void setToxicologicalInfo(String toxicologicalInfo) { this.toxicologicalInfo = toxicologicalInfo; }
    public String getEcologicalInfo() { return ecologicalInfo; }
    public void setEcologicalInfo(String ecologicalInfo) { this.ecologicalInfo = ecologicalInfo; }
    public String getDisposalInfo() { return disposalInfo; }
    public void setDisposalInfo(String disposalInfo) { this.disposalInfo = disposalInfo; }
    public String getTransportInfo() { return transportInfo; }
    public void setTransportInfo(String transportInfo) { this.transportInfo = transportInfo; }
}