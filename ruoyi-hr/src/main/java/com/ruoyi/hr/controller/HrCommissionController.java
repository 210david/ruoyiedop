package com.ruoyi.hr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hr.domain.HrCommissionBill;
import com.ruoyi.hr.domain.HrCommissionPlan;
import com.ruoyi.hr.service.IHrCommissionService;

/**
 * 提成方案/核算表 Controller（需求三）
 */
@RestController
@RequestMapping("/hr/commission")
public class HrCommissionController extends BaseController {
    @Autowired
    private IHrCommissionService commissionService;

    // ---- 方案 ----
    @PreAuthorize("@ss.hasPermi('hr:commission:list')")
    @GetMapping("/plan/list")
    public TableDataInfo planList(HrCommissionPlan plan) { return getDataTable(commissionService.selectPlanList(plan)); }

    @PreAuthorize("@ss.hasPermi('hr:commission:query')")
    @GetMapping("/plan/{planId}")
    public AjaxResult getPlan(@PathVariable Long planId) { return success(commissionService.selectPlanById(planId)); }

    @Log(title = "提成方案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:commission:add')")
    @PostMapping("/plan")
    public AjaxResult addPlan(@RequestBody HrCommissionPlan plan) { return toAjax(commissionService.insertPlan(plan)); }

    @Log(title = "提成方案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:commission:edit')")
    @PutMapping("/plan")
    public AjaxResult editPlan(@RequestBody HrCommissionPlan plan) { return toAjax(commissionService.updatePlan(plan)); }

    @Log(title = "提成方案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('hr:commission:remove')")
    @DeleteMapping("/plan/{planIds}")
    public AjaxResult removePlan(@PathVariable Long[] planIds) { return toAjax(commissionService.deletePlanByIds(planIds)); }

    // ---- 核算表 ----
    @PreAuthorize("@ss.hasPermi('hr:commission:list')")
    @GetMapping("/bill/list")
    public TableDataInfo billList(HrCommissionBill bill) { return getDataTable(commissionService.selectBillList(bill)); }

    @Log(title = "生成提成核算表", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:commission:generate')")
    @PostMapping("/bill/generate/{period}")
    public AjaxResult generate(@PathVariable String period) { return success(commissionService.generateBills(period)); }

    @Log(title = "提成核算确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:commission:confirm')")
    @PutMapping("/bill/confirm/{billId}")
    public AjaxResult confirm(@PathVariable Long billId) { return toAjax(commissionService.confirmBill(billId)); }

    @Log(title = "提成核算锁定", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:commission:confirm')")
    @PutMapping("/bill/lock/{period}")
    public AjaxResult lock(@PathVariable String period) { return toAjax(commissionService.lockBills(period)); }

    @Log(title = "提成核算删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('hr:commission:remove')")
    @DeleteMapping("/bill/{period}")
    public AjaxResult removeBills(@PathVariable String period) { return toAjax(commissionService.deleteBills(period)); }
}
