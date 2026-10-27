package com.ruoyi.bid.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.bid.domain.BidCrawlKeyword;
import com.ruoyi.bid.domain.BidKeywordScope;
import com.ruoyi.bid.mapper.BidCrawlKeywordMapper;
import com.ruoyi.bid.service.IBidCrawlKeywordService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 关键词订阅 Service 实现
 */
@Service
public class BidCrawlKeywordServiceImpl implements IBidCrawlKeywordService
{
    @Autowired
    private BidCrawlKeywordMapper keywordMapper;

    @Override
    public List<BidCrawlKeyword> selectKeywordList(BidCrawlKeyword keyword)
    {
        return keywordMapper.selectKeywordList(keyword);
    }

    @Override
    public BidCrawlKeyword selectKeywordById(Long keywordId)
    {
        BidCrawlKeyword keyword = keywordMapper.selectKeywordById(keywordId);
        if (keyword != null)
        {
            keyword.setScopeList(keywordMapper.selectScopeByKeywordId(keywordId));
        }
        return keyword;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertKeyword(BidCrawlKeyword keyword)
    {
        validate(keyword);
        int rows = keywordMapper.insertKeyword(keyword);
        saveScopes(keyword);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateKeyword(BidCrawlKeyword keyword)
    {
        validate(keyword);
        int rows = keywordMapper.updateKeyword(keyword);
        // 范围整体重建
        keywordMapper.deleteScopeByKeywordId(keyword.getKeywordId());
        saveScopes(keyword);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteKeywordByIds(Long[] keywordIds)
    {
        for (Long id : keywordIds)
        {
            keywordMapper.deleteScopeByKeywordId(id);
        }
        return keywordMapper.deleteKeywordByIds(keywordIds);
    }

    private void saveScopes(BidCrawlKeyword keyword)
    {
        if (keyword.getScopeList() != null)
        {
            for (BidKeywordScope scope : keyword.getScopeList())
            {
                scope.setKeywordId(keyword.getKeywordId());
                keywordMapper.insertScope(scope);
            }
        }
    }

    private void validate(BidCrawlKeyword keyword)
    {
        if (StringUtils.isEmpty(keyword.getKeyword()))
        {
            throw new ServiceException("关键词不能为空");
        }
    }
}
