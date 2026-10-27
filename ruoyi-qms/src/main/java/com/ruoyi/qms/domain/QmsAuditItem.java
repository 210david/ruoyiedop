package com.ruoyi.qms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsAuditItem extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long itemId;
    private Long auditPlanId;
    @Excel(name = "审核编号") private String auditPlanNo;
    @Excel(name = "条款引用") private String clauseRef;
    private String ncDesc;
    @Excel(name = "不符合类型") private String ncType;
    private Long deptId;
    @Excel(name = "责任部门") private String deptName;
    private String correctiveAction;
    private Long capaId;
    private String verifyResult;
    @Excel(name = "状态", dictType = "qms_audit_item_status") private String itemStatus;
    private String delFlag;
    private String status;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getAuditPlanId() { return auditPlanId; }
    public void setAuditPlanId(Long auditPlanId) { this.auditPlanId = auditPlanId; }
    public String getAuditPlanNo() { return auditPlanNo; }
    public void setAuditPlanNo(String auditPlanNo) { this.auditPlanNo = auditPlanNo; }
    public String getClauseRef() { return clauseRef; }
    public void setClauseRef(String clauseRef) { this.clauseRef = clauseRef; }
    public String getNcDesc() { return ncDesc; }
    public void setNcDesc(String ncDesc) { this.ncDesc = ncDesc; }
    public String getNcType() { return ncType; }
    public void setNcType(String ncType) { this.ncType = ncType; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
    public Long getCapaId() { return capaId; }
    public void setCapaId(Long capaId) { this.capaId = capaId; }
    public String getVerifyResult() { return verifyResult; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public String getItemStatus() { return itemStatus; }
    public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
