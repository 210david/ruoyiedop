package com.ruoyi.ai.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.domain.AiCallLog;
import com.ruoyi.ai.mapper.AiCallLogMapper;
import com.ruoyi.ai.service.IAiAuditService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI调用审计 Service实现
 *
 * @author ruoyi
 */
@Service
public class AiAuditServiceImpl implements IAiAuditService
{
    @Autowired
    private AiCallLogMapper logMapper;

    @Override
    public int insertLog(AiCallLog log)
    {
        // 自动填充当前登录人（定时任务/异步等无登录上下文时跳过）
        try
        {
            log.setUserId(SecurityUtils.getUserId());
            log.setUserName(SecurityUtils.getUsername());
        }
        catch (Exception ignore) {}
        // SSE等异步线程无SecurityContext：使用入口线程显式捕获透传的用户上下文
        if (log.getUserId() == null && AiClient.getAsyncUserId() != null)
        {
            log.setUserId(AiClient.getAsyncUserId());
            log.setUserName(AiClient.getAsyncUserName());
        }
        if (StringUtils.isEmpty(log.getUserName()))
        {
            log.setUserName("系统");
        }
        log.setCallTime(DateUtils.getNowDate());
        return logMapper.insertLog(log);
    }

    @Override
    public List<AiCallLog> selectLogList(AiCallLog log)
    {
        return logMapper.selectLogList(log);
    }

    @Override
    public Map<String, Object> selectUsageStat(int days)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("days", days);
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", logMapper.selectTotalStat(params));
        stat.put("moduleStat", logMapper.selectModuleStat(params));
        stat.put("featureStat", logMapper.selectFeatureStat(params));
        stat.putAll(selectUsageRichStat(days));
        return stat;
    }

    @Override
    public Map<String, Object> selectUsageRichStat(int days)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("days", days);
        Map<String, Object> stat = new HashMap<>();
        stat.put("dailyTrend", logMapper.selectDailyTrend(params));
        stat.put("userStat", logMapper.selectUserStat(params));
        stat.put("hourStat", logMapper.selectHourStat(params));
        stat.put("statusStat", logMapper.selectStatusStat(params));
        stat.put("moduleDaily", logMapper.selectModuleDailyStat(params));
        stat.put("alerts", buildAlerts(days));
        return stat;
    }

    /**
     * 异常预警检测（近N天）：
     * 1. 高失败/降级率（可能是网络中断、配置错误）
     * 2. 模型抖动（平均耗时波动大，耗时骤增）
     * 3. 人员过量使用（单日或单期调用量异常偏高）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildAlerts(int days)
    {
        List<Map<String, Object>> alerts = new java.util.ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("days", days);

        // 1. 失败率/降级率预警
        Map<String, Object> total = logMapper.selectTotalStat(params);
        long totalCnt = toLong(total != null ? total.get("total") : 0);
        long failedCnt = toLong(total != null ? total.get("failed") : 0);
        long degradedCnt = toLong(total != null ? total.get("degraded") : 0);
        if (totalCnt >= 20)
        {
            double failRate = (failedCnt + degradedCnt) * 100.0 / totalCnt;
            if (failRate >= 30)
            {
                alerts.add(alert("error", "服务异常",
                        String.format("近%d天失败+降级率达 %.0f%%（失败%d/降级%d/共%d），可能存在网络中断或AI配置异常，请检查AI中心配置与服务状态",
                                days, failRate, failedCnt, degradedCnt, totalCnt)));
            }
            else if (failRate >= 15)
            {
                alerts.add(alert("warning", "稳定性波动",
                        String.format("近%d天失败+降级率 %.0f%%，略高于正常水平，建议关注", days, failRate)));
            }
        }

        // 2. 模型抖动：按天平均耗时，最大值超过均值2倍且样本充足
        List<Map<String, Object>> trend = logMapper.selectDailyTrend(params);
        if (trend != null && trend.size() >= 3)
        {
            double sum = 0; int n = 0;
            for (Map<String, Object> d : trend)
            {
                Long avg = toLongObj(d.get("avgDuration"));
                if (avg != null && avg > 0) { sum += avg; n++; }
            }
            if (n >= 3)
            {
                double mean = sum / n;
                for (Map<String, Object> d : trend)
                {
                    Long avg = toLongObj(d.get("avgDuration"));
                    Long dayTotal = toLongObj(d.get("total"));
                    if (avg != null && dayTotal != null && avg > mean * 2 && mean > 0 && dayTotal >= 5)
                    {
                        alerts.add(alert("warning", "模型响应抖动",
                                String.format("%s 平均耗时 %dms，显著高于期间均值 %.0fms（调用%d次），可能存在模型服务波动或网络延迟",
                                        d.get("date"), avg, mean, dayTotal)));
                        break;
                    }
                }
            }
        }

        // 3. 人员过量使用：单人调用量超过全体日均的10倍 或 单人占比超50%
        List<Map<String, Object>> users = logMapper.selectUserStat(params);
        if (users != null && !users.isEmpty() && totalCnt > 0)
        {
            Map<String, Object> top = users.get(0);
            long topCnt = toLong(top.get("total"));
            Object nameObj = top.get("userName");
            String topName = (nameObj == null || "null".equals(String.valueOf(nameObj))) ? "未知用户（定时任务）" : String.valueOf(nameObj);
            double share = topCnt * 100.0 / totalCnt;
            double perDay = (double) totalCnt / days;
            if (topCnt > perDay * 10 && topCnt >= 50)
            {
                alerts.add(alert("warning", "人员用量异常",
                        String.format("用户「%s」近%d天调用 %d 次（日均%.0f次），远高于全员日均 %.0f 次，请确认是否属于正常业务使用",
                                topName, days, topCnt, topCnt / (double) days, perDay)));
            }
            else if (share >= 50 && totalCnt >= 40)
            {
                alerts.add(alert("info", "用量集中提醒",
                        String.format("用户「%s」贡献了近%d%%的调用量（%d次），用量较为集中", topName, (int) share, topCnt)));
            }
        }

        // 4. 限额拦截突增
        long limitedCnt = toLong(total != null ? total.get("limited") : 0);
        if (limitedCnt >= 10)
        {
            alerts.add(alert("error", "限额拦截预警",
                    String.format("近%d天有 %d 次调用被月度限额拦截，影响正常使用，请评估是否调整限额配置", days, limitedCnt)));
        }

        return alerts;
    }

    private Map<String, Object> alert(String level, String title, String message)
    {
        Map<String, Object> a = new HashMap<>();
        a.put("level", level);
        a.put("title", title);
        a.put("message", message);
        return a;
    }

    private long toLong(Object v)
    {
        return v == null ? 0L : Long.parseLong(String.valueOf(v));
    }

    private Long toLongObj(Object v)
    {
        if (v == null) return null;
        try { return Long.parseLong(String.valueOf(v)); } catch (NumberFormatException e) { return null; }
    }

    @Override
    public java.util.List<Map<String, Object>> selectStatusCounts()
    {
        return logMapper.selectStatusCounts();
    }
}
