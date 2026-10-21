package com.ruoyi.bid.service;

import java.util.List;
import com.ruoyi.bid.domain.BidCrawlKeyword;
import com.ruoyi.bid.domain.BidCrawlLog;
import com.ruoyi.bid.domain.BidDeposit;
import com.ruoyi.bid.domain.BidQualification;

/**
 * 关键词订阅 / 抓取日志 / 保证金 / 资质 通用查询型 Service 接口
 */
public interface IBidCrawlKeywordService
{
    public List<BidCrawlKeyword> selectKeywordList(BidCrawlKeyword keyword);

    public BidCrawlKeyword selectKeywordById(Long keywordId);

    public int insertKeyword(BidCrawlKeyword keyword);

    public int updateKeyword(BidCrawlKeyword keyword);

    public int deleteKeywordByIds(Long[] keywordIds);
}
