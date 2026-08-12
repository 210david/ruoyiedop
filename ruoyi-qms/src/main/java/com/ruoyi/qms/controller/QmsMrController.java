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
import com.ruoyi.qms.service.IQmsMrService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/mr")
public class QmsMrController extends BaseController {
    @Autowired
    private IQmsMrService service;

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
}
