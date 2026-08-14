package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsAuditPlan extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long auditPlanId;
    @Excel(name = "审核编号") private String auditPlanNo;
    @Excel(name = "审核标题") private String auditTitle;
    @Excel(name = "年度") private Integer auditYear;
    private String auditType;
    private String auditScope;
    @Excel(name = "主审") private String leadAuditor;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date auditDateFrom;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date auditDateTo;
    private String auditConclusion;
    @Excel(name = "状态", dictType = "qms_audit_plan_status") private String planStatus;
    private String delFlag;
    private String status;

    public Long getAuditPlanId() { return auditPlanId; }
    public void setAuditPlanId(Long auditPlanId) { this.auditPlanId = auditPlanId; }
    public String getAuditPlanNo() { return auditPlanNo; }
    public void setAuditPlanNo(String auditPlanNo) { this.auditPlanNo = auditPlanNo; }
    public String getAuditTitle() { return auditTitle; }
    public void setAuditTitle(String auditTitle) { this.auditTitle = auditTitle; }
    public Integer getAuditYear() { return auditYear; }
    public void setAuditYear(Integer auditYear) { this.auditYear = auditYear; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String auditType) { this.auditType = auditType; }
    public String getAuditScope() { return auditScope; }
    public void setAuditScope(String auditScope) { this.auditScope = auditScope; }
    public String getLeadAuditor() { return leadAuditor; }
    public void setLeadAuditor(String leadAuditor) { this.leadAuditor = leadAuditor; }
    public Date getAuditDateFrom() { return auditDateFrom; }
    public void setAuditDateFrom(Date auditDateFrom) { this.auditDateFrom = auditDateFrom; }
    public Date getAuditDateTo() { return auditDateTo; }
    public void setAuditDateTo(Date auditDateTo) { this.auditDateTo = auditDateTo; }
    public String getAuditConclusion() { return auditConclusion; }
    public void setAuditConclusion(String auditConclusion) { this.auditConclusion = auditConclusion; }
    public String getPlanStatus() { return planStatus; }
    public void setPlanStatus(String planStatus) { this.planStatus = planStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
