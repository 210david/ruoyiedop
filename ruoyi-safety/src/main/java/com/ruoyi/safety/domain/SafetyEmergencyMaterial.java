package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SafetyEmergencyMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long materialId;
    @Excel(name = "物资编码")
    private String materialCode;
    @NotBlank(message = "物资名称不能为空")
    @Excel(name = "物资名称")
    private String materialName;
    @NotBlank(message = "物资类别不能为空")
    @Excel(name = "物资类别")
    private String materialType;
    @Excel(name = "规格型号")
    private String specModel;
    @NotNull(message = "数量不能为空")
    @Excel(name = "数量")
    private BigDecimal quantity;
    @NotBlank(message = "单位不能为空")
    @Excel(name = "单位")
    private String unit;
    @NotBlank(message = "存放位置不能为空")
    @Excel(name = "存放位置")
    private String storageLocation;
    @NotNull(message = "管理人不能为空")
    private Long personId;
    @Excel(name = "管理人")
    private String personName;
    @NotNull(message = "有效期至不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 120)
    private Date effectiveDate;
    private String remindStatus;
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
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}