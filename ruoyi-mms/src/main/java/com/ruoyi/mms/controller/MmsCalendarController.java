package com.ruoyi.mms.controller;

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
import com.ruoyi.mms.domain.MmsCalendar;
import com.ruoyi.mms.service.IMmsCalendarService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mms/calendar")
public class MmsCalendarController extends BaseController
{
    @Autowired
    private IMmsCalendarService mmsCalendarService;

    @PreAuthorize("@ss.hasPermi('mms:calendar:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsCalendar calendar)
    {
        startPage();
        List<MmsCalendar> list = mmsCalendarService.selectCalendarList(calendar);
        return getDataTable(list);
    }

    @Log(title = "工厂日历", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:calendar:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsCalendar calendar)
    {
        List<MmsCalendar> list = mmsCalendarService.selectCalendarList(calendar);
        ExcelUtil<MmsCalendar> util = new ExcelUtil<>(MmsCalendar.class);
        util.exportExcel(response, list, "工厂日历数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:calendar:query')")
    @GetMapping(value = "/{calendarId}")
    public AjaxResult getInfo(@PathVariable("calendarId") Long calendarId)
    {
        return AjaxResult.success(mmsCalendarService.selectCalendarById(calendarId));
    }

    @Log(title = "工厂日历", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:calendar:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsCalendar calendar)
    {
        return toAjax(mmsCalendarService.insertCalendar(calendar));
    }

    @Log(title = "工厂日历", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:calendar:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsCalendar calendar)
    {
        return toAjax(mmsCalendarService.updateCalendar(calendar));
    }

    @Log(title = "工厂日历", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:calendar:remove')")
    @DeleteMapping("/{calendarIds}")
    public AjaxResult remove(@PathVariable Long[] calendarIds)
    {
        return toAjax(mmsCalendarService.deleteCalendarByIds(calendarIds));
    }
}
