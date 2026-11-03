package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 提成阶梯对象 biz_hr_commission_tier
 */
public class HrCommissionTier extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 阶梯ID */
    private Long tierId;

    /** 方案ID */
    private Long planId;

    /** 区间下限 */
    private BigDecimal minAmount;

    /** 区间上限（空=不限） */
    private BigDecimal maxAmount;

    /** 提成比例（%） */
    private BigDecimal ratio;

    public Long getTierId() { return tierId; }
    public void setTierId(Long tierId) { this.tierId = tierId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public BigDecimal getRatio() { return ratio; }
    public void setRatio(BigDecimal ratio) { this.ratio = ratio; }
}
