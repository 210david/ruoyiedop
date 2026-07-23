package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkPaymentPlan;

/**
 * 回款计划 Service接口
 *
 * @author ruoyi
 */
public interface IMkPaymentPlanService
{
    List<MkPaymentPlan> selectPaymentPlanList(MkPaymentPlan paymentPlan);

    MkPaymentPlan selectPaymentPlanById(Long planId);

    int insertPaymentPlan(MkPaymentPlan paymentPlan);

    int updatePaymentPlan(MkPaymentPlan paymentPlan);

    int deletePaymentPlanByIds(Long[] planIds);

    /** 检查并标记逾期回款计划 */
    int checkOverduePayments();

    /** 回款统计：总计划金额、已回款金额、待回款金额、逾期金额 */
    java.util.Map<String, Object> selectPaymentStatistics();
}
