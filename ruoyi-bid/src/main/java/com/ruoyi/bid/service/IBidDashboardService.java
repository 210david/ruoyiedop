package com.ruoyi.bid.service;

import java.util.List;
import java.util.Map;

/**
 * 投标看板 Service 接口
 */
public interface IBidDashboardService
{
    public Map<String, Object> overview();

    public List<Map<String, Object>> statusDistribution();

    public List<Map<String, Object>> trend();

    public List<Map<String, Object>> failReason();
}
