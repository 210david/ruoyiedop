package com.ruoyi.bid.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.bid.mapper.BidDepositMapper;
import com.ruoyi.bid.mapper.BidProjectMapper;
import com.ruoyi.bid.service.IBidDashboardService;

/**
 * 投标看板 Service 实现
 */
@Service
public class BidDashboardServiceImpl implements IBidDashboardService
{
    @Autowired
    private BidProjectMapper projectMapper;

    @Autowired
    private BidDepositMapper depositMapper;

    @Override
    public Map<String, Object> overview()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("project", projectMapper.selectOverview());
        result.put("deposit", depositMapper.selectOccupancySummary());
        return result;
    }

    @Override
    public List<Map<String, Object>> statusDistribution()
    {
        return projectMapper.selectStatusCount();
    }

    @Override
    public List<Map<String, Object>> trend()
    {
        return projectMapper.selectMonthlyTrend();
    }

    @Override
    public List<Map<String, Object>> failReason()
    {
        return projectMapper.selectFailReasonCount();
    }
}
