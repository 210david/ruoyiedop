package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsComplaint extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long complaintId;
    @Excel(name = "客诉编号") private String complaintNo;
    @Excel(name = "客户名称") private String customerName;
    private String customerCode;
    @Excel(name = "物料名称") private String materialName;
    private String materialCode;
    private String batchNo;
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "投诉日期", dateFormat = "yyyy-MM-dd") private Date complaintDate;
    @Excel(name = "投诉类型") private String complaintType;
    private String defectDesc;
    @Excel(name = "严重程度") private String severity;
    private BigDecimal returnQty;
    private BigDecimal returnAmt;
    private BigDecimal claimAmt;
    private String handleDesc;
    private String handleResult;
    @Excel(name = "状态") private String complaintStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date closeTime;
    private String delFlag;
    private String status;

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
    public String getComplaintNo() { return complaintNo; }
    public void setComplaintNo(String complaintNo) { this.complaintNo = complaintNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getComplaintDate() { return complaintDate; }
    public void setComplaintDate(Date complaintDate) { this.complaintDate = complaintDate; }
    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }
    public String getDefectDesc() { return defectDesc; }
    public void setDefectDesc(String defectDesc) { this.defectDesc = defectDesc; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public BigDecimal getReturnQty() { return returnQty; }
    public void setReturnQty(BigDecimal returnQty) { this.returnQty = returnQty; }
    public BigDecimal getReturnAmt() { return returnAmt; }
    public void setReturnAmt(BigDecimal returnAmt) { this.returnAmt = returnAmt; }
    public BigDecimal getClaimAmt() { return claimAmt; }
    public void setClaimAmt(BigDecimal claimAmt) { this.claimAmt = claimAmt; }
    public String getHandleDesc() { return handleDesc; }
    public void setHandleDesc(String handleDesc) { this.handleDesc = handleDesc; }
    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
    public String getComplaintStatus() { return complaintStatus; }
    public void setComplaintStatus(String complaintStatus) { this.complaintStatus = complaintStatus; }
    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
