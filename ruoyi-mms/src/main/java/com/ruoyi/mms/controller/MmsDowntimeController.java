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
import com.ruoyi.mms.domain.MmsDowntime;
import com.ruoyi.mms.service.IMmsDowntimeService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mms/downtime")
public class MmsDowntimeController extends BaseController
{
    @Autowired
    private IMmsDowntimeService mmsDowntimeService;

    @PreAuthorize("@ss.hasPermi('mms:downtime:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsDowntime downtime)
    {
        startPage();
        List<MmsDowntime> list = mmsDowntimeService.selectDowntimeList(downtime);
        return getDataTable(list);
    }

    @Log(title = "停机记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:downtime:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsDowntime downtime)
    {
        List<MmsDowntime> list = mmsDowntimeService.selectDowntimeList(downtime);
        ExcelUtil<MmsDowntime> util = new ExcelUtil<>(MmsDowntime.class);
        util.exportExcel(response, list, "停机记录数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:downtime:query')")
    @GetMapping(value = "/{downtimeId}")
    public AjaxResult getInfo(@PathVariable("downtimeId") Long downtimeId)
    {
        return AjaxResult.success(mmsDowntimeService.selectDowntimeById(downtimeId));
    }

    @Log(title = "停机记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:downtime:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsDowntime downtime)
    {
        return toAjax(mmsDowntimeService.insertDowntime(downtime));
    }

    @Log(title = "停机记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:downtime:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsDowntime downtime)
    {
        return toAjax(mmsDowntimeService.updateDowntime(downtime));
    }

    @Log(title = "停机记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:downtime:remove')")
    @DeleteMapping("/{downtimeIds}")
    public AjaxResult remove(@PathVariable Long[] downtimeIds)
    {
        return toAjax(mmsDowntimeService.deleteDowntimeByIds(downtimeIds));
    }
}
