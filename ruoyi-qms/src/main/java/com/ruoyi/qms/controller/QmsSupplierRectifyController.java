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
import com.ruoyi.qms.domain.QmsSupplierRectify;
import com.ruoyi.qms.service.IQmsSupplierRectifyService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 供应商整改单（客诉供应商责任处理）Controller
 */
@RestController
@RequestMapping("/qms/rectify")
public class QmsSupplierRectifyController extends BaseController {
    @Autowired
    private IQmsSupplierRectifyService service;

    @PreAuthorize("@ss.hasPermi('qms:rectify:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsSupplierRectify rectify) { startPage(); return getDataTable(service.selectRectifyList(rectify)); }

    @Log(title = "供应商整改单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:rectify:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsSupplierRectify rectify) {
        ExcelUtil<QmsSupplierRectify> util = new ExcelUtil<>(QmsSupplierRectify.class);
        util.exportExcel(response, service.selectRectifyList(rectify), "供应商整改单数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:rectify:query')")
    @GetMapping("/{rectifyId}")
    public AjaxResult getInfo(@PathVariable Long rectifyId) { return AjaxResult.success(service.selectRectifyById(rectifyId)); }

    @Log(title = "供应商整改单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:rectify:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsSupplierRectify rectify) { return toAjax(service.insertRectify(rectify)); }

    @Log(title = "供应商整改单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:rectify:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsSupplierRectify rectify) { return toAjax(service.updateRectify(rectify)); }

    @Log(title = "供应商整改单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:rectify:remove')")
    @DeleteMapping("/{rectifyIds}")
    public AjaxResult remove(@PathVariable Long[] rectifyIds) { return toAjax(service.deleteRectifyByIds(rectifyIds)); }

    @Log(title = "供应商整改单关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:rectify:edit')")
    @PutMapping("/close/{rectifyId}")
    public AjaxResult close(@PathVariable Long rectifyId) { return toAjax(service.closeRectify(rectifyId)); }
}
