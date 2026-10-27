package com.ruoyi.qms.service.impl;

import java.math.BigDecimal;
import org.springframework.context.ApplicationEvent;
import com.ruoyi.qms.domain.QmsSqmPushLog;

/**
 * SQM→PMS 质量评分推送事件
 *
 * QMS 发布此事件后，PMS 侧可通过以下方式监听（PMS 侧代码，本期不实现）：
 * <pre>
 * &#64;EventListener
 * public void onSqmScorePushed(SqmScorePushEvent event) {
 *     // 将质量评分写入 PMS 综合评估的质量维度
 *     Long supplierId = event.getSupplierId();
 *     BigDecimal qualityScore = event.getQualityScore();
 *     String grade = event.getGrade();
 *     // ... 更新 pms_supplier_eval 的 quality_score 字段
 * }
 * </pre>
 *
 * 对应 PRD FR-SQM-06 / 技术方案 IF-10
 */
public class SqmScorePushEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final Long supplierId;
    private final String supplierName;
    private final Long evalId;
    private final String evalPeriod;
    private final BigDecimal qualityScore;
    private final String grade;
    private final BigDecimal passRate;
    private final BigDecimal ppm;

    public SqmScorePushEvent(Object source, QmsSqmPushLog pushLog) {
        super(source);
        this.supplierId = pushLog.getSupplierId();
        this.supplierName = pushLog.getSupplierName();
        this.evalId = pushLog.getEvalId();
        this.evalPeriod = pushLog.getEvalPeriod();
        this.qualityScore = pushLog.getQualityScore();
        this.grade = pushLog.getGrade();
        this.passRate = pushLog.getPassRate();
        this.ppm = pushLog.getPpm();
    }

    public Long getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }
    public Long getEvalId() { return evalId; }
    public String getEvalPeriod() { return evalPeriod; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public String getGrade() { return grade; }
    public BigDecimal getPassRate() { return passRate; }
    public BigDecimal getPpm() { return ppm; }
}
