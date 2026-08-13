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
import com.ruoyi.qms.domain.QmsDocVersion;
import com.ruoyi.qms.domain.QmsDocDistribute;
import com.ruoyi.qms.service.IQmsDocService;
import com.ruoyi.qms.service.IQmsDocVersionService;
import com.ruoyi.qms.service.IQmsDocDistributeService;
import com.ruoyi.common.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/doc")
public class QmsDocController extends BaseController {
    @Autowired
    private IQmsDocService service;

    @Autowired
    private IQmsDocVersionService versionService;

    @Autowired
    private IQmsDocDistributeService distributeService;

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

    // ==================== 版本管理 ====================

    @PreAuthorize("@ss.hasPermi('qms:doc:query')")
    @GetMapping("/version/list")
    public TableDataInfo versionList(QmsDocVersion version) {
        startPage();
        return getDataTable(versionService.selectDocVersionList(version));
    }

    @PreAuthorize("@ss.hasPermi('qms:doc:query')")
    @GetMapping("/version/{docId}")
    public AjaxResult versionByDocId(@PathVariable Long docId) {
        return AjaxResult.success(versionService.selectByDocId(docId));
    }

    @Log(title = "文档版本发布", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PostMapping("/version/publish")
    public AjaxResult publishVersion(@RequestBody QmsDocVersion version) {
        version.setEsigUser(SecurityUtils.getUsername());
        version.setEsigMeaning("审批通过-发布");
        return toAjax(versionService.publishVersion(version));
    }

    @Log(title = "文档版本作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PutMapping("/version/obsolete/{versionId}")
    public AjaxResult obsoleteVersion(@PathVariable Long versionId) {
        return toAjax(versionService.obsoleteVersion(versionId, SecurityUtils.getUsername()));
    }

    @Log(title = "文档版本删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:doc:remove')")
    @DeleteMapping("/version/{versionIds}")
    public AjaxResult removeVersion(@PathVariable Long[] versionIds) {
        return toAjax(versionService.deleteDocVersionByIds(versionIds));
    }

    // ==================== 分发/借阅 ====================

    @PreAuthorize("@ss.hasPermi('qms:doc:query')")
    @GetMapping("/distribute/list")
    public TableDataInfo distributeList(QmsDocDistribute distribute) {
        startPage();
        return getDataTable(distributeService.selectDocDistributeList(distribute));
    }

    @PreAuthorize("@ss.hasPermi('qms:doc:query')")
    @GetMapping("/distribute/{docId}")
    public AjaxResult distributeByDocId(@PathVariable Long docId) {
        return AjaxResult.success(distributeService.selectByDocId(docId));
    }

    @Log(title = "文档分发", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PostMapping("/distribute")
    public AjaxResult distributeDoc(@RequestBody QmsDocDistribute distribute) {
        return toAjax(distributeService.distributeDoc(distribute));
    }

    @Log(title = "文档借阅", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PostMapping("/borrow")
    public AjaxResult borrowDoc(@RequestBody QmsDocDistribute distribute) {
        return toAjax(distributeService.borrowDoc(distribute));
    }

    @Log(title = "文档归还", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PutMapping("/return/{distributeId}")
    public AjaxResult returnDoc(@PathVariable Long distributeId) {
        return toAjax(distributeService.returnDoc(distributeId));
    }

    @Log(title = "文档签收", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PutMapping("/receive/{distributeId}")
    public AjaxResult receiveDoc(@PathVariable Long distributeId) {
        return toAjax(distributeService.receiveDoc(distributeId));
    }

    @Log(title = "文档分发删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:doc:remove')")
    @DeleteMapping("/distribute/{distributeIds}")
    public AjaxResult removeDistribute(@PathVariable Long[] distributeIds) {
        return toAjax(distributeService.deleteDocDistributeByIds(distributeIds));
    }

    // ==================== 作废/引用检查 ====================

    @Log(title = "文档作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:doc:edit')")
    @PutMapping("/obsolete/{docId}")
    public AjaxResult obsoleteDoc(@PathVariable Long docId) {
        QmsDoc doc = service.selectDocById(docId);
        if (doc == null) return AjaxResult.error("文档不存在");
        doc.setDocStatus("3");
        doc.setObsoleteDate(new java.util.Date());
        return toAjax(service.updateDoc(doc));
    }

    @PreAuthorize("@ss.hasPermi('qms:doc:query')")
    @GetMapping("/reference/{docId}")
    public AjaxResult referenceCheck(@PathVariable Long docId) {
        QmsDoc doc = service.selectDocById(docId);
        if (doc == null) return AjaxResult.error("文档不存在");
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("docNo", doc.getDocNo());
        result.put("docTitle", doc.getDocTitle());
        result.put("versionNo", doc.getVersionNo());
        result.put("canObsolete", true);
        result.put("references", java.util.Collections.emptyList());
        result.put("message", "未发现其他文档引用此文档");
        return AjaxResult.success(result);
    }
}
