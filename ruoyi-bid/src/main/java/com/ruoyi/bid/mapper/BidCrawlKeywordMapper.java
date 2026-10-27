package com.ruoyi.bid.mapper;

import java.util.List;
import com.ruoyi.bid.domain.BidCrawlKeyword;
import com.ruoyi.bid.domain.BidKeywordScope;

/**
 * 关键词订阅 Mapper 接口
 */
public interface BidCrawlKeywordMapper
{
    public List<BidCrawlKeyword> selectKeywordList(BidCrawlKeyword keyword);

    public BidCrawlKeyword selectKeywordById(Long keywordId);

    public List<BidCrawlKeyword> selectEnabledKeywords();

    public int insertKeyword(BidCrawlKeyword keyword);

    public int updateKeyword(BidCrawlKeyword keyword);

    public int deleteKeywordByIds(Long[] keywordIds);

    public List<BidKeywordScope> selectScopeByKeywordId(Long keywordId);

    public int insertScope(BidKeywordScope scope);

    public int deleteScopeByKeywordId(Long keywordId);
}
