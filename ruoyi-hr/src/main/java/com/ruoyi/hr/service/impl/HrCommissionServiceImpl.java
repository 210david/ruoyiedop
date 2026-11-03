package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrCommissionBill;
import com.ruoyi.hr.domain.HrCommissionPlan;
import com.ruoyi.hr.domain.HrCommissionTier;
import com.ruoyi.hr.domain.HrPerfMonth;
import com.ruoyi.hr.domain.HrSalesProfile;
import com.ruoyi.hr.mapper.HrCommissionMapper;
import com.ruoyi.hr.mapper.HrSalesPerfMapper;
import com.ruoyi.hr.service.IHrCommissionService;

/**
 * 提成方案/核算 Service业务层处理（需求三）
 *
 * 业务规则：
 * MR-01 提成计算默认回款口径，可按方案切换签单口径；已生成核算表的方案锁定后不可追溯修改
 * MR-03 核算表按周期确认/锁定，保留历史版本支持审计追溯
 */
@Service
public class HrCommissionServiceImpl implements IHrCommissionService {
    @Autowired
    private HrCommissionMapper mapper;

    @Autowired
    private HrSalesPerfMapper perfMapper;

    // ---- 方案 ----
    @Override
    public List<HrCommissionPlan> selectPlanList(HrCommissionPlan plan) { return mapper.selectPlanList(plan); }

