package com.ruoyi.bid.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.mapper.BidCrawlSourceMapper;
import com.ruoyi.bid.service.IBidCrawlSourceService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 采集源管理 Service 实现
 */
@Service
public class BidCrawlSourceServiceImpl implements IBidCrawlSourceService
{
    @Autowired
    private BidCrawlSourceMapper sourceMapper;

    @Override
    public List<BidCrawlSource> selectSourceList(BidCrawlSource source)
    {
        return sourceMapper.selectSourceList(source);
    }

    @Override
    public BidCrawlSource selectSourceById(Long sourceId)
    {
        return sourceMapper.selectSourceById(sourceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSource(BidCrawlSource source)
    {
        validate(source);
        if (source.getCrawlInterval() != null && source.getCrawlInterval() < 30)
        {
            throw new ServiceException("抓取间隔最小 30 分钟，请控制请求频率");
        }
        return sourceMapper.insertSource(source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSource(BidCrawlSource source)
    {
        validate(source);
        if (source.getCrawlInterval() != null && source.getCrawlInterval() < 30)
        {
            throw new ServiceException("抓取间隔最小 30 分钟，请控制请求频率");
        }
        return sourceMapper.updateSource(source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSourceByIds(Long[] sourceIds)
    {
        return sourceMapper.deleteSourceByIds(sourceIds);
    }

    private void validate(BidCrawlSource source)
    {
        if (StringUtils.isEmpty(source.getSourceName()))
        {
            throw new ServiceException("采集源名称不能为空");
        }
        if (StringUtils.isEmpty(source.getBaseUrl()))
        {
            throw new ServiceException("采集地址不能为空");
        }
        if (StringUtils.isEmpty(source.getParserCode()))
        {
            throw new ServiceException("解析器标识不能为空");
        }
        if (StringUtils.isNotEmpty(source.getParseConfig()))
        {
            try
            {
                com.alibaba.fastjson2.JSONObject.parseObject(source.getParseConfig());
            }
            catch (Exception e)
            {
                throw new ServiceException("解析配置不是合法的 JSON：" + e.getMessage());
            }
        }
    }
}
