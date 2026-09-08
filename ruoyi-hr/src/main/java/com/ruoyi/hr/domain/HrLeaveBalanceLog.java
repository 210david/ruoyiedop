package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 假期余额扣减明细 hr_leave_balance_log
 */
public class HrLeaveBalanceLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long balanceId;
    private Long leaveReqId;
    @Excel(name = "请假单号")
    private String leaveReqNo;
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "假别")
    private String leaveType;
    @Excel(name = "年度")
    private Integer year;
    @Excel(name = "变动天数")
    private BigDecimal changeDays;
    @Excel(name = "变动前已休")
    private BigDecimal beforeUsed;
    @Excel(name = "变动后已休")
    private BigDecimal afterUsed;
    @Excel(name = "变动前剩余")
    private BigDecimal beforeRemaining;
    @Excel(name = "变动后剩余")
    private BigDecimal afterRemaining;
    @Excel(name = "变动前冻结")
    private BigDecimal beforeFrozen;
    @Excel(name = "变动后冻结")
    private BigDecimal afterFrozen;
    @Excel(name = "变动类型", readConverterExp = "deduct=扣减,refund=退回,adjust=手动调整,freeze=冻结,unfreeze=解冻")
    private String changeType;
    @Excel(name = "变动原因")
    private String changeReason;
    @Excel(name = "操作人")
    private String operator;
    private String delFlag;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getBalanceId() { return balanceId; }
    public void setBalanceId(Long balanceId) { this.balanceId = balanceId; }
    public Long getLeaveReqId() { return leaveReqId; }
    public void setLeaveReqId(Long leaveReqId) { this.leaveReqId = leaveReqId; }
    public String getLeaveReqNo() { return leaveReqNo; }
    public void setLeaveReqNo(String leaveReqNo) { this.leaveReqNo = leaveReqNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getChangeDays() { return changeDays; }
    public void setChangeDays(BigDecimal changeDays) { this.changeDays = changeDays; }
    public BigDecimal getBeforeUsed() { return beforeUsed; }
    public void setBeforeUsed(BigDecimal beforeUsed) { this.beforeUsed = beforeUsed; }
    public BigDecimal getAfterUsed() { return afterUsed; }
    public void setAfterUsed(BigDecimal afterUsed) { this.afterUsed = afterUsed; }
    public BigDecimal getBeforeRemaining() { return beforeRemaining; }
    public void setBeforeRemaining(BigDecimal beforeRemaining) { this.beforeRemaining = beforeRemaining; }
    public BigDecimal getAfterRemaining() { return afterRemaining; }
    public void setAfterRemaining(BigDecimal afterRemaining) { this.afterRemaining = afterRemaining; }
    public BigDecimal getBeforeFrozen() { return beforeFrozen; }
    public void setBeforeFrozen(BigDecimal beforeFrozen) { this.beforeFrozen = beforeFrozen; }
    public BigDecimal getAfterFrozen() { return afterFrozen; }
    public void setAfterFrozen(BigDecimal afterFrozen) { this.afterFrozen = afterFrozen; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