    @Override
    public HrCommissionPlan selectPlanById(Long planId) {
        HrCommissionPlan plan = mapper.selectPlanById(planId);
        if (plan != null) plan.setTiers(mapper.selectTiersByPlan(planId));
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPlan(HrCommissionPlan plan) {
        validatePlan(plan);
        plan.setPlanStatus("0");
        plan.setLocked("0");
        plan.setDelFlag("0");
        plan.setCreateBy(SecurityUtils.getUsername());
        int rows = mapper.insertPlan(plan);
        saveTiers(plan);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePlan(HrCommissionPlan plan) {
        HrCommissionPlan existing = mapper.selectPlanById(plan.getPlanId());
        if (existing == null) throw new ServiceException("提成方案不存在");
        // MR-01：方案锁定后不可追溯修改
        if ("1".equals(existing.getLocked())) throw new ServiceException("该提成方案已锁定，不可追溯修改（MR-01）");
        validatePlan(plan);
        plan.setUpdateBy(SecurityUtils.getUsername());
        int rows = mapper.updatePlan(plan);
        if (plan.getTiers() != null) {
            mapper.deleteTiersByPlan(plan.getPlanId());
            saveTiers(plan);
        }
        return rows;
    }

    private void validatePlan(HrCommissionPlan plan) {
        if (StringUtils.isEmpty(plan.getPlanName())) throw new ServiceException("方案名称不能为空");
        if (StringUtils.isEmpty(plan.getCalcBase())) throw new ServiceException("请选择计算口径");
        if ("0".equals(plan.getPlanType()) && (plan.getBaseRatio() == null || plan.getBaseRatio().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new ServiceException("固定比例方案必须设置提成比例");
        }
        if ("1".equals(plan.getPlanType()) && (plan.getTiers() == null || plan.getTiers().isEmpty())) {
            throw new ServiceException("阶梯方案必须配置阶梯明细");
        }
        if ("2".equals(plan.getPlanType()) && (plan.getBonusThreshold() == null || plan.getBonusRatio() == null)) {
            throw new ServiceException("超额奖励方案必须设置奖励起点与奖励比例");
        }
    }

    private void saveTiers(HrCommissionPlan plan) {
        for (HrCommissionTier tier : plan.getTiers()) {
            tier.setPlanId(plan.getPlanId());
            mapper.insertTier(tier);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePlanByIds(Long[] planIds) {
        for (Long planId : planIds) {
            HrCommissionPlan plan = mapper.selectPlanById(planId);
            if (plan != null && "1".equals(plan.getLocked())) throw new ServiceException("已锁定的提成方案不允许删除（MR-01）");
        }
        return mapper.deletePlanByIds(planIds);
    }

    // ---- 核算表 ----
    @Override
    public List<HrCommissionBill> selectBillList(HrCommissionBill bill) { return mapper.selectBillList(bill); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateBills(String period) {
        if (StringUtils.isEmpty(period) || !period.matches("\\d{4}-\\d{2}")) throw new ServiceException("核算周期格式应为 yyyy-MM");
        if (mapper.countLockedBills(period) > 0) throw new ServiceException("该周期核算表已锁定，不允许重新生成（MR-03）");
        List<HrSalesProfile> profiles = perfMapper.selectProfileList(new HrSalesProfile());
        String oper = SecurityUtils.getUsername();
        int rows = 0;
        for (HrSalesProfile p : profiles) {
            if (p.getCommissionPlanId() == null) continue;
            HrCommissionPlan plan = mapper.selectPlanById(p.getCommissionPlanId());
            if (plan == null || !"1".equals(plan.getPlanStatus())) continue;
            // 月度归集快照（无归集数据时按零处理）
            HrPerfMonth query = new HrPerfMonth();
            query.setPeriod(period);
            query.setEmployeeId(p.getEmployeeId());
            List<HrPerfMonth> perfs = perfMapper.selectPerfList(query);
            HrPerfMonth perf = perfs.isEmpty() ? null : perfs.get(0);
            BigDecimal base = "0".equals(plan.getCalcBase())
                    ? (perf != null && perf.getPaymentAmount() != null ? perf.getPaymentAmount() : BigDecimal.ZERO)
                    : (perf != null && perf.getTotalAmount() != null ? perf.getTotalAmount() : BigDecimal.ZERO);
            BigDecimal commission = calcCommission(plan, base);
            HrCommissionBill bill = new HrCommissionBill();
            bill.setPeriod(period);
            bill.setEmployeeId(p.getEmployeeId());
            bill.setEmployeeName(p.getEmployeeName());
            bill.setPlanId(plan.getPlanId());
            bill.setPlanName(plan.getPlanName());
            bill.setCalcBase(plan.getCalcBase());
            bill.setBaseAmount(base);
            bill.setCommissionAmount(commission);
            bill.setCreateBy(oper);
            rows += mapper.insertBill(bill);
        }
        return rows;
    }

    /** 提成计算：固定比例 / 阶梯 / 超额奖励 */
    private BigDecimal calcCommission(HrCommissionPlan plan, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal commission;
        if ("0".equals(plan.getPlanType())) {
            commission = base.multiply(plan.getBaseRatio()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("1".equals(plan.getPlanType())) {
            // 阶梯：全额按命中区间的比例计算（区间取 base 所在档）
            List<HrCommissionTier> tiers = mapper.selectTiersByPlan(plan.getPlanId());
            BigDecimal ratio = BigDecimal.ZERO;
            for (HrCommissionTier tier : tiers) {
                boolean geMin = base.compareTo(tier.getMinAmount()) >= 0;
                boolean leMax = tier.getMaxAmount() == null || base.compareTo(tier.getMaxAmount()) <= 0;
                if (geMin && leMax) { ratio = tier.getRatio(); break; }
            }
            commission = base.multiply(ratio).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // 超额奖励：起点内按固定比例，超出部分按奖励比例
            BigDecimal threshold = plan.getBonusThreshold();
            BigDecimal within = base.min(threshold);
            BigDecimal over = base.subtract(threshold).max(BigDecimal.ZERO);
            commission = within.multiply(plan.getBaseRatio()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    .add(over.multiply(plan.getBonusRatio()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }
        return commission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmBill(Long billId) {
        HrCommissionBill existing = mapper.selectBillById(billId);
        if (existing == null) throw new ServiceException("核算表记录不存在");
        if (!"0".equals(existing.getBillStatus())) throw new ServiceException("该核算表已确认，请勿重复操作");
        HrCommissionBill update = new HrCommissionBill();
        update.setBillId(billId);
        update.setBillStatus("1");
        update.setConfirmer(SecurityUtils.getUsername());
        update.setConfirmTime(DateUtils.getNowDate());
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateBill(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int lockBills(String period) {
        List<HrCommissionBill> bills = mapper.selectBillList(new HrCommissionBill() {{ setPeriod(period); }});
        String oper = SecurityUtils.getUsername();
        int rows = 0;
        for (HrCommissionBill bill : bills) {
            if (!"1".equals(bill.getBillStatus())) throw new ServiceException("周期内存在未确认的核算表，请先全部确认后再锁定");
        }
        for (HrCommissionBill bill : bills) {
            HrCommissionBill update = new HrCommissionBill();
            update.setBillId(bill.getBillId());
            update.setBillStatus("2");
            update.setUpdateBy(oper);
            rows += mapper.updateBill(update);
            // MR-01：核算表锁定后，关联提成方案自动锁定
            if (bill.getPlanId() != null) {
                HrCommissionPlan planUpdate = new HrCommissionPlan();
                planUpdate.setPlanId(bill.getPlanId());
                planUpdate.setLocked("1");
                planUpdate.setUpdateBy(oper);
                mapper.updatePlan(planUpdate);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBills(String period) {
        if (mapper.countLockedBills(period) > 0) throw new ServiceException("该周期核算表已锁定，不允许删除（MR-03）");
        return mapper.deleteBillByPeriod(period);
    }
}
