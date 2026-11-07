package com.ruoyi.mk.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.domain.MkLead;
import com.ruoyi.mk.domain.MkOpportunity;
import com.ruoyi.mk.domain.MkPaymentPlan;
import com.ruoyi.mk.mapper.MkContractMapper;
import com.ruoyi.mk.mapper.MkLeadMapper;
import com.ruoyi.mk.mapper.MkOpportunityMapper;
import com.ruoyi.mk.mapper.MkPaymentPlanMapper;

/**
 * 营销模块统计指标提供者
 *
 * 复用 MK 现有 Mapper 与看板口径（合同趋势、赢单输单分析、线索漏斗、回款计划），
 * 不新建平行统计实现（统一开发规范 2.8）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：MK_SIG_AMT / MK_WIN_RATE / MK_LEAD_CVR / MK_RCPT_AMT / OTC_CYCLE
 *
 * @author ruoyi
 */
@Component
public class MkStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private MkContractMapper contractMapper;

    @Autowired
    private MkOpportunityMapper opportunityMapper;

    @Autowired
    private MkLeadMapper leadMapper;

    @Autowired
    private MkPaymentPlanMapper paymentPlanMapper;

    @Override
    public String getModuleCode()
    {
        return "mk";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("MK_SIG_AMT", "MK_WIN_RATE", "MK_LEAD_CVR", "MK_RCPT_AMT", "OTC_CYCLE");
    }

    @Override
    public MetricValue getMetric(String metricCode, Date metricDate, Long deptId)
    {
        // 部门维度（PRD F5）：deptId=null 全部门汇总；非 null 按单据归属部门过滤
        // （合同/商机/线索主表有 dept_id；回款计划无部门归属，经关联合同的部门判定）
        BigDecimal value = null;
        String unit = null;
        switch (metricCode)
        {
            case "MK_SIG_AMT":
                // 口径【已确认 v1】：当月签约额 = 当月签约的已生效合同金额合计（contract_status=2）
                BigDecimal sigAmt = BigDecimal.ZERO;
                for (MkContract c : contractMapper.selectContractList(new MkContract()))
                {
                    if ("2".equals(c.getContractStatus()) && c.getSignDate() != null
                            && inSameMonth(c.getSignDate(), metricDate)
                            && inDept(deptId, c.getDeptId()))
                    {
                        sigAmt = sigAmt.add(nvl(c.getContractAmount()));
                    }
                }
                value = sigAmt;
                unit = "元";
                break;

            case "MK_WIN_RATE":
                // 口径：赢单率 = 已赢单 / (已赢单 + 已输单)，复用 MK 看板赢单分析口径
                int won = 0;
                int lost = 0;
                for (MkOpportunity o : opportunityMapper.selectOpportunityList(new MkOpportunity()))
                {
                    if (!inDept(deptId, o.getDeptId()))
                    {
                        continue;
                    }
                    if ("1".equals(o.getOpportunityStatus()))
                    {
                        won++;
                    }
                    else if ("2".equals(o.getOpportunityStatus()))
                    {
                        lost++;
                    }
                }
                value = rate(won, won + lost);
                unit = "%";
                break;

            case "MK_LEAD_CVR":
                // 口径：线索转化率 = 已转化线索 / 全部线索，复用 MK 看板线索漏斗口径
                int converted = 0;
                int total = 0;
                for (MkLead l : leadMapper.selectLeadList(new MkLead()))
                {
                    if (!inDept(deptId, l.getDeptId()))
                    {
                        continue;
                    }
                    total++;
                    if ("4".equals(l.getLeadStatus()))
                    {
                        converted++;
                    }
                }
                value = rate(converted, total);
                unit = "%";
                break;

            case "MK_RCPT_AMT":
                // 口径【已确认 v1】：当月回款额 = 当月实际回款日期在月内的回款金额合计
                // （payment_status 1=已回款 / 3=部分回款 的 actual_amount 均计入；
                //  部门维度经关联合同的 dept_id 判定，仅部门查询时建映射，避免全部门时多一次查询）
                BigDecimal rcptAmt = BigDecimal.ZERO;
                Map<Long, Long> contractDeptMap = null;
                if (deptId != null)
                {
                    contractDeptMap = new HashMap<>();
                    for (MkContract c : contractMapper.selectContractList(new MkContract()))
                    {
                        if (c.getContractId() != null)
                        {
                            contractDeptMap.put(c.getContractId(), c.getDeptId());
                        }
                    }
                }
                for (MkPaymentPlan p : paymentPlanMapper.selectPaymentPlanList(new MkPaymentPlan()))
                {
                    if (("1".equals(p.getPaymentStatus()) || "3".equals(p.getPaymentStatus()))
                            && p.getActualDate() != null && inSameMonth(p.getActualDate(), metricDate)
                            && inDept(deptId, contractDeptMap == null ? null : contractDeptMap.get(p.getContractId())))
                    {
                        rcptAmt = rcptAmt.add(nvl(p.getActualAmount()));
                    }
                }
                value = rcptAmt;
                unit = "元";
                break;

            case "OTC_CYCLE":
                // 口径【已确认 v1】：现金周期 = 合同签订到全额回清的平均天数（按全额回清口径）。
                // 全额回清 = 合同下全部回款计划均为"已回款"且实际回款合计 >= 合同金额；
                // 仅统计回清完成日期落在指标日期当月的合同；未回清合同不纳入。
                value = calcOtcCycle(metricDate, deptId);
                unit = "天";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, "real");
    }

    /**
     * 现金周期：合同签订 → 全额回清 的平均天数（当月回清合同）
     *
     * @param deptId 部门维度（PRD F5）：null 全部门；非 null 仅统计归属该部门的合同
     */
    private BigDecimal calcOtcCycle(Date metricDate, Long deptId)
    {
        // 一次拉取合同与回款计划，Java 内存按 contractId 分组（避免逐合同 N+1 查询）
        Map<Long, MkContract> contractMap = new HashMap<>();
        for (MkContract c : contractMapper.selectContractList(new MkContract()))
        {
            if (c.getContractId() != null)
            {
                contractMap.put(c.getContractId(), c);
            }
        }

        Map<Long, List<MkPaymentPlan>> planMap = new HashMap<>();
        for (MkPaymentPlan p : paymentPlanMapper.selectPaymentPlanList(new MkPaymentPlan()))
        {
            if (p.getContractId() != null)
            {
                planMap.computeIfAbsent(p.getContractId(), k -> new ArrayList<>()).add(p);
            }
        }

        long totalDays = 0;
        int clearedCount = 0;
        for (Map.Entry<Long, List<MkPaymentPlan>> entry : planMap.entrySet())
        {
            MkContract contract = contractMap.get(entry.getKey());
            List<MkPaymentPlan> plans = entry.getValue();
            if (contract == null || contract.getSignDate() == null || plans.isEmpty()
                    || !inDept(deptId, contract.getDeptId()))
            {
                continue;
            }
            // 全额回清判定：全部计划已回款 + 实际回款合计 >= 合同金额
            boolean allPaid = true;
            BigDecimal actualTotal = BigDecimal.ZERO;
            Date lastPaidDate = null;
            for (MkPaymentPlan p : plans)
            {
                if (!"1".equals(p.getPaymentStatus()))
                {
                    allPaid = false;
                    break;
                }
                actualTotal = actualTotal.add(nvl(p.getActualAmount()));
                if (p.getActualDate() != null
                        && (lastPaidDate == null || p.getActualDate().after(lastPaidDate)))
                {
                    lastPaidDate = p.getActualDate();
                }
            }
            if (!allPaid || lastPaidDate == null
                    || (contract.getContractAmount() != null
                        && actualTotal.compareTo(contract.getContractAmount()) < 0))
            {
                continue;
            }
            // 仅统计当月回清的合同（月度频率指标）
            if (!inSameMonth(lastPaidDate, metricDate))
            {
                continue;
            }
            long days = (lastPaidDate.getTime() - contract.getSignDate().getTime()) / (24L * 3600 * 1000);
            if (days >= 0)
            {
                totalDays += days;
                clearedCount++;
            }
        }
        if (clearedCount == 0)
        {
            // 当月无回清合同：返回 null（前端展示"—"），禁止用 0 冒充
            return null;
        }
        return BigDecimal.valueOf(totalDays)
                .divide(BigDecimal.valueOf(clearedCount), 1, RoundingMode.HALF_UP);
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

    /** 部门维度过滤（PRD F5）：deptId=null 全部门汇总；否则仅统计归属该部门的单据 */
    private boolean inDept(Long deptId, Long rowDeptId)
    {
        return deptId == null || (rowDeptId != null && rowDeptId.equals(deptId));
    }

    private BigDecimal rate(int part, int total)
    {
        if (total <= 0)
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }
}
