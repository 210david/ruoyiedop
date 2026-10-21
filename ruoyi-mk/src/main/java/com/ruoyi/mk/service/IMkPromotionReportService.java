package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;

/**
 * 推广看板报表 Service接口
 *
 * @author ruoyi
 */
public interface IMkPromotionReportService
{
    /** KPI 汇总（日期范围） */
    public Map<String, Object> overview(String beginDate, String endDate);

    /** 按渠道类型聚合效果 + 归因线索数 */
    public List<Map<String, Object>> channelCompare(String beginDate, String endDate);

    /** 内容包效果排行 */
    public List<Map<String, Object>> contentRank(String beginDate, String endDate);

    /** 活动归因漏斗：渠道→点击→报名→线索→商机→成交 */
    public Map<String, Object> funnel(Long activityId);

    /** 按日推广趋势（曝光/阅读/点赞/评论） */
    public List<Map<String, Object>> trend(String beginDate, String endDate);
}
