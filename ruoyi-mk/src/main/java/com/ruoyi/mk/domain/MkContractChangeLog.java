package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 合同变更记录 mk_contract_change_log
 *
 * @author ruoyi
 */
public class MkContractChangeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long logId;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    private String contractNo;

    /** 变更类型（1金额变更 2日期变更 3条款变更 4其他） */
    private String changeType;

    /** 变更字段 */
    private String fieldName;

    /** 原值 */
    private String oldValue;

    /** 新值 */
    private String newValue;

    /** 变更原因 */
    private String changeReason;

    /** 变更状态（0待审批 1已批准 2已驳回 3已执行） */
    private String changeStatus;

    /** 审批意见 */
    private String approveOpinion;

    /** 审批人 */
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getChangeStatus() { return changeStatus; }
    public void setChangeStatus(String changeStatus) { this.changeStatus = changeStatus; }
    public String getApproveOpinion() { return approveOpinion; }
    public void setApproveOpinion(String approveOpinion) { this.approveOpinion = approveOpinion; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
}
