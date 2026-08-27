package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public class HrPayrollBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long batchId;
    @NotBlank(message = "批次编号不能为空")
    @Excel(name = "批次编号")
    private String batchNo;
    @NotBlank(message = "年月不能为空")
    @Excel(name = "年月")
    private String yearMonth;
    @Excel(name = "批次名称")
    private String batchName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发放日期")
    private Date payDate;
    @Excel(name = "发薪银行")
    private String bankName;
    @Excel(name = "总人数")
    private Integer totalCount;
    @Excel(name = "总金额")
    private BigDecimal totalAmount;
    @Excel(name = "审批状态")
    private String auditStatus;
    @Excel(name = "审批人")
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间")
    private Date auditTime;
    @Excel(name = "审批意见")
    private String auditRemark;
    @Excel(name = "银行回单号")
    private String bankReceiptNo;
    @Excel(name = "封账状态")
    private String sealedStatus;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public Date getPayDate() { return payDate; }
    public void setPayDate(Date payDate) { this.payDate = payDate; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getBankReceiptNo() { return bankReceiptNo; }
    public void setBankReceiptNo(String bankReceiptNo) { this.bankReceiptNo = bankReceiptNo; }
    public String getSealedStatus() { return sealedStatus; }
    public void setSealedStatus(String sealedStatus) { this.sealedStatus = sealedStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
