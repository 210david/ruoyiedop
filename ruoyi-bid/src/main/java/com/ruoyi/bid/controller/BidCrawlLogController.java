package com.ruoyi.bid.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.bid.domain.BidCrawlLog;
import com.ruoyi.bid.service.IBidCommonService;

/**
 * 抓取日志 Controller（只读）
 */
@RestController
@RequestMapping("/bid/crawlLog")
public class BidCrawlLogController extends BaseController
{
    @Autowired
    private IBidCommonService commonService;

    @PreAuthorize("@ss.hasPermi('bid:crawlLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidCrawlLog log)
    {
        startPage();
        List<BidCrawlLog> list = commonService.selectLogList(log);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bid:crawlLog:list')")
    @GetMapping("/statusCounts")
    public AjaxResult statusCounts(BidCrawlLog log)
    {
        return AjaxResult.success(commonService.selectLogStatusCounts(log));
    }

    @PreAuthorize("@ss.hasPermi('bid:crawlLog:query')")
    @GetMapping("/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return AjaxResult.success(commonService.selectLogById(logId));
    }
}
