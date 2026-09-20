package com.ruoyi.bid.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.bid.service.IBidCrawlService;

/**
 * 招投标-公告抓取定时任务
 *
 * 调用方式（Quartz 定时任务配置）：
 * - Bean名称: bidCrawlTask
 * - 方法: crawlAllSources
 * - cron建议: 0 0 0/2 * * ?（每2小时执行一次）
 */
@Component("bidCrawlTask")
public class BidCrawlTask
{
    private static final Logger log = LoggerFactory.getLogger(BidCrawlTask.class);

    @Autowired
    private IBidCrawlService crawlService;

    public void crawlAllSources()
    {
        int count = crawlService.crawlAllSources("system");
        log.info("招标公告抓取任务完成，共执行 {} 个采集源", count);
    }
}
