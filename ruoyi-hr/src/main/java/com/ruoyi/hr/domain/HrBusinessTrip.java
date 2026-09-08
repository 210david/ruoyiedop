package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrBusinessTrip extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long tripId;
    @Excel(name = "出差单号")
    private String tripNo;
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
    @NotBlank(message = "出差目的地不能为空")
    @Excel(name = "出差目的地")
    private String destination;
    @NotBlank(message = "出差事由不能为空")
    @Excel(name = "出差事由")
    private String reason;
    @NotNull(message = "出发日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出发日期")
    private Date departureDate;
    @NotNull(message = "预计返回日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计返回日期")
    private Date expectedReturnDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际返回日期")
    private Date actualReturnDate;
    @Excel(name = "交通方式")
    private String transportMode;
    @Excel(name = "出差天数")
    private BigDecimal tripDays;
    @Excel(name = "费用预算")
    private BigDecimal budgetAmount;
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

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getTripNo() { return tripNo; }
    public void setTripNo(String tripNo) { this.tripNo = tripNo; }
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
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Date getDepartureDate() { return departureDate; }
    public void setDepartureDate(Date departureDate) { this.departureDate = departureDate; }
    public Date getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(Date expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    public Date getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(Date actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public BigDecimal getTripDays() { return tripDays; }
    public void setTripDays(BigDecimal tripDays) { this.tripDays = tripDays; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
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
    private List<HrBusinessTripAuditLog> auditLogList;
    public List<HrBusinessTripAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<HrBusinessTripAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
