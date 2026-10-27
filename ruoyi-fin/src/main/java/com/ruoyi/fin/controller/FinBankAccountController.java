package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinBankAccount;
import com.ruoyi.fin.domain.FinCashFlow;
import com.ruoyi.fin.service.IFinBankAccountService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资金管理 Controller
 *
 * <p><b>职责划分</b>：账户维护属出纳（fin:fund:add/edit），删除属财务经理（fin:fund:remove），
 * 流水登记属出纳（fin:fund:flow），对账属财务经理（fin:fund:reconcile）。</p>
 *
 * <p><b>账号安全</b>：银行账号密文入库，接口永不返回完整账号，
 * 仅返回 account_tail 供前端展示为 ****1234。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/fund")
public class FinBankAccountController extends BaseController
{
    @Autowired
    private IFinBankAccountService fundService;

    /**
     * 查询账户列表
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:list')")
    @GetMapping("/account/list")
    public TableDataInfo accountList(FinBankAccount account)
    {
        startPage();
        return getDataTable(fundService.selectAccountList(account));
    }

    /**
     * 查询账户详情（返回脱敏账号，不含密文）
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/account/{accountId}")
    public AjaxResult accountDetail(@PathVariable("accountId") Long accountId)
    {
        FinBankAccount account = fundService.selectAccountById(accountId);
        if (account == null)
        {
            return AjaxResult.error("资金账户不存在或已删除");
        }
        Map<String, Object> r = new HashMap<>();
        // 抹掉密文，接口层永不外发
        account.setAccountNumberEncrypt(null);
        r.put("account", account);
        r.put("totalBalance", fundService.totalBalance());
        return AjaxResult.success(r);
    }

    /**
     * 新增账户
     */
    @Log(title = "资金账户", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:fund:add')")
    @PostMapping("/account")
    public AjaxResult addAccount(@Validated @RequestBody FinBankAccount account)
    {
        return toAjax(fundService.insertAccount(account));
    }

    /**
     * 修改账户
     */
    @Log(title = "资金账户", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:fund:edit')")
    @PutMapping("/account")
    public AjaxResult editAccount(@Validated @RequestBody FinBankAccount account)
    {
        return toAjax(fundService.updateAccount(account));
    }

    /**
     * 删除账户（有流水或余额非零时拒绝）
     */
    @Log(title = "资金账户", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:fund:remove')")
    @DeleteMapping("/account/{accountIds}")
    public AjaxResult removeAccount(@PathVariable Long[] accountIds)
    {
        return toAjax(fundService.deleteAccountByIds(accountIds));
    }

    /**
     * 资金流水列表
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:list')")
    @GetMapping("/flow/list")
    public TableDataInfo flowList(FinCashFlow flow)
    {
        startPage();
        return getDataTable(fundService.selectFlowList(flow));
    }

    /**
     * 登记资金流水
     */
    @Log(title = "资金流水", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:fund:flow')")
    @PostMapping("/flow")
    public AjaxResult addFlow(@RequestBody FinCashFlow flow)
    {
        if (flow.getFlowDate() == null)
        {
            flow.setFlowDate(new Date());
        }
        return AjaxResult.success(fundService.insertFlow(flow));
    }

    /**
     * 账户间转账
     */
    @Log(title = "资金转账", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:fund:flow')")
    @PostMapping("/transfer")
    public AjaxResult transfer(@RequestParam Long fromAccountId,
                               @RequestParam Long toAccountId,
                               @RequestParam BigDecimal amount,
                               @RequestParam(required = false) String remark,
                               @RequestParam(required = false) String transferDate)
    {
        return AjaxResult.success(
                fundService.transfer(fromAccountId, toAccountId, amount,
                        parseDate(transferDate), remark));
    }

    /**
     * 解析 yyyy-MM-dd 日期，非法格式直接拒绝。
     */
    private Date parseDate(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return new Date();
        }
        try
        {
            return java.sql.Date.valueOf(java.time.LocalDate.parse(text.trim()));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("日期格式应为 yyyy-MM-dd：" + text);
        }
    }

    /**
     * 标记流水已对账
     */
    @Log(title = "资金对账", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:fund:reconcile')")
    @PutMapping("/flow/reconcile/{flowIds}")
    public AjaxResult reconcile(@PathVariable Long[] flowIds)
    {
        return toAjax(fundService.markReconciled(flowIds));
    }

    /**
     * 资金日报
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/dailyReport")
    public AjaxResult dailyReport(@RequestParam(required = false) String date)
    {
        return AjaxResult.success(fundService.dailyReport(parseDate(date)));
    }

    /**
     * 资金总余额
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/totalBalance")
    public AjaxResult totalBalance()
    {
        return AjaxResult.success(fundService.totalBalance());
    }

    /**
     * 期间资金汇总
     */
    @PreAuthorize("@ss.hasPermi('fin:fund:query')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(required = false) String period)
    {
        return AjaxResult.success(fundService.summary(period));
    }

    /**
     * 导出资金流水
     */
    @Log(title = "资金流水", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:fund:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinCashFlow flow)
    {
        ExcelUtil<FinCashFlow> util = new ExcelUtil<>(FinCashFlow.class);
        util.exportExcel(response, fundService.selectFlowList(flow), "资金流水数据");
    }

    /**
     * 导出账户
     */
    @Log(title = "资金账户", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:fund:export')")
    @PostMapping("/account/export")
    public void exportAccount(HttpServletResponse response, FinBankAccount account)
    {
        ExcelUtil<FinBankAccount> util = new ExcelUtil<>(FinBankAccount.class);
        util.exportExcel(response, fundService.selectAccountList(account), "资金账户数据");
    }
}