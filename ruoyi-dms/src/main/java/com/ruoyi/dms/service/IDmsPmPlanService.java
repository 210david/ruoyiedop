package com.ruoyi.dms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.dms.domain.DmsPmPlan;

public interface IDmsPmPlanService
{
    public List<DmsPmPlan> selectPmPlanList(DmsPmPlan pmPlan);
    public DmsPmPlan selectPmPlanById(Long planId);
    public int insertPmPlan(DmsPmPlan pmPlan);
    public int updatePmPlan(DmsPmPlan pmPlan);
    public int deletePmPlanByIds(Long[] planIds);

    /** 根据PM计划自动生成工单 */
    public int generateWorkOrder(Long planId);

    /** 定时任务：扫描到期PM计划并自动生成工单 */
    public int autoGeneratePmWorkOrders();

    /** 获取日历视图数据（按月统计PM计划到期情况） */
    public List<Map<String, Object>> selectPmPlanCalendar(String month);
}
