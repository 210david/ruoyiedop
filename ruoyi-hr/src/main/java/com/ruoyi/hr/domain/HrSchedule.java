package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotNull;

public class HrSchedule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long scheduleId;
    @NotNull(message = "排班日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "排班日期")
    private Date scheduleDate;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "组织ID")
    private Long orgId;
    @Excel(name = "班次ID")
    private Long shiftId;
    @Excel(name = "班次名称")
    private String shiftName;
    @Excel(name = "休息日")
    private Integer restDay;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Date getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(Date scheduleDate) { this.scheduleDate = scheduleDate; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public Integer getRestDay() { return restDay; }
    public void setRestDay(Integer restDay) { this.restDay = restDay; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
