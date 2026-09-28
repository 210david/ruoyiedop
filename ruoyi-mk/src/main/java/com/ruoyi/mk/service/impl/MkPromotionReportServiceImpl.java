package com.ruoyi.mk.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.mapper.MkPromotionReportMapper;
import com.ruoyi.mk.service.IMkPromotionReportService;

/**
 * 推广看板报表 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkPromotionReportServiceImpl implements IMkPromotionReportService
{
    @Autowired
    private MkPromotionReportMapper mkPromotionReportMapper;

    @Override
    public Map<String, Object> overview(String beginDate, String endDate)
    {
        return mkPromotionReportMapper.selectOverview(beginDate, endDate);
    }

    @Override
    public List<Map<String, Object>> channelCompare(String beginDate, String endDate)
    {
        return mkPromotionReportMapper.selectChannelCompare(beginDate, endDate);
    }

    @Override
    public List<Map<String, Object>> contentRank(String beginDate, String endDate)
    {
        return mkPromotionReportMapper.selectContentRank(beginDate, endDate);
    }

    @Override
    public Map<String, Object> funnel(Long activityId)
    {
        return mkPromotionReportMapper.selectFunnel(activityId);
    }

    @Override
    public List<Map<String, Object>> trend(String beginDate, String endDate)
    {
        return mkPromotionReportMapper.selectTrend(beginDate, endDate);
    }
}
