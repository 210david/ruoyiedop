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
import com.ruoyi.hr.domain.HrSalaryPlan;
import com.ruoyi.hr.service.IHrSalaryPlanService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/salaryPlan")
public class HrSalaryPlanController extends BaseController
{
    @Autowired
    private IHrSalaryPlanService hrSalaryPlanService;

    @PreAuthorize("@ss.hasPermi('hr:salaryPlan:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSalaryPlan hrSalaryPlan)
    {
        startPage();
        List<HrSalaryPlan> list = hrSalaryPlanService.selectHrSalaryPlanList(hrSalaryPlan);
        return getDataTable(list);
    }

    @Log(title = "定薪调薪", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:salaryPlan:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSalaryPlan hrSalaryPlan)
    {
        List<HrSalaryPlan> list = hrSalaryPlanService.selectHrSalaryPlanList(hrSalaryPlan);
        ExcelUtil<HrSalaryPlan> util = new ExcelUtil<>(HrSalaryPlan.class);
        util.exportExcel(response, list, "定薪调薪");
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryPlan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(hrSalaryPlanService.selectHrSalaryPlanById(planId));
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryPlan:add')")
    @Log(title = "定薪调薪", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSalaryPlan hrSalaryPlan)
    {
        return toAjax(hrSalaryPlanService.insertHrSalaryPlan(hrSalaryPlan));
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryPlan:edit')")
    @Log(title = "定薪调薪", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSalaryPlan hrSalaryPlan)
    {
        return toAjax(hrSalaryPlanService.updateHrSalaryPlan(hrSalaryPlan));
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryPlan:remove')")
    @Log(title = "定薪调薪", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(hrSalaryPlanService.deleteHrSalaryPlanByIds(planIds));
    }
}
