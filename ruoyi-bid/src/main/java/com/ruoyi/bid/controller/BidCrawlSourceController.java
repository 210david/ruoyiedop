package com.ruoyi.bid.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.service.IBidCrawlService;
import com.ruoyi.bid.service.IBidCrawlSourceService;

/**
 * 采集源管理 Controller
 */
@RestController
@RequestMapping("/bid/source")
public class BidCrawlSourceController extends BaseController
{
    @Autowired
    private IBidCrawlSourceService sourceService;

    @Autowired
    private IBidCrawlService crawlService;

    @PreAuthorize("@ss.hasPermi('bid:source:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidCrawlSource source)
    {
        startPage();
        List<BidCrawlSource> list = sourceService.selectSourceList(source);
        return getDataTable(list);
    }

    @Log(title = "采集源", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bid:source:list')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BidCrawlSource source)
    {
        List<BidCrawlSource> list = sourceService.selectSourceList(source);
        ExcelUtil<BidCrawlSource> util = new ExcelUtil<>(BidCrawlSource.class);
        util.exportExcel(response, list, "采集源数据");
    }

    @PreAuthorize("@ss.hasPermi('bid:source:query')")
    @GetMapping("/{sourceId}")
    public AjaxResult getInfo(@PathVariable("sourceId") Long sourceId)
    {
        return AjaxResult.success(sourceService.selectSourceById(sourceId));
    }

    @Log(title = "采集源", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:source:add')")
    @PostMapping
    public AjaxResult add(@org.springframework.validation.annotation.Validated @RequestBody BidCrawlSource source)
    {
        return toAjax(sourceService.insertSource(source));
    }

    @Log(title = "采集源", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:source:edit')")
    @PutMapping
    public AjaxResult edit(@org.springframework.validation.annotation.Validated @RequestBody BidCrawlSource source)
    {
        return toAjax(sourceService.updateSource(source));
    }

    @Log(title = "采集源", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('bid:source:remove')")
    @DeleteMapping("/{sourceIds}")
    public AjaxResult remove(@PathVariable Long[] sourceIds)
    {
        return toAjax(sourceService.deleteSourceByIds(sourceIds));
    }

    /** 立即执行一次 */
    @Log(title = "采集源立即执行", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('bid:source:run')")
    @PostMapping("/run/{sourceId}")
    public AjaxResult run(@PathVariable("sourceId") Long sourceId)
    {
        Long logId = crawlService.crawlSource(sourceId, getUsername());
        return AjaxResult.success("执行完成", logId);
    }
}
