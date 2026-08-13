package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * SQM→PMS 质量评分推送日志 qms_sqm_push_log
 *
 * 对应 PRD FR-SQM-06：QMS 质量评分计算完成后推送至 PMS 供综合评估引用
 */
public class QmsSqmPushLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** QMS质量评价ID */
    private Long evalId;

    /** 评价周期 */
    @Excel(name = "评价周期")
    private String evalPeriod;

    /** 质量评分 */
    @Excel(name = "质量评分")
    private BigDecimal qualityScore;

    /** 等级(A/B/C/D) */
    @Excel(name = "等级")
    private String grade;

    /** 合格率(%) */
    @Excel(name = "合格率(%)")
    private BigDecimal passRate;

    /** PPM */
    @Excel(name = "PPM")
    private BigDecimal ppm;

    /** 推送状态(0待推送 1成功 2失败) */
    @Excel(name = "推送状态", readConverterExp = "0=待推送,1=成功,2=失败")
    private String pushStatus;

    /** 推送时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "推送时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date pushTime;

    /** 重试次数 */
    @Excel(name = "重试次数")
    private Integer retryCount;

    /** 失败原因 */
    private String errorMsg;

    /** 删除标志 */
    private String delFlag;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getEvalId() { return evalId; }
    public void setEvalId(Long evalId) { this.evalId = evalId; }
    public String getEvalPeriod() { return evalPeriod; }
    public void setEvalPeriod(String evalPeriod) { this.evalPeriod = evalPeriod; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }
    public BigDecimal getPpm() { return ppm; }
    public void setPpm(BigDecimal ppm) { this.ppm = ppm; }
    public String getPushStatus() { return pushStatus; }
    public void setPushStatus(String pushStatus) { this.pushStatus = pushStatus; }
    public Date getPushTime() { return pushTime; }
    public void setPushTime(Date pushTime) { this.pushTime = pushTime; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
