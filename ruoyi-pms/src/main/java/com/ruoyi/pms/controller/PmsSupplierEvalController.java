package com.ruoyi.pms.controller;

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
import com.ruoyi.pms.domain.PmsSupplierEval;
import com.ruoyi.pms.service.IPmsSupplierEvalService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 供应商评估 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/eval")
public class PmsSupplierEvalController extends BaseController
{
    @Autowired
    private IPmsSupplierEvalService pmsSupplierEvalService;

    @PreAuthorize("@ss.hasPermi('pms:eval:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsSupplierEval eval)
    {
        startPage();
        List<PmsSupplierEval> list = pmsSupplierEvalService.selectEvalList(eval);
        return getDataTable(list);
    }

    @Log(title = "供应商评估", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:eval:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsSupplierEval eval)
    {
        List<PmsSupplierEval> list = pmsSupplierEvalService.selectEvalList(eval);
        ExcelUtil<PmsSupplierEval> util = new ExcelUtil<>(PmsSupplierEval.class);
        util.exportExcel(response, list, "供应商评估数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:eval:query')")
    @GetMapping(value = "/{evalId}")
    public AjaxResult getInfo(@PathVariable("evalId") Long evalId)
    {
        return AjaxResult.success(pmsSupplierEvalService.selectEvalById(evalId));
    }

    @Log(title = "供应商评估", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:eval:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsSupplierEval eval)
    {
        return toAjax(pmsSupplierEvalService.insertEval(eval));
    }

    @Log(title = "供应商评估", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:eval:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsSupplierEval eval)
    {
        return toAjax(pmsSupplierEvalService.updateEval(eval));
    }

    @Log(title = "供应商评估", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:eval:remove')")
    @DeleteMapping("/{evalIds}")
    public AjaxResult remove(@PathVariable Long[] evalIds)
    {
        return toAjax(pmsSupplierEvalService.deleteEvalByIds(evalIds));
    }

    /**
     * 审批
     */
    @Log(title = "供应商评估", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:eval:audit')")
    @PutMapping("/audit/{evalId}")
    public AjaxResult audit(@PathVariable Long evalId, @RequestParam String status,
            @RequestParam(required = false) String auditRemark)
    {
        return toAjax(pmsSupplierEvalService.auditEval(evalId, status, auditRemark));
    }
}
