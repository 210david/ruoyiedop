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
import com.ruoyi.qms.service.IQmsInspTaskService;
import com.ruoyi.qms.domain.QmsInspTask;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/supplierEval")
public class QmsSupplierEvalController extends BaseController {
    @Autowired
    private IQmsSupplierEvalService service;

    @Autowired
    private IQmsInspTaskService inspTaskService;

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
    @GetMapping("/get/{evalId}")
    public AjaxResult getInfo(@PathVariable Long evalId) { return AjaxResult.success(service.selectEvalById(evalId)); }

    /** 根据供应商+评价周期自动统计来料检验批次（从检验任务表） */
    @PreAuthorize("@ss.hasPermi('qms:supplierEval:query')")
    @GetMapping("/batchStats")
    public AjaxResult batchStats(@RequestParam Long supplierId, @RequestParam String evalPeriod) {
        return AjaxResult.success(service.selectBatchStats(supplierId, evalPeriod));
    }

    /** 新增评价时自动填充来料总批次/合格批次 */
    @PreAuthorize("@ss.hasPermi('qms:supplierEval:query')")
    @GetMapping("/autoFill")
    public AjaxResult autoFill(@RequestParam Long supplierId, @RequestParam String evalPeriod) {
        java.util.Map<String, Object> stats = service.selectBatchStats(supplierId, evalPeriod);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (stats != null) {
            result.putAll(stats);
        }
        // 从IQC检验任务自动汇总
        QmsInspTask query = new QmsInspTask();
        query.setSupplierId(supplierId);
        List<QmsInspTask> iqcList = inspTaskService.selectInspTaskList(query);
        int totalBatches = 0, passBatches = 0, failBatches = 0;
        for (QmsInspTask task : iqcList) {
            if ("IQC".equals(task.getTaskType()) && "2".equals(task.getTaskStatus())) {
                totalBatches++;
                if ("1".equals(task.getInspectResult())) passBatches++;
                if ("2".equals(task.getInspectResult())) failBatches++;
            }
        }
        result.put("totalBatches", totalBatches);
        result.put("passBatches", passBatches);
        result.put("failBatches", failBatches);
        double batchPassRate = totalBatches > 0 ? (double) passBatches / totalBatches * 100 : 0;
        result.put("batchPassRate", Math.round(batchPassRate * 100) / 100.0);
        return AjaxResult.success(result);
    }

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
