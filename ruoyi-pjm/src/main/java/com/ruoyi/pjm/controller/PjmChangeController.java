package com.ruoyi.pjm.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmChange;
import com.ruoyi.pjm.service.IPjmChangeService;

@RestController
@RequestMapping("/pjm/change")
public class PjmChangeController extends BaseController
{
    @Autowired
    private IPjmChangeService changeService;

    @PreAuthorize("@ss.hasPermi('pjm:change:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmChange change) { startPage(); return getDataTable(changeService.selectChangeList(change)); }

    @PreAuthorize("@ss.hasPermi('pjm:change:query')")
    @GetMapping(value = "/{changeId}")
    public AjaxResult getInfo(@PathVariable Long changeId) { return AjaxResult.success(changeService.selectChangeById(changeId)); }

    @PreAuthorize("@ss.hasPermi('pjm:change:add')")
    @Log(title = "变更管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmChange change) { return toAjax(changeService.insertChange(change)); }

    @PreAuthorize("@ss.hasPermi('pjm:change:edit')")
    @Log(title = "变更管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmChange change) { return toAjax(changeService.updateChange(change)); }

    @PreAuthorize("@ss.hasPermi('pjm:change:remove')")
    @Log(title = "变更管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{changeIds}")
    public AjaxResult remove(@PathVariable Long[] changeIds) { return toAjax(changeService.deleteChangeByIds(changeIds)); }

    @PreAuthorize("@ss.hasPermi('pjm:change:edit')")
    @Log(title = "变更提交", businessType = BusinessType.UPDATE)
    @PutMapping("/submit/{changeId}")
    public AjaxResult submit(@PathVariable Long changeId) { return toAjax(changeService.submit(changeId)); }

    @PreAuthorize("@ss.hasPermi('pjm:change:audit')")
    @Log(title = "变更审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody Map<String, Object> params)
    {
        Long changeId = Long.valueOf(params.get("changeId").toString());
        String status = (String) params.get("status");
        String opinion = params.get("auditOpinion") == null ? null : params.get("auditOpinion").toString();
        return toAjax(changeService.audit(changeId, status, opinion));
    }
}
