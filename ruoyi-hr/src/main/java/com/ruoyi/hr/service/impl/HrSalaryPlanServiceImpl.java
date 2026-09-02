package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrSalaryPlan;
import com.ruoyi.hr.mapper.HrSalaryPlanMapper;
import com.ruoyi.hr.service.IHrSalaryPlanService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrSalaryPlanServiceImpl implements IHrSalaryPlanService
{
    @Autowired
    private HrSalaryPlanMapper hrSalaryPlanMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrSalaryPlan selectHrSalaryPlanById(Long planId) { return hrSalaryPlanMapper.selectHrSalaryPlanById(planId); }

    @Override
    public List<HrSalaryPlan> selectHrSalaryPlanList(HrSalaryPlan hrSalaryPlan) { return hrSalaryPlanMapper.selectHrSalaryPlanList(hrSalaryPlan); }

    @Override
    public int insertHrSalaryPlan(HrSalaryPlan hrSalaryPlan)
    {
        if (StringUtils.isEmpty(hrSalaryPlan.getPlanNo()))
        {
            hrSalaryPlan.setPlanNo(mkNumberRuleService.generateNumber("hr_salary_plan"));
        }
        return hrSalaryPlanMapper.insertHrSalaryPlan(hrSalaryPlan);
    }

    @Override
    public int updateHrSalaryPlan(HrSalaryPlan hrSalaryPlan) { return hrSalaryPlanMapper.updateHrSalaryPlan(hrSalaryPlan); }

    @Override
    public int deleteHrSalaryPlanByIds(Long[] planIds) { return hrSalaryPlanMapper.deleteHrSalaryPlanByIds(planIds); }

    @Override
    public int deleteHrSalaryPlanById(Long planId) { return hrSalaryPlanMapper.deleteHrSalaryPlanById(planId); }
}
