package com.ruoyi.qms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.qms.domain.QmsAuditPlan;
import com.ruoyi.qms.service.IQmsAuditPlanService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/audit")
public class QmsAuditPlanController extends BaseController {
    @Autowired
    private IQmsAuditPlanService service;

    @PreAuthorize("@ss.hasPermi('qms:audit:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsAuditPlan plan) { startPage(); return getDataTable(service.selectAuditPlanList(plan)); }

    @Log(title = "内审计划", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:audit:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsAuditPlan plan) {
        ExcelUtil<QmsAuditPlan> util = new ExcelUtil<>(QmsAuditPlan.class);
        util.exportExcel(response, service.selectAuditPlanList(plan), "内审计划");
    }

    @PreAuthorize("@ss.hasPermi('qms:audit:query')")
    @GetMapping("/{auditPlanId}")
    public AjaxResult getInfo(@PathVariable Long auditPlanId) { return AjaxResult.success(service.selectAuditPlanById(auditPlanId)); }

    @Log(title = "内审计划", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:audit:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsAuditPlan plan) { return toAjax(service.insertAuditPlan(plan)); }

    @Log(title = "内审计划", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:audit:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsAuditPlan plan) { return toAjax(service.updateAuditPlan(plan)); }

    @Log(title = "内审计划", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:audit:remove')")
    @DeleteMapping("/{auditPlanIds}")
    public AjaxResult remove(@PathVariable Long[] auditPlanIds) { return toAjax(service.deleteAuditPlanByIds(auditPlanIds)); }
}
