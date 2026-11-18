package com.ruoyi.oa.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 司机台账对象 biz_oa_driver
 */
public class OaDriver extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long driverId;

    @NotBlank(message = "司机姓名不能为空")
    @Excel(name = "司机姓名")
    private String driverName;

    @Excel(name = "工号")
    private String driverNo;

    @Excel(name = "手机号")
    private String phone;

    @Excel(name = "身份证号")
    private String idCard;

    @Excel(name = "驾驶证号")
    private String licenseNo;

    @Excel(name = "准驾车型")
    private String licenseType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "驾驶证有效期至", width = 120, dateFormat = "yyyy-MM-dd")
    private Date licenseExpire;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职日期", width = 120, dateFormat = "yyyy-MM-dd")
    private Date entryDate;

    @Excel(name = "所属部门")
    private String ownerDept;

    @Excel(name = "驾龄(年)")
    private Integer drivingYears;

    @Excel(name = "状态", readConverterExp = "0=在岗,1=休假,2=停用")
    private String status;

    /** 关联流程实例ID（公车使用申请流程写回） */
    private String flowInstanceId;

    /** 关联流程标题（列表联查展示，不入库） */
    private String flowTitle;

    /** 关联流程运行状态（列表联查展示：1进行中 0已结束） */
    private String flowRunning;

    private String attachment;

    private String delFlag;

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverNo() { return driverNo; }
    public void setDriverNo(String driverNo) { this.driverNo = driverNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }
    public Date getLicenseExpire() { return licenseExpire; }
    public void setLicenseExpire(Date licenseExpire) { this.licenseExpire = licenseExpire; }
    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    public String getOwnerDept() { return ownerDept; }
    public void setOwnerDept(String ownerDept) { this.ownerDept = ownerDept; }
    public Integer getDrivingYears() { return drivingYears; }
    public void setDrivingYears(Integer drivingYears) { this.drivingYears = drivingYears; }
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
