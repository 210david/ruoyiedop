package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinClosing;
import com.ruoyi.fin.domain.FinPeriod;
import com.ruoyi.fin.domain.FinVoucherRule;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 凭证规则与期末结账 Mapper
 *
 * @author ruoyi
 */
public interface FinVoucherRuleMapper
{
    /**
     * 按来源类型查询启用规则
     *
     * @param sourceType 来源类型 invoice/shipment/payroll
     * @return 规则列表，按 priority 排序
     */
    List<FinVoucherRule> selectRulesBySourceType(@Param("sourceType") String sourceType);

    /**
     * 查询全部规则（规则维护页）
     *
     * @return 规则列表
     */
    List<FinVoucherRule> selectAllRules();

    /**
     * 查询规则
     *
     * @param ruleKey 规则键
     * @return 规则
     */
    FinVoucherRule selectByRuleKey(@Param("ruleKey") String ruleKey);

    /**
     * 更新规则
     *
     * @param rule 规则
     * @return 影响行数
     */
    int updateRule(FinVoucherRule rule);

    /**
     * 取来源单据金额（表达式 → 金额），供引擎组装配分录
     *
     * @param sourceModule 来源模块
     * @param sourceId     来源单据ID
     * @return key 为 total_amount/amount_excl_tax/tax_amount
     */
    Map<String, Object> selectSourceAmount(@Param("sourceModule") String sourceModule,
                                           @Param("sourceId") Long sourceId);

    // ==================== 期末结账 ====================

    /**
     * 查询期间列表
     *
     * @return 期间列表
     */
    List<FinPeriod> selectPeriodList();

    /**
     * 按年月查询期间
     *
     * @param yearMonth 年月（yyyyMM）
     * @return 期间，不存在返回 null
     */
    FinPeriod selectPeriodByMonth(@Param("yearMonth") String yearMonth);

    /**
     * 取上一个未结账期间
     *
     * @param yearMonth 当前年月
     * @return 期间，不存在返回 null
     */
    FinPeriod selectPrevPeriod(@Param("yearMonth") String yearMonth);

    /**
     * 结账：更新期间状态为已结账
     *
     * @param yearMonth 年月
     * @param closeBy   结账人
     * @return 影响行数
     */
    int closePeriod(@Param("yearMonth") String yearMonth,
                    @Param("closeBy") String closeBy);

    /**
     * 反结账
     *
     * @param yearMonth   年月
     * @param unlockBy    反结账人
     * @param unlockReason 原因
     * @return 影响行数
     */
    int unlockPeriod(@Param("yearMonth") String yearMonth,
                     @Param("unlockBy") String unlockBy,
                     @Param("unlockReason") String unlockReason);

    /**
     * 统计期间未审核凭证数（结账前置校验）
     *
     * @param yearMonth 年月
     * @return 凭证数
     */
    int countDraftVoucher(@Param("yearMonth") String yearMonth);

    /**
     * 生成账簿：把本期发生额结转为账簿
     *
     * <p>按科目写入 fin_ledger，opening 取上期期末。
     * 上期月份由 Service 计算传入，避免 SQL 内拼月份易错。</p>
     *
     * @param yearMonth   本期年月
     * @param prevPeriod  上期年月
     * @param user        操作人
     * @return 写入行数
     */
    int generateLedger(@Param("yearMonth") String yearMonth,
                       @Param("prevPeriod") String prevPeriod,
                       @Param("user") String user);

    /**
     * 插入结转损益凭证主表。
     *
     * @param params 含 voucherNo/voucherDate/yearMonth/totalAmount/entryCount/user
     * @return 凭证ID
     */
    int insertCarryForwardVoucher(Map<String, Object> params);

    /**
     * 结转损益：查询本期损益类科目余额（供Service 组装结转分录）
     *
     * @param yearMonth 年月
     * @return 科目余额列表
     */
    List<Map<String, Object>> selectProfitBalance(@Param("yearMonth") String yearMonth);

    /**
     * 插入结账检查记录
     *
     * @param closing 检查记录
     * @return 影响行数
     */
    int insertClosing(FinClosing closing);

    /**
     * 查询结账检查记录
     *
     * @param yearMonth 年月
     * @return 检查记录
     */
    List<FinClosing> selectClosingList(@Param("yearMonth") String yearMonth);

    /**
     * 清空期间检查记录（反结账时用）
     *
     * @param yearMonth 年月
     * @return 影响行数
     */
    int deleteClosingByPeriod(@Param("yearMonth") String yearMonth);

/**
     * 期间结账状态查询（供凭证新增时拦截）
     *
     * @param yearMonth 年月
     * @return 状态 OPEN/CLOSED
     */
    String selectPeriodStatus(@Param("yearMonth") String yearMonth);

    /**
     * 清理期间账簿（反结账时用）
     *
     * @param yearMonth 年月
     * @return 影响行数
     */
    int deleteLedgerByPeriod(@Param("yearMonth") String yearMonth);
}