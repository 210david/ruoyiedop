package com.ruoyi.dms.controller;

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
import com.ruoyi.dms.domain.DmsInspectionRoute;
import com.ruoyi.dms.service.IDmsInspectionRouteService;

@RestController
@RequestMapping("/dms/inspection/route")
public class DmsInspectionRouteController extends BaseController
{
    @Autowired
    private IDmsInspectionRouteService dmsInspectionRouteService;

    @PreAuthorize("@ss.hasPermi('dms:inspection:route:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsInspectionRoute route)
    {
        startPage();
        List<DmsInspectionRoute> list = dmsInspectionRouteService.selectRouteList(route);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('dms:inspection:route:query')")
    @GetMapping("/{routeId}")
    public AjaxResult getInfo(@PathVariable Long routeId)
    {
        return AjaxResult.success(dmsInspectionRouteService.selectRouteById(routeId));
    }

    @Log(title = "巡检路线", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:inspection:route:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsInspectionRoute route)
    {
        return toAjax(dmsInspectionRouteService.insertRoute(route));
    }

    @Log(title = "巡检路线", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:inspection:route:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsInspectionRoute route)
    {
        return toAjax(dmsInspectionRouteService.updateRoute(route));
    }

    @Log(title = "巡检路线", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:inspection:route:remove')")
    @DeleteMapping("/{routeIds}")
    public AjaxResult remove(@PathVariable Long[] routeIds)
    {
        return toAjax(dmsInspectionRouteService.deleteRouteByIds(routeIds));
    }
}
