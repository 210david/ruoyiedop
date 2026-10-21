package com.ruoyi.safety.domain.vo;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;

/**
 * 学时统计导出VO
 * 与前端列表页面显示的列保持一致：参训人员、所属部门、累计学时、参训次数、合格次数、合格率、达标状态
 */
public class SafetyTrainingHoursExportVO
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "参训人员")
    private String userName;

    @Excel(name = "所属部门")
    private String deptName;

    @Excel(name = "累计学时", cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal totalHours;

    @Excel(name = "参训次数")
    private Integer attendCount;

    @Excel(name = "合格次数")
    private Integer passCount;

    @Excel(name = "合格率(%)")
    private Integer passRate;

    @Excel(name = "达标状态", readConverterExp = "0=未达标,1=达标")
    private String complianceStatus;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public Integer getAttendCount() { return attendCount; }
    public void setAttendCount(Integer attendCount) { this.attendCount = attendCount; }
    public Integer getPassCount() { return passCount; }
    public void setPassCount(Integer passCount) { this.passCount = passCount; }
    public Integer getPassRate() { return passRate; }
    public void setPassRate(Integer passRate) { this.passRate = passRate; }
    public String getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(String complianceStatus) { this.complianceStatus = complianceStatus; }
}
