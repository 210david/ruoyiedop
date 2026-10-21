package com.ruoyi.hr.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 调动审核日志对象 biz_hr_transfer_audit_log
 *
 * @author ruoyi
 */
public class HrTransferAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 调动单ID */
    private Long transferId;

    /** 审核动作（1通过 2驳回） */
    @Excel(name = "审核动作", readConverterExp = "1=审核通过,2=审核驳回")
    private String auditAction;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    @Excel(name = "审核意见")
    private String auditRemark;

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getLogId()
    {
        return logId;
    }

    public void setTransferId(Long transferId)
    {
        this.transferId = transferId;
    }

    public Long getTransferId()
    {
        return transferId;
    }

    public void setAuditAction(String auditAction)
    {
        this.auditAction = auditAction;
    }

    public String getAuditAction()
    {
        return auditAction;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }
}
