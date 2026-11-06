package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车辆台账 biz_safety_vehicle
 */
public class SafetyVehicle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long vehicleId;
    @Excel(name = "车辆类型", readConverterExp = "1=特种车辆,2=公用车辆")
    private String vehicleType;
    @Excel(name = "车辆编号")
    private String vehicleCode;
    @Excel(name = "车牌号")
    private String plateNo;
    @Excel(name = "品牌型号")
    private String vehicleModel;
    @Excel(name = "车辆用途")
    private String vehicleUse;
    private String specialSubType;
    @Excel(name = "驾驶员/操作员")
    private String operatorName;
    private String operatorPhone;
    private String licenseNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "操作证有效期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date certExpire;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "检验有效期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date inspectExpire;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "保险有效期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date insuranceExpire;
    private Long enterpriseId;
    @Excel(name = "所属企业")
    private String enterpriseName;
    @Excel(name = "状态", readConverterExp = "0=在用,1=停用,2=报废")
    private String status;
    private String delFlag;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleCode() { return vehicleCode; }
    public void setVehicleCode(String vehicleCode) { this.vehicleCode = vehicleCode; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getVehicleUse() { return vehicleUse; }
    public void setVehicleUse(String vehicleUse) { this.vehicleUse = vehicleUse; }
    public String getSpecialSubType() { return specialSubType; }
    public void setSpecialSubType(String specialSubType) { this.specialSubType = specialSubType; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getOperatorPhone() { return operatorPhone; }
    public void setOperatorPhone(String operatorPhone) { this.operatorPhone = operatorPhone; }
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public Date getCertExpire() { return certExpire; }
    public void setCertExpire(Date certExpire) { this.certExpire = certExpire; }
    public Date getInspectExpire() { return inspectExpire; }
    public void setInspectExpire(Date inspectExpire) { this.inspectExpire = inspectExpire; }
    public Date getInsuranceExpire() { return insuranceExpire; }
    public void setInsuranceExpire(Date insuranceExpire) { this.insuranceExpire = insuranceExpire; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
