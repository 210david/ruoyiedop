package com.ruoyi.mk.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 推广看板报表 数据层
 *
 * @author ruoyi
 */
public interface MkPromotionReportMapper
{
    /** KPI 汇总（日期范围） */
    public Map<String, Object> selectOverview(@Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /** 按渠道类型聚合效果 + 归因线索数 */
    public List<Map<String, Object>> selectChannelCompare(@Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /** 内容包效果排行 */
    public List<Map<String, Object>> selectContentRank(@Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /** 活动归因漏斗（渠道→点击→报名→线索→商机→成交） */
    public Map<String, Object> selectFunnel(@Param("activityId") Long activityId);

    /** 按日推广趋势（曝光/阅读/点赞/评论） */
    public List<Map<String, Object>> selectTrend(@Param("beginDate") String beginDate, @Param("endDate") String endDate);
}
