package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinBankStatement;
import com.ruoyi.fin.domain.FinBankStatementItem;
import com.ruoyi.fin.mapper.FinBankAccountMapper;
import com.ruoyi.fin.mapper.FinBankStatementMapper;
import com.ruoyi.fin.service.IFinBankStatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 银行对账单 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FinBankStatementServiceImpl implements IFinBankStatementService
{
    private static final Logger log = LoggerFactory.getLogger(FinBankStatementServiceImpl.class);

    /** 匹配日期容差（天） */
    private static final int MATCH_DATE_TOLERANCE_DAYS = 3;

    @Autowired
    private FinBankStatementMapper statementMapper;

    @Autowired
    private FinBankAccountMapper accountMapper;

    @Override
    public List<FinBankStatement> selectStatementList(FinBankStatement statement)
    {
        return statementMapper.selectStatementList(statement);
    }

    @Override
    public Map<String, Object> selectStatementDetail(Long statementId)
    {
        FinBankStatement s = statementMapper.selectStatementById(statementId);
        if (s == null)
        {
            throw new IllegalArgumentException("对账单不存在或已删除");
        }
        Map<String, Object> r = new HashMap<>();
        r.put("statement", s);
        r.put("items", statementMapper.selectItems(statementId, null));
        r.put("stat", statementMapper.matchStat(statementId));
        r.put("bankSum", statementMapper.sumBankFlow(statementId));
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importStatement(FinBankStatement statement, List<FinBankStatementItem> items)
    {
        if (statement.getAccountId() == null)
        {
            throw new IllegalArgumentException("请选择资金账户");
        }
        if (statement.getStatementDate() == null)
        {
            throw new IllegalArgumentException("对账日期不能为空");
        }
        if (items == null || items.isEmpty())
        {
            throw new IllegalArgumentException("银行流水明细不能为空");
        }

        // 幂等：同账户同期只允许一张对账单
        FinBankStatement exist = statementMapper.selectByAccountDate(
                statement.getAccountId(), statement.getStatementDate());
        if (exist != null)
        {
            throw new IllegalArgumentException(
                    "该账户在此日期已有对账单「" + exist.getStatementDate() + "」，请先删除后再导入");
        }

        String user = SecurityUtils.getUsername();
        statement.setPeriod(periodOf(statement.getStatementDate()));
        statement.setStatus(FinBankStatement.STATUS_IMPORTED);
        statement.setImportBy(user);
        statement.setImportTime(new Date());
        statement.setUserId(SecurityUtils.getUserId());
        statement.setCreateBy(user);
        if (statement.getBeginBalance() == null)
        {
            statement.setBeginBalance(statement.getBankBeginBalance());
        }
        statementMapper.insertStatement(statement);

        // 批量写入明细，默认未匹配
        int i = 0;
        for (FinBankStatementItem item : items)
        {
            item.setStatementId(statement.getStatementId());
            if (item.getMatchStatus() == null)
            {
                item.setMatchStatus(FinBankStatementItem.MATCH_NONE);
            }
            if (item.getTransAmount() != null && item.getTransAmount().compareTo(BigDecimal.ZERO) == 0)
            {
                i++;
                continue;   // 零金额行无对账意义，跳过
            }
            i++;
        }
        if (i > 0)
        {
            statementMapper.insertItems(items);
        }

        log.info("导入对账单：账户 {} 日期 {}，明细 {} 行",
                statement.getAccountId(), statement.getStatementDate(), items.size());
        return statement.getStatementId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoMatch(Long statementId)
    {
        FinBankStatement s = statementMapper.selectStatementById(statementId);
        if (s == null)
        {
            throw new IllegalArgumentException("对账单不存在或已删除");
        }

        List<FinBankStatementItem> unmatched = statementMapper.selectUnmatched(statementId);
        int matchedCount = 0;

        for (FinBankStatementItem item : unmatched)
        {
            List<Map<String, Object>> candidates = statementMapper.selectMatchCandidates(
                    s.getAccountId(), item.getTransAmount(), item.getTransDate());
            if (candidates.isEmpty())
            {
                continue;
            }

            Long flowId = toLong(candidates.get(0).get("flowId"));

            // 【防重复匹配】同一账面流水不能被多条银行明细匹配，
            // 否则一笔付款会被多次核销，对账虚假平。
            boolean alreadyUsed = false;
            for (FinBankStatementItem done : statementMapper.selectItems(statementId, FinBankStatementItem.MATCH_OK))
            {
                if (flowId.equals(done.getFlowId()))
                {
                    alreadyUsed = true;
                    break;
                }
            }
            if (alreadyUsed)
            {
                log.info("账面流水 {} 已被其他银行明细匹配，跳过", flowId);
                continue;
            }

            // D-08：带状态守卫，0 行说明已被并发匹配，不计入本次
            if (statementMapper.markMatched(item.getItemId(), flowId) > 0)
            {
                matchedCount++;
            }
        }

        log.info("自动匹配完成：对账单 {} 共匹配 {} 条", statementId, matchedCount);
        Map<String, Object> stat = statementMapper.matchStat(statementId);
        stat.put("newMatched", matchedCount);
        return stat;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int manualMatch(Long itemId, Long flowId)
    {
        if (flowId == null)
        {
            throw new IllegalArgumentException("请选择要匹配的账面流水");
        }
        // D-08 修复：markMatched 仅对未匹配明细生效，0 行=已被匹配
        int rows = statementMapper.markMatched(itemId, flowId);
        if (rows == 0)
        {
            throw new com.ruoyi.common.exception.ServiceException(
                    "该明细已被匹配，请先解除原匹配后再重新匹配");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unmatch(Long itemId)
    {
        return statementMapper.unmarkMatch(itemId);
    }

    @Override
    public List<Map<String, Object>> matchCandidates(Long accountId, BigDecimal amount, Date date)
    {
        return statementMapper.selectMatchCandidates(accountId, amount, date);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reconcile(Long statementId)
    {
        FinBankStatement s = statementMapper.selectStatementById(statementId);
        if (s == null)
        {
            throw new IllegalArgumentException("对账单不存在或已删除");
        }

        // 银行侧：期初 + Σ明细 = 期末
        Map<String, Object> bankSum = statementMapper.sumBankFlow(statementId);
        BigDecimal bankTotal = num(bankSum.get("totalAmount"));
        BigDecimal bankBegin = nz(s.getBankBeginBalance());
        BigDecimal bankEnd = bankBegin.add(bankTotal);

        // 账面侧：期初 + Σ账面流水 = 期末
        Date begin = monthBegin(s.getStatementDate());
        Date end = monthEnd(s.getStatementDate());
        Map<String, Object> bookSum = statementMapper.sumBookFlow(
                s.getAccountId(), begin, end);
        BigDecimal bookNet = num(bookSum.get("netAmount"));
        // 账面期初 = 账户当前余额 - 本期净发生
        BigDecimal accountNow = nz(accountMapper.selectAccountById(s.getAccountId()) == null
                ? BigDecimal.ZERO
                : accountMapper.selectAccountById(s.getAccountId()).getCurrentBalance());
        BigDecimal bookBegin2 = accountNow.subtract(bookNet);
        BigDecimal bookEnd = bookBegin2.add(bookNet);

        BigDecimal diff = bankEnd.subtract(bookEnd);

        FinBankStatement upd = new FinBankStatement();
        upd.setStatementId(statementId);
        upd.setBankEndBalance(bankEnd);
        upd.setBookEndBalance(bookEnd);
        upd.setDiffAmount(diff);
        upd.setStatus(diff.compareTo(BigDecimal.ZERO) == 0
                ? FinBankStatement.STATUS_RECONCILED : FinBankStatement.STATUS_DIFF);
        statementMapper.updateStatement(upd);

        Map<String, Object> r = new HashMap<>();
        r.put("bankBegin", bankBegin);
        r.put("bankTotal", bankTotal);
        r.put("bankEnd", bankEnd);
        r.put("bookBegin", bookBegin2);
        r.put("bookNet", bookNet);
        r.put("bookEnd", bookEnd);
        r.put("diff", diff);
        r.put("balanced", diff.compareTo(BigDecimal.ZERO) == 0);
        r.put("stat", statementMapper.matchStat(statementId));
        return r;
    }

    @Override
    public Map<String, Object> unreconciled(Long statementId)
    {
        FinBankStatement s = statementMapper.selectStatementById(statementId);
        if (s == null)
        {
            throw new IllegalArgumentException("对账单不存在或已删除");
        }
        Date begin = monthBegin(s.getStatementDate());
        Date end = monthEnd(s.getStatementDate());

        Map<String, Object> r = new HashMap<>();
        // 银行有、账面无（或未匹配）
        r.put("bankOnly", statementMapper.selectUnmatched(statementId));
        // 账面有、银行无
        r.put("bookOnly", statementMapper.selectBookOnlyFlow(
                s.getAccountId(), begin, end));
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteStatementByIds(Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return 0;
        }
        for (Long id : ids)
        {
            statementMapper.deleteItemsByStatement(id);
        }
        return statementMapper.deleteStatementByIds(ids);
    }

    // ==================== 私有方法 ====================

    private Date monthBegin(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTime();
    }

    private Date monthEnd(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return c.getTime();
    }

    private String periodOf(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return String.format("%04d%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
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

    private static BigDecimal nz(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static Long toLong(Object o)
    {
        if (o == null)
        {
            return null;
        }
        if (o instanceof Long)
        {
            return (Long) o;
        }
        if (o instanceof Number)
        {
            return ((Number) o).longValue();
        }
        return Long.valueOf(o.toString());
    }
}