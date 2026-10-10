package com.ruoyi.fin.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 财务报表 Mapper
 *
 * <p><b>取数原则</b>：全部基于 fin_voucher_entry（记账凭证分录），
 * 仅统计已审核/已记账凭证（DRAFT 不进报表），
 * 口径与 P1 试算平衡完全一致，确保报表与账簿勾稽相符。</p>
 *
 * <p><b>公式约定</b>：
 * <ul>
 *   <li>发生额：借贷方按方向分别汇总</li>
 *   <li>期末余额 = 期初余额 + 本期借贷差（口径与科目余额表一致）</li>
 *   <li>损益类科目（category=INCOME/COST）取本年累计，资产负债类取期末时点</li>
 * </ul>
 *
 * @author ruoyi
 */
public interface FinReportMapper
{
    /**
     * 按科目类别汇总发生额（资产负债表 / 利润表共用）
     *
     * @param period       会计期间
     * @param yearPrefix   年份前缀（如 2026），损益类取本年累计用
     * @param yearToDate   true=损益类取本年累计，false=取本期发生
     * @return 按 category 与 direction 汇总
     */
    List<Map<String, Object>> sumByCategory(@Param("period") String period,
                                            @Param("yearPrefix") String yearPrefix,
                                            @Param("yearToDate") boolean yearToDate);

    /**
     * 资产负债表明细行（含期初、本期发生、期末）
     *
     * @param period 会计期间
     * @return 明细行
     */
    List<Map<String, Object>> selectBalanceSheet(@Param("period") String period);

    /**
     * 利润表明细行（含本期金额、本年累计）
     *
     * @param period     会计期间
     * @param yearPrefix 年份前缀
     * @return 明细行
     */
    List<Map<String, Object>> selectIncomeStatement(@Param("period") String period,
                                                    @Param("yearPrefix") String yearPrefix);

    /**
     * 现金流量表：按科目现金流量分类汇总本期发生额
     *
     * @param period     会计期间
     * @param yearPrefix 年份前缀
     * @return 按 cash_flow_category 汇总
     */
    List<Map<String, Object>> selectCashFlow(@Param("period") String period,
                                             @Param("yearPrefix") String yearPrefix);

    /**
     * 现金流量表明细（按科目列出，便于核对）
     *
     * @param period 会计期间
     * @return 明细
     */
    List<Map<String, Object>> selectCashFlowDetail(@Param("period") String period);

    /**
     * 往来对账：应收按客户、应付按供应商汇总
     *
     * @return 应收汇总
     */
    List<Map<String, Object>> selectArBalance();

    /**
     * 应付汇总
     *
     * @return 应付汇总
     */
    List<Map<String, Object>> selectApBalance();

    /**
     * 账龄分析（应收）
     *
     * @param today 今日
     * @return 账龄分桶
     */
    List<Map<String, Object>> selectArAging(@Param("today") String today);

    /**
     * 账龄分析（应付）
     *
     * @param today 今日
     * @return 账龄分桶
     */
    List<Map<String, Object>> selectApAging(@Param("today") String today);

    /**
     * 会计恒等式校验（资产 = 负债 + 所有者权益）
     *
     * @param period 会计期间
     * @return 含 totalAsset / totalLiability / totalEquity / diff / balanced
     */
    Map<String, Object> checkBalanceEquation(@Param("period") String period);
}