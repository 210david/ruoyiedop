package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商评估表 pms_supplier_eval
 *
 * @author ruoyi
 */
public class PmsSupplierEval extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评估ID */
    private Long evalId;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 评估周期 */
    @Excel(name = "评估周期")
    private String evalPeriod;

    /** 评估日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "评估日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date evalDate;

    /** 交货准时率评分 */
    @Excel(name = "交货评分")
    private BigDecimal deliveryScore;

    /** 交货评分权重(%) */
    @Excel(name = "交货权重")
    private BigDecimal deliveryWeight;

    /** 交货单项得分 */
    @Excel(name = "交货得分")
    private BigDecimal deliveryItemScore;

    /** 质量合格率评分 */
    @Excel(name = "质量评分")
    private BigDecimal qualityScore;

    /** 质量评分权重(%) */
    @Excel(name = "质量权重")
    private BigDecimal qualityWeight;

    /** 质量单项得分 */
    @Excel(name = "质量得分")
    private BigDecimal qualityItemScore;

    /** 价格竞争力评分 */
    @Excel(name = "价格评分")
    private BigDecimal priceScore;

    /** 价格评分权重(%) */
    @Excel(name = "价格权重")
    private BigDecimal priceWeight;

    /** 价格单项得分 */
    @Excel(name = "价格得分")
    private BigDecimal priceItemScore;

    /** 服务评分 */
    @Excel(name = "服务评分")
    private BigDecimal serviceScore;

    /** 服务评分权重(%) */
    @Excel(name = "服务权重")
    private BigDecimal serviceWeight;

    /** 服务单项得分 */
    @Excel(name = "服务得分")
    private BigDecimal serviceItemScore;

    /** 综合评分 */
    @Excel(name = "综合评分")
    private BigDecimal totalScore;

    /** 评估等级（字典 pms_eval_level） */
    @Excel(name = "评估等级")
    private String evalLevel;

    /** 状态（0待审核 1已审核） */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=已审核")
    private String status;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 评估人ID */
    private Long evaluatorId;

    /** 评估人姓名 */
    @Excel(name = "评估人")
    private String evaluatorName;

    /** 删除标志 */
    private String delFlag;

    /** 审核日志列表 */
    private List<PmsSupplierEvalAuditLog> auditLogList;

    public Long getEvalId() { return evalId; }
    public void setEvalId(Long evalId) { this.evalId = evalId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getEvalPeriod() { return evalPeriod; }
    public void setEvalPeriod(String evalPeriod) { this.evalPeriod = evalPeriod; }
    public Date getEvalDate() { return evalDate; }
    public void setEvalDate(Date evalDate) { this.evalDate = evalDate; }
    public BigDecimal getDeliveryScore() { return deliveryScore; }
    public void setDeliveryScore(BigDecimal deliveryScore) { this.deliveryScore = deliveryScore; }
    public BigDecimal getDeliveryWeight() { return deliveryWeight; }
    public void setDeliveryWeight(BigDecimal deliveryWeight) { this.deliveryWeight = deliveryWeight; }
    public BigDecimal getDeliveryItemScore() { return deliveryItemScore; }
    public void setDeliveryItemScore(BigDecimal deliveryItemScore) { this.deliveryItemScore = deliveryItemScore; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }
    public BigDecimal getQualityWeight() { return qualityWeight; }
    public void setQualityWeight(BigDecimal qualityWeight) { this.qualityWeight = qualityWeight; }
    public BigDecimal getQualityItemScore() { return qualityItemScore; }
    public void setQualityItemScore(BigDecimal qualityItemScore) { this.qualityItemScore = qualityItemScore; }
    public BigDecimal getPriceScore() { return priceScore; }
    public void setPriceScore(BigDecimal priceScore) { this.priceScore = priceScore; }
    public BigDecimal getPriceWeight() { return priceWeight; }
    public void setPriceWeight(BigDecimal priceWeight) { this.priceWeight = priceWeight; }
    public BigDecimal getPriceItemScore() { return priceItemScore; }
    public void setPriceItemScore(BigDecimal priceItemScore) { this.priceItemScore = priceItemScore; }
    public BigDecimal getServiceScore() { return serviceScore; }
    public void setServiceScore(BigDecimal serviceScore) { this.serviceScore = serviceScore; }
    public BigDecimal getServiceWeight() { return serviceWeight; }
    public void setServiceWeight(BigDecimal serviceWeight) { this.serviceWeight = serviceWeight; }
    public BigDecimal getServiceItemScore() { return serviceItemScore; }
    public void setServiceItemScore(BigDecimal serviceItemScore) { this.serviceItemScore = serviceItemScore; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getEvalLevel() { return evalLevel; }
    public void setEvalLevel(String evalLevel) { this.evalLevel = evalLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public Long getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsSupplierEvalAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsSupplierEvalAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
