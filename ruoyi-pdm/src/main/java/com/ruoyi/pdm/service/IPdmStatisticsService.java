package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmStatistics;

/**
 * 产品设计统计分析 Service 接口
 *
 * @author ruoyi
 */
public interface IPdmStatisticsService
{
    /**
     * 统计明细列表
     */
    public List<PdmStatistics> selectStatisticsList(PdmStatistics query);

    /**
     * 统计汇总
     */
    public PdmStatistics selectStatisticsSummary(PdmStatistics query);

    /**
     * 图表聚合数据（分类分布/状态分布/月度趋势/变更状态/产品产出TOP）
     */
    public java.util.Map<String, Object> selectChartsData();
}
