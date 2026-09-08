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
import com.ruoyi.hr.domain.HrClockRepair;
import com.ruoyi.hr.service.IHrClockRepairService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/clockRepair")
public class HrClockRepairController extends BaseController
{
    @Autowired
    private IHrClockRepairService hrClockRepairService;

    @PreAuthorize("@ss.hasPermi('hr:clockRepair:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrClockRepair hrClockRepair)
    {
        startPage();
        List<HrClockRepair> list = hrClockRepairService.selectHrClockRepairList(hrClockRepair);
        return getDataTable(list);
    }

    @Log(title = "补卡", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:clockRepair:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrClockRepair hrClockRepair)
    {
        List<HrClockRepair> list = hrClockRepairService.selectHrClockRepairList(hrClockRepair);
        ExcelUtil<HrClockRepair> util = new ExcelUtil<>(HrClockRepair.class);
        util.exportExcel(response, list, "补卡");
    }

    @PreAuthorize("@ss.hasPermi('hr:clockRepair:query')")
    @GetMapping(value = "/{clockRepairId}")
    public AjaxResult getInfo(@PathVariable("clockRepairId") Long clockRepairId)
    {
        return AjaxResult.success(hrClockRepairService.selectHrClockRepairById(clockRepairId));
    }

    @PreAuthorize("@ss.hasPermi('hr:clockRepair:add')")
    @Log(title = "补卡", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrClockRepair hrClockRepair)
    {
        return toAjax(hrClockRepairService.insertHrClockRepair(hrClockRepair));
    }

    @PreAuthorize("@ss.hasPermi('hr:clockRepair:edit')")
    @Log(title = "补卡", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrClockRepair hrClockRepair)
    {
        return toAjax(hrClockRepairService.updateHrClockRepair(hrClockRepair));
    }

    @PreAuthorize("@ss.hasPermi('hr:clockRepair:remove')")
    @Log(title = "补卡", businessType = BusinessType.DELETE)
    @DeleteMapping("/{clockRepairIds}")
    public AjaxResult remove(@PathVariable Long[] clockRepairIds)
    {
        return toAjax(hrClockRepairService.deleteHrClockRepairByIds(clockRepairIds));
    }

    @PreAuthorize("@ss.hasPermi('hr:clockRepair:audit')")
    @Log(title = "补卡审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{clockRepairId}")
    public AjaxResult audit(@PathVariable Long clockRepairId, @RequestParam String auditAction, @RequestParam(required = false) String auditRemark)
    {
        return toAjax(hrClockRepairService.auditHrClockRepair(clockRepairId, auditAction, auditRemark));
    }
}
