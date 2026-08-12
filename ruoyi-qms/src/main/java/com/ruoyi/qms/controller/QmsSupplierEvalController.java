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
import com.ruoyi.qms.domain.QmsSupplierEval;
import com.ruoyi.qms.service.IQmsSupplierEvalService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/supplierEval")
public class QmsSupplierEvalController extends BaseController {
    @Autowired
    private IQmsSupplierEvalService service;

    @PreAuthorize("@ss.hasPermi('qms:supplierEval:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsSupplierEval eval) {
        startPage();
        return getDataTable(service.selectEvalList(eval));
    }

    @Log(title = "供应商质量评价", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:supplierEval:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsSupplierEval eval) {
        List<QmsSupplierEval> list = service.selectEvalList(eval);
        ExcelUtil<QmsSupplierEval> util = new ExcelUtil<>(QmsSupplierEval.class);
        util.exportExcel(response, list, "供应商质量评价");
    }

    @PreAuthorize("@ss.hasPermi('qms:supplierEval:query')")
    @GetMapping("/{evalId}")
    public AjaxResult getInfo(@PathVariable Long evalId) { return AjaxResult.success(service.selectEvalById(evalId)); }

    @Log(title = "供应商质量评价", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:supplierEval:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsSupplierEval eval) { return toAjax(service.insertEval(eval)); }

    @Log(title = "供应商质量评价", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:supplierEval:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsSupplierEval eval) { return toAjax(service.updateEval(eval)); }

    @Log(title = "供应商质量评价", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:supplierEval:remove')")
    @DeleteMapping("/{evalIds}")
    public AjaxResult remove(@PathVariable Long[] evalIds) { return toAjax(service.deleteEvalByIds(evalIds)); }
}
