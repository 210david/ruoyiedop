package com.ruoyi.fin.service.impl;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinBankAccount;
import com.ruoyi.fin.domain.FinCashFlow;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinBankAccountMapper;
import com.ruoyi.fin.service.IFinBankAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资金管理 Service 实现
 *
 * <p><b>余额安全的三重保障</b>（资金是账实勾稽的底线）：</p>
 * <ol>
 *   <li>流水登记时 {@code selectAccountForUpdate} 行锁串行化同账户操作</li>
 *   <li>余额更新用 SQL 内 {@code current_balance = current_balance + delta}，
 *       原子操作，避免「读-改-写」并发丢更新</li>
 *   <li>付款前校验余额，不足直接拒绝，杜绝负余额</li>
 * </ol>
 *
 * @author ruoyi
 */
@Service
public class FinBankAccountServiceImpl implements IFinBankAccountService
{
    private static final Logger log = LoggerFactory.getLogger(FinBankAccountServiceImpl.class);

    @Autowired
    private FinBankAccountMapper accountMapper;

    @Autowired
    private com.ruoyi.fin.util.FinCryptoUtil cryptoUtil;

    @Override
    @DataScope(deptAlias = "b", userAlias = "b")
    public List<FinBankAccount> selectAccountList(FinBankAccount account)
    {
        return accountMapper.selectAccountList(account);
    }

