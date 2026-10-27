package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmStatistics;

/**
 * 产品设计统计分析 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmStatisticsMapper
{
    /**
     * 产品设计统计明细（按产品聚合方案/图纸/BOM/变更单数量）
     */
    public List<PdmStatistics> selectStatisticsList(PdmStatistics query);

    /**
     * 产品设计统计汇总（筛选条件下各类单据总数）
     */
    public PdmStatistics selectStatisticsSummary(PdmStatistics query);

    /** 产品分类分布（饼图） */
    public List<java.util.Map<String, Object>> selectCategoryDist();

    /** 产品状态分布（环形图） */
    public List<java.util.Map<String, Object>> selectStatusDist();

    /** 近12月设计文档月度新增趋势（折线图） */
    public List<java.util.Map<String, Object>> selectMonthTrend();

    /** 变更单状态分布（柱状图） */
    public List<java.util.Map<String, Object>> selectChangeStatusDist();

    /** 产品设计产出 TOP10（横向柱状图） */
    public List<java.util.Map<String, Object>> selectProductTop();
}
