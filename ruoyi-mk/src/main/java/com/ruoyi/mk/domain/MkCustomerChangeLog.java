package com.ruoyi.mk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户变更日志 mk_customer_change_log
 *
 * @author ruoyi
 */
public class MkCustomerChangeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 客户ID */
    private Long customerId;

    /** 变更类型（customer_type客户类型 parent上级经销商） */
    private String changeType;

    /** 变更前值 */
    @Excel(name = "变更前值")
    private String oldValue;

    /** 变更后值 */
    @Excel(name = "变更后值")
    private String newValue;

    /** 变更原因 */
    @Excel(name = "变更原因")
    private String changeReason;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
}
