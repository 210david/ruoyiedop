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
import com.ruoyi.hr.domain.HrEntry;
import com.ruoyi.hr.service.IHrEntryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/hr/entry")
public class HrEntryController extends BaseController
{
    @Autowired
    private IHrEntryService hrEntryService;

    @PreAuthorize("@ss.hasPermi('hr:entry:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrEntry hrEntry)
    {
        startPage();
        List<HrEntry> list = hrEntryService.selectHrEntryList(hrEntry);
        return getDataTable(list);
    }

    @Log(title = "入职", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:entry:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrEntry hrEntry)
    {
        List<HrEntry> list = hrEntryService.selectHrEntryList(hrEntry);
        ExcelUtil<HrEntry> util = new ExcelUtil<>(HrEntry.class);
        util.exportExcel(response, list, "入职");
    }

    @PreAuthorize("@ss.hasPermi('hr:entry:query')")
    @GetMapping(value = "/{entryId}")
    public AjaxResult getInfo(@PathVariable("entryId") Long entryId)
    {
        return AjaxResult.success(hrEntryService.selectHrEntryById(entryId));
    }

    @PreAuthorize("@ss.hasPermi('hr:entry:add')")
    @Log(title = "入职", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrEntry hrEntry)
    {
        return toAjax(hrEntryService.insertHrEntry(hrEntry));
    }

    @PreAuthorize("@ss.hasPermi('hr:entry:edit')")
    @Log(title = "入职", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrEntry hrEntry)
    {
        return toAjax(hrEntryService.updateHrEntry(hrEntry));
    }

    @PreAuthorize("@ss.hasPermi('hr:entry:remove')")
    @Log(title = "入职", businessType = BusinessType.DELETE)
    @DeleteMapping("/{entryIds}")
    public AjaxResult remove(@PathVariable Long[] entryIds)
    {
        return toAjax(hrEntryService.deleteHrEntryByIds(entryIds));
    }

    @PreAuthorize("@ss.hasPermi('hr:entry:audit')")
    @Log(title = "入职审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{entryId}")
    public AjaxResult audit(@PathVariable Long entryId, @RequestParam String auditAction, @RequestParam(required = false) String auditRemark)
    {
        return toAjax(hrEntryService.auditHrEntry(entryId, auditAction, auditRemark));
    }
}
