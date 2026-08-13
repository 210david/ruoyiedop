package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsAqlPlan;
import com.ruoyi.qms.service.IQmsAqlPlanService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/aqlplan")
public class QmsAqlPlanController extends BaseController {
    @Autowired
    private IQmsAqlPlanService service;

    @PreAuthorize("@ss.hasPermi('qms:aqlplan:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsAqlPlan plan) {
        startPage();
        return getDataTable(service.selectAqlPlanList(plan));
    }

    @Log(title = "抽样方案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:aqlplan:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsAqlPlan plan) {
        List<QmsAqlPlan> list = service.selectAqlPlanList(plan);
        ExcelUtil<QmsAqlPlan> util = new ExcelUtil<>(QmsAqlPlan.class);
        util.exportExcel(response, list, "AQL抽样方案");
    }

    @PreAuthorize("@ss.hasPermi('qms:aqlplan:query')")
    @GetMapping("/{planId}")
    public AjaxResult getInfo(@PathVariable Long planId) {
        return AjaxResult.success(service.selectAqlPlanById(planId));
    }

    @PreAuthorize("@ss.hasPermi('qms:aqlplan:query')")
    @GetMapping("/lookup")
    public AjaxResult lookup(@RequestParam String aqlLevel, @RequestParam String codeLetter,
                             @RequestParam(defaultValue = "1") String inspectLevel) {
        return AjaxResult.success(service.selectByAqlAndCode(aqlLevel, codeLetter, inspectLevel));
    }

    @Log(title = "抽样方案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:aqlplan:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsAqlPlan plan) {
        return toAjax(service.insertAqlPlan(plan));
    }

    @Log(title = "抽样方案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:aqlplan:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsAqlPlan plan) {
        return toAjax(service.updateAqlPlan(plan));
    }

    @Log(title = "抽样方案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:aqlplan:remove')")
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds) {
        return toAjax(service.deleteAqlPlanByIds(planIds));
    }
}
