package com.ruoyi.bi.task;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.bi.service.IBiMetricService;
import com.ruoyi.common.utils.DateUtils;

/**
 * BI指标快照定时任务
 *
 * 调用方式：在 Quartz 定时任务管理中配置
 * - Bean名称: biMetricSnapshotTask
 * - 快照方法: snapshotMetrics  cron建议: 0 0 1 * * ?（每日 01:00，落 T-1 数据）
 * - 对账方法: checkMetrics     cron建议: 0 30 1 * * ?（每日 01:30，对 T-1 对账）
 * - 预警方法: alertMetrics     cron建议: 0 10 1 * * ?（每日 01:10，快照后阈值预警推送）
 *
 * 幂等说明：两方法均按 metricDate 先删后写，可安全手动重跑。
 *
 * @author ruoyi
 */
@Component("biMetricSnapshotTask")
public class BiMetricSnapshotTask
{
    private static final Logger log = LoggerFactory.getLogger(BiMetricSnapshotTask.class);

    @Autowired
    private IBiMetricService biMetricService;

    /**
     * 生成指标日快照（T-1：每日 01:00 执行，快照日期 = 昨日）
     */
    public void snapshotMetrics()
    {
        Date metricDate = DateUtils.addDays(new Date(), -1);
        int count = biMetricService.snapshotMetrics(metricDate);
        if (count > 0)
        {
            log.info("BI指标快照任务完成，日期={}，写入{}个指标", metricDate, count);
        }
        else
        {
            log.warn("BI指标快照任务完成但无数据写入，日期={}（请检查是否已接入 IModuleStatsProvider 实现）", metricDate);
        }
    }

    /**
     * F7 阈值预警匹配（T-1：每日 01:10 执行，快照后按抑制规则推送站内预警）
     */
    public void alertMetrics()
    {
        Date metricDate = DateUtils.addDays(new Date(), -1);
        int triggered = biMetricService.alertMetrics(metricDate);
        if (triggered > 0)
        {
            log.info("BI指标预警任务完成，新触发{}条预警，日期={}（含抑制未推，详见 bi_metric_alert）",
                    triggered, metricDate);
        }
        else
        {
            log.info("BI指标预警任务完成，无新触发预警，日期={}", metricDate);
        }
    }

    /**
     * 指标对账（T-1：每日 01:30 执行，对昨日快照与实时值比对）
     */
    public void checkMetrics()
    {
        Date metricDate = DateUtils.addDays(new Date(), -1);
        int inconsistent = biMetricService.checkMetrics(metricDate);
        if (inconsistent > 0)
        {
            log.warn("BI指标对账任务发现{}个不一致指标，日期={}，详见 bi_metric_check_log", inconsistent, metricDate);
        }
        else
        {
            log.info("BI指标对账任务完成，全部一致，日期={}", metricDate);
        }
    }

    /**
     * 历史快照回补（PRD 开放问题 3）：从 T-{days} 逐日补齐快照
     *
     * 调用方式（Quartz 任务管理，Bean 名 biMetricSnapshotTask）：
     * - 方法: backfillMetrics  参数: days（如 180，从昨日往前回补 180 天）
     * - 复用 snapshotMetrics 幂等设计（同日先删后插），可安全手动重跑；
     * - 建议仅在上线初始化或口径变更后手动执行一次，不配置周期调度。
     *
     * @param days 回补天数（1~365，空默认 90）
     */
    public void backfillMetrics(Integer days)
    {
        int n = (days == null || days < 1) ? 90 : Math.min(days, 365);
        Date from = DateUtils.addDays(new Date(), -n);
        log.info("BI历史回补任务开始：天数={}（自 {} 起逐日补齐）", n, DateUtils.parseDateToStr("yyyy-MM-dd", from));
        int total = biMetricService.backfillMetrics(n);
        log.info("BI历史回补任务完成：累计写入快照行={}（逐日幂等）", total);
    }
}
