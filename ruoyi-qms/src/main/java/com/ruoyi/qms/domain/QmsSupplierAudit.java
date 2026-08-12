package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsSupplierAudit extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long auditId;
    @Excel(name = "审核编号") private String auditNo;
    private Long supplierId;
    @Excel(name = "供应商名称") private String supplierName;
    @Excel(name = "审核类型") private String auditType;
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "审核日期", dateFormat = "yyyy-MM-dd") private Date auditDate;
    @Excel(name = "审核员") private String auditor;
    private String auditScope;
    @Excel(name = "审核结论") private String auditResult;
    private String nonconformity;
    private String auditStatus;
    private String delFlag;
    private String status;

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    public String getAuditNo() { return auditNo; }
    public void setAuditNo(String auditNo) { this.auditNo = auditNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String auditType) { this.auditType = auditType; }
    public Date getAuditDate() { return auditDate; }
    public void setAuditDate(Date auditDate) { this.auditDate = auditDate; }
    public String getAuditor() { return auditor; }
    public void setAuditor(String auditor) { this.auditor = auditor; }
    public String getAuditScope() { return auditScope; }
    public void setAuditScope(String auditScope) { this.auditScope = auditScope; }
    public String getAuditResult() { return auditResult; }
    public void setAuditResult(String auditResult) { this.auditResult = auditResult; }
    public String getNonconformity() { return nonconformity; }
    public void setNonconformity(String nonconformity) { this.nonconformity = nonconformity; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
