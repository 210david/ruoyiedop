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
import com.ruoyi.safety.domain.SafetyEquipment;
import com.ruoyi.safety.service.ISafetyEquipmentService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/equipment")
public class SafetyEquipmentController extends BaseController
{
    @Autowired
    private ISafetyEquipmentService safetyEquipmentService;

    @PreAuthorize("@ss.hasPermi('safety:equipment:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyEquipment equipment)
    {
        startPage();
        List<SafetyEquipment> list = safetyEquipmentService.selectSafetyEquipmentList(equipment);
        return getDataTable(list);
    }

    @Log(title = "安全设备设施", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:equipment:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyEquipment equipment)
    {
        List<SafetyEquipment> list = safetyEquipmentService.selectSafetyEquipmentList(equipment);
        ExcelUtil<SafetyEquipment> util = new ExcelUtil<>(SafetyEquipment.class);
        util.exportExcel(response, list, "安全设备设施");
    }

    @PreAuthorize("@ss.hasPermi('safety:equipment:query')")
    @GetMapping(value = "/{equipmentId}")
    public AjaxResult getInfo(@PathVariable("equipmentId") Long equipmentId)
    {
        return AjaxResult.success(safetyEquipmentService.selectSafetyEquipmentById(equipmentId));
    }

    @PreAuthorize("@ss.hasPermi('safety:equipment:add')")
    @Log(title = "安全设备设施", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyEquipment equipment)
    {
        return toAjax(safetyEquipmentService.insertSafetyEquipment(equipment));
    }

    @PreAuthorize("@ss.hasPermi('safety:equipment:edit')")
    @Log(title = "安全设备设施", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyEquipment equipment)
    {
        return toAjax(safetyEquipmentService.updateSafetyEquipment(equipment));
    }

    @PreAuthorize("@ss.hasPermi('safety:equipment:remove')")
    @Log(title = "安全设备设施", businessType = BusinessType.DELETE)
    @DeleteMapping("/{equipmentIds}")
    public AjaxResult remove(@PathVariable Long[] equipmentIds)
    {
        return toAjax(safetyEquipmentService.deleteSafetyEquipmentByIds(equipmentIds));
    }
}