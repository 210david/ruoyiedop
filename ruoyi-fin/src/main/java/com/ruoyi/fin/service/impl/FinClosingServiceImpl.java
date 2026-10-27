package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinClosing;
import com.ruoyi.fin.domain.FinPeriod;
import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.FinVoucherEntry;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinVoucherMapper;
import com.ruoyi.fin.mapper.FinVoucherRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 期末结账 Service 实现
 *
 * <p><b>结账流程</b>（四步，顺序不可颠倒）：</p>
 * <ol>
 *   <li><b>前置检查</b> —— 上期是否已结账、本期有无未审核凭证、试算是否平</li>
 *   <li><b>结转损益</b> —— 损益类科目余额归零转入本年利润</li>
 *   <li><b>生成账簿</b> —— 本期发生额物化到 fin_ledger</li>
 *   <li><b>封期</b> —— fin_period.status 置为 CLOSED</li>
 * </ol>
 *
 * <p><b>为什么要结转损益</b>：损益类科目（6001/6401/6601…）是临时的，
 * 期末须把累计发生额转入 4103 本年利润，否则资产负债表无法结出
 * 未分配利润，账就不算平。</p>
 *
 * <p><b>反结账</b>：必须连带清理该期间的 fin_ledger 与结转损益凭证，
 * 否则重新结账会重复生成账簿与结转凭证。</p>
 *
 * @author ruoyi
 */
@Service
public class FinClosingServiceImpl
{
    private static final Logger log = LoggerFactory.getLogger(FinClosingServiceImpl.class);

    /** 本年利润科目编码 */
    private static final String ACCOUNT_PROFIT = "4103";

    @Autowired
    private FinVoucherRuleMapper closingMapper;

    @Autowired
    private FinVoucherMapper voucherMapper;

    /**
     * 结账前置检查（不落库，供前端预检）。
     *
     * @param yearMonth 年月
     * @return 检查项列表，含 code/name/result/detail
     */
    public List<Map<String, Object>> checkBeforeClose(String yearMonth)
    {
        List<Map<String, Object>> checks = new ArrayList<>();
        String user = SecurityUtils.getUsername();

        // 检查1：期间存在
        FinPeriod period = closingMapper.selectPeriodByMonth(yearMonth);
        addCheck(checks, "PERIOD_EXIST", "会计期间存在",
                period != null,
                period != null ? "期间「" + period.getPeriodName() + "」" : "期间「" + yearMonth + "」不存在");

        if (period == null)
        {
            return checks;
        }

        // 检查2：本期未结账（已结账则拒绝重复结账）
        addCheck(checks, "PERIOD_OPEN", "本期尚未结账",
                FinPeriod.STATUS_OPEN.equals(period.getStatus()),
                FinPeriod.STATUS_OPEN.equals(period.getStatus()) ? null : "本期已结账于 " + period.getCloseTime());

        // 检查3：上一期间已结账
        FinPeriod prev = closingMapper.selectPrevPeriod(yearMonth);
        addCheck(checks, "PREV_CLOSED", "上一期间已结账",
                prev == null || FinPeriod.STATUS_CLOSED.equals(prev.getStatus()),
                (prev == null || FinPeriod.STATUS_CLOSED.equals(prev.getStatus()))
                        ? null : "上期「" + prev.getPeriodName() + "」尚未结账，请先结上期");

        // 检查4：无未审核凭证
        int draft = closingMapper.countDraftVoucher(yearMonth);
        addCheck(checks, "NO_DRAFT_VOUCHER", "无未审核凭证",
                draft == 0, draft == 0 ? null : "尚有 " + draft + " 张草稿凭证，请先审核或删除");

        return checks;
    }

