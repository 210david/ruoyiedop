package com.ruoyi.bid.service;

import java.util.List;
import com.ruoyi.bid.domain.BidCrawlSource;

/**
 * 采集源管理 Service 接口
 */
public interface IBidCrawlSourceService
{
    public List<BidCrawlSource> selectSourceList(BidCrawlSource source);

    public BidCrawlSource selectSourceById(Long sourceId);

    public int insertSource(BidCrawlSource source);

    public int updateSource(BidCrawlSource source);

    public int deleteSourceByIds(Long[] sourceIds);
}
