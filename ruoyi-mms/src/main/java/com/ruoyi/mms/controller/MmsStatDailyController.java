package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsStatDaily;
import com.ruoyi.mms.service.IMmsStatDailyService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mms/daily")
public class MmsStatDailyController extends BaseController
{
    @Autowired
    private IMmsStatDailyService mmsStatDailyService;

    @PreAuthorize("@ss.hasPermi('mms:daily:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsStatDaily statDaily)
    {
        startPage();
        List<MmsStatDaily> list = mmsStatDailyService.selectStatDailyList(statDaily);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:daily:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsStatDaily statDaily)
    {
        List<MmsStatDaily> list = mmsStatDailyService.selectStatDailyList(statDaily);
        ExcelUtil<MmsStatDaily> util = new ExcelUtil<>(MmsStatDaily.class);
        util.exportExcel(response, list, "生产日报数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:daily:query')")
    @GetMapping(value = "/{statId}")
    public AjaxResult getInfo(@PathVariable("statId") Long statId)
    {
        return AjaxResult.success(mmsStatDailyService.selectStatDailyById(statId));
    }
}
