package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class HrLeave extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long leaveId;
    @Excel(name = "离职单号")
    private String leaveNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "离职类型")
    private String leaveType;
    @NotNull(message = "离职日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "离职日期")
    private Date leaveDate;
    @Excel(name = "离职原因")
    private String reason;
    @Excel(name = "交接状态")
    private String handoverStatus;
    @Excel(name = "交接清单")
    private String handoverRemark;
    @Excel(name = "离岗体检")
    private String healthCheck;
    @Excel(name = "补偿类型")
    private String compensationType;
    @Excel(name = "补偿金额")
    private BigDecimal compensationAmount;
    @Excel(name = "未休年假天数")
    private BigDecimal annualLeaveBalance;
    @Excel(name = "年假折算金额")
    private BigDecimal annualLeaveAmount;
    @Excel(name = "结算总金额")
    private BigDecimal settlementAmount;
    @Excel(name = "离职证明编号")
    private String certificateNo;
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

    public Long getLeaveId() { return leaveId; }
    public void setLeaveId(Long leaveId) { this.leaveId = leaveId; }
    public String getLeaveNo() { return leaveNo; }
    public void setLeaveNo(String leaveNo) { this.leaveNo = leaveNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Date getLeaveDate() { return leaveDate; }
    public void setLeaveDate(Date leaveDate) { this.leaveDate = leaveDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getHandoverStatus() { return handoverStatus; }
    public void setHandoverStatus(String handoverStatus) { this.handoverStatus = handoverStatus; }
    public String getHandoverRemark() { return handoverRemark; }
    public void setHandoverRemark(String handoverRemark) { this.handoverRemark = handoverRemark; }
    public String getHealthCheck() { return healthCheck; }
    public void setHealthCheck(String healthCheck) { this.healthCheck = healthCheck; }
    public String getCompensationType() { return compensationType; }
    public void setCompensationType(String compensationType) { this.compensationType = compensationType; }
    public BigDecimal getCompensationAmount() { return compensationAmount; }
    public void setCompensationAmount(BigDecimal compensationAmount) { this.compensationAmount = compensationAmount; }
    public BigDecimal getAnnualLeaveBalance() { return annualLeaveBalance; }
    public void setAnnualLeaveBalance(BigDecimal annualLeaveBalance) { this.annualLeaveBalance = annualLeaveBalance; }
    public BigDecimal getAnnualLeaveAmount() { return annualLeaveAmount; }
    public void setAnnualLeaveAmount(BigDecimal annualLeaveAmount) { this.annualLeaveAmount = annualLeaveAmount; }
    public BigDecimal getSettlementAmount() { return settlementAmount; }
    public void setSettlementAmount(BigDecimal settlementAmount) { this.settlementAmount = settlementAmount; }
    public String getCertificateNo() { return certificateNo; }
    public void setCertificateNo(String certificateNo) { this.certificateNo = certificateNo; }
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

    /** 审核日志列表 */
    private List<HrLeaveAuditLog> auditLogList;

    public List<HrLeaveAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<HrLeaveAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
