package com.ruoyi.hr.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.hr.domain.vo.HrStatisticsVO;
import com.ruoyi.hr.mapper.HrStatisticsMapper;
import com.ruoyi.hr.service.IHrStatisticsService;

/**
 * 人力资源统计报表 服务实现
 *
 * @author ruoyi
 */
@Service
public class HrStatisticsServiceImpl implements IHrStatisticsService
{
    @Autowired
    private HrStatisticsMapper hrStatisticsMapper;

    /**
     * 概览统计
     */
    @Override
    public Map<String, Object> selectOverview()
    {
        return hrStatisticsMapper.selectOverview(new HrStatisticsVO());
    }

    /**
     * 在册人员按部门分布
     */
    @Override
    public List<HrStatisticsVO> selectHeadcountByOrg()
    {
        return hrStatisticsMapper.selectHeadcountByOrg();
    }

    /**
     * 人员流动（按月统计入职/离职），未指定年份取当年
     */
    @Override
    public List<HrStatisticsVO> selectFlowTrend(HrStatisticsVO query)
    {
        if (query.getParams() == null || query.getParams().get("year") == null
                || "".equals(query.getParams().get("year").toString()))
        {
            query.setParams(new java.util.HashMap<>());
            query.getParams().put("year", DateUtils.parseDateToStr("yyyy", DateUtils.getNowDate()));
        }
        return hrStatisticsMapper.selectFlowTrend(query);
    }

    /**
     * 出勤月报（按部门聚合），未指定年月取最近有数据月份
     */
    @Override
    public List<HrStatisticsVO> selectAttendanceByOrg(HrStatisticsVO query)
    {
        if (query.getYearMonth() == null || "".equals(query.getYearMonth()))
        {
            query.setYearMonth(DateUtils.parseDateToStr("yyyy-MM", DateUtils.getNowDate()));
        }
        return hrStatisticsMapper.selectAttendanceByOrg(query);
    }

    /**
     * 出勤趋势（按月），未指定年份取当年
     */
    @Override
    public List<HrStatisticsVO> selectAttendanceTrend(HrStatisticsVO query)
    {
        if (query.getParams() == null || query.getParams().get("year") == null
                || "".equals(query.getParams().get("year").toString()))
        {
            query.setParams(new java.util.HashMap<>());
            query.getParams().put("year", DateUtils.parseDateToStr("yyyy", DateUtils.getNowDate()));
        }
        return hrStatisticsMapper.selectAttendanceTrend(query);
    }

    /**
     * 人工成本（按月），未指定年份取当年
     */
    @Override
    public List<HrStatisticsVO> selectCostByMonth(HrStatisticsVO query)
    {
        if (query.getParams() == null || query.getParams().get("year") == null
                || "".equals(query.getParams().get("year").toString()))
        {
            query.setParams(new java.util.HashMap<>());
            query.getParams().put("year", DateUtils.parseDateToStr("yyyy", DateUtils.getNowDate()));
        }
        return hrStatisticsMapper.selectCostByMonth(query);
    }

    /**
     * 人工成本（按部门，指定月），未指定年月取最近有数据月份
     */
    @Override
    public List<HrStatisticsVO> selectCostByOrg(HrStatisticsVO query)
    {
        if (query.getYearMonth() == null || "".equals(query.getYearMonth()))
        {
            query.setYearMonth(DateUtils.parseDateToStr("yyyy-MM", DateUtils.getNowDate()));
        }
        return hrStatisticsMapper.selectCostByOrg(query);
    }

    /**
     * 薪酬发放台账（批次）
     */
    @Override
    public List<HrStatisticsVO> selectPayrollLedger(HrStatisticsVO query)
    {
        return hrStatisticsMapper.selectPayrollLedger(query);
    }
}
