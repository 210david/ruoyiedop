package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrAttendanceMonthly extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long monthlyId;
    @NotBlank(message = "年月不能为空")
    @Excel(name = "年月")
    private String yearMonth;
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    private Long orgId;
    /** 部门名称（关联 sys_dept 回显，非表字段） */
    private String orgName;
    /** 部门ID（统计维度，冗余存储） */
    private Long deptId;
    @Excel(name = "部门名称")
    private String deptName;
    @Excel(name = "应出勤天数")
    private BigDecimal requiredDays;
    @Excel(name = "出勤天数")
    private BigDecimal attendDays;
    @Excel(name = "旷工天数")
    private BigDecimal absentDays;
    @Excel(name = "请假天数")
    private BigDecimal leaveDays;
    @Excel(name = "外出天数")
    private BigDecimal outingDays;
    @Excel(name = "出差天数")
    private BigDecimal tripDays;
    @Excel(name = "加班小时")
    private BigDecimal overtimeHours;
    @Excel(name = "迟到次数")
    private Integer lateCount;
    @Excel(name = "早退次数")
    private Integer earlyCount;
    /** 应出勤天数 = 当月有排班(shift_id>0)且日历为工作日的天数（按班次与节假日理论计算） */
    /** 实际工作天数 = 出勤 + 出差 + 外出（计薪口径，仅后端核算用，不展示/导出） */
    private BigDecimal actualDays;
    @Excel(name = "夜班次数")
    private Integer nightShiftCount;
    @Excel(name = "高温天数")
    private BigDecimal highTempDays;
    @Excel(name = "封账状态")
    private String sealedStatus;
    @Excel(name = "封账人")
    private String sealedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "封账时间")
    private Date sealedTime;
    private String delFlag;

    public Long getMonthlyId() { return monthlyId; }
    public void setMonthlyId(Long monthlyId) { this.monthlyId = monthlyId; }
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
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
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public BigDecimal getAttendDays() { return attendDays; }
    public void setAttendDays(BigDecimal attendDays) { this.attendDays = attendDays; }
    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }
    public Integer getEarlyCount() { return earlyCount; }
    public void setEarlyCount(Integer earlyCount) { this.earlyCount = earlyCount; }
    public BigDecimal getAbsentDays() { return absentDays; }
    public void setAbsentDays(BigDecimal absentDays) { this.absentDays = absentDays; }
    public BigDecimal getLeaveDays() { return leaveDays; }
    public void setLeaveDays(BigDecimal leaveDays) { this.leaveDays = leaveDays; }
    public BigDecimal getOutingDays() { return outingDays; }
    public void setOutingDays(BigDecimal outingDays) { this.outingDays = outingDays; }
    public BigDecimal getTripDays() { return tripDays; }
    public void setTripDays(BigDecimal tripDays) { this.tripDays = tripDays; }
    public BigDecimal getRequiredDays() { return requiredDays; }
    public void setRequiredDays(BigDecimal requiredDays) { this.requiredDays = requiredDays; }
    public BigDecimal getActualDays() { return actualDays; }
    public void setActualDays(BigDecimal actualDays) { this.actualDays = actualDays; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public Integer getNightShiftCount() { return nightShiftCount; }
    public void setNightShiftCount(Integer nightShiftCount) { this.nightShiftCount = nightShiftCount; }
    public BigDecimal getHighTempDays() { return highTempDays; }
    public void setHighTempDays(BigDecimal highTempDays) { this.highTempDays = highTempDays; }
    public String getSealedStatus() { return sealedStatus; }
    public void setSealedStatus(String sealedStatus) { this.sealedStatus = sealedStatus; }
    public String getSealedBy() { return sealedBy; }
    public void setSealedBy(String sealedBy) { this.sealedBy = sealedBy; }
    public Date getSealedTime() { return sealedTime; }
    public void setSealedTime(Date sealedTime) { this.sealedTime = sealedTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
