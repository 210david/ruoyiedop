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
import com.ruoyi.safety.domain.SafetyDrill;
import com.ruoyi.safety.service.ISafetyDrillService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/emergency/drill")
public class SafetyDrillController extends BaseController
{
    @Autowired
    private ISafetyDrillService safetyDrillService;

    @PreAuthorize("@ss.hasPermi('safety:emergency:drill:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyDrill drill)
    {
        startPage();
        List<SafetyDrill> list = safetyDrillService.selectSafetyDrillList(drill);
        return getDataTable(list);
    }

    @Log(title = "演练记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:emergency:drill:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyDrill drill)
    {
        List<SafetyDrill> list = safetyDrillService.selectSafetyDrillList(drill);
        ExcelUtil<SafetyDrill> util = new ExcelUtil<>(SafetyDrill.class);
        util.exportExcel(response, list, "演练记录");
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:drill:query')")
    @GetMapping(value = "/{drillId}")
    public AjaxResult getInfo(@PathVariable("drillId") Long drillId)
    {
        return AjaxResult.success(safetyDrillService.selectSafetyDrillById(drillId));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:drill:add')")
    @Log(title = "演练记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyDrill drill)
    {
        return toAjax(safetyDrillService.insertSafetyDrill(drill));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:drill:edit')")
    @Log(title = "演练记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyDrill drill)
    {
        return toAjax(safetyDrillService.updateSafetyDrill(drill));
    }

    @PreAuthorize("@ss.hasPermi('safety:emergency:drill:remove')")
    @Log(title = "演练记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{drillIds}")
    public AjaxResult remove(@PathVariable Long[] drillIds)
    {
        return toAjax(safetyDrillService.deleteSafetyDrillByIds(drillIds));
    }
}