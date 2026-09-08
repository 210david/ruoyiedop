package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrClockRepair extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long clockRepairId;
    @Excel(name = "补卡单号")
    private String clockRepairNo;
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
    @NotNull(message = "补卡日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "补卡日期")
    private Date repairDate;
    @NotBlank(message = "补卡时段不能为空")
    @Excel(name = "补卡时段")
    private String repairType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "应打卡时间")
    private Date expectedClockTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "实际补卡时间")
    private Date actualClockTime;
    @NotBlank(message = "补卡原因不能为空")
    @Excel(name = "补卡原因")
    private String reason;
    @Excel(name = "附件URL")
    private String attachmentUrl;
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

    public Long getClockRepairId() { return clockRepairId; }
    public void setClockRepairId(Long clockRepairId) { this.clockRepairId = clockRepairId; }
    public String getClockRepairNo() { return clockRepairNo; }
    public void setClockRepairNo(String clockRepairNo) { this.clockRepairNo = clockRepairNo; }
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
    public Date getRepairDate() { return repairDate; }
    public void setRepairDate(Date repairDate) { this.repairDate = repairDate; }
    public String getRepairType() { return repairType; }
    public void setRepairType(String repairType) { this.repairType = repairType; }
    public Date getExpectedClockTime() { return expectedClockTime; }
    public void setExpectedClockTime(Date expectedClockTime) { this.expectedClockTime = expectedClockTime; }
    public Date getActualClockTime() { return actualClockTime; }
    public void setActualClockTime(Date actualClockTime) { this.actualClockTime = actualClockTime; }
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

    /** 审核日志列表 */
    private List<HrClockRepairAuditLog> auditLogList;
    public List<HrClockRepairAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<HrClockRepairAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
