package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyEmergencyPlan;

public interface SafetyEmergencyPlanMapper {
    public SafetyEmergencyPlan selectSafetyEmergencyPlanById(Long planId);
    public List<SafetyEmergencyPlan> selectSafetyEmergencyPlanList(SafetyEmergencyPlan safetyEmergencyPlan);
    public int insertSafetyEmergencyPlan(SafetyEmergencyPlan safetyEmergencyPlan);
    public int updateSafetyEmergencyPlan(SafetyEmergencyPlan safetyEmergencyPlan);
    public int deleteSafetyEmergencyPlanByIds(Long[] planIds);
    public int deleteSafetyEmergencyPlanById(Long planId);
}