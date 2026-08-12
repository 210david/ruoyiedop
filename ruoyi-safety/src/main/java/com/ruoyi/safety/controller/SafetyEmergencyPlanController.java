package com.ruoyi.safety.controller;

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
import com.ruoyi.safety.domain.SafetyEmergencyPlan;
import com.ruoyi.safety.service.ISafetyEmergencyPlanService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/emergency/plan")
public class SafetyEmergencyPlanController extends BaseController
{
    @Autowired
    private ISafetyEmergencyPlanService safetyEmergencyPlanService;

    @PreAuthorize("@ss.hasPermi('safety:emergency:plan:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyEmergencyPlan plan)
    {
        startPage();
        List<SafetyEmergencyPlan> list = safetyEmergencyPlanService.selectSafetyEmergencyPlanList(plan);
        return getDataTable(list);
    }

    @Log(title = "应急预案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:emergency:plan:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyEmergencyPlan plan)
    {
        List<SafetyEmergencyPlan> list = safetyEmergencyPlanService.selectSafetyEmergencyPlanList(plan);
        ExcelUtil<SafetyEmergencyPlan> util = new ExcelUtil<>(SafetyEmergencyPlan.class);
        util.exportExcel(response, list, "应急预案");
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:plan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(safetyEmergencyPlanService.selectSafetyEmergencyPlanById(planId));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:plan:add')")
    @Log(title = "应急预案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyEmergencyPlan plan)
    {
        return toAjax(safetyEmergencyPlanService.insertSafetyEmergencyPlan(plan));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:plan:edit')")
    @Log(title = "应急预案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyEmergencyPlan plan)
    {
        return toAjax(safetyEmergencyPlanService.updateSafetyEmergencyPlan(plan));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:plan:remove')")
    @Log(title = "应急预案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(safetyEmergencyPlanService.deleteSafetyEmergencyPlanByIds(planIds));
    }
}