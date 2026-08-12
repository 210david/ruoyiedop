package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyEquipment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long equipmentId;
    @Excel(name = "设备编号")
    private String equipmentCode;
    @Excel(name = "设备名称")
    private String equipmentName;
    @Excel(name = "型号规格")
    private String specModel;
    private Long areaId;
    @Excel(name = "所属区域")
    private String areaName;
    @Excel(name = "设备类别", readConverterExp = "1=特种设备,2=普通设备,3=安全设备")
    private String equipmentCategory;
    @Excel(name = "特种设备子类")
    private String specialSubType;
    @Excel(name = "登记证号")
    private String regCertNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "检验有效期", width = 120)
    private Date inspectExpire;
    private String remindStatus;
    private Long personId;
    @Excel(name = "责任人")
    private String personName;
    private Long enterpriseId;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getEquipmentCategory() { return equipmentCategory; }
    public void setEquipmentCategory(String equipmentCategory) { this.equipmentCategory = equipmentCategory; }
    public String getSpecialSubType() { return specialSubType; }
    public void setSpecialSubType(String specialSubType) { this.specialSubType = specialSubType; }
    public String getRegCertNo() { return regCertNo; }
    public void setRegCertNo(String regCertNo) { this.regCertNo = regCertNo; }
    public Date getInspectExpire() { return inspectExpire; }
    public void setInspectExpire(Date inspectExpire) { this.inspectExpire = inspectExpire; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}