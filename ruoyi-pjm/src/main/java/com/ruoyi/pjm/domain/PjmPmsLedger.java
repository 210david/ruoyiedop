package com.ruoyi.pjm.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目采购台账（跨模块只读视图，聚合 pms 采购申请/计划/订单/合同）
 */
public class PjmPmsLedger extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 单据类型：request=采购申请 plan=采购计划 order=采购订单 contract=采购合同 */
    private String docType;

    /** 单据ID */
    private Long docId;

    /** 单据编号 */
    private String docNo;

    /** 标题（申请/计划为title，合同为合同名称，订单为供应商+订单号） */
    private String title;

    /** 供应商名称 */
    private String supplierName;

    /** 单据状态（各单据类型原始状态码） */
    private String status;

    /** 单据金额 */
    private BigDecimal amount;

    /** 已付款金额（合同/订单有值） */
    private BigDecimal paidAmount;

    /** 单据日期（申请=期望到货日期，计划=开始日期，订单=订单日期，合同=签订日期） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date docDate;

    /** 关联单号（申请→计划号，订单→申请号，合同→订单号） */
    private String relNo;

    /** 项目ID（查询条件） */
    private Long projectId;

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Date getDocDate() { return docDate; }
    public void setDocDate(Date docDate) { this.docDate = docDate; }
    public String getRelNo() { return relNo; }
    public void setRelNo(String relNo) { this.relNo = relNo; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
