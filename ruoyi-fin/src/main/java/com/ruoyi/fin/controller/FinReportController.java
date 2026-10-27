package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fin.service.IFinReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 财务报表 Controller
 *
 * <p><b>权限划分</b>（perms 已在 fin_02_menu.sql 预置）：
 * <ul>
 *   <li>fin:report:assets    资产负债表</li>
 *   <li>fin:report:profit    利润表</li>
 *   <li>fin:report:cashflow  现金流量表</li>
 *   <li>fin:report:ledger    往来对账</li>
 *   <li>fin:report:aging     账龄分析</li>
 *   <li>fin:report:trial     试算平衡（P1 已实现，此处复用）</li>
 * </ul>
 *
 * <p><b>所有报表只读</b>，不提供任何写接口——报表是账簿的投影，
 * 修改只能通过凭证，改报表等于掩盖账务错误。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/report")
public class FinReportController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(FinReportController.class);

    @Autowired
    private IFinReportService reportService;

    /**
     * 资产负债表
     */
    @PreAuthorize("@ss.hasPermi('fin:report:assets')")
    @GetMapping("/balanceSheet")
    public AjaxResult balanceSheet(@RequestParam String period)
    {
        return AjaxResult.success(reportService.balanceSheet(period));
    }

    /**
     * 利润表
     */
    @PreAuthorize("@ss.hasPermi('fin:report:profit')")
    @GetMapping("/incomeStatement")
    public AjaxResult incomeStatement(@RequestParam String period)
    {
        return AjaxResult.success(reportService.incomeStatement(period));
    }

    /**
     * 现金流量表
     */
    @PreAuthorize("@ss.hasPermi('fin:report:cashflow')")
    @GetMapping("/cashFlow")
    public AjaxResult cashFlow(@RequestParam String period)
    {
        return AjaxResult.success(reportService.cashFlow(period));
    }

    /**
     * 往来对账（应收 + 应付）
     */
    @PreAuthorize("@ss.hasPermi('fin:report:ledger')")
    @GetMapping("/reconciliation")
    public AjaxResult reconciliation()
    {
        return AjaxResult.success(reportService.reconciliation());
    }

    /**
     * 账龄分析
     */
    @PreAuthorize("@ss.hasPermi('fin:report:aging')")
    @GetMapping("/aging")
    public AjaxResult aging(@RequestParam(required = false) String today)
    {
        return AjaxResult.success(reportService.aging(today));
    }

    /**
     * 会计恒等式校验
     *
     * <p>资产 = 负债 + 所有者权益。差额不为零说明凭证编制有问题，
     * 属于报表可信度的前置检查。</p>
     */
    @PreAuthorize("@ss.hasPermi('fin:report:assets')")
    @GetMapping("/balanceCheck")
    public AjaxResult balanceCheck(@RequestParam String period)
    {
        Map<String, Object> result = reportService.balanceCheck(period);
        boolean balanced = Boolean.TRUE.equals(result.get("balanced"));
        if (!balanced)
        {
            log.warn("会计恒等式不平衡：期间 {}，差额 {}", period, result.get("diff"));
        }
        return AjaxResult.success(result);
    }

    // ==================== 报表导出（D-01 修复） ====================

    /**
     * 导出当前报表为 Excel。
     *
     * <p>reportType: assets/profit/cashflow/reconcile/aging（与页面 Tab 对应）。
     * 金额以文本写入——导出用于存档与打印，不参与二次计算。</p>
     */
    @Log(title = "财务报表导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:report:export')")
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response,
                       @RequestParam String reportType,
                       @RequestParam String period) throws Exception
    {
        Map<String, Object> data;
        String sheetName;
        try (XSSFWorkbook wb = new XSSFWorkbook())
        {
            Sheet sheet;
            int[] rowIdx = {0};

            switch (reportType)
            {
                case "assets":
                {
                    sheetName = "资产负债表";
                    sheet = wb.createSheet(sheetName);
                    Map<String, Object> r = reportService.balanceSheet(period);
                    writeRow(sheet, rowIdx, "资产", "科目编码", "科目名称", "期末余额");
                    for (Map<String, Object> row : asList(r.get("assetRows")))
                    {
                        writeRow(sheet, rowIdx, "资产", str(row.get("accountCode")),
                                str(row.get("accountName")), str(row.get("closingBalance")));
                    }
                    writeRow(sheet, rowIdx, "资产总计", "", "", str(r.get("totalAsset")));
                    writeRow(sheet, rowIdx, "", "", "", "");
                    writeRow(sheet, rowIdx, "负债", "科目编码", "科目名称", "期末余额");
                    for (Map<String, Object> row : asList(r.get("liabilityRows")))
                    {
                        writeRow(sheet, rowIdx, "负债", str(row.get("accountCode")),
                                str(row.get("accountName")), str(num(row.get("closingBalance")).negate()));
                    }
                    for (Map<String, Object> row : asList(r.get("equityRows")))
                    {
                        writeRow(sheet, rowIdx, "所有者权益", str(row.get("accountCode")),
                                str(row.get("accountName")), str(num(row.get("closingBalance")).negate()));
                    }
                    writeRow(sheet, rowIdx, "负债和权益总计", "", "", str(num(r.get("totalLiability")).add(num(r.get("totalEquity")))));
                    break;
                }
                case "profit":
                {
                    sheetName = "利润表";
                    sheet = wb.createSheet(sheetName);
                    Map<String, Object> r = reportService.incomeStatement(period);
                    writeRow(sheet, rowIdx, "项目", "科目编码", "科目名称", "本年累计");
                    for (Map<String, Object> row : asList(r.get("incomeRows")))
                    {
                        writeRow(sheet, rowIdx, "收入", str(row.get("accountCode")),
                                str(row.get("accountName")), str(row.get("ytdAmount")));
                    }
                    writeRow(sheet, rowIdx, "收入合计", "", "", str(r.get("totalRevenue")));
                    for (Map<String, Object> row : asList(r.get("costRows")))
                    {
                        writeRow(sheet, rowIdx, "成本费用", str(row.get("accountCode")),
                                str(row.get("accountName")), str(row.get("ytdAmount")));
                    }
                    writeRow(sheet, rowIdx, "成本费用合计", "", "", str(r.get("totalCost")));
                    writeRow(sheet, rowIdx, "利润总额", "", "", str(r.get("totalProfit")));
                    break;
                }
                case "cashflow":
                {
                    sheetName = "现金流量表";
                    sheet = wb.createSheet(sheetName);
                    Map<String, Object> r = reportService.cashFlow(period);
                    writeRow(sheet, rowIdx, "分类", "流入", "流出", "净额");
                    for (Map<String, Object> row : asList(r.get("summary")))
                    {
                        writeRow(sheet, rowIdx, cfLabel(str(row.get("cashFlowCategory"))),
                                str(row.get("inflowAmount")), str(row.get("outflowAmount")),
                                str(row.get("netAmount")));
                    }
                    writeRow(sheet, rowIdx, "现金及现金等价物净增加额", "", "", str(r.get("netIncrease")));
                    writeRow(sheet, rowIdx, "", "", "", "");
                    writeRow(sheet, rowIdx, "分类", "科目编码", "科目名称", "流入", "流出", "净额");
                    for (Map<String, Object> row : asList(r.get("detail")))
                    {
                        writeRow(sheet, rowIdx, cfLabel(str(row.get("cashFlowCategory"))),
                                str(row.get("accountCode")), str(row.get("accountName")),
                                str(row.get("inflowAmount")), str(row.get("outflowAmount")),
                                str(row.get("netAmount")));
                    }
                    break;
                }
                case "reconcile":
                {
                    sheetName = "往来对账";
                    sheet = wb.createSheet(sheetName);
                    Map<String, Object> r = reportService.reconciliation();
                    writeRow(sheet, rowIdx, "应收对账", "客户", "单数", "应收总额", "已收", "未收余额", "已提坏账");
                    for (Map<String, Object> row : asList(r.get("arList")))
                    {
                        writeRow(sheet, rowIdx, "应收", str(row.get("partnerName")), str(row.get("arCount")),
                                str(row.get("totalAmount")), str(row.get("receivedAmount")),
                                str(row.get("unreceivedAmount")), str(row.get("badDebtAmount")));
                    }
                    writeRow(sheet, rowIdx, "应收合计", "", "", "", "", str(r.get("totalAr")), "");
                    writeRow(sheet, rowIdx, "", "", "", "", "", "", "");
                    writeRow(sheet, rowIdx, "应付对账", "供应商", "单数", "应付总额", "已付", "未付余额");
                    for (Map<String, Object> row : asList(r.get("apList")))
                    {
                        writeRow(sheet, rowIdx, "应付", str(row.get("partnerName")), str(row.get("apCount")),
                                str(row.get("totalAmount")), str(row.get("paidAmount")),
                                str(row.get("unpaidAmount")));
                    }
                    writeRow(sheet, rowIdx, "应付合计", "", "", "", "", str(r.get("totalAp")));
                    break;
                }
                case "aging":
                {
                    sheetName = "账龄分析";
                    sheet = wb.createSheet(sheetName);
                    Map<String, Object> r = reportService.aging(null);
                    writeRow(sheet, rowIdx, "应收账龄", "区间", "笔数", "应收总额", "未收金额");
                    for (Map<String, Object> row : asList(r.get("arAging")))
                    {
                        writeRow(sheet, rowIdx, "应收", str(row.get("bucket")), str(row.get("cnt")),
                                str(row.get("totalAmount")), str(row.get("unreceivedAmount")));
                    }
                    writeRow(sheet, rowIdx, "", "", "", "", "");
                    writeRow(sheet, rowIdx, "应付账龄", "区间", "笔数", "应付总额", "未付金额");
                    for (Map<String, Object> row : asList(r.get("apAging")))
                    {
                        writeRow(sheet, rowIdx, "应付", str(row.get("bucket")), str(row.get("cnt")),
                                str(row.get("totalAmount")), str(row.get("unpaidAmount")));
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("未知报表类型：" + reportType);
            }

            // 列宽
            for (int i = 0; i < 6; i++)
            {
                sheet.setColumnWidth(i, 16 * 256);
            }
            sheet.setColumnWidth(2, 24 * 256);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode(sheetName + "_" + period + ".xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            wb.write(response.getOutputStream());
        }
    }

    private void writeRow(Sheet sheet, int[] rowIdx, Object... cells)
    {
        Row row = sheet.createRow(rowIdx[0]++);
        for (int i = 0; i < cells.length; i++)
        {
            Cell cell = row.createCell(i);
            Object v = cells[i];
            if (v instanceof BigDecimal)
            {
                cell.setCellValue(((BigDecimal) v).toPlainString());
            }
            else
            {
                cell.setCellValue(v == null ? "" : v.toString());
            }
        }
    }

    private java.util.List<Map<String, Object>> asList(Object o)
    {
        if (o instanceof List)
        {
            return (List<Map<String, Object>>) o;
        }
        return java.util.Collections.emptyList();
    }

    private BigDecimal num(Object o)
    {
        if (o == null) { return BigDecimal.ZERO; }
        if (o instanceof BigDecimal) { return (BigDecimal) o; }
        return new BigDecimal(o.toString());
    }

    private String str(Object o)
    {
        return o == null ? "" : o.toString();
    }

    private String cfLabel(String c)
    {
        return switch (c) { case "OPERATING" -> "经营活动"; case "INVESTING" -> "投资活动";
            case "FINANCING" -> "筹资活动"; default -> c; };
    }
}
