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
import com.ruoyi.qms.domain.QmsSupplierAudit;
import com.ruoyi.qms.service.IQmsSupplierAuditService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/supplierAudit")
public class QmsSupplierAuditController extends BaseController {
    @Autowired
    private IQmsSupplierAuditService service;

    @PreAuthorize("@ss.hasPermi('qms:supplierAudit:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsSupplierAudit audit) { startPage(); return getDataTable(service.selectAuditList(audit)); }

    @Log(title = "供应商审核", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:supplierAudit:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsSupplierAudit audit) {
        ExcelUtil<QmsSupplierAudit> util = new ExcelUtil<>(QmsSupplierAudit.class);
        util.exportExcel(response, service.selectAuditList(audit), "供应商审核");
    }

    @PreAuthorize("@ss.hasPermi('qms:supplierAudit:query')")
    @GetMapping("/{auditId}")
    public AjaxResult getInfo(@PathVariable Long auditId) { return AjaxResult.success(service.selectAuditById(auditId)); }

    @Log(title = "供应商审核", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:supplierAudit:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsSupplierAudit audit) { return toAjax(service.insertAudit(audit)); }

    @Log(title = "供应商审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:supplierAudit:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsSupplierAudit audit) { return toAjax(service.updateAudit(audit)); }

    @Log(title = "供应商审核", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:supplierAudit:remove')")
    @DeleteMapping("/{auditIds}")
    public AjaxResult remove(@PathVariable Long[] auditIds) { return toAjax(service.deleteAuditByIds(auditIds)); }
}
