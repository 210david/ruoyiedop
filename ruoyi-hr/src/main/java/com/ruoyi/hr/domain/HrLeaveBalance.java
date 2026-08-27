package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrLeaveBalance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long balanceId;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @NotBlank(message = "假别不能为空")
    @Excel(name = "假别")
    private String leaveType;
    @NotNull(message = "年度不能为空")
    @Excel(name = "年度")
    private Integer year;
    @Excel(name = "总额度")
    private BigDecimal totalDays;
    @Excel(name = "已休天数")
    private BigDecimal usedDays;
    @Excel(name = "剩余天数")
    private BigDecimal remainingDays;
    @Excel(name = "冻结天数")
    private BigDecimal frozenDays;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "过期日期")
    private Date expireDate;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getBalanceId() { return balanceId; }
    public void setBalanceId(Long balanceId) { this.balanceId = balanceId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getTotalDays() { return totalDays; }
    public void setTotalDays(BigDecimal totalDays) { this.totalDays = totalDays; }
    public BigDecimal getUsedDays() { return usedDays; }
    public void setUsedDays(BigDecimal usedDays) { this.usedDays = usedDays; }
    public BigDecimal getRemainingDays() { return remainingDays; }
    public void setRemainingDays(BigDecimal remainingDays) { this.remainingDays = remainingDays; }
    public BigDecimal getFrozenDays() { return frozenDays; }
    public void setFrozenDays(BigDecimal frozenDays) { this.frozenDays = frozenDays; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
