package com.ruoyi.mk.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.mk.service.IMkPaymentPlanService;

/**
 * 回款定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: mkPaymentTask
 * - 方法: checkOverduePayments
 * - cron表达式建议: 0 0 8 * * ? （每天早上8点执行）
 *
 * @author ruoyi
 */
@Component("mkPaymentTask")
public class MkPaymentTask
{
    private static final Logger log = LoggerFactory.getLogger(MkPaymentTask.class);

    @Autowired
    private IMkPaymentPlanService mkPaymentPlanService;

    /**
     * 检查并标记逾期回款计划
     * 将计划回款日期已过但未完成回款的计划标记为"已逾期"
     */
    public void checkOverduePayments()
    {
        int count = mkPaymentPlanService.checkOverduePayments();
        if (count > 0)
        {
            log.warn("回款逾期检查完成，共发现{}条逾期回款计划", count);
        }
        else
        {
            log.info("回款逾期检查完成，无逾期回款计划");
        }
    }
}
