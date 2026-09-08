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
import com.ruoyi.hr.domain.HrOuting;
import com.ruoyi.hr.service.IHrOutingService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/outing")
public class HrOutingController extends BaseController
{
    @Autowired
    private IHrOutingService hrOutingService;

    @PreAuthorize("@ss.hasPermi('hr:outing:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrOuting hrOuting)
    {
        startPage();
        List<HrOuting> list = hrOutingService.selectHrOutingList(hrOuting);
        return getDataTable(list);
    }

    @Log(title = "外出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:outing:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrOuting hrOuting)
    {
        List<HrOuting> list = hrOutingService.selectHrOutingList(hrOuting);
        ExcelUtil<HrOuting> util = new ExcelUtil<>(HrOuting.class);
        util.exportExcel(response, list, "外出");
    }

    @PreAuthorize("@ss.hasPermi('hr:outing:query')")
    @GetMapping(value = "/{outingId}")
    public AjaxResult getInfo(@PathVariable("outingId") Long outingId)
    {
        return AjaxResult.success(hrOutingService.selectHrOutingById(outingId));
    }

    @PreAuthorize("@ss.hasPermi('hr:outing:add')")
    @Log(title = "外出", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrOuting hrOuting)
    {
        return toAjax(hrOutingService.insertHrOuting(hrOuting));
    }

    @PreAuthorize("@ss.hasPermi('hr:outing:edit')")
    @Log(title = "外出", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrOuting hrOuting)
    {
        return toAjax(hrOutingService.updateHrOuting(hrOuting));
    }

    @PreAuthorize("@ss.hasPermi('hr:outing:remove')")
    @Log(title = "外出", businessType = BusinessType.DELETE)
    @DeleteMapping("/{outingIds}")
    public AjaxResult remove(@PathVariable Long[] outingIds)
    {
        return toAjax(hrOutingService.deleteHrOutingByIds(outingIds));
    }

    @PreAuthorize("@ss.hasPermi('hr:outing:audit')")
    @Log(title = "外出审批", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{outingId}")
    public AjaxResult audit(@PathVariable Long outingId, @RequestParam String auditAction, @RequestParam(required = false) String auditRemark)
    {
        return toAjax(hrOutingService.auditHrOuting(outingId, auditAction, auditRemark));
    }
}
