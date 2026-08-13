package com.ruoyi.safety.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.safety.service.*;

@RestController
@RequestMapping("/safety/dashboard")
public class SafetyDashboardController extends BaseController
{
    @Autowired
    private ISafetyHazardService safetyHazardService;
    @Autowired
    private ISafetyRiskPointService safetyRiskPointService;
    @Autowired
    private ISafetyRemindService safetyRemindService;
    @Autowired
    private com.ruoyi.safety.service.ISafetyMaterialService safetyMaterialService;

    @PreAuthorize("@ss.hasPermi('safety:dashboard:list')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Map<String, Object> data = new HashMap<>();
        // 隐患统计
        data.put("hazardTotal", safetyHazardService.countHazardsByStatus(null));
        data.put("hazardPending", safetyHazardService.countHazardsByStatus("1"));
        data.put("hazardRectifying", safetyHazardService.countHazardsByStatus("2"));
        data.put("hazardVerifying", safetyHazardService.countHazardsByStatus("3"));
        data.put("hazardClosed", safetyHazardService.countHazardsByStatus("4"));
        data.put("hazardOverdue", safetyHazardService.countHazardsByStatus("5"));
        // 风险统计
        data.put("riskRed", safetyRiskPointService.countByRiskLevel("4"));
        data.put("riskOrange", safetyRiskPointService.countByRiskLevel("3"));
        data.put("riskYellow", safetyRiskPointService.countByRiskLevel("2"));
        data.put("riskBlue", safetyRiskPointService.countByRiskLevel("1"));
        // 到期提醒统计
        int remindUnread = safetyRemindService.countByStatus("0");
        int remindRead = safetyRemindService.countByStatus("1");
        data.put("remindUnread", remindUnread);
        data.put("remindRead", remindRead);
        data.put("remindPending", remindUnread + remindRead);
        data.put("remindHandled", safetyRemindService.countByStatus("2"));
        // 危化品库存预警统计
        List<com.ruoyi.safety.domain.SafetyMaterial> stockAlertList = safetyMaterialService.selectStockAlertList();
        data.put("materialStockAlert", stockAlertList.size());
        data.put("materialStockAlertList", stockAlertList);
        return AjaxResult.success(data);
    }

    @PreAuthorize("@ss.hasPermi('safety:dashboard:list')")
    @GetMapping("/chartData")
    public AjaxResult chartData()
    {
        Map<String, Object> data = new HashMap<>();
        // 隐患趋势（近6个月）
        data.put("hazardTrend", safetyHazardService.getHazardTrendByMonth());
        // 隐患按类型统计
        data.put("hazardByType", safetyHazardService.getHazardCountByType());
        // 隐患按等级统计
        data.put("hazardByLevel", safetyHazardService.getHazardCountByLevel());
        // 隐患按来源统计
        data.put("hazardBySource", safetyHazardService.getHazardCountBySource());
        // 隐患按部门统计（Top 5）
        data.put("hazardByDept", safetyHazardService.getHazardCountByDept());
        // 到期提醒按类型统计
        data.put("remindByType", safetyRemindService.getRemindCountByType());
        // 即将到期提醒列表
        data.put("upcomingReminds", safetyRemindService.getUpcomingReminds());
        return AjaxResult.success(data);
    }
}
