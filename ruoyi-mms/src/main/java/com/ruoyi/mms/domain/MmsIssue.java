package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 领料单 mms_issue
 *
 * @author ruoyi
 */
public class MmsIssue extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long issueId;

    @Excel(name = "领料单号")
    private String issueNo;

    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "领料数量")
    private BigDecimal issueQty;

    @Excel(name = "批次号")
    private String batchNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date issueTime;

    @Excel(name = "领料人")
    private String issueBy;

    @Excel(name = "状态", readConverterExp = "0=待领料,1=已领料")
    private String status;

    private String delFlag;

    /** 产品编码（来源工单，非持久化） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（来源工单，非持久化） */
    @Excel(name = "产品名称")
    private String productName;

    /** 工单数量（来源工单，非持久化） */
    @Excel(name = "工单数量")
    private BigDecimal planQty;

    /** 领料明细列表（批量领料） */
    private List<MmsIssueDetail> detailList;

    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public String getIssueNo() { return issueNo; }
    public void setIssueNo(String issueNo) { this.issueNo = issueNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getIssueQty() { return issueQty; }
    public void setIssueQty(BigDecimal issueQty) { this.issueQty = issueQty; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getIssueTime() { return issueTime; }
    public void setIssueTime(Date issueTime) { this.issueTime = issueTime; }
    public String getIssueBy() { return issueBy; }
    public void setIssueBy(String issueBy) { this.issueBy = issueBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public List<MmsIssueDetail> getDetailList() { return detailList; }
    public void setDetailList(List<MmsIssueDetail> detailList) { this.detailList = detailList; }
}