    /**
     * 执行结账。
     *
     * @param yearMonth 年月
     * @return 结转损益凭证ID（0 表示无损益发生额，未生成结转凭证）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> closePeriod(String yearMonth)
    {
        String user = SecurityUtils.getUsername();

        // ===== 步骤1：前置检查 =====
        List<Map<String, Object>> checks = checkBeforeClose(yearMonth);
        saveChecks(yearMonth, checks, user);

        List<String> failed = new ArrayList<>();
        for (Map<String, Object> c : checks)
        {
            if (!Boolean.TRUE.equals(c.get("pass")))
            {
                failed.add(c.get("name") + "：" + c.get("detail"));
            }
        }
        if (!failed.isEmpty())
        {
            throw FinException.closingCheckFailed(String.join("；", failed));
        }

        FinPeriod period = closingMapper.selectPeriodByMonth(yearMonth);

        // ===== 步骤2：结转损益 =====
        Long carryVoucherId = carryForwardProfit(yearMonth, user);

        // ===== 步骤3：生成账簿 =====
        String prevPeriod = prevPeriodOf(yearMonth);
        int ledgerRows = closingMapper.generateLedger(yearMonth, prevPeriod, user);
        log.info("期间 {} 账簿生成完成，共 {} 个科目", yearMonth, ledgerRows);

        // ===== 步骤4：封期 =====
        int rows = closingMapper.closePeriod(yearMonth, user);
        if (rows == 0)
        {
            throw FinException.periodClosed(period == null ? yearMonth : period.getPeriodName());
        }

        log.info("期间 {} 结账完成，结转损益凭证ID={}，账簿 {} 行",
                yearMonth, carryVoucherId, ledgerRows);

        Map<String, Object> r = new HashMap<>();
        r.put("yearMonth", yearMonth);
        r.put("carryForwardVoucherId", carryVoucherId);
        r.put("ledgerRows", ledgerRows);
        r.put("closeBy", user);
        return r;
    }

    /**
     * 结转损益：把本期损益类科目余额归零，转入本年利润。
     *
     * <p>规则：</p>
     * <ul>
     *   <li>收入类（余额在贷方为正）→ 借记该科目、贷记本年利润</li>
     *   <li>费用类（余额在借方为正）→ 借记本年利润、贷记该科目</li>
     * </ul>
     *
     * @return 凭证ID，无损益发生额返回 0
     */
    private Long carryForwardProfit(String yearMonth, String user)
    {
        List<Map<String, Object>> balances = closingMapper.selectProfitBalance(yearMonth);
        if (balances == null || balances.isEmpty())
        {
            return 0L;
        }

        List<FinVoucherEntry> entries = new ArrayList<>();
        BigDecimal totalProfit = BigDecimal.ZERO;   // 净利润（收入 - 费用）
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Map<String, Object> row : balances)
        {
            String accountCode = str(row.get("accountCode"));
            String accountName = str(row.get("accountName"));
            String direction = str(row.get("direction"));
            BigDecimal balance = num(row.get("balance"));

            if (balance.compareTo(BigDecimal.ZERO) == 0)
            {
                continue;
            }

            // 结转方向与原方向相反，使该科目本期归零
            boolean isCreditBalance = balance.compareTo(BigDecimal.ZERO) > 0;
            String closeDir = isCreditBalance ? FinVoucherEntry.DIR_DEBIT : FinVoucherEntry.DIR_CREDIT;
            BigDecimal amount = balance.abs();

            FinVoucherEntry e = new FinVoucherEntry();
            e.setAccountCode(accountCode);
            e.setAccountName(accountName);
            e.setDirection(closeDir);
            e.setAmount(amount);
            e.setCurrency("CNY");
            e.setSummary("结转本期损益");
            e.setCreateBy(user);
            entries.add(e);

            if (FinVoucherEntry.DIR_DEBIT.equals(closeDir))
            {
                totalDebit = totalDebit.add(amount);
            }
            else
            {
                totalCredit = totalCredit.add(amount);
            }

            // 净利润累计（收入贷方为正，费用借方为负）
            totalProfit = isCreditBalance ? totalProfit.add(amount)
                    : totalProfit.subtract(amount);
        }

