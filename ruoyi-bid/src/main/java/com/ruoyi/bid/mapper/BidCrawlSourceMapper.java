package com.ruoyi.bid.mapper;

import java.util.List;
import com.ruoyi.bid.domain.BidCrawlSource;

/**
 * 采集源配置 Mapper 接口
 */
public interface BidCrawlSourceMapper
{
    public List<BidCrawlSource> selectSourceList(BidCrawlSource source);

    public BidCrawlSource selectSourceById(Long sourceId);

    public List<BidCrawlSource> selectEnabledSources();

    public int insertSource(BidCrawlSource source);

    public int updateSource(BidCrawlSource source);

    public int deleteSourceByIds(Long[] sourceIds);
}
