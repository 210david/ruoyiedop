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
import com.ruoyi.safety.domain.SafetyTrainingPlan;
import com.ruoyi.safety.service.ISafetyTrainingPlanService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/training/plan")
public class SafetyTrainingPlanController extends BaseController
{
    @Autowired
    private ISafetyTrainingPlanService safetyTrainingPlanService;

    @PreAuthorize("@ss.hasPermi('safety:training:plan:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyTrainingPlan plan)
    {
        startPage();
        List<SafetyTrainingPlan> list = safetyTrainingPlanService.selectSafetyTrainingPlanList(plan);
        return getDataTable(list);
    }

    @Log(title = "培训计划", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:plan:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyTrainingPlan plan)
    {
        List<SafetyTrainingPlan> list = safetyTrainingPlanService.selectSafetyTrainingPlanList(plan);
        ExcelUtil<SafetyTrainingPlan> util = new ExcelUtil<>(SafetyTrainingPlan.class);
        util.exportExcel(response, list, "培训计划");
    }

    @PreAuthorize("@ss.hasPermi('safety:training:plan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(safetyTrainingPlanService.selectSafetyTrainingPlanById(planId));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:plan:add')")
    @Log(title = "培训计划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyTrainingPlan plan)
    {
        return toAjax(safetyTrainingPlanService.insertSafetyTrainingPlan(plan));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:plan:edit')")
    @Log(title = "培训计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyTrainingPlan plan)
    {
        return toAjax(safetyTrainingPlanService.updateSafetyTrainingPlan(plan));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:plan:remove')")
    @Log(title = "培训计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(safetyTrainingPlanService.deleteSafetyTrainingPlanByIds(planIds));
    }

    /** 开始执行培训计划 */
    @PreAuthorize("@ss.hasPermi('safety:training:plan:edit')")
    @Log(title = "培训计划", businessType = BusinessType.UPDATE)
    @PutMapping("/start/{planId}")
    public AjaxResult start(@PathVariable Long planId)
    {
        return toAjax(safetyTrainingPlanService.startPlan(planId));
    }

    /** 完成培训计划 */
    @PreAuthorize("@ss.hasPermi('safety:training:plan:edit')")
    @Log(title = "培训计划", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{planId}")
    public AjaxResult complete(@PathVariable Long planId)
    {
        return toAjax(safetyTrainingPlanService.completePlan(planId));
    }

    /** 取消培训计划 */
    @PreAuthorize("@ss.hasPermi('safety:training:plan:edit')")
    @Log(title = "培训计划", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{planId}")
    public AjaxResult cancel(@PathVariable Long planId)
    {
        return toAjax(safetyTrainingPlanService.cancelPlan(planId));
    }
}