package com.ruoyi.pms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购合同变更记录表 pms_contract_change
 *
 * @author ruoyi
 */
public class PmsContractChange extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 变更ID */
    private Long changeId;

    /** 合同ID */
    private Long contractId;

    /** 变更编号 */
    @Excel(name = "变更编号")
    private String changeNo;

    /** 变更类型（字典 pms_contract_change_type） */
    @Excel(name = "变更类型", readConverterExp = "0=金额变更,1=交期变更,2=条款变更,9=其他变更")
    private String changeType;

    /** 变更字段名 */
    private String fieldName;

    /** 新值 */
    private String newValue;

    /** 变更内容 */
    private String changeContent;

    /** 原内容 */
    private String originalContent;

    /** 变更原因 */
    @Excel(name = "变更原因")
    private String changeReason;

    /** 变更日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "变更日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date changeDate;

    /** 变更人 */
    @Excel(name = "变更人")
    private String changeBy;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核状态（字典 pms_contract_change_status） */
    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已审核,2=已驳回")
    private String auditStatus;

    /** 审核意见 */
    private String auditRemark;

    /** 删除标志 */
    private String delFlag;

    public Long getChangeId() { return changeId; }
    public void setChangeId(Long changeId) { this.changeId = changeId; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getChangeNo() { return changeNo; }
    public void setChangeNo(String changeNo) { this.changeNo = changeNo; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getChangeContent() { return changeContent; }
    public void setChangeContent(String changeContent) { this.changeContent = changeContent; }
    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public Date getChangeDate() { return changeDate; }
    public void setChangeDate(Date changeDate) { this.changeDate = changeDate; }
    public String getChangeBy() { return changeBy; }
    public void setChangeBy(String changeBy) { this.changeBy = changeBy; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
