package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsSupplierEval extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long evalId;
    private Long supplierId;
    @Excel(name = "供应商名称") private String supplierName;
    @Excel(name = "评价周期") private String evalPeriod;
    @Excel(name = "评价类型") private String evalType;
    private Integer incomingTotal;
    private Integer incomingPass;
    @Excel(name = "PPM") private BigDecimal ppm;
    @Excel(name = "合格率(%)") private BigDecimal passRate;
    private BigDecimal qualityScore;
    private BigDecimal deliveryScore;
    private BigDecimal serviceScore;
    @Excel(name = "综合评分") private BigDecimal totalScore;
    @Excel(name = "等级") private String grade;
    private String evalStatus;
    private String delFlag;
    private String status;

    public Long getEvalId() { return evalId; }
    public void setEvalId(Long evalId) { this.evalId = evalId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getEvalPeriod() { return evalPeriod; }
    public void setEvalPeriod(String evalPeriod) { this.evalPeriod = evalPeriod; }
    public String getEvalType() { return evalType; }
    public void setEvalType(String evalType) { this.evalType = evalType; }
    public Integer getIncomingTotal() { return incomingTotal; }
    public void setIncomingTotal(Integer incomingTotal) { this.incomingTotal = incomingTotal; }
    public Integer getIncomingPass() { return incomingPass; }
    public void setIncomingPass(Integer incomingPass) { this.incomingPass = incomingPass; }
    public BigDecimal getPpm() { return ppm; }
    public void setPpm(BigDecimal ppm) { this.ppm = ppm; }
    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }
    public BigDecimal getDeliveryScore() { return deliveryScore; }
    public void setDeliveryScore(BigDecimal deliveryScore) { this.deliveryScore = deliveryScore; }
    public BigDecimal getServiceScore() { return serviceScore; }
    public void setServiceScore(BigDecimal serviceScore) { this.serviceScore = serviceScore; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getEvalStatus() { return evalStatus; }
    public void setEvalStatus(String evalStatus) { this.evalStatus = evalStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
