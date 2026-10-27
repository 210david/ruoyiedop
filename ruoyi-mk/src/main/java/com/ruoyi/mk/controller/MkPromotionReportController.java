package com.ruoyi.mk.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mk.service.IMkPromotionReportService;

/**
 * 推广看板报表 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/promotionReport")
public class MkPromotionReportController extends BaseController
{
    @Autowired
    private IMkPromotionReportService mkPromotionReportService;

    /** KPI 汇总 */
    @PreAuthorize("@ss.hasPermi('marketing:promotionReport:view')")
    @GetMapping("/overview")
    public AjaxResult overview(@RequestParam(required = false) String beginDate, @RequestParam(required = false) String endDate)
    {
        return AjaxResult.success(mkPromotionReportService.overview(beginDate, endDate));
    }

    /** 渠道对比 */
    @PreAuthorize("@ss.hasPermi('marketing:promotionReport:view')")
    @GetMapping("/channelCompare")
    public AjaxResult channelCompare(@RequestParam(required = false) String beginDate, @RequestParam(required = false) String endDate)
    {
        return AjaxResult.success(mkPromotionReportService.channelCompare(beginDate, endDate));
    }

    /** 内容排行 */
    @PreAuthorize("@ss.hasPermi('marketing:promotionReport:view')")
    @GetMapping("/contentRank")
    public AjaxResult contentRank(@RequestParam(required = false) String beginDate, @RequestParam(required = false) String endDate)
    {
        return AjaxResult.success(mkPromotionReportService.contentRank(beginDate, endDate));
    }

    /** 活动归因漏斗 */
    @PreAuthorize("@ss.hasPermi('marketing:promotionReport:view')")
    @GetMapping("/funnel")
    public AjaxResult funnel(@RequestParam Long activityId)
    {
        return AjaxResult.success(mkPromotionReportService.funnel(activityId));
    }

    /** 按日推广趋势 */
    @PreAuthorize("@ss.hasPermi('marketing:promotionReport:view')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(required = false) String beginDate, @RequestParam(required = false) String endDate)
    {
        return AjaxResult.success(mkPromotionReportService.trend(beginDate, endDate));
    }
}
