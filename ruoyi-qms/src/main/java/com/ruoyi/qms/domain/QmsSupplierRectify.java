package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商整改单（客诉供应商责任处理）对象 qms_supplier_rectify
 *
 * 需求二：客诉管理——增加供应商信息
 * 业务规则：KR-03 供应商整改单逾期未回复，消息中心自动升级提醒采购负责人
 */
public class QmsSupplierRectify extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 整改单ID */
    private Long rectifyId;
    /** 整改单编号 */
    @Excel(name = "整改单编号")
    private String rectifyNo;
    /** 客诉ID */
    private Long complaintId;
    /** 客诉编号 */
    @Excel(name = "客诉编号")
    private String complaintNo;
    /** 供应商ID（引用wms_supplier） */
    private Long supplierId;
    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;
    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;
    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;
    /** 整改要求 */
    @Excel(name = "整改要求")
    private String rectifyRequirement;
    /** 整改证据附件 */
    private String evidenceAttachment;
    /** 整改结果 */
    private String rectifyResult;
    /** 供应商回复 */
    private String supplierReply;
    /** 供应商回复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date replyTime;
    /** 供应商回复时限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "回复时限", dateFormat = "yyyy-MM-dd")
    private Date replyDeadline;
    /** 状态（0待回复 1已回复 2逾期升级 3已关闭） */
    @Excel(name = "状态", dictType = "qms_rectify_status")
    private String rectifyStatus;
    /** 删除标志 */
    private String delFlag;

    public Long getRectifyId() { return rectifyId; }
    public void setRectifyId(Long rectifyId) { this.rectifyId = rectifyId; }
    public String getRectifyNo() { return rectifyNo; }
    public void setRectifyNo(String rectifyNo) { this.rectifyNo = rectifyNo; }
    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
    public String getComplaintNo() { return complaintNo; }
    public void setComplaintNo(String complaintNo) { this.complaintNo = complaintNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getRectifyRequirement() { return rectifyRequirement; }
    public void setRectifyRequirement(String rectifyRequirement) { this.rectifyRequirement = rectifyRequirement; }
    public String getEvidenceAttachment() { return evidenceAttachment; }
    public void setEvidenceAttachment(String evidenceAttachment) { this.evidenceAttachment = evidenceAttachment; }
    public String getRectifyResult() { return rectifyResult; }
    public void setRectifyResult(String rectifyResult) { this.rectifyResult = rectifyResult; }
    public String getSupplierReply() { return supplierReply; }
    public void setSupplierReply(String supplierReply) { this.supplierReply = supplierReply; }
    public Date getReplyTime() { return replyTime; }
    public void setReplyTime(Date replyTime) { this.replyTime = replyTime; }
    public Date getReplyDeadline() { return replyDeadline; }
    public void setReplyDeadline(Date replyDeadline) { this.replyDeadline = replyDeadline; }
    public String getRectifyStatus() { return rectifyStatus; }
    public void setRectifyStatus(String rectifyStatus) { this.rectifyStatus = rectifyStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
