package com.ruoyi.wms.stats;

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
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.mapper.WmsStatsMapper;
import com.ruoyi.wms.mapper.WmsStatisticsMapper;

/**
 * 仓库模块统计指标提供者
 *
 * 复用说明：
 * - WMS_ALERT_CNT 复用现有 WmsStatisticsMapper.selectStockAlertSummary（统一预警模型）；
 * - WMS_TURNOVER / WMS_SLOW_AMT 使用新增的 WmsStatsMapper（不改动现有 Mapper，改动范围已声明）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，避免 DataScope 影响（规范 3.5.3）。
 *
 * v1 简化声明：库存周转率的"平均库存金额"以统计日时点库存金额近似，
 * 快照按日留存后可升级为月内日均（PRD v1.2 F1.2 实施备注）。
 *
 * 覆盖指标：WMS_ALERT_CNT / WMS_TURNOVER / WMS_SLOW_AMT
 *
 * @author ruoyi
 */
@Component
public class WmsStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private WmsStatisticsMapper wmsStatisticsMapper;

    @Autowired
    private WmsStatsMapper wmsStatsMapper;

    @Override
    public String getModuleCode()
    {
        return "wms";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("WMS_ALERT_CNT", "WMS_TURNOVER", "WMS_SLOW_AMT");
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
        String dataFlag = "real";
        switch (metricCode)
        {
            case "WMS_ALERT_CNT":
                // 口径：低库存+积压+临期+过期预警数量合计（复用 WMS 统一预警模型）
                int total = 0;
                List<WmsStatisticsVO> summary = wmsStatisticsMapper.selectStockAlertSummary();
                for (WmsStatisticsVO vo : summary)
                {
                    if (vo.getCount() != null)
                    {
                        total += vo.getCount();
                    }
                }
                value = BigDecimal.valueOf(total);
                unit = "个";
                break;

            case "WMS_TURNOVER":
                // 口径：出库成本（按入库单价取价）/ 库存金额【月频】
                Date beginDate = monthStart(metricDate);
                BigDecimal outboundCost = wmsStatsMapper.selectOutboundCost(beginDate, metricDate);
                BigDecimal inventoryAmount = wmsStatsMapper.selectInventoryAmount();
                if (inventoryAmount != null && inventoryAmount.compareTo(BigDecimal.ZERO) > 0)
                {
                    value = outboundCost.divide(inventoryAmount, 2, RoundingMode.HALF_UP);
                    unit = "次/月";
                }
                else
                {
                    unit = "次/月";
                }
                break;

            case "WMS_SLOW_AMT":
                // 口径：超 90 天无出库记录的库存金额（不含委外/在制）
                value = wmsStatsMapper.selectSlowStockAmount(metricDate)
                        .setScale(2, RoundingMode.HALF_UP);
                unit = "元";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, dataFlag);
    }

    /**
     * 指标日期所在月 1 日（零点）
     */
    private Date monthStart(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
