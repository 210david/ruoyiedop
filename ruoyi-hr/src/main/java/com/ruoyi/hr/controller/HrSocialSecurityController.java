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
import com.ruoyi.hr.domain.HrSocialSecurity;
import com.ruoyi.hr.service.IHrSocialSecurityService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/socialSecurity")
public class HrSocialSecurityController extends BaseController
{
    @Autowired
    private IHrSocialSecurityService hrSocialSecurityService;

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSocialSecurity hrSocialSecurity)
    {
        startPage();
        List<HrSocialSecurity> list = hrSocialSecurityService.selectHrSocialSecurityList(hrSocialSecurity);
        return getDataTable(list);
    }

    @Log(title = "社保公积金", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSocialSecurity hrSocialSecurity)
    {
        List<HrSocialSecurity> list = hrSocialSecurityService.selectHrSocialSecurityList(hrSocialSecurity);
        ExcelUtil<HrSocialSecurity> util = new ExcelUtil<>(HrSocialSecurity.class);
        util.exportExcel(response, list, "社保公积金");
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:query')")
    @GetMapping(value = "/{ssId}")
    public AjaxResult getInfo(@PathVariable("ssId") Long ssId)
    {
        return AjaxResult.success(hrSocialSecurityService.selectHrSocialSecurityById(ssId));
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:add')")
    @Log(title = "社保公积金", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSocialSecurity hrSocialSecurity)
    {
        return toAjax(hrSocialSecurityService.insertHrSocialSecurity(hrSocialSecurity));
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:edit')")
    @Log(title = "社保公积金", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSocialSecurity hrSocialSecurity)
    {
        return toAjax(hrSocialSecurityService.updateHrSocialSecurity(hrSocialSecurity));
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:remove')")
    @Log(title = "社保公积金", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ssIds}")
    public AjaxResult remove(@PathVariable Long[] ssIds)
    {
        return toAjax(hrSocialSecurityService.deleteHrSocialSecurityByIds(ssIds));
    }
}
