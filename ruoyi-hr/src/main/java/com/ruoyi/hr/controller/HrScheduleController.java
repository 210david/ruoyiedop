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
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.service.IHrScheduleService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/schedule")
public class HrScheduleController extends BaseController
{
    @Autowired
    private IHrScheduleService hrScheduleService;

    @PreAuthorize("@ss.hasPermi('hr:schedule:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSchedule hrSchedule)
    {
        startPage();
        List<HrSchedule> list = hrScheduleService.selectHrScheduleList(hrSchedule);
        return getDataTable(list);
    }

    @Log(title = "排班", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:schedule:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSchedule hrSchedule)
    {
        List<HrSchedule> list = hrScheduleService.selectHrScheduleList(hrSchedule);
        ExcelUtil<HrSchedule> util = new ExcelUtil<>(HrSchedule.class);
        util.exportExcel(response, list, "排班");
    }

    @PreAuthorize("@ss.hasPermi('hr:schedule:query')")
    @GetMapping(value = "/{scheduleId}")
    public AjaxResult getInfo(@PathVariable("scheduleId") Long scheduleId)
    {
        return AjaxResult.success(hrScheduleService.selectHrScheduleById(scheduleId));
    }

    @PreAuthorize("@ss.hasPermi('hr:schedule:add')")
    @Log(title = "排班", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSchedule hrSchedule)
    {
        return toAjax(hrScheduleService.insertHrSchedule(hrSchedule));
    }

    @PreAuthorize("@ss.hasPermi('hr:schedule:edit')")
    @Log(title = "排班", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSchedule hrSchedule)
    {
        return toAjax(hrScheduleService.updateHrSchedule(hrSchedule));
    }

    @PreAuthorize("@ss.hasPermi('hr:schedule:remove')")
    @Log(title = "排班", businessType = BusinessType.DELETE)
    @DeleteMapping("/{scheduleIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleIds)
    {
        return toAjax(hrScheduleService.deleteHrScheduleByIds(scheduleIds));
    }
}
