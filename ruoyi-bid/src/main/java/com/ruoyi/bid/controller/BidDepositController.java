package com.ruoyi.bid.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.bid.domain.BidDeposit;
import com.ruoyi.bid.service.IBidCommonService;

/**
 * 保证金台账 Controller
 */
@RestController
@RequestMapping("/bid/deposit")
public class BidDepositController extends BaseController
{
    @Autowired
    private IBidCommonService commonService;

    @PreAuthorize("@ss.hasPermi('bid:deposit:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidDeposit deposit)
    {
        startPage();
        List<BidDeposit> list = commonService.selectDepositList(deposit);
        return getDataTable(list);
    }

    @Log(title = "保证金台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bid:deposit:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BidDeposit deposit)
    {
        List<BidDeposit> list = commonService.selectDepositList(deposit);
        ExcelUtil<BidDeposit> util = new ExcelUtil<>(BidDeposit.class);
        util.exportExcel(response, list, "保证金台账数据");
    }

    @PreAuthorize("@ss.hasPermi('bid:deposit:query')")
    @GetMapping("/{depositId}")
    public AjaxResult getInfo(@PathVariable("depositId") Long depositId)
    {
        return AjaxResult.success(commonService.selectDepositById(depositId));
    }

    @Log(title = "保证金台账", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:deposit:add')")
    @PostMapping
    public AjaxResult add(@org.springframework.validation.annotation.Validated @RequestBody BidDeposit deposit)
    {
        return toAjax(commonService.insertDeposit(deposit));
    }

    @Log(title = "保证金台账", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:deposit:edit')")
    @PutMapping
    public AjaxResult edit(@org.springframework.validation.annotation.Validated @RequestBody BidDeposit deposit)
    {
        return toAjax(commonService.updateDeposit(deposit));
    }

    @Log(title = "保证金台账", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('bid:deposit:remove')")
    @DeleteMapping("/{depositIds}")
    public AjaxResult remove(@PathVariable Long[] depositIds)
    {
        return toAjax(commonService.deleteDepositByIds(depositIds));
    }

    /** 登记退回 */
    @Log(title = "保证金退回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:deposit:edit')")
    @PutMapping("/refund/{depositId}")
    public AjaxResult refund(@PathVariable("depositId") Long depositId,
            @RequestParam String actualRefundTime, @RequestParam(required = false) String remark)
    {
        try
        {
            return toAjax(commonService.refundDeposit(depositId,
                    com.ruoyi.common.utils.DateUtils.parseDate(actualRefundTime), remark));
        }
        catch (Exception e)
        {
            return AjaxResult.error("退回时间格式不正确，应为 yyyy-MM-dd HH:mm:ss");
        }
    }
}
