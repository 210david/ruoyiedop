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
import com.ruoyi.hr.domain.HrOrganization;
import com.ruoyi.hr.service.IHrOrganizationService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/organization")
public class HrOrganizationController extends BaseController
{
    @Autowired
    private IHrOrganizationService hrOrganizationService;

    @PreAuthorize("@ss.hasPermi('hr:organization:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrOrganization hrOrganization)
    {
        startPage();
        List<HrOrganization> list = hrOrganizationService.selectHrOrganizationList(hrOrganization);
        return getDataTable(list);
    }

    @Log(title = "组织架构", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:organization:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrOrganization hrOrganization)
    {
        List<HrOrganization> list = hrOrganizationService.selectHrOrganizationList(hrOrganization);
        ExcelUtil<HrOrganization> util = new ExcelUtil<>(HrOrganization.class);
        util.exportExcel(response, list, "组织架构");
    }

    @PreAuthorize("@ss.hasPermi('hr:organization:query')")
    @GetMapping(value = "/{orgId}")
    public AjaxResult getInfo(@PathVariable("orgId") Long orgId)
    {
        return AjaxResult.success(hrOrganizationService.selectHrOrganizationById(orgId));
    }

    @PreAuthorize("@ss.hasPermi('hr:organization:add')")
    @Log(title = "组织架构", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrOrganization hrOrganization)
    {
        return toAjax(hrOrganizationService.insertHrOrganization(hrOrganization));
    }

    @PreAuthorize("@ss.hasPermi('hr:organization:edit')")
    @Log(title = "组织架构", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrOrganization hrOrganization)
    {
        return toAjax(hrOrganizationService.updateHrOrganization(hrOrganization));
    }

    @PreAuthorize("@ss.hasPermi('hr:organization:remove')")
    @Log(title = "组织架构", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orgIds}")
    public AjaxResult remove(@PathVariable Long[] orgIds)
    {
        return toAjax(hrOrganizationService.deleteHrOrganizationByIds(orgIds));
    }
}
