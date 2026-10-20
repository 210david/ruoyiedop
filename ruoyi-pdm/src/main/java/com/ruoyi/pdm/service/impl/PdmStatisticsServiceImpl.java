package com.ruoyi.pdm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.pdm.domain.PdmStatistics;
import com.ruoyi.pdm.mapper.PdmStatisticsMapper;
import com.ruoyi.pdm.service.IPdmStatisticsService;

/**
 * 产品设计统计分析 Service 实现
 *
 * @author ruoyi
 */
@Service
public class PdmStatisticsServiceImpl implements IPdmStatisticsService
{
    @Autowired
    private PdmStatisticsMapper statisticsMapper;

    @Override
    public List<PdmStatistics> selectStatisticsList(PdmStatistics query)
    {
        return statisticsMapper.selectStatisticsList(query);
    }

    @Override
    public PdmStatistics selectStatisticsSummary(PdmStatistics query)
    {
        return statisticsMapper.selectStatisticsSummary(query);
    }

    @Override
    public java.util.Map<String, Object> selectChartsData()
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("categoryDist", statisticsMapper.selectCategoryDist());
        result.put("statusDist", statisticsMapper.selectStatusDist());
        result.put("monthTrend", statisticsMapper.selectMonthTrend());
        result.put("changeStatusDist", statisticsMapper.selectChangeStatusDist());
        result.put("productTop", statisticsMapper.selectProductTop());
        return result;
    }
}
