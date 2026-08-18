package com.ruoyi.mms.controller;

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
import com.ruoyi.mms.domain.MmsOutsource;
import com.ruoyi.mms.service.IMmsOutsourceService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mms/outsource")
public class MmsOutsourceController extends BaseController
{
    @Autowired
    private IMmsOutsourceService mmsOutsourceService;

    @PreAuthorize("@ss.hasPermi('mms:outsource:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsOutsource outsource)
    {
        startPage();
        List<MmsOutsource> list = mmsOutsourceService.selectOutsourceList(outsource);
        return getDataTable(list);
    }

    @Log(title = "外协管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:outsource:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsOutsource outsource)
    {
        List<MmsOutsource> list = mmsOutsourceService.selectOutsourceList(outsource);
        ExcelUtil<MmsOutsource> util = new ExcelUtil<>(MmsOutsource.class);
        util.exportExcel(response, list, "外协管理数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:outsource:query')")
    @GetMapping(value = "/{outsourceId}")
    public AjaxResult getInfo(@PathVariable("outsourceId") Long outsourceId)
    {
        return AjaxResult.success(mmsOutsourceService.selectOutsourceById(outsourceId));
    }

    @Log(title = "外协管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:outsource:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsOutsource outsource)
    {
        return toAjax(mmsOutsourceService.insertOutsource(outsource));
    }

    @Log(title = "外协管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:outsource:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsOutsource outsource)
    {
        return toAjax(mmsOutsourceService.updateOutsource(outsource));
    }

    @Log(title = "外协管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:outsource:remove')")
    @DeleteMapping("/{outsourceIds}")
    public AjaxResult remove(@PathVariable Long[] outsourceIds)
    {
        return toAjax(mmsOutsourceService.deleteOutsourceByIds(outsourceIds));
    }
}
