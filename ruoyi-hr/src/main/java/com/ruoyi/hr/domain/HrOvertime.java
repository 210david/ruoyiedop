package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrOvertime extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long overtimeId;
    @NotBlank(message = "加班单号不能为空")
    @Excel(name = "加班单号")
    private String overtimeNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @NotNull(message = "加班日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "加班日期")
    private Date overtimeDate;
    @Excel(name = "加班类型")
    private String overtimeType;
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间")
    private Date startTime;
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间")
    private Date endTime;
    @NotNull(message = "加班小时不能为空")
    @Excel(name = "加班小时")
    private BigDecimal overtimeHours;
    @Excel(name = "倍率")
    private BigDecimal multiplier;
    @Excel(name = "补偿方式")
    private String compensationType;
    @Excel(name = "加班原因")
    private String reason;
    @Excel(name = "审批状态")
    private String auditStatus;
    @Excel(name = "审批人")
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间")
    private Date auditTime;
    @Excel(name = "审批意见")
    private String auditRemark;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getOvertimeId() { return overtimeId; }
    public void setOvertimeId(Long overtimeId) { this.overtimeId = overtimeId; }
    public String getOvertimeNo() { return overtimeNo; }
    public void setOvertimeNo(String overtimeNo) { this.overtimeNo = overtimeNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Date getOvertimeDate() { return overtimeDate; }
    public void setOvertimeDate(Date overtimeDate) { this.overtimeDate = overtimeDate; }
    public String getOvertimeType() { return overtimeType; }
    public void setOvertimeType(String overtimeType) { this.overtimeType = overtimeType; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
    public String getCompensationType() { return compensationType; }
    public void setCompensationType(String compensationType) { this.compensationType = compensationType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
