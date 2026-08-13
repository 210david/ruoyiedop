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
import com.ruoyi.qms.domain.QmsMr;
import com.ruoyi.qms.domain.QmsMrAction;
import com.ruoyi.qms.service.IQmsMrService;
import com.ruoyi.qms.service.IQmsMrActionService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/mr")
public class QmsMrController extends BaseController {
    @Autowired
    private IQmsMrService service;

    @Autowired
    private IQmsMrActionService actionService;

    @PreAuthorize("@ss.hasPermi('qms:mr:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsMr mr) { startPage(); return getDataTable(service.selectMrList(mr)); }

    @Log(title = "管理评审", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:mr:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsMr mr) {
        ExcelUtil<QmsMr> util = new ExcelUtil<>(QmsMr.class);
        util.exportExcel(response, service.selectMrList(mr), "管理评审");
    }

    @PreAuthorize("@ss.hasPermi('qms:mr:query')")
    @GetMapping("/{mrId}")
    public AjaxResult getInfo(@PathVariable Long mrId) { return AjaxResult.success(service.selectMrById(mrId)); }

    @Log(title = "管理评审", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:mr:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsMr mr) { return toAjax(service.insertMr(mr)); }

    @Log(title = "管理评审", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:mr:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsMr mr) { return toAjax(service.updateMr(mr)); }

    @Log(title = "管理评审", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:mr:remove')")
    @DeleteMapping("/{mrIds}")
    public AjaxResult remove(@PathVariable Long[] mrIds) { return toAjax(service.deleteMrByIds(mrIds)); }

    // ==================== 改进项管理 ====================

    @PreAuthorize("@ss.hasPermi('qms:mr:query')")
    @GetMapping("/action/list")
    public TableDataInfo actionList(QmsMrAction action) {
        startPage();
        return getDataTable(actionService.selectMrActionList(action));
    }

    @PreAuthorize("@ss.hasPermi('qms:mr:query')")
    @GetMapping("/action/{mrId}")
    public AjaxResult actionByMrId(@PathVariable Long mrId) {
        return AjaxResult.success(actionService.selectByMrId(mrId));
    }

    @Log(title = "管理评审改进项", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:mr:edit')")
    @PostMapping("/action")
    public AjaxResult addAction(@RequestBody QmsMrAction action) {
        return toAjax(actionService.insertMrAction(action));
    }

    @Log(title = "管理评审改进项", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:mr:edit')")
    @PutMapping("/action")
    public AjaxResult editAction(@RequestBody QmsMrAction action) {
        return toAjax(actionService.updateMrAction(action));
    }

    @Log(title = "改进项关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:mr:edit')")
    @PutMapping("/action/close/{actionId}")
    public AjaxResult closeAction(@PathVariable Long actionId, @RequestParam String actionResult) {
        return toAjax(actionService.closeAction(actionId, actionResult));
    }

    @Log(title = "管理评审改进项", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:mr:remove')")
    @DeleteMapping("/action/{ids}")
    public AjaxResult removeAction(@PathVariable Long[] ids) {
        return toAjax(actionService.deleteMrActionByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('qms:mr:query')")
    @GetMapping("/action/checkAllClosed/{mrId}")
    public AjaxResult checkAllActionsClosed(@PathVariable Long mrId) {
        return AjaxResult.success(actionService.checkAllActionsClosed(mrId));
    }

    // ==================== 输入自动汇总 ====================

    @PreAuthorize("@ss.hasPermi('qms:mr:query')")
    @GetMapping("/inputSummary/{mrId}")
    public AjaxResult inputSummary(@PathVariable Long mrId) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("mrId", mrId);
        result.put("auditSummary", "自动汇总：内审完成率、不符合项数量、CAPA关闭率等");
        result.put("capaSummary", "自动汇总：CAPA总数、进行中、已关闭、平均关闭周期");
        result.put("complaintSummary", "自动汇总：客诉总数、处理中、已关闭、重复客诉率");
        result.put("supplierSummary", "自动汇总：供应商评价A/B/C/D等级分布、降级预警");
        result.put("kpiSummary", "自动汇总：质量目标达成率、趋势分析");
        result.put("ncrSummary", "自动汇总：NCR总数、按缺陷等级分布、按物料分布");
        return AjaxResult.success(result);
    }
}
