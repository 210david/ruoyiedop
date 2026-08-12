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
import com.ruoyi.qms.domain.QmsDoc;
import com.ruoyi.qms.service.IQmsDocService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/doc")
public class QmsDocController extends BaseController {
    @Autowired
    private IQmsDocService service;

    @PreAuthorize("@ss.hasPermi('qms:doc:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsDoc doc) { startPage(); return getDataTable(service.selectDocList(doc)); }

    @Log(title = "质量文档", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:doc:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsDoc doc) {
        ExcelUtil<QmsDoc> util = new ExcelUtil<>(QmsDoc.class);
        util.exportExcel(response, service.selectDocList(doc), "质量文档");
    }

    @PreAuthorize("@ss.hasPermi('qms:doc:query')")
    @GetMapping("/{docId}")
    public AjaxResult getInfo(@PathVariable Long docId) { return AjaxResult.success(service.selectDocById(docId)); }

    @Log(title = "质量文档", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:doc:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsDoc doc) { return toAjax(service.insertDoc(doc)); }

    @Log(title = "质量文档", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsDoc doc) { return toAjax(service.updateDoc(doc)); }

    @Log(title = "质量文档", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:doc:remove')")
    @DeleteMapping("/{docIds}")
    public AjaxResult remove(@PathVariable Long[] docIds) { return toAjax(service.deleteDocByIds(docIds)); }
}
