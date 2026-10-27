package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrLeaveRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long leaveReqId;
    @Excel(name = "请假单号")
    private String leaveReqNo;
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
    @NotBlank(message = "假别不能为空")
    @Excel(name = "假别", dictType = "hr_leave_type")
    private String leaveType;
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @NotNull(message = "请假天数不能为空")
    @Excel(name = "请假天数")
    private BigDecimal leaveDays;
    @Excel(name = "请假原因")
    private String reason;
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

    /* ===== 假期余额关联字段（查询时从 biz_hr_leave_balance JOIN 带出）===== */
    @Excel(name = "剩余天数")
    private BigDecimal remainingDays;
    @Excel(name = "总额度")
    private BigDecimal totalDays;
    @Excel(name = "已休天数")
    private BigDecimal usedDays;

    public Long getLeaveReqId() { return leaveReqId; }
    public void setLeaveReqId(Long leaveReqId) { this.leaveReqId = leaveReqId; }
    public String getLeaveReqNo() { return leaveReqNo; }
    public void setLeaveReqNo(String leaveReqNo) { this.leaveReqNo = leaveReqNo; }
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
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public BigDecimal getLeaveDays() { return leaveDays; }
    public void setLeaveDays(BigDecimal leaveDays) { this.leaveDays = leaveDays; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
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

    public BigDecimal getRemainingDays() { return remainingDays; }
    public void setRemainingDays(BigDecimal remainingDays) { this.remainingDays = remainingDays; }
    public BigDecimal getTotalDays() { return totalDays; }
    public void setTotalDays(BigDecimal totalDays) { this.totalDays = totalDays; }
    public BigDecimal getUsedDays() { return usedDays; }
    public void setUsedDays(BigDecimal usedDays) { this.usedDays = usedDays; }

    /** 审核日志列表 */
    private List<HrLeaveRequestAuditLog> auditLogList;

    public List<HrLeaveRequestAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<HrLeaveRequestAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
