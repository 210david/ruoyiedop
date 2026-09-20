package com.ruoyi.bid.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.bid.domain.BidNotice;

/**
 * 采集服务接口（抓取/匹配/推送）
 */
public interface IBidCrawlService
{
    /** 抓取单个采集源，返回日志ID */
    public Long crawlSource(Long sourceId, String operName);

    /** 抓取所有启用采集源（定时任务入口），返回执行源数量 */
    public int crawlAllSources(String operName);

    /** 关键词匹配并推送，返回命中条数 */
    public int matchAndPush(BidNotice notice);

    /** 看板-公告统计 */
    public Map<String, Object> noticeStats();

    /** 看板-公告月度趋势 */
    public List<Map<String, Object>> noticeTrend();
}
