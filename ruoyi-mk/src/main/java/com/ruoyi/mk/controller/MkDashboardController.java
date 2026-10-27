package com.ruoyi.mk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mk.service.IMkDashboardService;
import com.ruoyi.mk.service.IMkContactService;

/**
 * 营销管理统计分析 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/dashboard")
public class MkDashboardController extends BaseController
{
    @Autowired
    private IMkDashboardService mkDashboardService;

    @Autowired
    private IMkContactService mkContactService;

    /** 概览统计 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(mkDashboardService.selectOverview());
    }

    /** 线索转化漏斗 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/leadFunnel")
    public AjaxResult leadFunnel()
    {
        return AjaxResult.success(mkDashboardService.selectLeadFunnel());
    }

    /** 商机销售漏斗 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/opportunityPipeline")
    public AjaxResult opportunityPipeline()
    {
        return AjaxResult.success(mkDashboardService.selectOpportunityPipeline());
    }

    /** 客户行业分布 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/customerByIndustry")
    public AjaxResult customerByIndustry()
    {
        return AjaxResult.success(mkDashboardService.selectCustomerByIndustry());
    }

    /** 客户等级分布 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/customerByLevel")
    public AjaxResult customerByLevel()
    {
        return AjaxResult.success(mkDashboardService.selectCustomerByLevel());
    }

    /** 销售业绩排行 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/salesRanking")
    public AjaxResult salesRanking()
    {
        return AjaxResult.success(mkDashboardService.selectSalesRanking());
    }

    /** 合同/订单月度趋势 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/revenueTrend")
    public AjaxResult revenueTrend()
    {
        return AjaxResult.success(mkDashboardService.selectRevenueTrend());
    }

    /** 活动效果统计 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/activityResult")
    public AjaxResult activityResult()
    {
        return AjaxResult.success(mkDashboardService.selectActivityResult());
    }

    /** P3-14: 待跟进联系人提醒 */
    @PreAuthorize("@ss.hasPermi('marketing:contact:list')")
    @GetMapping("/followUpReminders")
    public AjaxResult followUpReminders()
    {
        return AjaxResult.success(mkContactService.selectFollowUpReminders(getUserId()));
    }

    /** 赢单输单分析 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/winLossAnalysis")
    public AjaxResult winLossAnalysis()
    {
        return AjaxResult.success(mkDashboardService.selectWinLossAnalysis());
    }

    /** 销售预测 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/salesForecast")
    public AjaxResult salesForecast()
    {
        return AjaxResult.success(mkDashboardService.selectSalesForecast());
    }

    /** 销售人员综合统计 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/salespersonOverview")
    public AjaxResult salespersonOverview()
    {
        return AjaxResult.success(mkDashboardService.selectSalespersonOverview());
    }

    /** 销售人员月度业绩趋势 */
    @PreAuthorize("@ss.hasPermi('marketing:report:view')")
    @GetMapping("/salespersonTrend")
    public AjaxResult salespersonTrend(@RequestParam("userId") Long userId)
    {
        return AjaxResult.success(mkDashboardService.selectSalespersonMonthlyTrend(userId));
    }
}
