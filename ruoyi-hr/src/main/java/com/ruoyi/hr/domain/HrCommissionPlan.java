package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 提成方案对象 biz_hr_commission_plan
 *
 * 需求三：提成规则配置（口径/类型/比例/阶梯/超额奖励）
 * MR-01 方案锁定后不可追溯修改（已生成核算表的方案自动锁定）
 */
public class HrCommissionPlan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 方案ID */
    private Long planId;

    /** 方案名称 */
    @Excel(name = "方案名称")
    private String planName;

    /** 计算口径（0回款口径 1签单口径） */
    @Excel(name = "计算口径", dictType = "hr_commission_base")
    private String calcBase;

    /** 类型（0固定比例 1阶梯 2超额奖励） */
    @Excel(name = "方案类型", dictType = "hr_commission_type")
    private String planType;

    /** 固定比例（%） */
    private BigDecimal baseRatio;

    /** 超额奖励起点 */
    private BigDecimal bonusThreshold;

    /** 超额部分奖励比例（%） */
    private BigDecimal bonusRatio;

    /** 状态（0草稿 1启用 2停用） */
    @Excel(name = "状态")
    private String planStatus;

    /** 是否锁定（MR-01：已生成核算表后锁定，不可追溯修改） */
    @Excel(name = "是否锁定")
    private String locked;

    /** 删除标志 */
    private String delFlag;

    /** 阶梯明细（编辑用） */
    private java.util.List<HrCommissionTier> tiers;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getCalcBase() { return calcBase; }
    public void setCalcBase(String calcBase) { this.calcBase = calcBase; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public BigDecimal getBaseRatio() { return baseRatio; }
    public void setBaseRatio(BigDecimal baseRatio) { this.baseRatio = baseRatio; }
    public BigDecimal getBonusThreshold() { return bonusThreshold; }
    public void setBonusThreshold(BigDecimal bonusThreshold) { this.bonusThreshold = bonusThreshold; }
    public BigDecimal getBonusRatio() { return bonusRatio; }
    public void setBonusRatio(BigDecimal bonusRatio) { this.bonusRatio = bonusRatio; }
    public String getPlanStatus() { return planStatus; }
    public void setPlanStatus(String planStatus) { this.planStatus = planStatus; }
    public String getLocked() { return locked; }
    public void setLocked(String locked) { this.locked = locked; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public java.util.List<HrCommissionTier> getTiers() { return tiers; }
    public void setTiers(java.util.List<HrCommissionTier> tiers) { this.tiers = tiers; }
}
