package com.ruoyi.hr.controller;

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
import com.ruoyi.hr.domain.HrBusinessTrip;
import com.ruoyi.hr.service.IHrBusinessTripService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/businessTrip")
public class HrBusinessTripController extends BaseController
{
    @Autowired
    private IHrBusinessTripService hrBusinessTripService;

    @PreAuthorize("@ss.hasPermi('hr:businessTrip:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrBusinessTrip hrBusinessTrip)
    {
        startPage();
        List<HrBusinessTrip> list = hrBusinessTripService.selectHrBusinessTripList(hrBusinessTrip);
        return getDataTable(list);
    }

    @Log(title = "出差", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:businessTrip:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrBusinessTrip hrBusinessTrip)
    {
        List<HrBusinessTrip> list = hrBusinessTripService.selectHrBusinessTripList(hrBusinessTrip);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, hrBusinessTrip);
        ExcelUtil<HrBusinessTrip> util = new ExcelUtil<>(HrBusinessTrip.class);
        util.exportExcel(response, list, "出差");
    }

    @PreAuthorize("@ss.hasPermi('hr:businessTrip:query')")
    @GetMapping(value = "/{tripId}")
    public AjaxResult getInfo(@PathVariable("tripId") Long tripId)
    {
        return AjaxResult.success(hrBusinessTripService.selectHrBusinessTripById(tripId));
    }

    @PreAuthorize("@ss.hasPermi('hr:businessTrip:add')")
    @Log(title = "出差", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrBusinessTrip hrBusinessTrip)
    {
        return toAjax(hrBusinessTripService.insertHrBusinessTrip(hrBusinessTrip));
    }

    @PreAuthorize("@ss.hasPermi('hr:businessTrip:edit')")
    @Log(title = "出差", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrBusinessTrip hrBusinessTrip)
    {
        return toAjax(hrBusinessTripService.updateHrBusinessTrip(hrBusinessTrip));
    }

    @PreAuthorize("@ss.hasPermi('hr:businessTrip:remove')")
    @Log(title = "出差", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tripIds}")
    public AjaxResult remove(@PathVariable Long[] tripIds)
    {
        return toAjax(hrBusinessTripService.deleteHrBusinessTripByIds(tripIds));
    }

    @PreAuthorize("@ss.hasPermi('hr:businessTrip:audit')")
    @Log(title = "出差审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{tripId}")
    public AjaxResult audit(@PathVariable Long tripId, @RequestParam String auditAction, @RequestParam(required = false) String auditRemark)
    {
        return toAjax(hrBusinessTripService.auditHrBusinessTrip(tripId, auditAction, auditRemark));
    }
}
