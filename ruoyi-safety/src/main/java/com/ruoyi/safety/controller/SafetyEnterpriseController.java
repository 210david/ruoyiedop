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
import com.ruoyi.safety.domain.SafetyEnterprise;
import com.ruoyi.safety.service.ISafetyEnterpriseService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/enterprise")
public class SafetyEnterpriseController extends BaseController
{
    @Autowired
    private ISafetyEnterpriseService safetyEnterpriseService;

    @PreAuthorize("@ss.hasPermi('safety:enterprise:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyEnterprise enterprise)
    {
        startPage();
        List<SafetyEnterprise> list = safetyEnterpriseService.selectSafetyEnterpriseList(enterprise);
        return getDataTable(list);
    }

    @Log(title = "企业安全档案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:enterprise:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyEnterprise enterprise)
    {
        List<SafetyEnterprise> list = safetyEnterpriseService.selectSafetyEnterpriseList(enterprise);
        ExcelUtil<SafetyEnterprise> util = new ExcelUtil<>(SafetyEnterprise.class);
        util.exportExcel(response, list, "企业安全档案");
    }

    @PreAuthorize("@ss.hasPermi('safety:enterprise:query')")
    @GetMapping(value = "/{enterpriseId}")
    public AjaxResult getInfo(@PathVariable("enterpriseId") Long enterpriseId)
    {
        return AjaxResult.success(safetyEnterpriseService.selectSafetyEnterpriseById(enterpriseId));
    }

    @PreAuthorize("@ss.hasPermi('safety:enterprise:add')")
    @Log(title = "企业安全档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyEnterprise enterprise)
    {
        return toAjax(safetyEnterpriseService.insertSafetyEnterprise(enterprise));
    }

    @PreAuthorize("@ss.hasPermi('safety:enterprise:edit')")
    @Log(title = "企业安全档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyEnterprise enterprise)
    {
        return toAjax(safetyEnterpriseService.updateSafetyEnterprise(enterprise));
    }

    @PreAuthorize("@ss.hasPermi('safety:enterprise:remove')")
    @Log(title = "企业安全档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{enterpriseIds}")
    public AjaxResult remove(@PathVariable Long[] enterpriseIds)
    {
        return toAjax(safetyEnterpriseService.deleteSafetyEnterpriseByIds(enterpriseIds));
    }
}