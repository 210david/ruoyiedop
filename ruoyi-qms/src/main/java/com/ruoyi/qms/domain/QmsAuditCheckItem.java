package com.ruoyi.qms.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 内审检查项记录 */
public class QmsAuditCheckItem extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long checkId;
    private Long auditPlanId;
    private String clauseRef;
    private String checkContent;
    /** 0=符合 1=不符合 2=不适用 */
    private String checkResult;
    private String evidence;
    private Integer sortNo;
    private String delFlag;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date queryDate;

    public Long getCheckId() { return checkId; }
    public void setCheckId(Long checkId) { this.checkId = checkId; }
    public Long getAuditPlanId() { return auditPlanId; }
    public void setAuditPlanId(Long auditPlanId) { this.auditPlanId = auditPlanId; }
    public String getClauseRef() { return clauseRef; }
    public void setClauseRef(String clauseRef) { this.clauseRef = clauseRef; }
    public String getCheckContent() { return checkContent; }
    public void setCheckContent(String checkContent) { this.checkContent = checkContent; }
    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Date getQueryDate() { return queryDate; }
    public void setQueryDate(Date queryDate) { this.queryDate = queryDate; }
}
