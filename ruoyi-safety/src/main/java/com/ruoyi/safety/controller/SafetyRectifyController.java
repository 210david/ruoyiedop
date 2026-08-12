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
import com.ruoyi.safety.domain.SafetyRectify;
import com.ruoyi.safety.service.ISafetyRectifyService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/rectify")
public class SafetyRectifyController extends BaseController
{
    @Autowired
    private ISafetyRectifyService safetyRectifyService;

    @PreAuthorize("@ss.hasPermi('safety:hazard:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyRectify rectify)
    {
        startPage();
        List<SafetyRectify> list = safetyRectifyService.selectSafetyRectifyList(rectify);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:query')")
    @GetMapping(value = "/{rectifyId}")
    public AjaxResult getInfo(@PathVariable("rectifyId") Long rectifyId)
    {
        return AjaxResult.success(safetyRectifyService.selectSafetyRectifyById(rectifyId));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:add')")
    @Log(title = "整改记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyRectify rectify)
    {
        return toAjax(safetyRectifyService.insertSafetyRectify(rectify));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:edit')")
    @Log(title = "整改记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyRectify rectify)
    {
        return toAjax(safetyRectifyService.updateSafetyRectify(rectify));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:remove')")
    @Log(title = "整改记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{rectifyIds}")
    public AjaxResult remove(@PathVariable Long[] rectifyIds)
    {
        return toAjax(safetyRectifyService.deleteSafetyRectifyByIds(rectifyIds));
    }
}