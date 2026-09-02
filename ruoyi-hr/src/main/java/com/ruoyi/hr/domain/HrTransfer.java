package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrTransfer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long transferId;
    @Excel(name = "调动单号")
    private String transferNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "调动类型")
    private String transferType;
    @Excel(name = "原组织ID")
    private Long fromOrgId;
    @Excel(name = "原组织名称")
    private String fromOrgName;
    @Excel(name = "目标组织ID")
    private Long toOrgId;
    @Excel(name = "目标组织名称")
    private String toOrgName;
    @Excel(name = "原岗位")
    private String fromPosition;
    @Excel(name = "目标岗位")
    private String toPosition;
    @Excel(name = "原薪资")
    private BigDecimal fromSalary;
    @Excel(name = "目标薪资")
    private BigDecimal toSalary;
@NotNull(message = "申请日期不能为空")
@JsonFormat(pattern = "yyyy-MM-dd")
@Excel(name = "申请日期")
    private Date effectiveDate;
    @Excel(name = "调动原因")
    private String reason;
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

    public Long getTransferId() { return transferId; }
    public void setTransferId(Long transferId) { this.transferId = transferId; }
    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getTransferType() { return transferType; }
    public void setTransferType(String transferType) { this.transferType = transferType; }
    public Long getFromOrgId() { return fromOrgId; }
    public void setFromOrgId(Long fromOrgId) { this.fromOrgId = fromOrgId; }
    public String getFromOrgName() { return fromOrgName; }
    public void setFromOrgName(String fromOrgName) { this.fromOrgName = fromOrgName; }
    public Long getToOrgId() { return toOrgId; }
    public void setToOrgId(Long toOrgId) { this.toOrgId = toOrgId; }
    public String getToOrgName() { return toOrgName; }
    public void setToOrgName(String toOrgName) { this.toOrgName = toOrgName; }
    public String getFromPosition() { return fromPosition; }
    public void setFromPosition(String fromPosition) { this.fromPosition = fromPosition; }
    public String getToPosition() { return toPosition; }
    public void setToPosition(String toPosition) { this.toPosition = toPosition; }
    public BigDecimal getFromSalary() { return fromSalary; }
    public void setFromSalary(BigDecimal fromSalary) { this.fromSalary = fromSalary; }
    public BigDecimal getToSalary() { return toSalary; }
    public void setToSalary(BigDecimal toSalary) { this.toSalary = toSalary; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
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
    private List<HrTransferAuditLog> auditLogList;

    public List<HrTransferAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<HrTransferAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
