package com.ruoyi.pms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商资质表 pms_supplier_qualification
 *
 * @author ruoyi
 */
public class PmsSupplierQualification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资质ID */
    private Long qualId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 资质名称 */
    @Excel(name = "资质名称")
    private String qualName;

    /** 资质类型（字典 pms_qual_type） */
    @Excel(name = "资质类型", readConverterExp = "0=营业执照,1=生产许可证,2=经营许可证,3=ISO认证,9=其他资质")
    private String qualType;

    /** 资质编号 */
    @Excel(name = "资质编号")
    private String qualCode;

    /** 颁发日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "颁发日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date issueDate;

    /** 有效期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 状态（字典 pms_qual_status） */
    @Excel(name = "状态", readConverterExp = "1=有效,3=已过期")
    private String status;

    /** 资质文件路径 */
    private String fileUrl;

    /** 资质文件名称 */
    @Excel(name = "资质文件")
    private String fileName;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 删除标志 */
    private String delFlag;

    public Long getQualId() { return qualId; }
    public void setQualId(Long qualId) { this.qualId = qualId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getQualName() { return qualName; }
    public void setQualName(String qualName) { this.qualName = qualName; }
    public String getQualType() { return qualType; }
    public void setQualType(String qualType) { this.qualType = qualType; }
    public String getQualCode() { return qualCode; }
    public void setQualCode(String qualCode) { this.qualCode = qualCode; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
