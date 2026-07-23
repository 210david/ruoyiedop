package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mk.domain.MkPaymentPlan;
import com.ruoyi.mk.mapper.MkPaymentPlanMapper;
import com.ruoyi.mk.mapper.MkPaymentRecordMapper;
import com.ruoyi.mk.service.IMkPaymentPlanService;

/**
 * 回款计划 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkPaymentPlanServiceImpl implements IMkPaymentPlanService
{
    @Autowired
    private MkPaymentPlanMapper mkPaymentPlanMapper;

    @Autowired
    private MkPaymentRecordMapper mkPaymentRecordMapper;

    @Override
    public List<MkPaymentPlan> selectPaymentPlanList(MkPaymentPlan paymentPlan)
    {
        List<MkPaymentPlan> list = mkPaymentPlanMapper.selectPaymentPlanList(paymentPlan);
        // 为每个计划补充回款记录列表
        for (MkPaymentPlan plan : list)
        {
            plan.setRecordList(mkPaymentRecordMapper.selectPaymentRecordByPlanId(plan.getPlanId()));
        }
        return list;
    }

    @Override
    public MkPaymentPlan selectPaymentPlanById(Long planId)
    {
        MkPaymentPlan plan = mkPaymentPlanMapper.selectPaymentPlanById(planId);
        if (plan != null)
        {
            plan.setRecordList(mkPaymentRecordMapper.selectPaymentRecordByPlanId(planId));
        }
        return plan;
    }

    @Override
    public int insertPaymentPlan(MkPaymentPlan paymentPlan)
    {
        if (paymentPlan.getPaymentStatus() == null)
        {
            paymentPlan.setPaymentStatus("0");
        }
        if (paymentPlan.getActualAmount() == null)
        {
            paymentPlan.setActualAmount(BigDecimal.ZERO);
        }
        if (paymentPlan.getOverdueFlag() == null)
        {
            paymentPlan.setOverdueFlag("0");
        }
        return mkPaymentPlanMapper.insertPaymentPlan(paymentPlan);
    }

    @Override
    public int updatePaymentPlan(MkPaymentPlan paymentPlan)
    {
        return mkPaymentPlanMapper.updatePaymentPlan(paymentPlan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePaymentPlanByIds(Long[] planIds)
    {
        // 删除关联的回款记录
        for (Long planId : planIds)
        {
            mkPaymentRecordMapper.deletePaymentRecordByPlanId(planId);
        }
        return mkPaymentPlanMapper.deletePaymentPlanByIds(planIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkOverduePayments()
    {
        List<MkPaymentPlan> overdueList = mkPaymentPlanMapper.selectOverduePaymentPlans();
        int count = 0;
        for (MkPaymentPlan plan : overdueList)
        {
            // 只标记逾期标志，不修改回款状态（回款状态仅由回款金额决定）
            plan.setOverdueFlag("1");
            mkPaymentPlanMapper.updatePaymentPlan(plan);
            count++;
        }
        return count;
    }

    @Override
    public Map<String, Object> selectPaymentStatistics()
    {
        Map<String, Object> result = new HashMap<>();
        // 总计划回款金额
        MkPaymentPlan queryAll = new MkPaymentPlan();
        List<MkPaymentPlan> allPlans = mkPaymentPlanMapper.selectPaymentPlanList(queryAll);

        BigDecimal totalPlanAmount = BigDecimal.ZERO;
        BigDecimal totalActualAmount = BigDecimal.ZERO;
        BigDecimal totalPendingAmount = BigDecimal.ZERO;
        BigDecimal totalOverdueAmount = BigDecimal.ZERO;
        int totalCount = allPlans.size();
        int paidCount = 0;
        int pendingCount = 0;
        int overdueCount = 0;
        int partialCount = 0;

        for (MkPaymentPlan plan : allPlans)
        {
            totalPlanAmount = totalPlanAmount.add(plan.getPlanAmount() != null ? plan.getPlanAmount() : BigDecimal.ZERO);
            BigDecimal actual = plan.getActualAmount() != null ? plan.getActualAmount() : BigDecimal.ZERO;
            totalActualAmount = totalActualAmount.add(actual);

            String status = plan.getPaymentStatus();
            if ("1".equals(status))
            {
                paidCount++;
            }
            else if ("3".equals(status))
            {
                totalPendingAmount = totalPendingAmount.add(plan.getPlanAmount().subtract(actual));
                partialCount++;
            }
            else
            {
                totalPendingAmount = totalPendingAmount.add(plan.getPlanAmount().subtract(actual));
                pendingCount++;
            }
            // 逾期统计独立于回款状态，仅看overdueFlag
            if ("1".equals(plan.getOverdueFlag()) && !"1".equals(status))
            {
                totalOverdueAmount = totalOverdueAmount.add(plan.getPlanAmount().subtract(actual));
                overdueCount++;
            }
        }

        result.put("totalPlanAmount", totalPlanAmount);
        result.put("totalActualAmount", totalActualAmount);
        result.put("totalPendingAmount", totalPendingAmount);
        result.put("totalOverdueAmount", totalOverdueAmount);
        result.put("totalCount", totalCount);
        result.put("paidCount", paidCount);
        result.put("pendingCount", pendingCount);
        result.put("overdueCount", overdueCount);
        result.put("partialCount", partialCount);
        // 回款率
        if (totalPlanAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            BigDecimal rate = totalActualAmount.multiply(new BigDecimal(100))
                .divide(totalPlanAmount, 2, BigDecimal.ROUND_HALF_UP);
            result.put("paymentRate", rate);
        }
        else
        {
            result.put("paymentRate", BigDecimal.ZERO);
        }
        return result;
    }
}
