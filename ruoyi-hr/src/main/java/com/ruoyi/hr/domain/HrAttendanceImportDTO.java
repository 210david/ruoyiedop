package com.ruoyi.hr.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 考勤原始数据导入DTO
 *
 * 两种数据源形态：
 * 1. 考勤机 Excel：只有工号、姓名、打卡时间、机号，无打卡类型 → 按工号+日期分组，取最早为上班、最晚为下班
 * 2. 第三方平台（钉钉/企业微信）：数据源已明确给出打卡类型 → 按 punchType 精确落位，不再靠时间推断
 */
public class HrAttendanceImportDTO
{
    /** 打卡类型：上班卡（数据源明确提供时使用） */
    public static final String PUNCH_ON_DUTY = "ON_DUTY";

    /** 打卡类型：下班卡（数据源明确提供时使用） */
    public static final String PUNCH_OFF_DUTY = "OFF_DUTY";

    @Excel(name = "工号", type = Excel.Type.IMPORT)
    private String employeeNo;

    @Excel(name = "姓名", type = Excel.Type.IMPORT)
    private String employeeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "打卡时间", width = 30, type = Excel.Type.IMPORT, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date clockTime;

    @Excel(name = "考勤机号", type = Excel.Type.IMPORT)
    private String machineNo;

    /**
     * 打卡类型：ON_DUTY=上班卡 / OFF_DUTY=下班卡
     * <p>为空表示数据源未提供（考勤机 Excel 导入），此时按打卡时间推断上下班；</p>
     * <p>有值时按类型精确落位，避免"最早当上班、最晚当下班"的推断误差（夜班跨零点、外出打卡等场景）</p>
     */
    private String punchType;

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Date getClockTime() { return clockTime; }
    public void setClockTime(Date clockTime) { this.clockTime = clockTime; }
    public String getMachineNo() { return machineNo; }
    public void setMachineNo(String machineNo) { this.machineNo = machineNo; }
    public String getPunchType() { return punchType; }
    public void setPunchType(String punchType) { this.punchType = punchType; }
}
