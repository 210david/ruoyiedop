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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.safety.domain.SafetyCert;
import com.ruoyi.safety.service.ISafetyCertService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/cert")
public class SafetyCertController extends BaseController
{
    @Autowired
    private ISafetyCertService safetyCertService;

    @PreAuthorize("@ss.hasPermi('safety:cert:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyCert cert)
    {
        startPage();
        List<SafetyCert> list = safetyCertService.selectSafetyCertList(cert);
        return getDataTable(list);
    }

    @Log(title = "安全证照", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:cert:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyCert cert)
    {
        List<SafetyCert> list = safetyCertService.selectSafetyCertList(cert);
        ExcelUtil<SafetyCert> util = new ExcelUtil<>(SafetyCert.class);
        util.exportExcel(response, list, "安全证照");
    }

    @PreAuthorize("@ss.hasPermi('safety:cert:query')")
    @GetMapping(value = "/{certId}")
    public AjaxResult getInfo(@PathVariable("certId") Long certId)
    {
        return AjaxResult.success(safetyCertService.selectSafetyCertById(certId));
    }

    @PreAuthorize("@ss.hasPermi('safety:cert:add')")
    @Log(title = "安全证照", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyCert cert)
    {
        return toAjax(safetyCertService.insertSafetyCert(cert));
    }

    @PreAuthorize("@ss.hasPermi('safety:cert:edit')")
    @Log(title = "安全证照", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyCert cert)
    {
        return toAjax(safetyCertService.updateSafetyCert(cert));
    }

    @PreAuthorize("@ss.hasPermi('safety:cert:remove')")
    @Log(title = "安全证照", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certIds}")
    public AjaxResult remove(@PathVariable Long[] certIds)
    {
        return toAjax(safetyCertService.deleteSafetyCertByIds(certIds));
    }
}