package com.ruoyi.qms.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.qms.domain.QmsCapa;
import com.ruoyi.qms.domain.QmsInspTask;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.mapper.QmsCapaMapper;
import com.ruoyi.qms.mapper.QmsInspTaskMapper;
import com.ruoyi.qms.mapper.QmsNcrMapper;

/**
 * 质量模块统计指标提供者
 *
 * 复用 QMS 现有 Mapper 查询与看板口径（QmsDashboardController.summary 的 passRate/capaCloseRate），
 * 不新建平行统计实现（统一开发规范 2.8）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：QMS_PASS_RATE / QMS_NCR_OPEN / QMS_CAPA_RATE
 *
 * @author ruoyi
 */
@Component
public class QmsStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private QmsInspTaskMapper inspTaskMapper;

    @Autowired
    private QmsNcrMapper ncrMapper;

    @Autowired
    private QmsCapaMapper capaMapper;

    @Override
    public String getModuleCode()
    {
        return "qms";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("QMS_PASS_RATE", "QMS_NCR_OPEN", "QMS_CAPA_RATE");
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
            case "QMS_PASS_RATE":
                // 口径：一次合格数 / 已完成检验数（复用 QMS 看板 passRate 口径）
                List<QmsInspTask> inspList = inspTaskMapper.selectInspTaskList(new QmsInspTask());
                int inspCompleted = 0;
                int inspPass = 0;
                for (QmsInspTask task : inspList)
                {
                    if ("2".equals(task.getTaskStatus()))
                    {
                        inspCompleted++;
                    }
                    if ("1".equals(task.getInspectResult()))
                    {
                        inspPass++;
                    }
                }
                value = rate(inspPass, inspCompleted);
                unit = "%";
                break;

            case "QMS_NCR_OPEN":
                // 口径：状态≠关闭的 NCR 数量（4=关闭；5=驳回亦视为未关闭）
                List<QmsNcr> ncrList = ncrMapper.selectNcrList(new QmsNcr());
                int ncrOpen = 0;
                for (QmsNcr ncr : ncrList)
                {
                    if (!"4".equals(ncr.getNcrStatus()))
                    {
                        ncrOpen++;
                    }
                }
                value = BigDecimal.valueOf(ncrOpen);
                unit = "个";
                break;

            case "QMS_CAPA_RATE":
                // 口径：已关闭 CAPA / CAPA 总数（复用 QMS 看板 capaCloseRate 口径）
                List<QmsCapa> capaList = capaMapper.selectCapaList(new QmsCapa());
                int capaClosed = 0;
                for (QmsCapa capa : capaList)
                {
                    if ("3".equals(capa.getCapaStatus()))
                    {
                        capaClosed++;
                    }
                }
                value = rate(capaClosed, capaList.size());
                unit = "%";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, "real");
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
