package com.ruoyi.oa.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车辆台账对象 biz_oa_vehicle
 */
public class OaVehicle extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long vehicleId;

    @Excel(name = "车辆编号")
    private String vehicleCode;

    @NotBlank(message = "车牌号不能为空")
    @Excel(name = "车牌号")
    private String plateNo;

    @Excel(name = "车辆类型", dictType = "oa_vehicle_type")
    private String vehicleType;

    @Excel(name = "品牌型号")
    private String vehicleModel;

    @Excel(name = "使用用途")
    private String vehicleUse;

    @Excel(name = "座位数")
    private Integer seatCount;

    @Excel(name = "所属部门")
    private String ownerDept;

    /** 所属部门ID（与部门弹框选择联动，ownerDept 保存部门名称用于展示与导入导出） */
    private Long ownerDeptId;

    @Excel(name = "默认驾驶员")
    private String driverName;

    @Excel(name = "驾驶员电话")
    private String driverPhone;

    @Excel(name = "行驶证号")
    private String licenseNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "购置日期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date purchaseDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "年检到期日", width = 120, dateFormat = "yyyy-MM-dd")
    private Date inspectExpire;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "保险到期日", width = 120, dateFormat = "yyyy-MM-dd")
    private Date insuranceExpire;

    @Excel(name = "状态", readConverterExp = "0=可用,1=使用中,2=维修中,3=停用,4=已报废")
    private String status;

    /** 关联流程实例ID（公车使用申请流程写回） */
    private String flowInstanceId;

    /** 关联流程标题（列表联查展示，不入库） */
    private String flowTitle;

    /** 关联流程运行状态（列表联查展示：1进行中 0已结束） */
    private String flowRunning;

    private String attachment;

    private String delFlag;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public String getVehicleCode() { return vehicleCode; }
    public void setVehicleCode(String vehicleCode) { this.vehicleCode = vehicleCode; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getVehicleUse() { return vehicleUse; }
    public void setVehicleUse(String vehicleUse) { this.vehicleUse = vehicleUse; }
    public Integer getSeatCount() { return seatCount; }
    public void setSeatCount(Integer seatCount) { this.seatCount = seatCount; }
    public String getOwnerDept() { return ownerDept; }
    public void setOwnerDept(String ownerDept) { this.ownerDept = ownerDept; }
    public Long getOwnerDeptId() { return ownerDeptId; }
    public void setOwnerDeptId(Long ownerDeptId) { this.ownerDeptId = ownerDeptId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }
    public Date getInspectExpire() { return inspectExpire; }
    public void setInspectExpire(Date inspectExpire) { this.inspectExpire = inspectExpire; }
    public Date getInsuranceExpire() { return insuranceExpire; }
    public void setInsuranceExpire(Date insuranceExpire) { this.insuranceExpire = insuranceExpire; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFlowInstanceId() { return flowInstanceId; }
    public void setFlowInstanceId(String flowInstanceId) { this.flowInstanceId = flowInstanceId; }
    public String getFlowTitle() { return flowTitle; }
    public void setFlowTitle(String flowTitle) { this.flowTitle = flowTitle; }
    public String getFlowRunning() { return flowRunning; }
    public void setFlowRunning(String flowRunning) { this.flowRunning = flowRunning; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
