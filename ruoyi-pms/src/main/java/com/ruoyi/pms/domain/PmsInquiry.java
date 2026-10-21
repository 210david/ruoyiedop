package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 询价单表 pms_inquiry
 *
 * @author ruoyi
 */
public class PmsInquiry extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 询价ID */
    private Long inquiryId;

    /** 询价单号 */
    @Excel(name = "询价单号")
    private String inquiryNo;

    /** 询价标题 */
    @Excel(name = "询价标题")
    private String title;

    /** 状态（0-草稿 1-询价中 2-已截止 3-已比价 4-已定标） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=询价中,2=已截止,3=已比价,4=已定标")
    private String status;

    /** 询价方式（0-公开询价 1-邀请询价） */
    @Excel(name = "询价方式", readConverterExp = "0=公开询价,1=邀请询价")
    private String inquiryType;

    /** 采购类型（0-物资 1-服务 2-工程） */
    @Excel(name = "采购类型", readConverterExp = "0=物资,1=服务,2=工程")
    private String purchaseType;

    /** 报价截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报价截止", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;

    /** 预计采购金额 */
    @Excel(name = "预计金额")
    private BigDecimal estimateAmount;

    /** 定标金额 */
    @Excel(name = "定标金额")
    private BigDecimal awardAmount;

    /** 定标供应商ID */
    private Long awardSupplierId;

    /** 定标供应商名称 */
    @Excel(name = "定标供应商")
    private String awardSupplierName;

    /** 询价说明 */
    private String description;

    /** 询价人ID */
    private Long inquirerId;

    /** 询价人姓名 */
    @Excel(name = "询价人")
    private String inquirerName;

    /** 询价部门ID */
    private Long deptId;

    /** 询价部门名称 */
    @Excel(name = "询价部门")
    private String deptName;

    /** 删除标志 */
    private String delFlag;

    /** 询价明细列表 */
    private List<PmsInquiryDetail> detailList;

    /** 报价列表 */
    private List<PmsQuotation> quotationList;

    /** 报价数量（列表查询用） */
    private Integer quotationCount;

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
    private List<PmsInquiryAuditLog> auditLogList;

    public Long getInquiryId() { return inquiryId; }
    public void setInquiryId(Long inquiryId) { this.inquiryId = inquiryId; }

    public String getInquiryNo() { return inquiryNo; }
    public void setInquiryNo(String inquiryNo) { this.inquiryNo = inquiryNo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInquiryType() { return inquiryType; }
    public void setInquiryType(String inquiryType) { this.inquiryType = inquiryType; }

    public String getPurchaseType() { return purchaseType; }
    public void setPurchaseType(String purchaseType) { this.purchaseType = purchaseType; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public BigDecimal getEstimateAmount() { return estimateAmount; }
    public void setEstimateAmount(BigDecimal estimateAmount) { this.estimateAmount = estimateAmount; }

    public BigDecimal getAwardAmount() { return awardAmount; }
    public void setAwardAmount(BigDecimal awardAmount) { this.awardAmount = awardAmount; }

    public Long getAwardSupplierId() { return awardSupplierId; }
    public void setAwardSupplierId(Long awardSupplierId) { this.awardSupplierId = awardSupplierId; }

    public String getAwardSupplierName() { return awardSupplierName; }
    public void setAwardSupplierName(String awardSupplierName) { this.awardSupplierName = awardSupplierName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getInquirerId() { return inquirerId; }
    public void setInquirerId(Long inquirerId) { this.inquirerId = inquirerId; }

    public String getInquirerName() { return inquirerName; }
    public void setInquirerName(String inquirerName) { this.inquirerName = inquirerName; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public List<PmsInquiryDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsInquiryDetail> detailList) { this.detailList = detailList; }

    public List<PmsQuotation> getQuotationList() { return quotationList; }
    public void setQuotationList(List<PmsQuotation> quotationList) { this.quotationList = quotationList; }

    public Integer getQuotationCount() { return quotationCount; }
    public void setQuotationCount(Integer quotationCount) { this.quotationCount = quotationCount; }

    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public List<PmsInquiryAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsInquiryAuditLog> auditLogList) { this.auditLogList = auditLogList; }

    @Override
    public String toString() {
        return "PmsInquiry{" +
                "inquiryId=" + inquiryId +
                ", inquiryNo='" + inquiryNo + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", inquiryType='" + inquiryType + '\'' +
                ", purchaseType='" + purchaseType + '\'' +
                ", deadline=" + deadline +
                ", estimateAmount=" + estimateAmount +
                ", awardAmount=" + awardAmount +
                ", awardSupplierId=" + awardSupplierId +
                ", awardSupplierName='" + awardSupplierName + '\'' +
                ", description='" + description + '\'' +
                ", inquirerId=" + inquirerId +
                ", inquirerName='" + inquirerName + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", delFlag='" + delFlag + '\'' +
                ", detailList=" + detailList +
                ", quotationList=" + quotationList +
                '}';
    }
}
