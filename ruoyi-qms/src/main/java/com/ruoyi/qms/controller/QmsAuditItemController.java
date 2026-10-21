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
import com.ruoyi.qms.domain.QmsAuditItem;
import com.ruoyi.qms.service.IQmsAuditItemService;

@RestController
@RequestMapping("/qms/auditItem")
public class QmsAuditItemController extends BaseController {
    @Autowired
    private IQmsAuditItemService service;

    @PreAuthorize("@ss.hasPermi('qms:audit:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsAuditItem item) {
        startPage();
        return getDataTable(service.selectAuditItemList(item));
    }

    @PreAuthorize("@ss.hasPermi('qms:audit:query')")
    @GetMapping("/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId) {
        return AjaxResult.success(service.selectAuditItemById(itemId));
    }

    @PreAuthorize("@ss.hasPermi('qms:audit:query')")
    @GetMapping("/byPlan/{auditPlanId}")
    public AjaxResult listByPlanId(@PathVariable Long auditPlanId) {
        return AjaxResult.success(service.selectByAuditPlanId(auditPlanId));
    }

    @Log(title = "内审不符合项", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:audit:add')")
    @PostMapping
    public AjaxResult add(@RequestBody QmsAuditItem item) {
        return toAjax(service.insertAuditItem(item));
    }

    @Log(title = "内审不符合项", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:audit:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody QmsAuditItem item) {
        return toAjax(service.updateAuditItem(item));
    }

    @Log(title = "内审不符合项", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:audit:remove')")
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        return toAjax(service.deleteAuditItemByIds(itemIds));
    }

    /** 一键从不符合项发起CAPA */
    @Log(title = "不符合项发起CAPA", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:audit:edit')")
    @PostMapping("/createCapa/{itemId}")
    public AjaxResult createCapaFromItem(@PathVariable Long itemId) {
        Long capaId = service.createCapaFromAuditItem(itemId);
        return AjaxResult.success(capaId);
    }
}
