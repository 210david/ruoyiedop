package com.ruoyi.pjm.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目变更申请 pjm_change
 */
public class PjmChange extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long changeId;
    private String changeNo;
    private Long projectId;
    private String changeType;    // 1范围 2进度 3成本 4质量 5其他
    private String title;
    private String reason;
    private String impactAnalysis;
    private Integer scheduleImpact;
    private BigDecimal costImpact;
    private String status;        // 0草稿 1待审 2通过 3驳回 4已实施
    private Long requesterId;
    private String requesterName;
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;
    private String auditOpinion;
    private String delFlag;

    private String projectName;

    public Long getChangeId() { return changeId; }
    public void setChangeId(Long changeId) { this.changeId = changeId; }
    public String getChangeNo() { return changeNo; }
    public void setChangeNo(String changeNo) { this.changeNo = changeNo; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getImpactAnalysis() { return impactAnalysis; }
    public void setImpactAnalysis(String impactAnalysis) { this.impactAnalysis = impactAnalysis; }
    public Integer getScheduleImpact() { return scheduleImpact; }
    public void setScheduleImpact(Integer scheduleImpact) { this.scheduleImpact = scheduleImpact; }
    public BigDecimal getCostImpact() { return costImpact; }
    public void setCostImpact(BigDecimal costImpact) { this.costImpact = costImpact; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
