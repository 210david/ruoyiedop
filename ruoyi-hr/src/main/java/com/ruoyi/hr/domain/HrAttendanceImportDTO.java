package com.ruoyi.hr.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 考勤机原始数据导入DTO
 * 考勤机导出的原始记录通常包含：工号、姓名、打卡时间、机号
 * 导入时按工号+日期分组，取最早打卡为上班、最晚打卡为下班
 */
public class HrAttendanceImportDTO
{
    @Excel(name = "工号", type = Excel.Type.IMPORT)
    private String employeeNo;

    @Excel(name = "姓名", type = Excel.Type.IMPORT)
    private String employeeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "打卡时间", width = 30, type = Excel.Type.IMPORT)
    private Date clockTime;

    @Excel(name = "考勤机号", type = Excel.Type.IMPORT)
    private String machineNo;

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Date getClockTime() { return clockTime; }
    public void setClockTime(Date clockTime) { this.clockTime = clockTime; }
    public String getMachineNo() { return machineNo; }
    public void setMachineNo(String machineNo) { this.machineNo = machineNo; }
}
