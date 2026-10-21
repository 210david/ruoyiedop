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
    @Excel(name = "排班日期", width = 20, sort = 0, dateFormat = "yyyy-MM-dd")
    private Date scheduleDate;
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;
    @Excel(name = "员工姓名", width = 16, sort = 1)
    private String employeeName;
    @Excel(name = "工号", width = 14, sort = 2)
    private String employeeNo;
    private Long orgId;
    /** 部门名称（关联 sys_dept 回显，非表字段） */
    @Excel(name = "部门", width = 16, sort = 3)
    private String orgName;
    private Long shiftId;
    @Excel(name = "班次", width = 14, sort = 4)
    private String shiftName;
    @Excel(name = "状态", width = 10, sort = 5, readConverterExp = "0=正常,1=停用")
    private String status;
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
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