        if (entries.isEmpty())
        {
            return 0L;
        }

        // 本年利润分录：与差额反向
        FinVoucherEntry profitEntry = new FinVoucherEntry();
        profitEntry.setAccountCode(ACCOUNT_PROFIT);
        profitEntry.setAccountName("本年利润");
        BigDecimal profitAmount = totalDebit.subtract(totalCredit);
        profitEntry.setDirection(FinVoucherEntry.DIR_DEBIT);
        profitEntry.setAmount(profitAmount.abs());
        profitEntry.setCurrency("CNY");
        profitEntry.setSummary("结转本期损益");
        profitEntry.setCreateBy(user);
        entries.add(profitEntry);

        // 借贷平衡自检（结转损益必须平）
        BigDecimal checkDebit = BigDecimal.ZERO;
        BigDecimal checkCredit = BigDecimal.ZERO;
        for (FinVoucherEntry e : entries)
        {
            if (FinVoucherEntry.DIR_DEBIT.equals(e.getDirection()))
            {
                checkDebit = checkDebit.add(e.getAmount());
            }
            else
            {
                checkCredit = checkCredit.add(e.getAmount());
            }
        }
        if (checkDebit.compareTo(checkCredit) != 0)
        {
            log.error("结转损益借贷不平：借 {} 贷 {}，本期损益科目余额异常", checkDebit, checkCredit);
            throw FinException.ruleUnbalanced(checkDebit.toPlainString(),
                    checkCredit.toPlainString(), checkDebit.subtract(checkCredit).toPlainString(),
                    "结转损益");
        }

        // 组装凭证
        FinVoucher voucher = new FinVoucher();
        voucher.setVoucherDate(toDate(yearMonth));
        voucher.setPeriod(yearMonth);
        voucher.setVoucherType(FinVoucher.TYPE_MANUAL);
        voucher.setSourceModule("fin");
        voucher.setSourceType("carry_forward");
        voucher.setSourceId(0L);
        voucher.setSourceKey("结转损益");
        voucher.setRuleKey("carry_forward_profit");
        voucher.setEntryCount(entries.size());
        voucher.setStatus(FinVoucher.STATUS_AUDITED);   // 系统生成直接已审核
        voucher.setAuditBy(user);
        voucher.setAuditTime(new java.util.Date());
        voucher.setAuditOpinion("期末自动结转");
        voucher.setAttachCount(0);
        voucher.setTotalAmount(checkDebit);
        voucher.setUserId(SecurityUtils.getUserId());
        voucher.setCreateBy(user);
        voucher.setRemark(yearMonth + " 期末结转损益");

        voucherMapper.insertVoucher(voucher);

        int lineNo = 1;
        for (FinVoucherEntry e : entries)
        {
            e.setVoucherId(voucher.getVoucherId());
            e.setLineNo(lineNo++);
            e.setPeriod(yearMonth);
        }
        voucherMapper.insertEntries(entries);

