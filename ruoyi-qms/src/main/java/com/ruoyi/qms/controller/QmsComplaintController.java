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
import com.ruoyi.qms.domain.QmsComplaint;
import com.ruoyi.qms.service.IQmsComplaintService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/complaint")
public class QmsComplaintController extends BaseController {
    @Autowired
    private IQmsComplaintService service;

    @PreAuthorize("@ss.hasPermi('qms:complaint:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsComplaint complaint) { startPage(); return getDataTable(service.selectComplaintList(complaint)); }

    @Log(title = "客诉", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:complaint:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsComplaint complaint) {
        ExcelUtil<QmsComplaint> util = new ExcelUtil<>(QmsComplaint.class);
        util.exportExcel(response, service.selectComplaintList(complaint), "客诉数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:complaint:query')")
    @GetMapping("/{complaintId}")
    public AjaxResult getInfo(@PathVariable Long complaintId) { return AjaxResult.success(service.selectComplaintById(complaintId)); }

    @Log(title = "客诉", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:complaint:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsComplaint complaint) { return toAjax(service.insertComplaint(complaint)); }

    @Log(title = "客诉", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:complaint:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsComplaint complaint) { return toAjax(service.updateComplaint(complaint)); }

    @Log(title = "客诉", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:complaint:remove')")
    @DeleteMapping("/{complaintIds}")
    public AjaxResult remove(@PathVariable Long[] complaintIds) { return toAjax(service.deleteComplaintByIds(complaintIds)); }

    @Log(title = "客诉关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:complaint:close')")
    @PutMapping("/close/{complaintId}")
    public AjaxResult close(@PathVariable Long complaintId) { return toAjax(service.closeComplaint(complaintId)); }
}
