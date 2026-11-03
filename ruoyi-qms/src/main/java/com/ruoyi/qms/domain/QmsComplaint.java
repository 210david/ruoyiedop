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
    @Excel(name = "投诉类型", dictType = "qms_complaint_type") private String complaintType;
    private String defectDesc;
    @Excel(name = "严重程度", dictType = "qms_complaint_severity") private String severity;
    private BigDecimal returnQty;
    private BigDecimal returnAmt;
    private BigDecimal claimAmt;
    private String handleDesc;
    private String handleResult;
    /** 供应商ID（引用wms_supplier） */
    private Long supplierId;
    /** 供应商名称 */
    @Excel(name = "供应商名称") private String supplierName;
    /** 供应商联系人 */
    private String supplierContact;
    /** 供应商联系电话 */
    private String supplierPhone;
    /** 供货批次号/送货单号 */
    @Excel(name = "供货批次号/送货单号") private String deliveryNo;
    /** 供货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "供货日期", dateFormat = "yyyy-MM-dd") private Date supplyDate;
    /** 责任类型（0供应商责任 1自产责任 2物流责任 3客户原因 4待定） */
    @Excel(name = "责任类型", dictType = "qms_responsibility_type") private String responsibilityType;
    @Excel(name = "状态", dictType = "qms_complaint_status") private String complaintStatus;
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
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    public String getSupplierPhone() { return supplierPhone; }
    public void setSupplierPhone(String supplierPhone) { this.supplierPhone = supplierPhone; }
    public String getDeliveryNo() { return deliveryNo; }
    public void setDeliveryNo(String deliveryNo) { this.deliveryNo = deliveryNo; }
    public Date getSupplyDate() { return supplyDate; }
    public void setSupplyDate(Date supplyDate) { this.supplyDate = supplyDate; }
    public String getResponsibilityType() { return responsibilityType; }
    public void setResponsibilityType(String responsibilityType) { this.responsibilityType = responsibilityType; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
