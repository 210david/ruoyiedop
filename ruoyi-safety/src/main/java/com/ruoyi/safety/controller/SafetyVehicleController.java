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
import com.ruoyi.safety.domain.SafetyVehicle;
import com.ruoyi.safety.service.ISafetyVehicleService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/vehicle")
public class SafetyVehicleController extends BaseController
{
    @Autowired
    private ISafetyVehicleService safetyVehicleService;

    @PreAuthorize("@ss.hasPermi('safety:vehicle:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyVehicle vehicle)
    {
        startPage();
        List<SafetyVehicle> list = safetyVehicleService.selectSafetyVehicleList(vehicle);
        return getDataTable(list);
    }

    @Log(title = "车辆台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:vehicle:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyVehicle vehicle)
    {
        List<SafetyVehicle> list = safetyVehicleService.selectSafetyVehicleList(vehicle);
        ExcelUtil<SafetyVehicle> util = new ExcelUtil<>(SafetyVehicle.class);
        util.exportExcel(response, list, "车辆台账");
    }

    @PreAuthorize("@ss.hasPermi('safety:vehicle:query')")
    @GetMapping(value = "/{vehicleId}")
    public AjaxResult getInfo(@PathVariable("vehicleId") Long vehicleId)
    {
        return AjaxResult.success(safetyVehicleService.selectSafetyVehicleById(vehicleId));
    }

    @PreAuthorize("@ss.hasPermi('safety:vehicle:add')")
    @Log(title = "车辆台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyVehicle vehicle)
    {
        vehicle.setCreateBy(getUsername());
        return toAjax(safetyVehicleService.insertSafetyVehicle(vehicle));
    }

    @PreAuthorize("@ss.hasPermi('safety:vehicle:edit')")
    @Log(title = "车辆台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyVehicle vehicle)
    {
        vehicle.setUpdateBy(getUsername());
        return toAjax(safetyVehicleService.updateSafetyVehicle(vehicle));
    }

    @PreAuthorize("@ss.hasPermi('safety:vehicle:remove')")
    @Log(title = "车辆台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{vehicleIds}")
    public AjaxResult remove(@PathVariable Long[] vehicleIds)
    {
        return toAjax(safetyVehicleService.deleteSafetyVehicleByIds(vehicleIds));
    }
}
