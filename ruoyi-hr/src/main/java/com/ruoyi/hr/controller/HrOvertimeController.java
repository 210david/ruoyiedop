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
import com.ruoyi.hr.domain.HrOvertime;
import com.ruoyi.hr.service.IHrOvertimeService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/overtime")
public class HrOvertimeController extends BaseController
{
    @Autowired
    private IHrOvertimeService hrOvertimeService;

    @PreAuthorize("@ss.hasPermi('hr:overtime:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrOvertime hrOvertime)
    {
        startPage();
        List<HrOvertime> list = hrOvertimeService.selectHrOvertimeList(hrOvertime);
        return getDataTable(list);
    }

    @Log(title = "加班", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:overtime:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrOvertime hrOvertime)
    {
        List<HrOvertime> list = hrOvertimeService.selectHrOvertimeList(hrOvertime);
        ExcelUtil<HrOvertime> util = new ExcelUtil<>(HrOvertime.class);
        util.exportExcel(response, list, "加班");
    }

    @PreAuthorize("@ss.hasPermi('hr:overtime:query')")
    @GetMapping(value = "/{overtimeId}")
    public AjaxResult getInfo(@PathVariable("overtimeId") Long overtimeId)
    {
        return AjaxResult.success(hrOvertimeService.selectHrOvertimeById(overtimeId));
    }

    @PreAuthorize("@ss.hasPermi('hr:overtime:add')")
    @Log(title = "加班", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrOvertime hrOvertime)
    {
        return toAjax(hrOvertimeService.insertHrOvertime(hrOvertime));
    }

    @PreAuthorize("@ss.hasPermi('hr:overtime:edit')")
    @Log(title = "加班", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrOvertime hrOvertime)
    {
        return toAjax(hrOvertimeService.updateHrOvertime(hrOvertime));
    }

    @PreAuthorize("@ss.hasPermi('hr:overtime:remove')")
    @Log(title = "加班", businessType = BusinessType.DELETE)
    @DeleteMapping("/{overtimeIds}")
    public AjaxResult remove(@PathVariable Long[] overtimeIds)
    {
        return toAjax(hrOvertimeService.deleteHrOvertimeByIds(overtimeIds));
    }
}
