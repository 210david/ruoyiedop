package com.ruoyi.bid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.bid.service.IBidDashboardService;

/**
 * 投标看板 Controller
 */
@RestController
@RequestMapping("/bid/dashboard")
public class BidDashboardController extends BaseController
{
    @Autowired
    private IBidDashboardService dashboardService;

    /** 概览统计 */
    @PreAuthorize("@ss.hasPermi('bid:dashboard:view')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(dashboardService.overview());
    }

    /** 状态分布 */
    @PreAuthorize("@ss.hasPermi('bid:dashboard:view')")
    @GetMapping("/statusDistribution")
    public AjaxResult statusDistribution()
    {
        return AjaxResult.success(dashboardService.statusDistribution());
    }

    /** 月度趋势 */
    @PreAuthorize("@ss.hasPermi('bid:dashboard:view')")
    @GetMapping("/trend")
    public AjaxResult trend()
    {
        return AjaxResult.success(dashboardService.trend());
    }

    /** 废标/未中标原因占比 */
    @PreAuthorize("@ss.hasPermi('bid:dashboard:view')")
    @GetMapping("/failReason")
    public AjaxResult failReason()
    {
        return AjaxResult.success(dashboardService.failReason());
    }
}
