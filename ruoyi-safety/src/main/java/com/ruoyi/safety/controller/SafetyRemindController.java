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
import com.ruoyi.safety.domain.SafetyRemind;
import com.ruoyi.safety.service.ISafetyRemindService;

import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/safety/remind")
public class SafetyRemindController extends BaseController
{
    @Autowired
    private ISafetyRemindService safetyRemindService;

    @PreAuthorize("@ss.hasPermi('safety:remind:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyRemind remind)
    {
        startPage();
        List<SafetyRemind> list = safetyRemindService.selectSafetyRemindList(remind);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('safety:remind:export')")
    @Log(title = "到期提醒", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyRemind remind)
    {
        List<SafetyRemind> list = safetyRemindService.selectSafetyRemindList(remind);
        ExcelUtil<SafetyRemind> util = new ExcelUtil<>(SafetyRemind.class);
        util.exportExcel(response, list, "到期提醒数据");
    }

    @PreAuthorize("@ss.hasPermi('safety:remind:query')")
    @GetMapping(value = "/{remindId}")
    public AjaxResult getInfo(@PathVariable("remindId") Long remindId)
    {
        return AjaxResult.success(safetyRemindService.selectSafetyRemindById(remindId));
    }

    @PreAuthorize("@ss.hasPermi('safety:remind:handle')")
    @Log(title = "到期提醒", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@Validated @RequestBody SafetyRemind remind)
    {
        return toAjax(safetyRemindService.handleRemind(remind));
    }

    @PreAuthorize("@ss.hasPermi('safety:remind:handle')")
    @Log(title = "到期提醒", businessType = BusinessType.DELETE)
    @DeleteMapping("/{remindIds}")
    public AjaxResult remove(@PathVariable Long[] remindIds)
    {
        return toAjax(safetyRemindService.deleteSafetyRemindByIds(remindIds));
    }
}