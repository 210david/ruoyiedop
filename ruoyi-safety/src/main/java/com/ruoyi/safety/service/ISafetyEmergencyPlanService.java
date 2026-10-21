package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyEmergencyPlan;

public interface ISafetyEmergencyPlanService {
    public SafetyEmergencyPlan selectSafetyEmergencyPlanById(Long planId);
    public List<SafetyEmergencyPlan> selectSafetyEmergencyPlanList(SafetyEmergencyPlan safetyEmergencyPlan);
    public int insertSafetyEmergencyPlan(SafetyEmergencyPlan safetyEmergencyPlan);
    public int updateSafetyEmergencyPlan(SafetyEmergencyPlan safetyEmergencyPlan);
    public int deleteSafetyEmergencyPlanByIds(Long[] planIds);
    public int deleteSafetyEmergencyPlanById(Long planId);
}