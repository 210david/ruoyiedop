package com.ruoyi.hr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hr.domain.HrSalesProfile;
import com.ruoyi.hr.service.IHrSalesPerfService;

/**
 * 营销岗员工档案扩展 Controller（需求三）
 */
@RestController
@RequestMapping("/hr/salesProfile")
public class HrSalesProfileController extends BaseController {
    @Autowired
    private IHrSalesPerfService profileService;

    @PreAuthorize("@ss.hasPermi('hr:salesProfile:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSalesProfile profile) { return getDataTable(profileService.selectProfileList(profile)); }

    @PreAuthorize("@ss.hasPermi('hr:salesProfile:query')")
    @GetMapping("/{profileId}")
    public AjaxResult getInfo(@PathVariable Long profileId) { return success(profileService.selectProfileById(profileId)); }

    @Log(title = "营销档案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:salesProfile:add')")
    @PostMapping
    public AjaxResult add(@RequestBody HrSalesProfile profile) { return toAjax(profileService.insertProfile(profile)); }

    @Log(title = "营销档案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:salesProfile:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody HrSalesProfile profile) { return toAjax(profileService.updateProfile(profile)); }

    @Log(title = "营销档案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('hr:salesProfile:remove')")
    @DeleteMapping("/{profileIds}")
    public AjaxResult remove(@PathVariable Long[] profileIds) { return toAjax(profileService.deleteProfileByIds(profileIds)); }
}
