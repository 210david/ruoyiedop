package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 内容发布审核日志 mk_content_audit_log
 *
 * @author ruoyi
 */
public class MkContentAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 内容ID */
    private Long contentId;

    /** 审核动作（1通过 2驳回） */
    private String auditAction;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getAuditAction() { return auditAction; }
    public void setAuditAction(String auditAction) { this.auditAction = auditAction; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
}
