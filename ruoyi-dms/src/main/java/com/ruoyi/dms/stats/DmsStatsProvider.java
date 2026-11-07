package com.ruoyi.dms.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.mapper.DmsWorkOrderMapper;

/**
 * 设备管理模块统计指标提供者
 *
 * 复用 DMS 现有 Mapper 与工单停机时长字段。
 * 注：DMS_OEE 已划归 MmsStatsProvider（数据源 mms_oee_snapshot 归属 MMS 模块）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：DMS_DOWN_HRS
 *
 * @author ruoyi
 */
@Component
public class DmsStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private DmsWorkOrderMapper workOrderMapper;

    @Override
    public String getModuleCode()
    {
        return "dms";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("DMS_DOWN_HRS");
    }

    @Override
    public MetricValue getMetric(String metricCode, Date metricDate, Long deptId)
    {
        // 部门维度快照待后续迭代（当前仅全部门汇总）
        if (deptId != null)
        {
            return new MetricValue(metricCode, metricDate, deptId, null, null, "real");
        }

        BigDecimal value = null;
        String unit = null;
        switch (metricCode)
        {
            case "DMS_DOWN_HRS":
                // 口径：当月故障停机时长合计 = 当月报修工单 downtime_duration 汇总（按上报时间归月）
                BigDecimal downHours = BigDecimal.ZERO;
                for (DmsWorkOrder wo : workOrderMapper.selectWorkOrderList(new DmsWorkOrder()))
                {
                    if (wo.getReportTime() != null && inSameMonth(wo.getReportTime(), metricDate)
                            && wo.getDowntimeDuration() != null)
                    {
                        downHours = downHours.add(wo.getDowntimeDuration());
                    }
                }
                value = downHours.setScale(1, RoundingMode.HALF_UP);
                unit = "小时";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, "real");
    }

    private boolean inSameMonth(Date a, Date b)
    {
        if (a == null || b == null)
        {
            return false;
        }
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH);
    }
}
