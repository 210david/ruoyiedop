package com.ruoyi.qms.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.qms.domain.QmsQualityTarget;
import com.ruoyi.qms.service.IQmsQualityTargetService;

@RestController
@RequestMapping("/qms/target")
public class QmsQualityTargetController extends BaseController {
    @Autowired
    private IQmsQualityTargetService service;

    @PreAuthorize("@ss.hasPermi('qms:target:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsQualityTarget target) { startPage(); return getDataTable(service.selectTargetList(target)); }

    @PreAuthorize("@ss.hasPermi('qms:target:query')")
    @GetMapping("/{targetId}")
    public AjaxResult getInfo(@PathVariable Long targetId) { return AjaxResult.success(service.selectTargetById(targetId)); }

    @Log(title = "质量目标", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:target:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsQualityTarget target) { return toAjax(service.insertTarget(target)); }

    @Log(title = "质量目标", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:target:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsQualityTarget target) { return toAjax(service.updateTarget(target)); }

    @Log(title = "质量目标", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:target:remove')")
    @DeleteMapping("/{targetIds}")
    public AjaxResult remove(@PathVariable Long[] targetIds) { return toAjax(service.deleteTargetByIds(targetIds)); }

    @Log(title = "质量目标", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:target:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsQualityTarget target) {
        List<QmsQualityTarget> list = service.selectTargetList(target);
        ExcelUtil<QmsQualityTarget> util = new ExcelUtil<>(QmsQualityTarget.class);
        util.exportExcel(response, list, "质量目标数据");
    }
}
