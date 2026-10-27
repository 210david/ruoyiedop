package com.ruoyi.bid.controller;

import java.util.List;
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
import com.ruoyi.bid.domain.BidCrawlKeyword;
import com.ruoyi.bid.service.IBidCrawlKeywordService;

/**
 * 关键词订阅管理 Controller
 */
@RestController
@RequestMapping("/bid/keyword")
public class BidCrawlKeywordController extends BaseController
{
    @Autowired
    private IBidCrawlKeywordService keywordService;

    @PreAuthorize("@ss.hasPermi('bid:keyword:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidCrawlKeyword keyword)
    {
        startPage();
        List<BidCrawlKeyword> list = keywordService.selectKeywordList(keyword);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('bid:keyword:query')")
    @GetMapping("/{keywordId}")
    public AjaxResult getInfo(@PathVariable("keywordId") Long keywordId)
    {
        return AjaxResult.success(keywordService.selectKeywordById(keywordId));
    }

    @Log(title = "关键词订阅", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:keyword:add')")
    @PostMapping
    public AjaxResult add(@org.springframework.validation.annotation.Validated @RequestBody BidCrawlKeyword keyword)
    {
        return toAjax(keywordService.insertKeyword(keyword));
    }

    @Log(title = "关键词订阅", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:keyword:edit')")
    @PutMapping
    public AjaxResult edit(@org.springframework.validation.annotation.Validated @RequestBody BidCrawlKeyword keyword)
    {
        return toAjax(keywordService.updateKeyword(keyword));
    }

    @Log(title = "关键词订阅", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('bid:keyword:remove')")
    @DeleteMapping("/{keywordIds}")
    public AjaxResult remove(@PathVariable Long[] keywordIds)
    {
        return toAjax(keywordService.deleteKeywordByIds(keywordIds));
    }
}
