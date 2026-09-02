package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public class HrAttendanceRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    @NotNull(message = "考勤日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "考勤日期")
    private Date recordDate;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "组织ID")
    private Long orgId;
    /** 部门名称（关联 sys_dept 回显，非表字段） */
    private String orgName;
    @Excel(name = "班次ID")
    private Long shiftId;
    @Excel(name = "班次名称")
    private String shiftName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上班打卡")
    private Date clockIn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下班打卡")
    private Date clockOut;
    @Excel(name = "上班来源")
    private String clockInSource;
    @Excel(name = "下班来源")
    private String clockOutSource;
    @Excel(name = "迟到分钟")
    private Integer lateMinutes;
    @Excel(name = "早退分钟")
    private Integer earlyMinutes;
    @Excel(name = "考勤状态")
    private String attendanceStatus;
    @Excel(name = "加班小时")
    private BigDecimal overtimeHours;
    @Excel(name = "夜班")
    private Integer nightShift;
    @Excel(name = "备注")
    private String remark;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date recordDate) { this.recordDate = recordDate; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public Date getClockIn() { return clockIn; }
    public void setClockIn(Date clockIn) { this.clockIn = clockIn; }
    public Date getClockOut() { return clockOut; }
    public void setClockOut(Date clockOut) { this.clockOut = clockOut; }
    public String getClockInSource() { return clockInSource; }
    public void setClockInSource(String clockInSource) { this.clockInSource = clockInSource; }
    public String getClockOutSource() { return clockOutSource; }
    public void setClockOutSource(String clockOutSource) { this.clockOutSource = clockOutSource; }
    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
    public Integer getEarlyMinutes() { return earlyMinutes; }
    public void setEarlyMinutes(Integer earlyMinutes) { this.earlyMinutes = earlyMinutes; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public Integer getNightShift() { return nightShift; }
    public void setNightShift(Integer nightShift) { this.nightShift = nightShift; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
