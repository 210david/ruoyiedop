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
import com.ruoyi.hr.domain.HrCertificate;
import com.ruoyi.hr.service.IHrCertificateService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/certificate")
public class HrCertificateController extends BaseController
{
    @Autowired
    private IHrCertificateService hrCertificateService;

    @PreAuthorize("@ss.hasPermi('hr:certificate:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrCertificate hrCertificate)
    {
        startPage();
        List<HrCertificate> list = hrCertificateService.selectHrCertificateList(hrCertificate);
        return getDataTable(list);
    }

    @Log(title = "证书台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:certificate:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrCertificate hrCertificate)
    {
        List<HrCertificate> list = hrCertificateService.selectHrCertificateList(hrCertificate);
        ExcelUtil<HrCertificate> util = new ExcelUtil<>(HrCertificate.class);
        util.exportExcel(response, list, "证书台账");
    }

    @PreAuthorize("@ss.hasPermi('hr:certificate:query')")
    @GetMapping(value = "/{certId}")
    public AjaxResult getInfo(@PathVariable("certId") Long certId)
    {
        return AjaxResult.success(hrCertificateService.selectHrCertificateById(certId));
    }

    @PreAuthorize("@ss.hasPermi('hr:certificate:add')")
    @Log(title = "证书台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrCertificate hrCertificate)
    {
        return toAjax(hrCertificateService.insertHrCertificate(hrCertificate));
    }

    @PreAuthorize("@ss.hasPermi('hr:certificate:edit')")
    @Log(title = "证书台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrCertificate hrCertificate)
    {
        return toAjax(hrCertificateService.updateHrCertificate(hrCertificate));
    }

    @PreAuthorize("@ss.hasPermi('hr:certificate:remove')")
    @Log(title = "证书台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certIds}")
    public AjaxResult remove(@PathVariable Long[] certIds)
    {
        return toAjax(hrCertificateService.deleteHrCertificateByIds(certIds));
    }
}
