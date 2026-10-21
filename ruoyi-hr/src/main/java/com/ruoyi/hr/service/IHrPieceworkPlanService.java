package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrPieceworkPlan;

public interface IHrPieceworkPlanService {
    public HrPieceworkPlan selectHrPieceworkPlanById(Long pwPlanId);
    public List<HrPieceworkPlan> selectHrPieceworkPlanList(HrPieceworkPlan hrPieceworkPlan);
    public int insertHrPieceworkPlan(HrPieceworkPlan hrPieceworkPlan);
    public int updateHrPieceworkPlan(HrPieceworkPlan hrPieceworkPlan);
    public int deleteHrPieceworkPlanByIds(Long[] pwPlanIds);
    public int deleteHrPieceworkPlanById(Long pwPlanId);
}
