package com.ruoyi.mms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工单审核日志 mms_work_order_audit_log
 *
 * @author ruoyi
 */
public class MmsWorkOrderAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long workOrderId;
    private String auditBy;
    private String auditAction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private String auditRemark;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public String getAuditAction() { return auditAction; }
    public void setAuditAction(String auditAction) { this.auditAction = auditAction; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
}
