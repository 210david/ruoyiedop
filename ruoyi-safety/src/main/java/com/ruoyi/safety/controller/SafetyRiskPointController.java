package com.ruoyi.safety.controller;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.safety.domain.SafetyRiskPoint;
import com.ruoyi.safety.service.ISafetyRiskPointService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/risk")
public class SafetyRiskPointController extends BaseController
{
    @Autowired
    private ISafetyRiskPointService safetyRiskPointService;

    @PreAuthorize("@ss.hasPermi('safety:risk:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyRiskPoint riskPoint)
    {
        startPage();
        List<SafetyRiskPoint> list = safetyRiskPointService.selectSafetyRiskPointList(riskPoint);
        return getDataTable(list);
    }

    @Log(title = "风险点管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:risk:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyRiskPoint riskPoint)
    {
        List<SafetyRiskPoint> list = safetyRiskPointService.selectSafetyRiskPointList(riskPoint);
        ExcelUtil<SafetyRiskPoint> util = new ExcelUtil<>(SafetyRiskPoint.class);
        util.exportExcel(response, list, "风险点数据");
    }

    @PreAuthorize("@ss.hasPermi('safety:risk:query')")
    @GetMapping(value = "/{riskPointId}")
    public AjaxResult getInfo(@PathVariable("riskPointId") Long riskPointId)
    {
        return AjaxResult.success(safetyRiskPointService.selectSafetyRiskPointById(riskPointId));
    }

    @PreAuthorize("@ss.hasPermi('safety:risk:add')")
    @Log(title = "风险点管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyRiskPoint riskPoint)
    {
        return toAjax(safetyRiskPointService.insertSafetyRiskPoint(riskPoint));
    }

    @PreAuthorize("@ss.hasPermi('safety:risk:edit')")
    @Log(title = "风险点管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyRiskPoint riskPoint)
    {
        return toAjax(safetyRiskPointService.updateSafetyRiskPoint(riskPoint));
    }

    @PreAuthorize("@ss.hasPermi('safety:risk:remove')")
    @Log(title = "风险点管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{riskPointIds}")
    public AjaxResult remove(@PathVariable Long[] riskPointIds)
    {
        return toAjax(safetyRiskPointService.deleteSafetyRiskPointByIds(riskPointIds));
    }

    @PreAuthorize("@ss.hasPermi('safety:risk:list')")
    @GetMapping("/fourColorMap")
    public AjaxResult fourColorMap(@RequestParam(required = false) Long enterpriseId)
    {
        List<Map<String, Object>> data = safetyRiskPointService.selectFourColorMapData(enterpriseId);
        return AjaxResult.success(data);
    }
}