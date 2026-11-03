package com.ruoyi.hr.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hr.domain.HrKpiRecord;
import com.ruoyi.hr.domain.HrKpiScheme;
import com.ruoyi.hr.service.IHrKpiService;

/**
 * KPI考核 Controller（需求三）
 */
@RestController
@RequestMapping("/hr/kpi")
public class HrKpiController extends BaseController {
    @Autowired
    private IHrKpiService kpiService;

    // ---- 方案 ----
    @PreAuthorize("@ss.hasPermi('hr:kpi:list')")
    @GetMapping("/scheme/list")
    public TableDataInfo schemeList(HrKpiScheme scheme) { return getDataTable(kpiService.selectSchemeList(scheme)); }

    @PreAuthorize("@ss.hasPermi('hr:kpi:query')")
    @GetMapping("/scheme/{schemeId}")
    public AjaxResult getScheme(@PathVariable Long schemeId) { return success(kpiService.selectSchemeById(schemeId)); }

    @Log(title = "KPI考核方案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:kpi:add')")
    @PostMapping("/scheme")
    public AjaxResult addScheme(@RequestBody HrKpiScheme scheme) { return toAjax(kpiService.insertScheme(scheme)); }

    @Log(title = "KPI考核方案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:add')")
    @PutMapping("/scheme")
    public AjaxResult editScheme(@RequestBody HrKpiScheme scheme) { return toAjax(kpiService.updateScheme(scheme)); }

    @Log(title = "KPI考核方案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:add')")
    @DeleteMapping("/scheme/{schemeIds}")
    public AjaxResult removeScheme(@PathVariable Long[] schemeIds) { return toAjax(kpiService.deleteSchemeByIds(schemeIds)); }

    // ---- 考核记录 ----
    @PreAuthorize("@ss.hasPermi('hr:kpi:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(HrKpiRecord record) { return getDataTable(kpiService.selectRecordList(record)); }

    @PreAuthorize("@ss.hasPermi('hr:kpi:query')")
    @GetMapping("/record/rank")
    public AjaxResult recordRank(@RequestParam String period) { return success(kpiService.selectRecordRank(period)); }

    @Log(title = "发起考核", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:kpi:initiate')")
    @PostMapping("/record/initiate")
    public AjaxResult initiate(@RequestBody HrKpiRecord req) { return success(kpiService.initiateRecords(req.getSchemeId(), req.getPeriod(), req.getEmployeeIds())); }

    @Log(title = "考核自评", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:score')")
    @PutMapping("/record/self")
    public AjaxResult self(@RequestBody HrKpiRecord record) { return toAjax(kpiService.selfScore(record)); }

    @Log(title = "考核评分", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:score')")
    @PutMapping("/record/superior")
    public AjaxResult superior(@RequestBody HrKpiRecord record) { return toAjax(kpiService.superiorScore(record)); }

    @Log(title = "考核确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:confirm')")
    @PutMapping("/record/confirm/{recordId}")
    public AjaxResult confirm(@PathVariable Long recordId) { return toAjax(kpiService.confirmRecord(recordId)); }

    @Log(title = "考核申诉", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:appeal')")
    @PutMapping("/record/appeal")
    public AjaxResult appeal(@RequestBody HrKpiRecord record) { return toAjax(kpiService.appealRecord(record.getRecordId(), record.getAppealReason())); }

    @Log(title = "申诉处理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:kpi:confirm')")
    @PutMapping("/record/appeal/handle")
    public AjaxResult handleAppeal(@RequestBody HrKpiRecord record) { return toAjax(kpiService.handleAppeal(record.getRecordId(), record.getAppealResult(), record.getFinalScore())); }
}
