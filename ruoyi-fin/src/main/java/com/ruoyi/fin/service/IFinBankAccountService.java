package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinBankAccount;
import com.ruoyi.fin.domain.FinCashFlow;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资金管理 Service 接口
 *
 * @author ruoyi
 */
public interface IFinBankAccountService
{
    List<FinBankAccount> selectAccountList(FinBankAccount account);

    FinBankAccount selectAccountById(Long accountId);

    /** 新增账户（账号自动加密 + 提取尾号） */
    int insertAccount(FinBankAccount account);

    int updateAccount(FinBankAccount account);

    int deleteAccountByIds(Long[] accountIds);

    /** 资金流水列表 */
    List<FinCashFlow> selectFlowList(FinCashFlow flow);

    /**
     * 登记流水（手工登记）
     *
     * @param flow 流水
     * @return 流水ID
     */
    Long insertFlow(FinCashFlow flow);

    /**
     * 账户间转账（拆为两条流水 + 双方余额变动）
     *
     * @param fromAccountId 转出账户
     * @param toAccountId   转入账户
     * @param amount        金额
     * @param flowDate      日期
     * @param remark        备注
     * @return 转账流水号
     */
    String transfer(Long fromAccountId, Long toAccountId, BigDecimal amount,
                    Date flowDate, String remark);

    /** 资金日报 */
    List<Map<String, Object>> dailyReport(Date date);

    /** 期间资金汇总 */
    Map<String, Object> summary(String period);

    /** 资金总余额 */
    BigDecimal totalBalance();

    /**
     * 标记流水已对账
     *
     * @param flowIds 流水ID数组
     * @return 影响行数
     */
    int markReconciled(Long[] flowIds);
}