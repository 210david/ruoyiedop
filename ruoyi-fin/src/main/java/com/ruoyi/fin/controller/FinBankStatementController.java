package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinBankStatement;
import com.ruoyi.fin.domain.FinBankStatementItem;
import com.ruoyi.fin.service.IFinBankStatementService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 银行对账单 Controller
 *
 * <p><b>对账是资金安全的最后一道防线</b>：
 * 通过账实核对发现未达账项（银行已动我方未记 / 我方已记银行未动），
 * 及时发现资金异常。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/fund/statement")
public class FinBankStatementController extends BaseController
{
    @Autowired
    private IFinBankStatementService service;

    /**
     * 查询对账单列表
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/list")
    public TableDataInfo list(FinBankStatement statement)
    {
        startPage();
        return getDataTable(service.selectStatementList(statement));
    }

    /**
     * 查询对账单详情（含明细与勾稽统计）
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/{statementId}")
    public AjaxResult detail(@PathVariable("statementId") Long statementId)
    {
        return AjaxResult.success(service.selectStatementDetail(statementId));
    }

    /**
     * 导入对账单（含银行流水明细）
     *
     * <p>明细格式：trans_amount 收入为正、支出为负。</p>
     */
    @Log(title = "银行对账单导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @PostMapping("/import")
    public AjaxResult importStatement(@RequestBody Map<String, Object> body)
    {
        FinBankStatement statement = convertStatement(body);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) body.get("items");
        List<FinBankStatementItem> items = new java.util.ArrayList<>();
        if (itemList != null)
        {
            for (Map<String, Object> m : itemList)
            {
                items.add(convertItem(m));
            }
        }
        return AjaxResult.success(service.importStatement(statement, items));
    }

    /**
     * 执行自动匹配
     */
    @Log(title = "对账自动匹配", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @PostMapping("/autoMatch/{statementId}")
    public AjaxResult autoMatch(@PathVariable("statementId") Long statementId)
    {
        return AjaxResult.success(service.autoMatch(statementId));
    }

    /**
     * 手动匹配
     */
    @Log(title = "对账手动匹配", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @PutMapping("/match/{itemId}")
    public AjaxResult manualMatch(@PathVariable("itemId") Long itemId,
                                  @RequestParam Long flowId)
    {
        return toAjax(service.manualMatch(itemId, flowId));
    }

    /**
     * 解除匹配
     */
    @Log(title = "对账解除匹配", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @PutMapping("/unmatch/{itemId}")
    public AjaxResult unmatch(@PathVariable("itemId") Long itemId)
    {
        return toAjax(service.unmatch(itemId));
    }

    /**
     * 查询候选匹配流水（给手动匹配用）
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/candidates")
    public AjaxResult candidates(@RequestParam Long accountId,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date)
    {
        return AjaxResult.success(service.matchCandidates(accountId, amount, date));
    }

    /**
     * 执行余额核对
     */
    @Log(title = "资金对账", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @PostMapping("/reconcile/{statementId}")
    public AjaxResult reconcile(@PathVariable("statementId") Long statementId)
    {
        return AjaxResult.success(service.reconcile(statementId));
    }

    /**
     * 未达账项清单（双向）
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/unreconciled/{statementId}")
    public AjaxResult unreconciled(@PathVariable("statementId") Long statementId)
    {
        return AjaxResult.success(service.unreconciled(statementId));
    }

    /**
     * 删除对账单
     */
    @Log(title = "银行对账单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @DeleteMapping("/{statementIds}")
    public AjaxResult remove(@PathVariable Long[] statementIds)
    {
        return toAjax(service.deleteStatementByIds(statementIds));
    }

    /**
     * 导出对账单
     */
    @Log(title = "银行对账单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:fund:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinBankStatement statement)
    {
        ExcelUtil<FinBankStatement> util = new ExcelUtil<>(FinBankStatement.class);
        util.exportExcel(response, service.selectStatementList(statement), "银行对账单");
    }

    // ==================== 私有方法 ====================

    private FinBankStatement convertStatement(Map<String, Object> m)
    {
        FinBankStatement s = new FinBankStatement();
        s.setAccountId(toLong(m.get("accountId")));
        s.setStatementDate(toDate(m.get("statementDate")));
        s.setBankBeginBalance(toDec(m.get("bankBeginBalance")));
        s.setBeginBalance(toDec(m.get("beginBalance")));
        s.setFileName((String) m.get("fileName"));
        s.setRemark((String) m.get("remark"));
        if (s.getAccountId() == null)
        {
            throw new IllegalArgumentException("请选择资金账户");
        }
        return s;
    }

    private FinBankStatementItem convertItem(Map<String, Object> m)
    {
        FinBankStatementItem i = new FinBankStatementItem();
        i.setTransDate(toDate(m.get("transDate")));
        i.setTransType((String) m.get("transType"));
        i.setTransAmount(toDec(m.get("transAmount")));
        i.setBalanceAfter(toDec(m.get("balanceAfter")));
        i.setCounterparty((String) m.get("counterparty"));
        i.setCounterpartyAccount((String) m.get("counterpartyAccount"));
        i.setSummary((String) m.get("summary"));
        i.setVoucherNo((String) m.get("voucherNo"));
        if (i.getTransDate() == null)
        {
            throw new IllegalArgumentException("银行流水明细的交易日期不能为空");
        }
        return i;
    }

    private Date toDate(Object o)
    {
        if (o == null)
        {
            return null;
        }
        if (o instanceof Date)
        {
            return (Date) o;
        }
        String s = o.toString();
        if (s.length() > 10)
        {
            s = s.substring(0, 10);
        }
        return java.sql.Date.valueOf(s);
    }

    private BigDecimal toDec(Object o)
    {
        if (o == null)
        {
            return null;
        }
        if (o instanceof BigDecimal)
        {
            return (BigDecimal) o;
        }
        String s = o.toString().trim();
        if (s.isEmpty())
        {
            return null;
        }
        return new BigDecimal(s);
    }

    private Long toLong(Object o)
    {
        if (o == null)
        {
            return null;
        }
        if (o instanceof Number)
        {
            return ((Number) o).longValue();
        }
        return Long.valueOf(o.toString());
    }
}