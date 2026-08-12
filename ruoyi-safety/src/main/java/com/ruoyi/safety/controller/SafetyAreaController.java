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
import com.ruoyi.safety.domain.SafetyArea;
import com.ruoyi.safety.service.ISafetyAreaService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/area")
public class SafetyAreaController extends BaseController
{
    @Autowired
    private ISafetyAreaService safetyAreaService;

    @PreAuthorize("@ss.hasPermi('safety:area:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyArea area)
    {
        startPage();
        List<SafetyArea> list = safetyAreaService.selectSafetyAreaList(area);
        return getDataTable(list);
    }

    @Log(title = "风险区域", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:area:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyArea area)
    {
        List<SafetyArea> list = safetyAreaService.selectSafetyAreaList(area);
        ExcelUtil<SafetyArea> util = new ExcelUtil<>(SafetyArea.class);
        util.exportExcel(response, list, "风险区域");
    }

    @PreAuthorize("@ss.hasPermi('safety:area:query')")
    @GetMapping(value = "/{areaId}")
    public AjaxResult getInfo(@PathVariable("areaId") Long areaId)
    {
        return AjaxResult.success(safetyAreaService.selectSafetyAreaById(areaId));
    }

    @PreAuthorize("@ss.hasPermi('safety:area:add')")
    @Log(title = "风险区域", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyArea area)
    {
        return toAjax(safetyAreaService.insertSafetyArea(area));
    }

    @PreAuthorize("@ss.hasPermi('safety:area:edit')")
    @Log(title = "风险区域", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyArea area)
    {
        return toAjax(safetyAreaService.updateSafetyArea(area));
    }

    @PreAuthorize("@ss.hasPermi('safety:area:remove')")
    @Log(title = "风险区域", businessType = BusinessType.DELETE)
    @DeleteMapping("/{areaIds}")
    public AjaxResult remove(@PathVariable Long[] areaIds)
    {
        return toAjax(safetyAreaService.deleteSafetyAreaByIds(areaIds));
    }
}