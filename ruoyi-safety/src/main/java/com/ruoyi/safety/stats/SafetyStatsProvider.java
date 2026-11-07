package com.ruoyi.safety.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.mapper.SafetyHazardMapper;

/**
 * 安全管理模块统计指标提供者
 *
 * 复用 SAFETY 现有 Mapper 与看板口径（隐患整改状态），
 * 不新建平行统计实现（统一开发规范 2.8）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：SAFETY_RECT_RATE
 *
 * @author ruoyi
 */
@Component
public class SafetyStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private SafetyHazardMapper hazardMapper;

    @Override
    public String getModuleCode()
    {
        return "safety";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("SAFETY_RECT_RATE");
    }

    @Override
    public MetricValue getMetric(String metricCode, Date metricDate, Long deptId)
    {
        // 部门维度（PRD F5）：隐患主表有 dept_id，按归属部门过滤
        BigDecimal value = null;
        String unit = null;
        switch (metricCode)
        {
            case "SAFETY_RECT_RATE":
                // 口径：隐患整改率 = 已闭环(6) / 应整改总数(待整改3+整改中4+待验收5+已闭环6+超期未整改7)
                // 草稿/待审批/驳回(0/1/2) 尚未进入整改流程，不纳入分母
                int closed = 0;
                int due = 0;
                for (SafetyHazard h : hazardMapper.selectSafetyHazardList(new SafetyHazard()))
                {
                    if (!inDept(deptId, h.getDeptId()))
                    {
                        continue;
                    }
                    String s = h.getHazardStatus();
                    if ("6".equals(s))
                    {
                        closed++;
                        due++;
                    }
                    else if ("3".equals(s) || "4".equals(s) || "5".equals(s) || "7".equals(s))
                    {
                        due++;
                    }
                }
                value = rate(closed, due);
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
