package com.ruoyi.bid.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.mapper.BidProjectMapper;

/**
 * 投标管理模块统计指标提供者
 *
 * 复用 BID 现有 Mapper 与看板口径（投标状态分布），
 * 不新建平行统计实现（统一开发规范 2.8）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：BID_WIN_RATE
 *
 * @author ruoyi
 */
@Component
public class BidStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private BidProjectMapper projectMapper;

    @Override
    public String getModuleCode()
    {
        return "bid";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("BID_WIN_RATE");
    }

    @Override
    public MetricValue getMetric(String metricCode, Date metricDate, Long deptId)
    {
        // 部门维度（PRD F5）：项目主表有 dept_id，按归属部门过滤
        BigDecimal value = null;
        String unit = null;
        switch (metricCode)
        {
            case "BID_WIN_RATE":
                // 口径：中标率 = 中标(7) / 已出结果项目数(中标7 + 未中标8 + 废标流标9)
                int won = 0;
                int decided = 0;
                for (BidProject p : projectMapper.selectProjectList(new BidProject()))
                {
                    if (!inDept(deptId, p.getDeptId()))
                    {
                        continue;
                    }
                    String s = p.getStatus();
                    if ("7".equals(s))
                    {
                        won++;
                        decided++;
                    }
                    else if ("8".equals(s) || "9".equals(s))
                    {
                        decided++;
                    }
                }
                value = rate(won, decided);
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

    /** 部门维度过滤（PRD F5）：deptId=null 全部门汇总；否则仅统计归属该部门的单据 */
    private boolean inDept(Long deptId, Long rowDeptId)
    {
        return deptId == null || (rowDeptId != null && rowDeptId.equals(deptId));
    }
}
