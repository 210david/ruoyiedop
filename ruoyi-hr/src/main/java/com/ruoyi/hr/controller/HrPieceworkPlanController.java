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
import com.ruoyi.hr.domain.HrPieceworkPlan;
import com.ruoyi.hr.service.IHrPieceworkPlanService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/pieceworkPlan")
public class HrPieceworkPlanController extends BaseController
{
    @Autowired
    private IHrPieceworkPlanService hrPieceworkPlanService;

    @PreAuthorize("@ss.hasPermi('hr:piecework:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrPieceworkPlan hrPieceworkPlan)
    {
        startPage();
        List<HrPieceworkPlan> list = hrPieceworkPlanService.selectHrPieceworkPlanList(hrPieceworkPlan);
        return getDataTable(list);
    }

    @Log(title = "计件方案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:piecework:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrPieceworkPlan hrPieceworkPlan)
    {
        List<HrPieceworkPlan> list = hrPieceworkPlanService.selectHrPieceworkPlanList(hrPieceworkPlan);
        ExcelUtil<HrPieceworkPlan> util = new ExcelUtil<>(HrPieceworkPlan.class);
        util.exportExcel(response, list, "计件方案");
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:query')")
    @GetMapping(value = "/{pwPlanId}")
    public AjaxResult getInfo(@PathVariable("pwPlanId") Long pwPlanId)
    {
        return AjaxResult.success(hrPieceworkPlanService.selectHrPieceworkPlanById(pwPlanId));
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:add')")
    @Log(title = "计件方案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrPieceworkPlan hrPieceworkPlan)
    {
        return toAjax(hrPieceworkPlanService.insertHrPieceworkPlan(hrPieceworkPlan));
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:edit')")
    @Log(title = "计件方案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrPieceworkPlan hrPieceworkPlan)
    {
        return toAjax(hrPieceworkPlanService.updateHrPieceworkPlan(hrPieceworkPlan));
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:remove')")
    @Log(title = "计件方案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{pwPlanIds}")
    public AjaxResult remove(@PathVariable Long[] pwPlanIds)
    {
        return toAjax(hrPieceworkPlanService.deleteHrPieceworkPlanByIds(pwPlanIds));
    }
}
