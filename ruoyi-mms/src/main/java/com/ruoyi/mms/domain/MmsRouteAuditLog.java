package com.ruoyi.mms.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工艺路线审核日志 mms_route_audit_log
 */
public class MmsRouteAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 工艺路线ID */
    private Long routeId;

    /** 审核动作（1通过 2驳回） */
    private String auditAction;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getAuditAction() { return auditAction; }
    public void setAuditAction(String auditAction) { this.auditAction = auditAction; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public java.util.Date getAuditTime() { return auditTime; }
    public void setAuditTime(java.util.Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
}
