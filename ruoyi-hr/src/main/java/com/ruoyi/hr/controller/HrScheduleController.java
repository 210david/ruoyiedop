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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.dto.HrScheduleBatchDTO;
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

    /** 月度排班日历视图 */
    @PreAuthorize("@ss.hasPermi('hr:schedule:list')")
    @GetMapping("/calendar")
    public AjaxResult calendar(@RequestParam(required = false) List<Long> employeeIds,
                                @RequestParam(required = false) Long orgId,
                                @RequestParam String startDate,
                                @RequestParam String endDate)
    {
        List<HrSchedule> list = hrScheduleService.selectScheduleCalendar(employeeIds, orgId, startDate, endDate);
        return AjaxResult.success(list);
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

    /** 批量排班 */
    @PreAuthorize("@ss.hasPermi('hr:schedule:batch')")
    @Log(title = "批量排班", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batch(@RequestBody HrScheduleBatchDTO dto)
    {
        try {
            int count = hrScheduleService.batchSchedule(dto, SecurityUtils.getUsername());
            return AjaxResult.success("成功生成 " + count + " 条排班记录");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /** 清除排班（按范围批量删除） */
    @PreAuthorize("@ss.hasPermi('hr:schedule:clear')")
    @Log(title = "清除排班", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear")
    public AjaxResult clear(@RequestParam(required = false) List<Long> employeeIds,
                            @RequestParam(required = false) Long orgId,
                            @RequestParam String startDate,
                            @RequestParam String endDate)
    {
        int count = hrScheduleService.clearSchedule(employeeIds, orgId, startDate, endDate);
        return AjaxResult.success("成功清除 " + count + " 条排班记录");
    }
}
