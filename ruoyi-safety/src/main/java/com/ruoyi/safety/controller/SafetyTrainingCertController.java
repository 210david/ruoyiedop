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
import com.ruoyi.safety.domain.SafetyTrainingCert;
import com.ruoyi.safety.service.ISafetyTrainingCertService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/training/cert")
public class SafetyTrainingCertController extends BaseController
{
    @Autowired
    private ISafetyTrainingCertService safetyTrainingCertService;

    @PreAuthorize("@ss.hasPermi('safety:training:cert:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyTrainingCert cert)
    {
        startPage();
        List<SafetyTrainingCert> list = safetyTrainingCertService.selectSafetyTrainingCertList(cert);
        return getDataTable(list);
    }

    @Log(title = "培训证书", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:cert:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyTrainingCert cert)
    {
        List<SafetyTrainingCert> list = safetyTrainingCertService.selectSafetyTrainingCertList(cert);
        ExcelUtil<SafetyTrainingCert> util = new ExcelUtil<>(SafetyTrainingCert.class);
        util.exportExcel(response, list, "培训证书");
    }

    @PreAuthorize("@ss.hasPermi('safety:training:cert:query')")
    @GetMapping(value = "/{certId}")
    public AjaxResult getInfo(@PathVariable("certId") Long certId)
    {
        return AjaxResult.success(safetyTrainingCertService.selectSafetyTrainingCertById(certId));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:cert:add')")
    @Log(title = "培训证书", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyTrainingCert cert)
    {
        return toAjax(safetyTrainingCertService.insertSafetyTrainingCert(cert));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:cert:edit')")
    @Log(title = "培训证书", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyTrainingCert cert)
    {
        return toAjax(safetyTrainingCertService.updateSafetyTrainingCert(cert));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:cert:remove')")
    @Log(title = "培训证书", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certIds}")
    public AjaxResult remove(@PathVariable Long[] certIds)
    {
        return toAjax(safetyTrainingCertService.deleteSafetyTrainingCertByIds(certIds));
    }
}
