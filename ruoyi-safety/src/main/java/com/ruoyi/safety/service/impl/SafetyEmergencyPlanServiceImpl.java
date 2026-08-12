package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyEmergencyPlan;
import com.ruoyi.safety.mapper.SafetyEmergencyPlanMapper;
import com.ruoyi.safety.service.ISafetyEmergencyPlanService;

@Service
public class SafetyEmergencyPlanServiceImpl implements ISafetyEmergencyPlanService
{
    @Autowired
    private SafetyEmergencyPlanMapper safetyEmergencyPlanMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyEmergencyPlan selectSafetyEmergencyPlanById(Long planId) { return safetyEmergencyPlanMapper.selectSafetyEmergencyPlanById(planId); }

    @Override
    public List<SafetyEmergencyPlan> selectSafetyEmergencyPlanList(SafetyEmergencyPlan safetyEmergencyPlan) { return safetyEmergencyPlanMapper.selectSafetyEmergencyPlanList(safetyEmergencyPlan); }

    @Override
    public int insertSafetyEmergencyPlan(SafetyEmergencyPlan safetyEmergencyPlan) {
        if (StringUtils.isEmpty(safetyEmergencyPlan.getPlanCode())) {
            safetyEmergencyPlan.setPlanCode(mkNumberRuleService.generateNumber("safety_emergency_plan"));
        }
        return safetyEmergencyPlanMapper.insertSafetyEmergencyPlan(safetyEmergencyPlan); }

    @Override
    public int updateSafetyEmergencyPlan(SafetyEmergencyPlan safetyEmergencyPlan) { return safetyEmergencyPlanMapper.updateSafetyEmergencyPlan(safetyEmergencyPlan); }

    @Override
    public int deleteSafetyEmergencyPlanByIds(Long[] planIds) { return safetyEmergencyPlanMapper.deleteSafetyEmergencyPlanByIds(planIds); }

    @Override
    public int deleteSafetyEmergencyPlanById(Long planId) { return safetyEmergencyPlanMapper.deleteSafetyEmergencyPlanById(planId); }
}