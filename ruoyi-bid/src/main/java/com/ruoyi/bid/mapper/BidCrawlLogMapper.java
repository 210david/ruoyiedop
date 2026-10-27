package com.ruoyi.bid.mapper;

import java.util.List;
import com.ruoyi.bid.domain.BidCrawlLog;

/**
 * 抓取日志 Mapper 接口
 */
public interface BidCrawlLogMapper
{
    public List<BidCrawlLog> selectLogList(BidCrawlLog log);

    public BidCrawlLog selectLogById(Long logId);

    public List<BidCrawlLog> selectRecentBySourceId(Long sourceId);

    public List<java.util.Map<String, Object>> selectStatusCounts(BidCrawlLog log);

    public int insertLog(BidCrawlLog log);
}
