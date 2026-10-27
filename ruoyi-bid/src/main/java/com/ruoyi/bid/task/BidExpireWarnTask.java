package com.ruoyi.bid.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.bid.service.IBidCommonService;

/**
 * 招投标-保证金/资质到期预警任务
 *
 * 调用方式（Quartz 定时任务配置）：
 * - Bean名称: bidExpireWarnTask
 * - 方法: checkAll
 * - cron建议: 0 30 8 * * ?（每天早上8点30分）
 */
@Component("bidExpireWarnTask")
public class BidExpireWarnTask
{
    private static final Logger log = LoggerFactory.getLogger(BidExpireWarnTask.class);

    @Autowired
    private IBidCommonService commonService;

    public void checkAll()
    {
        int overdue = commonService.checkDepositOverdue();
        int expiring = commonService.checkQualificationExpire();
        if (overdue > 0 || expiring > 0)
        {
            log.warn("保证金/资质预警完成：超期保证金 {} 笔，临期资质 {} 条", overdue, expiring);
        }
        else
        {
            log.info("保证金/资质预警完成：无超期或临期数据");
        }
    }
}
