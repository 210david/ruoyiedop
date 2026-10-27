package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinBankAccount;
import com.ruoyi.fin.domain.FinCashFlow;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资金账户与流水 Mapper
 *
 * @author ruoyi
 */
public interface FinBankAccountMapper
{
    /**
     * 查询账户列表
     *
     * @param account 查询条件
     * @return 账户列表
     */
    List<FinBankAccount> selectAccountList(FinBankAccount account);

    /**
     * 按ID查询
     *
     * @param accountId 账户ID
     * @return 账户
     */
    FinBankAccount selectAccountById(Long accountId);

    /**
     * 按账户编码查询
     *
     * @param accountCode 账户编码
     * @return 账户，不存在返回 null
     */
    FinBankAccount selectByCode(@Param("accountCode") String accountCode);

    /**
     * 新增账户
     *
     * @param account 账户
     * @return 影响行数
     */
    int insertAccount(FinBankAccount account);

    /**
     * 修改账户
     *
     * @param account 账户
     * @return 影响行数
     */
    int updateAccount(FinBankAccount account);

    /**
     * 逻辑删除账户
     *
     * @param accountIds 账户ID数组
     * @return 影响行数
     */
    int deleteAccountByIds(Long[] accountIds);

    /**
     * 原子增减账户余额
     *
     * <p>用 SQL 内计算而非「读-改-写」，避免并发下余额覆盖丢更新。
     * IN 增加、OUT 减少，传入带符号金额即可。</p>
     *
     * @param accountId 账户ID
     * @param delta     带符号变动额（收为正，付为负）
     * @return 影响行数
     */
    int updateBalance(@Param("accountId") Long accountId,
                      @Param("delta") BigDecimal delta);

    /**
     * 锁定账户行（流水登记时用 FOR UPDATE）
     *
     * @param accountId 账户ID
     * @return 账户
     */
    FinBankAccount selectAccountForUpdate(Long accountId);

    /**
     * 账户流水列表
     *
     * @param flow 查询条件
     * @return 流水列表
     */
    List<FinCashFlow> selectFlowList(FinCashFlow flow);

    /**
     * 新增流水
     *
     * @param flow 流水
     * @return 影响行数
     */
    int insertFlow(FinCashFlow flow);

    /**
     * 按幂等键查询流水
     *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param sourceId     来源ID
     * @return 流水，不存在返回 null
     */
    FinCashFlow selectFlowBySource(@Param("sourceModule") String sourceModule,
                                   @Param("sourceType") String sourceType,
                                   @Param("sourceId") Long sourceId);

    /**
     * 资金日报（余额一览）
     *
     * @param date 日期
     * @return 各账户当日收付汇总
     */
    List<Map<String, Object>> selectDailyReport(@Param("date") Date date);

    /**
     * 资金流水汇总（总账取数：借银行存款 贷主营业务收入 等）
     *
     * @param period 会计期间
     * @return 按方向汇总
     */
    Map<String, Object> sumFlowByPeriod(@Param("period") String period);

/**
     * 标记流水已对账
     *
     * @param flowId 流水ID
     * @return 影响行数
     */
    int markFlowReconciled(@Param("flowId") Long flowId);

    /**
     * 存在流水的账户数（有流水不可删除）
     *
     * @param accountId 账户ID
     * @return 流水数
     */
    int countFlowByAccount(@Param("accountId") Long accountId);

    /**
     * 资金总余额
     *
     * @return 总余额
     */
    BigDecimal sumAllBalance();
}