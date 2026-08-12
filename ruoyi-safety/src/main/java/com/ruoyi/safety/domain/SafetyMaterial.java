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
}