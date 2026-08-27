package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrSalaryPlan;
import com.ruoyi.hr.mapper.HrSalaryPlanMapper;
import com.ruoyi.hr.service.IHrSalaryPlanService;

@Service
public class HrSalaryPlanServiceImpl implements IHrSalaryPlanService
{
    @Autowired
    private HrSalaryPlanMapper hrSalaryPlanMapper;

    @Override
    public HrSalaryPlan selectHrSalaryPlanById(Long planId) { return hrSalaryPlanMapper.selectHrSalaryPlanById(planId); }

    @Override
    public List<HrSalaryPlan> selectHrSalaryPlanList(HrSalaryPlan hrSalaryPlan) { return hrSalaryPlanMapper.selectHrSalaryPlanList(hrSalaryPlan); }

    @Override
    public int insertHrSalaryPlan(HrSalaryPlan hrSalaryPlan) { return hrSalaryPlanMapper.insertHrSalaryPlan(hrSalaryPlan); }

    @Override
    public int updateHrSalaryPlan(HrSalaryPlan hrSalaryPlan) { return hrSalaryPlanMapper.updateHrSalaryPlan(hrSalaryPlan); }

    @Override
    public int deleteHrSalaryPlanByIds(Long[] planIds) { return hrSalaryPlanMapper.deleteHrSalaryPlanByIds(planIds); }

    @Override
    public int deleteHrSalaryPlanById(Long planId) { return hrSalaryPlanMapper.deleteHrSalaryPlanById(planId); }
}
