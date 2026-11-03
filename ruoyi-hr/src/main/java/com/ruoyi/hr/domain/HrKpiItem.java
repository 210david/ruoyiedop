package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * KPI考核指标对象 biz_hr_kpi_item
 *
 * 需求三：考核方案下的指标明细（权重合计应=100）
 */
public class HrKpiItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 指标ID */
    private Long itemId;

    /** 方案ID */
    private Long schemeId;

    /** 指标名称 */
    private String indicatorName;

    /** 权重（%） */
    private BigDecimal weight;

    /** 目标值 */
    private String targetValue;

    /** 评分规则说明 */
    private String scoreRule;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public String getIndicatorName() { return indicatorName; }
    public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    public String getScoreRule() { return scoreRule; }
    public void setScoreRule(String scoreRule) { this.scoreRule = scoreRule; }
}
