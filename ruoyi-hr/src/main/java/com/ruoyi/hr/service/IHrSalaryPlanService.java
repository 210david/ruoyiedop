package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSalaryPlan;

public interface IHrSalaryPlanService {
    public HrSalaryPlan selectHrSalaryPlanById(Long planId);
    public List<HrSalaryPlan> selectHrSalaryPlanList(HrSalaryPlan hrSalaryPlan);
    public int insertHrSalaryPlan(HrSalaryPlan hrSalaryPlan);
    public int updateHrSalaryPlan(HrSalaryPlan hrSalaryPlan);
    public int deleteHrSalaryPlanByIds(Long[] planIds);
    public int deleteHrSalaryPlanById(Long planId);
}
