package com.ruoyi.mms.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工艺路线版本变更记录 mms_route_version_log
 */
public class MmsRouteVersionLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long routeId;
    private String routeNo;
    private String oldVersion;
    private String newVersion;
    private String oldStatus;
    private String newStatus;

    /** 操作类型：create/update/enable/audit/disable/copy */
    private String changeType;
    /** 操作描述 */
    private String changeDesc;

    /** 审核意见（仅审核操作时有值） */
    private String auditRemark;

    /** 操作人 */
    private String changeBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date changeTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteNo() { return routeNo; }
    public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
    public String getOldVersion() { return oldVersion; }
    public void setOldVersion(String oldVersion) { this.oldVersion = oldVersion; }
    public String getNewVersion() { return newVersion; }
    public void setNewVersion(String newVersion) { this.newVersion = newVersion; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getChangeDesc() { return changeDesc; }
    public void setChangeDesc(String changeDesc) { this.changeDesc = changeDesc; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getChangeBy() { return changeBy; }
    public void setChangeBy(String changeBy) { this.changeBy = changeBy; }
    public java.util.Date getChangeTime() { return changeTime; }
    public void setChangeTime(java.util.Date changeTime) { this.changeTime = changeTime; }
}