        log.info("期间 {} 结转损益：{} 行分录，净利润 {}",
                yearMonth, entries.size(), totalProfit);
        return voucher.getVoucherId();
    }

    /**
     * 反结账。
     *
     * <p><b>连带清理</b>：清理该期间的账簿与结转损益凭证，
     * 否则重新结账会重复生成。</p>
     *
     * @param yearMonth 年月
     * @param reason    反结账原因（必填，留痕）
     */
    @Transactional(rollbackFor = Exception.class)
    public int unlockPeriod(String yearMonth, String reason)
    {
        if (reason == null || reason.trim().isEmpty())
        {
            throw new IllegalArgumentException("反结账原因必须填写");
        }
        String user = SecurityUtils.getUsername();
        FinPeriod period = closingMapper.selectPeriodByMonth(yearMonth);
        if (period == null)
        {
            throw FinException.periodNotFound(yearMonth);
        }
        if (!FinPeriod.STATUS_CLOSED.equals(period.getStatus()))
        {
            throw FinException.periodNotFound(yearMonth + "（尚未结账）");
        }

        int rows = closingMapper.unlockPeriod(yearMonth, user, reason.trim());

        // 清理该期间的账簿
        deleteLedger(yearMonth);
        // 清理该期间的结转损益凭证
        deleteCarryForwardVoucher(yearMonth);
        // 清理检查记录
        closingMapper.deleteClosingByPeriod(yearMonth);

        log.info("期间 {} 已反结账，原因：{}", yearMonth, reason);
        return rows;
    }

    /**
     * 校验期间是否已结账（供凭证新增时拦截）。
     *
     * @param yearMonth 年月
     * @throws ServiceException 已结账则抛出
     */
    public void checkPeriodWritable(String yearMonth)
    {
        String status = closingMapper.selectPeriodStatus(yearMonth);
        if (FinPeriod.STATUS_CLOSED.equals(status))
        {
            FinPeriod p = closingMapper.selectPeriodByMonth(yearMonth);
            throw FinException.periodClosed(p == null ? yearMonth : p.getPeriodName());
        }
    }

    /**
     * 查询结账检查记录。
     */
    public List<FinClosing> selectClosingList(String yearMonth)
    {
        return closingMapper.selectClosingList(yearMonth);
    }

    // ==================== 私有方法 ====================

    private void deleteLedger(String yearMonth)
    {
        int n = closingMapper.deleteLedgerByPeriod(yearMonth);
        if (n > 0)
        {
            log.info("清理期间 {} 账簿 {} 行", yearMonth, n);
        }
    }

    private void deleteCarryForwardVoucher(String yearMonth)
    {
        int n = voucherMapper.deleteVoucherBySource("fin", "carry_forward", yearMonth);
        if (n > 0)
        {
            log.info("清理期间 {} 结转损益凭证 {} 张", yearMonth, n);
        }
    }

    private void saveChecks(String yearMonth, List<Map<String, Object>> checks, String user)
    {
        for (Map<String, Object> c : checks)
        {
            FinClosing fc = new FinClosing();
            fc.setYearMonth(yearMonth);
            fc.setCheckCode(str(c.get("code")));
            fc.setCheckName(str(c.get("name")));
            fc.setCheckResult(Boolean.TRUE.equals(c.get("pass")) ? "1" : "0");
            fc.setCheckDetail(c.get("detail") == null ? null : str(c.get("detail")));
            fc.setCheckBy(user);
            closingMapper.insertClosing(fc);
        }
    }

    private void addCheck(List<Map<String, Object>> list, String code, String name,
                          boolean pass, String detail)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("name", name);
        m.put("pass", pass);
        m.put("detail", pass ? null : detail);
        list.add(m);
    }

    /**
     * 上一期间年月（yyyyMM）。当前为 202601 时返回 202512。
     */
    private String prevPeriodOf(String yearMonth)
    {
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        int month = Integer.parseInt(yearMonth.substring(4, 6));
        if (month == 1)
        {
            return String.format("%04d%02d", year - 1, 12);
        }
        return String.format("%04d%02d", year, month - 1);
    }

    private Date toDate(String yearMonth)
    {
        int year = Integer.parseInt(yearMonth.substring(0, 4));
        int month = Integer.parseInt(yearMonth.substring(4, 6));
        // 期末结转凭证日期定为当月最后一天
        return java.sql.Date.valueOf(LocalDate.of(year, month, 1).withDayOfMonth(
                java.time.YearMonth.of(year, month).lengthOfMonth()));
    }

    private static BigDecimal num(Object o)
    {
        if (o == null)
        {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal)
        {
            return (BigDecimal) o;
        }
        return new BigDecimal(o.toString());
    }

    private static String str(Object o)
    {
        return o == null ? "" : o.toString();
    }
}