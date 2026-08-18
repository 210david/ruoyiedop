package com.ruoyi.mms.controller;

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
import com.ruoyi.mms.domain.MmsFactory;
import com.ruoyi.mms.domain.MmsWorkshop;
import com.ruoyi.mms.domain.MmsLine;
import com.ruoyi.mms.domain.MmsStation;
import com.ruoyi.mms.service.IMmsFactoryService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 工厂建模 Controller（工厂/车间/产线/工位四级）
 */
@RestController
@RequestMapping("/mms/factory")
public class MmsFactoryController extends BaseController
{
    @Autowired
    private IMmsFactoryService factoryService;

    // ==================== 工厂 ====================

    @PreAuthorize("@ss.hasPermi('mms:factory:list')")
    @GetMapping("/list")
    public TableDataInfo listFactory(MmsFactory factory)
    {
        startPage();
        List<MmsFactory> list = factoryService.selectFactoryList(factory);
        return getDataTable(list);
    }

    @Log(title = "工厂建模", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:factory:export')")
    @PostMapping("/export")
    public void exportFactory(HttpServletResponse response, MmsFactory factory)
    {
        List<MmsFactory> list = factoryService.selectFactoryList(factory);
        ExcelUtil<MmsFactory> util = new ExcelUtil<>(MmsFactory.class);
        util.exportExcel(response, list, "工厂");
    }

    @PreAuthorize("@ss.hasPermi('mms:factory:query')")
    @GetMapping("/{factoryId}")
    public AjaxResult getFactory(@PathVariable Long factoryId)
    {
        return AjaxResult.success(factoryService.selectFactoryById(factoryId));
    }

    @Log(title = "工厂建模", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:factory:add')")
    @PostMapping
    public AjaxResult addFactory(@RequestBody MmsFactory factory)
    {
        return toAjax(factoryService.insertFactory(factory));
    }

    @Log(title = "工厂建模", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:factory:edit')")
    @PutMapping
    public AjaxResult editFactory(@RequestBody MmsFactory factory)
    {
        return toAjax(factoryService.updateFactory(factory));
    }

    @Log(title = "工厂建模", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:factory:remove')")
    @DeleteMapping("/{factoryIds}")
    public AjaxResult removeFactory(@PathVariable Long[] factoryIds)
    {
        return toAjax(factoryService.deleteFactoryByIds(factoryIds));
    }

    // ==================== 车间 ====================

    @PreAuthorize("@ss.hasPermi('mms:factory:list')")
    @GetMapping("/workshop/list")
    public TableDataInfo listWorkshop(MmsWorkshop workshop)
    {
        startPage();
        List<MmsWorkshop> list = factoryService.selectWorkshopList(workshop);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:factory:query')")
    @GetMapping("/workshop/{workshopId}")
    public AjaxResult getWorkshop(@PathVariable Long workshopId)
    {
        return AjaxResult.success(factoryService.selectWorkshopById(workshopId));
    }

    @Log(title = "车间", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:factory:add')")
    @PostMapping("/workshop")
    public AjaxResult addWorkshop(@RequestBody MmsWorkshop workshop)
    {
        return toAjax(factoryService.insertWorkshop(workshop));
    }

    @Log(title = "车间", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:factory:edit')")
    @PutMapping("/workshop")
    public AjaxResult editWorkshop(@RequestBody MmsWorkshop workshop)
    {
        return toAjax(factoryService.updateWorkshop(workshop));
    }

    @Log(title = "车间", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:factory:remove')")
    @DeleteMapping("/workshop/{workshopIds}")
    public AjaxResult removeWorkshop(@PathVariable Long[] workshopIds)
    {
        return toAjax(factoryService.deleteWorkshopByIds(workshopIds));
    }

    // ==================== 产线 ====================

    @PreAuthorize("@ss.hasPermi('mms:factory:list')")
    @GetMapping("/line/list")
    public TableDataInfo listLine(MmsLine line)
    {
        startPage();
        List<MmsLine> list = factoryService.selectLineList(line);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:factory:query')")
    @GetMapping("/line/{lineId}")
    public AjaxResult getLine(@PathVariable Long lineId)
    {
        return AjaxResult.success(factoryService.selectLineById(lineId));
    }

    @Log(title = "产线", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:factory:add')")
    @PostMapping("/line")
    public AjaxResult addLine(@RequestBody MmsLine line)
    {
        return toAjax(factoryService.insertLine(line));
    }

    @Log(title = "产线", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:factory:edit')")
    @PutMapping("/line")
    public AjaxResult editLine(@RequestBody MmsLine line)
    {
        return toAjax(factoryService.updateLine(line));
    }

    @Log(title = "产线", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:factory:remove')")
    @DeleteMapping("/line/{lineIds}")
    public AjaxResult removeLine(@PathVariable Long[] lineIds)
    {
        return toAjax(factoryService.deleteLineByIds(lineIds));
    }

    // ==================== 工位 ====================

    @PreAuthorize("@ss.hasPermi('mms:factory:list')")
    @GetMapping("/station/list")
    public TableDataInfo listStation(MmsStation station)
    {
        startPage();
        List<MmsStation> list = factoryService.selectStationList(station);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:factory:query')")
    @GetMapping("/station/{stationId}")
    public AjaxResult getStation(@PathVariable Long stationId)
    {
        return AjaxResult.success(factoryService.selectStationById(stationId));
    }

    @Log(title = "工位", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:factory:add')")
    @PostMapping("/station")
    public AjaxResult addStation(@RequestBody MmsStation station)
    {
        return toAjax(factoryService.insertStation(station));
    }

    @Log(title = "工位", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:factory:edit')")
    @PutMapping("/station")
    public AjaxResult editStation(@RequestBody MmsStation station)
    {
        return toAjax(factoryService.updateStation(station));
    }

    @Log(title = "工位", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:factory:remove')")
    @DeleteMapping("/station/{stationIds}")
    public AjaxResult removeStation(@PathVariable Long[] stationIds)
    {
        return toAjax(factoryService.deleteStationByIds(stationIds));
    }
}
