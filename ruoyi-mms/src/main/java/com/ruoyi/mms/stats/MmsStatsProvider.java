package com.ruoyi.mms.stats;

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
import com.ruoyi.mms.domain.MmsAbnormal;
import com.ruoyi.mms.domain.MmsOeeSnapshot;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.mapper.MmsAbnormalMapper;
import com.ruoyi.mms.mapper.MmsOeeSnapshotMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;

/**
 * 生产制造模块统计指标提供者
 *
 * 复用 MMS 现有 Mapper 与看板口径（工单状态分布、异常统计、工序 OEE 快照），
 * 不新建平行统计实现（统一开发规范 2.8）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：MMS_WO_RATE / MMS_EXCEPTION / DMS_OEE
 * （DMS_OEE 数据源 mms_oee_snapshot 表归属 MMS 模块，故由本 Provider 提供）
 *
 * @author ruoyi
 */
@Component
public class MmsStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private MmsAbnormalMapper abnormalMapper;

    @Autowired
    private MmsOeeSnapshotMapper oeeSnapshotMapper;

    @Override
    public String getModuleCode()
    {
        return "mms";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("MMS_WO_RATE", "MMS_EXCEPTION", "DMS_OEE");
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
            case "MMS_WO_RATE":
                // 口径：工单完成率 = (已完工 3 + 已关闭 4) / 有效工单总数（不含草稿 0、已作废 6）
                int finished = 0;
                int valid = 0;
                for (MmsWorkOrder wo : workOrderMapper.selectWorkOrderList(new MmsWorkOrder()))
                {
                    String s = wo.getStatus();
                    if ("0".equals(s) || "6".equals(s))
                    {
                        continue;
                    }
                    valid++;
                    if ("3".equals(s) || "4".equals(s))
                    {
                        finished++;
                    }
                }
                value = rate(finished, valid);
                unit = "%";
                break;

            case "MMS_EXCEPTION":
                // 口径：当月生产异常单数（全部状态，按上报时间归月）
                int monthAbnormal = 0;
                for (MmsAbnormal ab : abnormalMapper.selectAbnormalList(new MmsAbnormal()))
                {
                    if (ab.getReportTime() != null && inSameMonth(ab.getReportTime(), metricDate))
                    {
                        monthAbnormal++;
                    }
                }
                value = BigDecimal.valueOf(monthAbnormal);
                unit = "单";
                break;

            case "DMS_OEE":
                // 口径【已确认 v1】：设备综合效率 = OEE 快照平均值（mms_oee_snapshot.oee）。
                // 估算口径：data_flag=estimate，前端强制"估算"角标，接入实测采集后切换。
                BigDecimal oeeSum = BigDecimal.ZERO;
                int oeeCnt = 0;
                for (MmsOeeSnapshot snap : oeeSnapshotMapper.selectOeeSnapshotList(new MmsOeeSnapshot()))
                {
                    if (snap.getOee() != null
                            && snap.getSnapshotDate() != null
                            && inSameMonth(snap.getSnapshotDate(), metricDate))
                    {
                        oeeSum = oeeSum.add(snap.getOee());
                        oeeCnt++;
                    }
                }
                value = oeeCnt == 0 ? null
                        : oeeSum.divide(BigDecimal.valueOf(oeeCnt), 2, RoundingMode.HALF_UP);
                unit = "%";
                dataFlag = "estimate";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, dataFlag);
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

    private BigDecimal rate(int part, int total)
    {
        if (total <= 0)
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }
}
