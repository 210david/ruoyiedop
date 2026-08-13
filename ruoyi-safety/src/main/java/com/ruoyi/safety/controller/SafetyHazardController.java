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
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.service.ISafetyHazardService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/hazard")
public class SafetyHazardController extends BaseController
{
    @Autowired
    private ISafetyHazardService safetyHazardService;

    @PreAuthorize("@ss.hasPermi('safety:hazard:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyHazard hazard)
    {
        startPage();
        List<SafetyHazard> list = safetyHazardService.selectSafetyHazardList(hazard);
        return getDataTable(list);
    }

    @Log(title = "隐患管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:hazard:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyHazard hazard)
    {
        List<SafetyHazard> list = safetyHazardService.selectSafetyHazardList(hazard);
        ExcelUtil<SafetyHazard> util = new ExcelUtil<>(SafetyHazard.class);
        util.exportExcel(response, list, "隐患数据");
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:query')")
    @GetMapping(value = "/{hazardId}")
    public AjaxResult getInfo(@PathVariable("hazardId") Long hazardId)
    {
        return AjaxResult.success(safetyHazardService.selectSafetyHazardById(hazardId));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:add')")
    @Log(title = "隐患管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyHazard hazard)
    {
        return toAjax(safetyHazardService.insertSafetyHazard(hazard));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:edit')")
    @Log(title = "隐患管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyHazard hazard)
    {
        return toAjax(safetyHazardService.updateSafetyHazard(hazard));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:remove')")
    @Log(title = "隐患管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{hazardIds}")
    public AjaxResult remove(@PathVariable Long[] hazardIds)
    {
        return toAjax(safetyHazardService.deleteSafetyHazardByIds(hazardIds));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:submit')")
    @Log(title = "提交隐患", businessType = BusinessType.UPDATE)
    @PutMapping("/submit/{hazardId}")
    public AjaxResult submit(@PathVariable Long hazardId)
    {
        return toAjax(safetyHazardService.submitHazard(hazardId));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:approve')")
    @Log(title = "审批隐患", businessType = BusinessType.UPDATE)
    @PutMapping("/approve")
    public AjaxResult approve(@Validated @RequestBody SafetyHazard hazard)
    {
        return toAjax(safetyHazardService.approveHazard(hazard));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:rectify')")
    @Log(title = "开始整改", businessType = BusinessType.UPDATE)
    @PutMapping("/start/{hazardId}")
    public AjaxResult start(@PathVariable Long hazardId)
    {
        return toAjax(safetyHazardService.startRectify(hazardId));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:rectify')")
    @Log(title = "提交整改", businessType = BusinessType.UPDATE)
    @PutMapping("/rectify")
    public AjaxResult rectify(@Validated @RequestBody SafetyHazard hazard)
    {
        return toAjax(safetyHazardService.submitRectify(hazard));
    }

    @PreAuthorize("@ss.hasPermi('safety:hazard:verify')")
    @Log(title = "验收隐患", businessType = BusinessType.UPDATE)
    @PutMapping("/verify")
    public AjaxResult verify(@Validated @RequestBody SafetyHazard hazard)
    {
        return toAjax(safetyHazardService.verifyHazard(hazard));
    }
}