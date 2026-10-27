package com.ruoyi.fin.service.impl;

import com.ruoyi.fin.mapper.FinReportMapper;
import com.ruoyi.fin.service.IFinReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务报表Service 实现
 *
 * <p><b>取数口径</b>（与 P1 试算平衡严格一致）：
 * 只统计已审核/已记账凭证，草稿不进报表，确保报表与账簿勾稽相符。</p>
 *
 * @author ruoyi
 */
@Service
public class FinReportServiceImpl implements IFinReportService
{
    private static final Logger log = LoggerFactory.getLogger(FinReportServiceImpl.class);

    @Autowired
    private FinReportMapper reportMapper;

    @Override
    public Map<String, Object> balanceSheet(String period)
    {
        List<Map<String, Object>> rows = reportMapper.selectBalanceSheet(period);

        List<Map<String, Object>> assetRows = new ArrayList<>();
        List<Map<String, Object>> liabilityRows = new ArrayList<>();
        List<Map<String, Object>> equityRows = new ArrayList<>();
        BigDecimal totalAsset = BigDecimal.ZERO;
        BigDecimal totalLiability = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (Map<String, Object> row : rows)
        {
            String cat = str(row.get("category"));
            BigDecimal closing = num(row.get("closingBalance"));
            if ("ASSET".equals(cat))
            {
                assetRows.add(row);
                totalAsset = totalAsset.add(closing);
            }
            else if ("LIABILITY".equals(cat))
            {
                // 负债类取负号，使其为正的负债余额
                liabilityRows.add(row);
                totalLiability = totalLiability.add(closing.negate());
            }
            else if ("EQUITY".equals(cat))
            {
                equityRows.add(row);
                totalEquity = totalEquity.add(closing.negate());
            }
        }

        Map<String, Object> r = new HashMap<>();
        r.put("period", period);
        r.put("assetRows", assetRows);
        r.put("liabilityRows", liabilityRows);
        r.put("equityRows", equityRows);
        r.put("totalAsset", totalAsset);
        r.put("totalLiability", totalLiability);
        r.put("totalEquity", totalEquity);
        r.put("balanced", totalAsset.compareTo(totalLiability.add(totalEquity)) == 0);
        return r;
    }

    @Override
    public Map<String, Object> incomeStatement(String period)
    {
        List<Map<String, Object>> rows =
                reportMapper.selectIncomeStatement(period, yearPrefix(period));

        List<Map<String, Object>> incomeRows = new ArrayList<>();
        List<Map<String, Object>> costRows = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map<String, Object> row : rows)
        {
            String cat = str(row.get("category"));
            String code = str(row.get("accountCode"));
            String dir = str(row.get("direction"));
            BigDecimal ytd = num(row.get("ytdAmount"));

            if ("COST".equals(cat))
            {
                // 成本类（5001/5101）：借方为成本
                costRows.add(row);
                totalCost = "DEBIT".equals(dir) ? totalCost.add(ytd) : totalCost.subtract(ytd);
            }
            else if ("INCOME".equals(cat))
            {
                if (isExpenseCode(code))
                {
                    // 费用类（6401/6601/6602/6603/6801）：虽挂 INCOME 类别，
                    // 实质是费用，借方为费用，计入成本侧
                    costRows.add(row);
                    totalCost = "DEBIT".equals(dir) ? totalCost.add(ytd) : totalCost.subtract(ytd);
                }
                else
                {
                    // 真正的收入类（6001）：贷方为收入
                    incomeRows.add(row);
                    totalRevenue = "CREDIT".equals(dir)
                            ? totalRevenue.add(ytd) : totalRevenue.subtract(ytd);
                }
            }
        }

        BigDecimal totalProfit = totalRevenue.subtract(totalCost);

        Map<String, Object> r = new HashMap<>();
        r.put("period", period);
        r.put("incomeRows", incomeRows);
        r.put("costRows", costRows);
        r.put("totalRevenue", totalRevenue);
        r.put("totalCost", totalCost);
        r.put("totalProfit", totalProfit);
        return r;
    }

    @Override
    public Map<String, Object> cashFlow(String period)
    {
        List<Map<String, Object>> summary = reportMapper.selectCashFlow(period, yearPrefix(period));
        List<Map<String, Object>> detail = reportMapper.selectCashFlowDetail(period);

        BigDecimal operating = BigDecimal.ZERO;
        BigDecimal investing = BigDecimal.ZERO;
        BigDecimal financing = BigDecimal.ZERO;

        for (Map<String, Object> row : summary)
        {
            String cat = str(row.get("cashFlowCategory"));
            BigDecimal net = num(row.get("netAmount"));
            if ("OPERATING".equals(cat))
            {
                operating = operating.add(net);
            }
            else if ("INVESTING".equals(cat))
            {
                investing = investing.add(net);
            }
            else if ("FINANCING".equals(cat))
            {
                financing = financing.add(net);
            }
        }

        Map<String, Object> r = new HashMap<>();
        r.put("period", period);
        r.put("summary", summary);
        r.put("detail", detail);
        r.put("operating", operating);
        r.put("investing", investing);
        r.put("financing", financing);
        r.put("netIncrease", operating.add(investing).add(financing));
        return r;
    }

    @Override
    public Map<String, Object> reconciliation()
    {
        List<Map<String, Object>> arList = reportMapper.selectArBalance();
        List<Map<String, Object>> apList = reportMapper.selectApBalance();

        BigDecimal totalAr = BigDecimal.ZERO;
        for (Map<String, Object> row : arList)
        {
            totalAr = totalAr.add(num(row.get("unreceivedAmount")));
        }
        BigDecimal totalAp = BigDecimal.ZERO;
        for (Map<String, Object> row : apList)
        {
            totalAp = totalAp.add(num(row.get("unpaidAmount")));
        }

        Map<String, Object> r = new HashMap<>();
        r.put("arList", arList);
        r.put("apList", apList);
        r.put("totalAr", totalAr);
        r.put("totalAp", totalAp);
        return r;
    }

    @Override
    public Map<String, Object> aging(String today)
    {
        String d = (today == null || today.trim().isEmpty())
                ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : today;
        Map<String, Object> r = new HashMap<>();
        r.put("today", d);
        r.put("arAging", reportMapper.selectArAging(d));
        r.put("apAging", reportMapper.selectApAging(d));
        return r;
    }

    @Override
    public Map<String, Object> balanceCheck(String period)
    {
        Map<String, Object> row = reportMapper.checkBalanceEquation(period);
        BigDecimal asset = num(row == null ? null : row.get("totalAsset"));
        BigDecimal liability = num(row == null ? null : row.get("totalLiability"));
        BigDecimal equity = num(row == null ? null : row.get("totalEquity"));
        BigDecimal diff = asset.subtract(liability).subtract(equity);

        Map<String, Object> r = new HashMap<>();
        r.put("period", period);
        r.put("totalAsset", asset);
        r.put("totalLiability", liability);
        r.put("totalEquity", equity);
        r.put("rightTotal", liability.add(equity));
        r.put("diff", diff);
        r.put("balanced", diff.compareTo(BigDecimal.ZERO) == 0);
        return r;
    }

    // ==================== 私有方法 ====================

    /**
     * 费用科目：虽挂在 INCOME 类别下，但实质是费用，须计入成本侧。
     */
    private boolean isExpenseCode(String code)
    {
        return "6401".equals(code) || "6601".equals(code)
                || "6602".equals(code) || "6603".equals(code)
                || "6801".equals(code);
    }

    private String yearPrefix(String period)
    {
        return period != null && period.length() >= 4 ? period.substring(0, 4) : null;
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