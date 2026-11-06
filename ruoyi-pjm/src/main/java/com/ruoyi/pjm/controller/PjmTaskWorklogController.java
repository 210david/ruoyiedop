package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmTaskWorklog;
import com.ruoyi.pjm.service.IPjmTaskWorklogService;

@RestController
@RequestMapping("/pjm/worklog")
public class PjmTaskWorklogController extends BaseController
{
    @Autowired
    private IPjmTaskWorklogService worklogService;

    @PreAuthorize("@ss.hasPermi('pjm:worklog:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmTaskWorklog worklog) { startPage(); return getDataTable(worklogService.selectWorklogList(worklog)); }

    @PreAuthorize("@ss.hasPermi('pjm:worklog:query')")
    @GetMapping(value = "/{worklogId}")
    public AjaxResult getInfo(@PathVariable Long worklogId) { return AjaxResult.success(worklogService.selectWorklogById(worklogId)); }

    @PreAuthorize("@ss.hasPermi('pjm:worklog:add')")
    @Log(title = "WBS工时", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmTaskWorklog worklog) { return toAjax(worklogService.insertWorklog(worklog)); }

    @PreAuthorize("@ss.hasPermi('pjm:worklog:edit')")
    @Log(title = "WBS工时", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmTaskWorklog worklog) { return toAjax(worklogService.updateWorklog(worklog)); }

    @PreAuthorize("@ss.hasPermi('pjm:worklog:remove')")
    @Log(title = "WBS工时", businessType = BusinessType.DELETE)
    @DeleteMapping("/{worklogIds}")
    public AjaxResult remove(@PathVariable Long[] worklogIds) { return toAjax(worklogService.deleteWorklogByIds(worklogIds)); }
}
