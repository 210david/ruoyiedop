package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyTrainingPlan;

public interface ISafetyTrainingPlanService {
    public SafetyTrainingPlan selectSafetyTrainingPlanById(Long planId);
    public List<SafetyTrainingPlan> selectSafetyTrainingPlanList(SafetyTrainingPlan safetyTrainingPlan);
    public int insertSafetyTrainingPlan(SafetyTrainingPlan safetyTrainingPlan);
    public int updateSafetyTrainingPlan(SafetyTrainingPlan safetyTrainingPlan);
    public int deleteSafetyTrainingPlanByIds(Long[] planIds);
    public int deleteSafetyTrainingPlanById(Long planId);

    /** 开始执行培训计划：待执行(0) → 执行中(1) */
    public int startPlan(Long planId);

    /** 完成培训计划：执行中(1) → 已完成(2) */
    public int completePlan(Long planId);

    /** 取消培训计划：待执行(0)/执行中(1) → 已取消(3) */
    public int cancelPlan(Long planId);

    /** 统计计划关联的培训记录数 */
    public int countRecordsByPlanId(Long planId);

    /** 统计计划关联的参训人员数 */
    public int countAttendeesByPlanId(Long planId);
}