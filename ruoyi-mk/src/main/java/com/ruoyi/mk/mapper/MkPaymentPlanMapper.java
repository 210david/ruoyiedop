package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkPaymentPlan;

public interface MkPaymentPlanMapper
{
    public List<MkPaymentPlan> selectPaymentPlanList(MkPaymentPlan paymentPlan);
    public MkPaymentPlan selectPaymentPlanById(Long planId);
    public int insertPaymentPlan(MkPaymentPlan paymentPlan);
    public int updatePaymentPlan(MkPaymentPlan paymentPlan);
    public int deletePaymentPlanByIds(Long[] planIds);
    /** 根据合同ID查询回款计划 */
    public List<MkPaymentPlan> selectPaymentPlanByContractId(Long contractId);
    /** 根据合同ID删除回款计划 */
    public int deletePaymentPlanByContractId(Long contractId);
    /** 查询已逾期但未回款的计划 */
    public List<MkPaymentPlan> selectOverduePaymentPlans();
}
