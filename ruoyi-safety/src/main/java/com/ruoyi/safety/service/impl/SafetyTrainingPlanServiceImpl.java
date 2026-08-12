package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyTrainingPlan;
import com.ruoyi.safety.mapper.SafetyTrainingPlanMapper;
import com.ruoyi.safety.service.ISafetyTrainingPlanService;

@Service
public class SafetyTrainingPlanServiceImpl implements ISafetyTrainingPlanService
{
    @Autowired
    private SafetyTrainingPlanMapper safetyTrainingPlanMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyTrainingPlan selectSafetyTrainingPlanById(Long planId) { return safetyTrainingPlanMapper.selectSafetyTrainingPlanById(planId); }

    @Override
    public List<SafetyTrainingPlan> selectSafetyTrainingPlanList(SafetyTrainingPlan safetyTrainingPlan) { return safetyTrainingPlanMapper.selectSafetyTrainingPlanList(safetyTrainingPlan); }

    @Override
    public int insertSafetyTrainingPlan(SafetyTrainingPlan safetyTrainingPlan) {
        if (StringUtils.isEmpty(safetyTrainingPlan.getPlanCode())) {
            safetyTrainingPlan.setPlanCode(mkNumberRuleService.generateNumber("safety_training_plan"));
        }
        return safetyTrainingPlanMapper.insertSafetyTrainingPlan(safetyTrainingPlan); }

    @Override
    public int updateSafetyTrainingPlan(SafetyTrainingPlan safetyTrainingPlan) { return safetyTrainingPlanMapper.updateSafetyTrainingPlan(safetyTrainingPlan); }

    @Override
    public int deleteSafetyTrainingPlanByIds(Long[] planIds) { return safetyTrainingPlanMapper.deleteSafetyTrainingPlanByIds(planIds); }

    @Override
    public int deleteSafetyTrainingPlanById(Long planId) { return safetyTrainingPlanMapper.deleteSafetyTrainingPlanById(planId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startPlan(Long planId)
    {
        SafetyTrainingPlan plan = safetyTrainingPlanMapper.selectSafetyTrainingPlanById(planId);
        if (plan == null)
        {
            throw new ServiceException("培训计划不存在");
        }
        if (!"0".equals(plan.getPlanStatus()))
        {
            throw new ServiceException("只有待执行状态的培训计划才能开始执行");
        }
        if (StringUtils.isEmpty(plan.getPlanName()))
        {
            throw new ServiceException("计划名称不能为空，请完善后开始执行");
        }
        return safetyTrainingPlanMapper.updatePlanStatus(planId, "1", SecurityUtils.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completePlan(Long planId)
    {
        SafetyTrainingPlan plan = safetyTrainingPlanMapper.selectSafetyTrainingPlanById(planId);
        if (plan == null)
        {
            throw new ServiceException("培训计划不存在");
        }
        if (!"1".equals(plan.getPlanStatus()))
        {
            throw new ServiceException("只有执行中状态的培训计划才能标记完成");
        }
        // ===== 完成前置校验：计划必须有实际培训记录 =====
        int recordCount = safetyTrainingPlanMapper.countRecordsByPlanId(planId);
        if (recordCount == 0)
        {
            throw new ServiceException("该计划尚未关联任何培训记录，无法完成。请先新增培训记录并完成签到后再标记计划完成。");
        }
        int attendeeCount = safetyTrainingPlanMapper.countAttendeesByPlanId(planId);
        if (attendeeCount == 0)
        {
            throw new ServiceException("该计划的培训记录尚无参训人员签到，无法完成。请先完成人员签到后再标记计划完成。");
        }
        return safetyTrainingPlanMapper.updatePlanStatus(planId, "2", SecurityUtils.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelPlan(Long planId)
    {
        SafetyTrainingPlan plan = safetyTrainingPlanMapper.selectSafetyTrainingPlanById(planId);
        if (plan == null)
        {
            throw new ServiceException("培训计划不存在");
        }
        if (!"0".equals(plan.getPlanStatus()) && !"1".equals(plan.getPlanStatus()))
        {
            throw new ServiceException("只有待执行或执行中状态的培训计划才能取消");
        }
        return safetyTrainingPlanMapper.updatePlanStatus(planId, "3", SecurityUtils.getUsername());
    }

    @Override
    public int countRecordsByPlanId(Long planId)
    {
        return safetyTrainingPlanMapper.countRecordsByPlanId(planId);
    }

    @Override
    public int countAttendeesByPlanId(Long planId)
    {
        return safetyTrainingPlanMapper.countAttendeesByPlanId(planId);
    }
}