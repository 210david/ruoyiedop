package com.ruoyi.hr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.HrLeaveBalance;
import com.ruoyi.hr.service.IHrLeaveBalanceService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/leaveBalance")
public class HrLeaveBalanceController extends BaseController
{
    @Autowired
    private IHrLeaveBalanceService hrLeaveBalanceService;

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrLeaveBalance hrLeaveBalance)
    {
        startPage();
        List<HrLeaveBalance> list = hrLeaveBalanceService.selectHrLeaveBalanceList(hrLeaveBalance);
        return getDataTable(list);
    }

    @Log(title = "假期余额", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrLeaveBalance hrLeaveBalance)
    {
        List<HrLeaveBalance> list = hrLeaveBalanceService.selectHrLeaveBalanceList(hrLeaveBalance);
        ExcelUtil<HrLeaveBalance> util = new ExcelUtil<>(HrLeaveBalance.class);
        util.exportExcel(response, list, "假期余额");
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:query')")
    @GetMapping(value = "/{balanceId}")
    public AjaxResult getInfo(@PathVariable("balanceId") Long balanceId)
    {
        return AjaxResult.success(hrLeaveBalanceService.selectHrLeaveBalanceById(balanceId));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:add')")
    @Log(title = "假期余额", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrLeaveBalance hrLeaveBalance)
    {
        return toAjax(hrLeaveBalanceService.insertHrLeaveBalance(hrLeaveBalance));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:edit')")
    @Log(title = "假期余额", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrLeaveBalance hrLeaveBalance)
    {
        return toAjax(hrLeaveBalanceService.updateHrLeaveBalance(hrLeaveBalance));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:remove')")
    @Log(title = "假期余额", businessType = BusinessType.DELETE)
    @DeleteMapping("/{balanceIds}")
    public AjaxResult remove(@PathVariable Long[] balanceIds)
    {
        return toAjax(hrLeaveBalanceService.deleteHrLeaveBalanceByIds(balanceIds));
    }
}
