package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购合同表 pms_contract
 *
 * @author ruoyi
 */
public class PmsContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 合同名称 */
    @Excel(name = "合同名称")
    private String contractName;

    /** 关联采购订单ID */
    private Long orderId;

    /** 关联采购单号（冗余） */
    @Excel(name = "关联采购单号")
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 状态（字典 pms_contract_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已签订,2=变更审批中,3=已到期,4=已终止,5=待审核,6=驳回")
    private String status;

    /** 合同类型（字典 pms_contract_type） */
    @Excel(name = "合同类型", readConverterExp = "0=框架协议,1=采购合同,2=补充协议")
    private String contractType;

    /** 签订日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签订日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date signDate;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "到期日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 合同金额 */
    @Excel(name = "合同金额")
    private BigDecimal contractAmount;

    /** 已付金额 */
    @Excel(name = "已付金额")
    private BigDecimal paidAmount;

    /** 付款条款 */
    @Excel(name = "付款条款")
    private String paymentTerms;

    /** 交货条款 */
    @Excel(name = "交货条款")
    private String deliveryTerms;

    /** 签订人 */
    @Excel(name = "签订人")
    private String signBy;

    /** 签订部门 */
    @Excel(name = "签订部门")
    private String signDepartment;

    /** 甲方 */
    @Excel(name = "甲方")
    private String partyA;

    /** 乙方 */
    @Excel(name = "乙方")
    private String partyB;

    /** 合同附件路径 */
    private String fileUrl;

    /** 合同附件名称 */
    @Excel(name = "合同附件")
    private String fileName;

    /** 变更次数 */
    @Excel(name = "变更次数")
    private Integer changeCount;

    /** 终止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "终止日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date terminateDate;

    /** 终止原因 */
    private String terminateReason;

    /** 删除标志 */
    private String delFlag;

    /** 合同变更记录列表 */
    private List<PmsContractChange> changeList;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 审核日志列表 */
    private List<PmsContractAuditLog> auditLogList;

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public Date getSignDate() { return signDate; }
    public void setSignDate(Date signDate) { this.signDate = signDate; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getDeliveryTerms() { return deliveryTerms; }
    public void setDeliveryTerms(String deliveryTerms) { this.deliveryTerms = deliveryTerms; }
    public String getSignBy() { return signBy; }
    public void setSignBy(String signBy) { this.signBy = signBy; }
    public String getSignDepartment() { return signDepartment; }
    public void setSignDepartment(String signDepartment) { this.signDepartment = signDepartment; }
    public String getPartyA() { return partyA; }
    public void setPartyA(String partyA) { this.partyA = partyA; }
    public String getPartyB() { return partyB; }
    public void setPartyB(String partyB) { this.partyB = partyB; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Integer getChangeCount() { return changeCount; }
    public void setChangeCount(Integer changeCount) { this.changeCount = changeCount; }
    public Date getTerminateDate() { return terminateDate; }
    public void setTerminateDate(Date terminateDate) { this.terminateDate = terminateDate; }
    public String getTerminateReason() { return terminateReason; }
    public void setTerminateReason(String terminateReason) { this.terminateReason = terminateReason; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsContractChange> getChangeList() { return changeList; }
    public void setChangeList(List<PmsContractChange> changeList) { this.changeList = changeList; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public List<PmsContractAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsContractAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
