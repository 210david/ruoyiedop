package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrOuting extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long outingId;
    @Excel(name = "外出单号")
    private String outingNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "部门ID")
    private Long deptId;
    @Excel(name = "部门名称")
    private String deptName;
    @NotNull(message = "外出日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "外出日期", dateFormat = "yyyy-MM-dd")
    private Date outingDate;
    @NotNull(message = "开始时间不能为空")
    @Excel(name = "开始时间")
    private String startTime;
    @NotNull(message = "结束时间不能为空")
    @Excel(name = "结束时间")
    private String endTime;
    @NotBlank(message = "外出地点不能为空")
    @Excel(name = "外出地点")
    private String destination;
    @NotBlank(message = "外出事由不能为空")
    @Excel(name = "外出事由")
    private String reason;
    @Excel(name = "交通工具")
    private String transportMode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预计返回时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expectedReturnTime;
    @Excel(name = "附件URL")
    private String attachmentUrl;
    @Excel(name = "审批状态", dictType = "hr_audit_status")
    private String auditStatus;
    @Excel(name = "审批人")
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    @Excel(name = "审批意见")
    private String auditRemark;
    @Excel(name = "状态", dictType = "hr_audit_status")
    private String status;
    @Excel(name = "删除标志", readConverterExp = "0=正常,2=已删除")
    private String delFlag;

    public Long getOutingId() { return outingId; }
    public void setOutingId(Long outingId) { this.outingId = outingId; }
    public String getOutingNo() { return outingNo; }
    public void setOutingNo(String outingNo) { this.outingNo = outingNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Date getOutingDate() { return outingDate; }
    public void setOutingDate(Date outingDate) { this.outingDate = outingDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public Date getExpectedReturnTime() { return expectedReturnTime; }
    public void setExpectedReturnTime(Date expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
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
    private List<HrOutingAuditLog> auditLogList;
    public List<HrOutingAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<HrOutingAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
