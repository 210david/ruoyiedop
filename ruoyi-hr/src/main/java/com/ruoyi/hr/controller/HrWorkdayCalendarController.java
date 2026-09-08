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
import com.ruoyi.hr.domain.HrWorkdayCalendar;
import com.ruoyi.hr.service.IHrWorkdayCalendarService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 工作日期维护 Controller
 */
@RestController
@RequestMapping("/hr/workdayCalendar")
public class HrWorkdayCalendarController extends BaseController
{
    @Autowired
    private IHrWorkdayCalendarService hrWorkdayCalendarService;

    /** 查询工作日历列表 */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrWorkdayCalendar hrWorkdayCalendar)
    {
        startPage();
        List<HrWorkdayCalendar> list = hrWorkdayCalendarService.selectHrWorkdayCalendarList(hrWorkdayCalendar);
        return getDataTable(list);
    }

    /** 导出工作日历 */
    @Log(title = "工作日期维护", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrWorkdayCalendar hrWorkdayCalendar)
    {
        List<HrWorkdayCalendar> list = hrWorkdayCalendarService.selectHrWorkdayCalendarList(hrWorkdayCalendar);
        ExcelUtil<HrWorkdayCalendar> util = new ExcelUtil<>(HrWorkdayCalendar.class);
        util.exportExcel(response, list, "工作日期维护");
    }

    /** 获取工作日历详细信息 */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:query')")
    @GetMapping(value = "/{calendarId}")
    public AjaxResult getInfo(@PathVariable("calendarId") Long calendarId)
    {
        return AjaxResult.success(hrWorkdayCalendarService.selectHrWorkdayCalendarById(calendarId));
    }

    /** 新增工作日历 */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:add')")
    @Log(title = "工作日期维护", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrWorkdayCalendar hrWorkdayCalendar)
    {
        return toAjax(hrWorkdayCalendarService.insertHrWorkdayCalendar(hrWorkdayCalendar));
    }

    /** 修改工作日历 */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:edit')")
    @Log(title = "工作日期维护", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrWorkdayCalendar hrWorkdayCalendar)
    {
        return toAjax(hrWorkdayCalendarService.updateHrWorkdayCalendar(hrWorkdayCalendar));
    }

    /** 删除工作日历 */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:remove')")
    @Log(title = "工作日期维护", businessType = BusinessType.DELETE)
    @DeleteMapping("/{calendarIds}")
    public AjaxResult remove(@PathVariable Long[] calendarIds)
    {
        return toAjax(hrWorkdayCalendarService.deleteHrWorkdayCalendarByIds(calendarIds));
    }

    /**
     * 生成年度工作日历
     * @param year 年份
     * @param weekendRest 周末是否休息（true=周六日休息）
     * @param shiftId 默认班次ID
     * @param shiftName 默认班次名称
     */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:generate')")
    @Log(title = "工作日期维护", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(Integer year, Boolean weekendRest, Long shiftId, String shiftName)
    {
        if (year == null)
        {
            return AjaxResult.error("年份不能为空");
        }
        int count = hrWorkdayCalendarService.generateYearlyCalendar(
                year,
                weekendRest != null && weekendRest,
                shiftId,
                shiftName,
                getUsername());
        return AjaxResult.success("成功生成 " + count + " 条日历记录", count);
    }

    /**
     * 批量调整日期类型
     */
    @PreAuthorize("@ss.hasPermi('hr:workdayCalendar:edit')")
    @Log(title = "工作日期维护", businessType = BusinessType.UPDATE)
    @PutMapping("/batchAdjust")
    public AjaxResult batchAdjust(Long[] calendarIds, String dateType, String holidayName)
    {
        return toAjax(hrWorkdayCalendarService.batchAdjustDateType(calendarIds, dateType, holidayName, getUsername()));
    }
}
