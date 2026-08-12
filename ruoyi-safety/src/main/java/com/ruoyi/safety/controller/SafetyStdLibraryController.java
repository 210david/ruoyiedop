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
import com.ruoyi.safety.domain.SafetyStdLibrary;
import com.ruoyi.safety.service.ISafetyStdLibraryService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/library")
public class SafetyStdLibraryController extends BaseController
{
    @Autowired
    private ISafetyStdLibraryService safetyStdLibraryService;

    @PreAuthorize("@ss.hasPermi('safety:library:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyStdLibrary library)
    {
        startPage();
        List<SafetyStdLibrary> list = safetyStdLibraryService.selectSafetyStdLibraryList(library);
        return getDataTable(list);
    }

    @Log(title = "标准库", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:library:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyStdLibrary library)
    {
        List<SafetyStdLibrary> list = safetyStdLibraryService.selectSafetyStdLibraryList(library);
        ExcelUtil<SafetyStdLibrary> util = new ExcelUtil<>(SafetyStdLibrary.class);
        util.exportExcel(response, list, "标准库");
    }

    @PreAuthorize("@ss.hasPermi('safety:library:query')")
    @GetMapping(value = "/{libraryId}")
    public AjaxResult getInfo(@PathVariable("libraryId") Long libraryId)
    {
        return AjaxResult.success(safetyStdLibraryService.selectSafetyStdLibraryById(libraryId));
    }

    @PreAuthorize("@ss.hasPermi('safety:library:add')")
    @Log(title = "标准库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyStdLibrary library)
    {
        return toAjax(safetyStdLibraryService.insertSafetyStdLibrary(library));
    }

    @PreAuthorize("@ss.hasPermi('safety:library:edit')")
    @Log(title = "标准库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyStdLibrary library)
    {
        return toAjax(safetyStdLibraryService.updateSafetyStdLibrary(library));
    }

    @PreAuthorize("@ss.hasPermi('safety:library:remove')")
    @Log(title = "标准库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{libraryIds}")
    public AjaxResult remove(@PathVariable Long[] libraryIds)
    {
        return toAjax(safetyStdLibraryService.deleteSafetyStdLibraryByIds(libraryIds));
    }
}