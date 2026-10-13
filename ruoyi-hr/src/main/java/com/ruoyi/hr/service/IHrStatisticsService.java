package com.ruoyi.hr.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.hr.domain.vo.HrStatisticsVO;

/**
 * 人力资源统计报表 服务接口
 *
 * @author ruoyi
 */
public interface IHrStatisticsService
{
    /** 概览统计 */
    public Map<String, Object> selectOverview();

    /** 在册人员按部门分布 */
    public List<HrStatisticsVO> selectHeadcountByOrg();

    /** 人员流动（按月统计入职/离职） */
    public List<HrStatisticsVO> selectFlowTrend(HrStatisticsVO query);

    /** 出勤月报（按部门聚合） */
    public List<HrStatisticsVO> selectAttendanceByOrg(HrStatisticsVO query);

    /** 出勤趋势（按月） */
    public List<HrStatisticsVO> selectAttendanceTrend(HrStatisticsVO query);

    /** 人工成本（按月） */
    public List<HrStatisticsVO> selectCostByMonth(HrStatisticsVO query);

    /** 人工成本（按部门，指定月） */
    public List<HrStatisticsVO> selectCostByOrg(HrStatisticsVO query);

    /** 薪酬发放台账（批次） */
    public List<HrStatisticsVO> selectPayrollLedger(HrStatisticsVO query);
}
