package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsPmPlan;

public interface DmsPmPlanMapper
{
    public List<DmsPmPlan> selectPmPlanList(DmsPmPlan pmPlan);
    public DmsPmPlan selectPmPlanById(Long planId);
    public int insertPmPlan(DmsPmPlan pmPlan);
    public int updatePmPlan(DmsPmPlan pmPlan);
    public int deletePmPlanByIds(Long[] planIds);

    /** 查询所有到期的PM计划（用于定时自动生成工单） */
    public List<DmsPmPlan> selectExpiredPmPlans();
}
