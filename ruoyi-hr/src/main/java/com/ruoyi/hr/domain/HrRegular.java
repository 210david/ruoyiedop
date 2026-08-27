package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrRegular extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long regularId;
    @NotBlank(message = "转正单号不能为空")
    @Excel(name = "转正单号")
    private String regularNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "组织名称")
    private String orgName;
    @Excel(name = "岗位名称")
    private String positionName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职日期")
    private Date entryDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "拟转正日期")
    private Date regularDate;
    @Excel(name = "工作表现")
    private String workPerformance;
    @Excel(name = "技能达标")
    private String skillAssessment;
    @Excel(name = "出勤情况")
    private String attendanceRecord;
    @Excel(name = "安全违规")
    private String safetyViolation;
    @Excel(name = "评估结果")
    private String evaluationResult;
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

    public Long getRegularId() { return regularId; }
    public void setRegularId(Long regularId) { this.regularId = regularId; }
    public String getRegularNo() { return regularNo; }
    public void setRegularNo(String regularNo) { this.regularNo = regularNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    public Date getRegularDate() { return regularDate; }
    public void setRegularDate(Date regularDate) { this.regularDate = regularDate; }
    public String getWorkPerformance() { return workPerformance; }
    public void setWorkPerformance(String workPerformance) { this.workPerformance = workPerformance; }
    public String getSkillAssessment() { return skillAssessment; }
    public void setSkillAssessment(String skillAssessment) { this.skillAssessment = skillAssessment; }
    public String getAttendanceRecord() { return attendanceRecord; }
    public void setAttendanceRecord(String attendanceRecord) { this.attendanceRecord = attendanceRecord; }
    public String getSafetyViolation() { return safetyViolation; }
    public void setSafetyViolation(String safetyViolation) { this.safetyViolation = safetyViolation; }
    public String getEvaluationResult() { return evaluationResult; }
    public void setEvaluationResult(String evaluationResult) { this.evaluationResult = evaluationResult; }
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
}
