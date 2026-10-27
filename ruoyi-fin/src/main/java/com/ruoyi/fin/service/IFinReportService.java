package com.ruoyi.fin.service;

import java.util.List;
import java.util.Map;

/**
 * 财务报表 Service 接口
 *
 * @author ruoyi
 */
public interface IFinReportService
{
    /**
     * 资产负债表
     *
     * @param period 会计期间
     * @return 含 assetRows / liabilityRows / equityRows / totalAsset / totalLiability / totalEquity
     */
    Map<String, Object> balanceSheet(String period);

    /**
     * 利润表
     *
     * @param period 会计期间
     * @return 含 incomeRows / costRows / totalRevenue / totalCost / totalProfit
     */
    Map<String, Object> incomeStatement(String period);

    /**
     * 现金流量表
     *
     * @param period 会计期间
     * @return 含 operating / investing / financing / netIncrease
     */
    Map<String, Object> cashFlow(String period);

    /**
     * 往来对账（应收 + 应付）
     *
     * @return 含 arList / apList / totalAr / totalAp
     */
    Map<String, Object> reconciliation();

    /**
     * 账龄分析（应收 + 应付）
     *
     * @param today 今日（yyyy-MM-dd）
     * @return 含 arAging / apAging
     */
    Map<String, Object> aging(String today);

    /**
     * 会计恒等式校验
     *
     * @param period 会计期间
     * @return 含 totalAsset / totalLiability / totalEquity / diff / balanced
     */
    Map<String, Object> balanceCheck(String period);
}