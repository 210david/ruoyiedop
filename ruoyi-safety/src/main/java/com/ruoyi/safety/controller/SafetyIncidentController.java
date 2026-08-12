package com.ruoyi.safety.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.safety.domain.SafetyIncident;
import com.ruoyi.safety.service.ISafetyIncidentService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/emergency/incident")
public class SafetyIncidentController extends BaseController
{
    @Autowired
    private ISafetyIncidentService safetyIncidentService;

    @PreAuthorize("@ss.hasPermi('safety:emergency:incident:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyIncident incident)
    {
        startPage();
        List<SafetyIncident> list = safetyIncidentService.selectSafetyIncidentList(incident);
        return getDataTable(list);
    }

    @Log(title = "事故管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:emergency:incident:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyIncident incident)
    {
        List<SafetyIncident> list = safetyIncidentService.selectSafetyIncidentList(incident);
        ExcelUtil<SafetyIncident> util = new ExcelUtil<>(SafetyIncident.class);
        util.exportExcel(response, list, "事故管理");
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:incident:query')")
    @GetMapping(value = "/{incidentId}")
    public AjaxResult getInfo(@PathVariable("incidentId") Long incidentId)
    {
        return AjaxResult.success(safetyIncidentService.selectSafetyIncidentById(incidentId));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:incident:add')")
    @Log(title = "事故管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyIncident incident)
    {
        return toAjax(safetyIncidentService.insertSafetyIncident(incident));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:incident:edit')")
    @Log(title = "事故管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyIncident incident)
    {
        return toAjax(safetyIncidentService.updateSafetyIncident(incident));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:incident:remove')")
    @Log(title = "事故管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{incidentIds}")
    public AjaxResult remove(@PathVariable Long[] incidentIds)
    {
        return toAjax(safetyIncidentService.deleteSafetyIncidentByIds(incidentIds));
    }
}