    @Override
    public FinBankAccount selectAccountById(Long accountId)
    {
        return accountMapper.selectAccountById(accountId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAccount(FinBankAccount account)
    {
        if (accountMapper.selectByCode(account.getAccountCode()) != null)
        {
            throw FinException.accountCodeDuplicated(account.getAccountCode());
        }

        // 账号按类型校验：银行类必填，现金类禁填
        String accountNo = account.getAccountTail();
        if (FinBankAccount.needAccountNo(account.getAccountType()))
        {
            if (accountNo == null || accountNo.trim().isEmpty())
            {
                throw FinException.accountNoRequired();
            }
            cryptoUtil.validate(accountNo);
            // 加密存密文 + 提取明文尾号
            account.setAccountNumberEncrypt(cryptoUtil.encrypt(accountNo.trim()));
            account.setAccountTail(cryptoUtil.tail(accountNo.trim()));
        }
        else
        {
            if (accountNo != null && !accountNo.trim().isEmpty())
            {
                throw FinException.accountNoNotAllowed();
            }
            account.setAccountNumberEncrypt(null);
            account.setAccountTail(null);
        }

        account.setCurrency(account.getCurrency() == null ? "CNY" : account.getCurrency());
        account.setOpeningBalance(nz(account.getOpeningBalance()));
        // 当前余额初始等于期初余额
        account.setCurrentBalance(nz(account.getOpeningBalance()));
        account.setStatus(account.getStatus() == null ? "0" : account.getStatus());
        account.setCreateBy(SecurityUtils.getUsername());
        return accountMapper.insertAccount(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAccount(FinBankAccount account)
    {
        FinBankAccount exist = accountMapper.selectAccountById(account.getAccountId());
        if (exist == null)
        {
            throw new IllegalArgumentException("资金账户不存在或已删除");
        }

        // 传了新账号才重新加密；未传则保留原密文
        String accountNo = account.getAccountTail();
        if (accountNo != null && !accountNo.trim().isEmpty())
        {
            if (FinBankAccount.needAccountNo(exist.getAccountType()))
            {
                cryptoUtil.validate(accountNo);
                account.setAccountNumberEncrypt(cryptoUtil.encrypt(accountNo.trim()));
                account.setAccountTail(cryptoUtil.tail(accountNo.trim()));
            }
        }
        account.setUpdateBy(SecurityUtils.getUsername());
        return accountMapper.updateAccount(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAccountByIds(Long[] accountIds)
    {
        if (accountIds == null || accountIds.length == 0)
        {
            return 0;
        }
        for (Long id : accountIds)
        {
            FinBankAccount a = accountMapper.selectAccountById(id);
            if (a == null)
            {
                continue;
            }
            // 有流水不可删（保留流水可追溯性）
            if (accountMapper.countFlowByAccount(id) > 0)
            {
                throw FinException.accountHasFlow(a.getAccountName());
            }
            // 余额非零不可删
            if (nz(a.getCurrentBalance()).compareTo(BigDecimal.ZERO) != 0)
            {
                throw FinException.accountBalanceNotZero(
                        a.getAccountName(), a.getCurrentBalance().toPlainString());
            }
        }
        return accountMapper.deleteAccountByIds(accountIds);
    }

    @Override
    public List<FinCashFlow> selectFlowList(FinCashFlow flow)
    {
        return accountMapper.selectFlowList(flow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertFlow(FinCashFlow flow)
    {
        if (flow.getAmount() == null || flow.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw FinException.flowAmountInvalid();
        }
        return doFlow(flow);
    }

    /**
     * 登记一笔流水：行锁 → 校验余额 → 原子更新余额 → 记流水。
     */
    private Long doFlow(FinCashFlow flow)
    {
        FinBankAccount account = accountMapper.selectAccountForUpdate(flow.getAccountId());
        if (account == null)
        {
            throw new IllegalArgumentException("资金账户不存在或已停用");
        }

        BigDecimal amount = flow.getAmount();
        BigDecimal before = nz(account.getCurrentBalance());
        boolean isIn = FinCashFlow.DIR_IN.equals(flow.getDirection());

        // 付款前校验余额，杜绝负余额
        if (!isIn && amount.compareTo(before) > 0)
        {
            throw FinException.accountBalanceNotEnough(
                    account.getAccountName(), before.toPlainString(), amount.toPlainString());
        }

        BigDecimal delta = isIn ? amount : amount.negate();
        BigDecimal after = before.add(delta);

        accountMapper.updateBalance(flow.getAccountId(), delta);

        flow.setBalanceAfter(after);
        flow.setFlowNo(buildFlowNo());
        flow.setPeriod(periodOf(flow.getFlowDate()));
        flow.setReconcileStatus(flow.getReconcileStatus() == null ? "0" : flow.getReconcileStatus());
        flow.setCreateBy(SecurityUtils.getUsername());
        if (flow.getSourceModule() == null)
        {
            flow.setSourceModule("manual");
        }
        if (flow.getSourceType() == null)
        {
            flow.setSourceType("manual");
        }
        flow.setSourceId(flow.getSourceId() == null ? 0L : flow.getSourceId());
        accountMapper.insertFlow(flow);
        log.info("资金流水登记：{} {} {} 余额 {} → {}",
                account.getAccountName(), flow.getDirection(), amount, before, after);
        return flow.getFlowId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String transfer(Long fromAccountId, Long toAccountId, BigDecimal amount,
                           Date flowDate, String remark)
    {
        if (fromAccountId.equals(toAccountId))
        {
            throw FinException.transferSameAccount();
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw FinException.flowAmountInvalid();
        }

        // 固定顺序加锁（按 accountId 从小到大），避免两账户互转时死锁
        long first = Math.min(fromAccountId, toAccountId);
        long second = Math.max(fromAccountId, toAccountId);
        accountMapper.selectAccountForUpdate(first);
        accountMapper.selectAccountForUpdate(second);

        Date date = flowDate == null ? new Date() : flowDate;

        FinCashFlow out = new FinCashFlow();
        out.setAccountId(fromAccountId);
        out.setFlowDate(date);
        out.setDirection(FinCashFlow.DIR_OUT);
        out.setBizType("TRANSFER");
        out.setAmount(amount);
        out.setSourceModule("manual");
        out.setSourceType("transfer");
        out.setSourceKey(remark);
        out.setRemark("转出至账户ID " + toAccountId);
        doFlow(out);

        FinCashFlow in = new FinCashFlow();
        in.setAccountId(toAccountId);
        in.setFlowDate(date);
        in.setDirection(FinCashFlow.DIR_IN);
        in.setBizType("TRANSFER");
        in.setAmount(amount);
        in.setSourceModule("manual");
        in.setSourceType("transfer");
        in.setSourceKey(out.getFlowNo());
        in.setRemark("由账户ID " + fromAccountId + " 转入");
        doFlow(in);

        return out.getFlowNo();
    }

    @Override
    public List<Map<String, Object>> dailyReport(Date date)
    {
        return accountMapper.selectDailyReport(date == null ? new Date() : date);
    }

    @Override
    public Map<String, Object> summary(String period)
    {
        Map<String, Object> r = accountMapper.sumFlowByPeriod(period);
        return r == null ? new HashMap<>() : r;
    }

    @Override
    public BigDecimal totalBalance()
    {
        return accountMapper.sumAllBalance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markReconciled(Long[] flowIds)
    {
        int n = 0;
        if (flowIds == null)
        {
            return 0;
        }
        for (Long id : flowIds)
        {
            n += accountMapper.markFlowReconciled(id);
        }
        return n;
    }

    // ==================== 私有方法 ====================

    private String buildFlowNo()
    {
        return "FL" + System.currentTimeMillis()
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    private String periodOf(Date date)
    {
        LocalDate d = date == null ? LocalDate.now()
                : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return String.format("%04d%02d", d.getYear(), d.getMonthValue());
    }

    private static BigDecimal nz(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }
}