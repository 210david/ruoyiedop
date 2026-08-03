package com.ruoyi.pms.controller;

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
import com.ruoyi.pms.domain.PmsSupplierQualification;
import com.ruoyi.pms.service.IPmsSupplierQualificationService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 供应商资质 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/qualification")
public class PmsSupplierQualificationController extends BaseController
{
    @Autowired
    private IPmsSupplierQualificationService pmsSupplierQualificationService;

    @PreAuthorize("@ss.hasPermi('pms:qualification:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsSupplierQualification qualification)
    {
        startPage();
        List<PmsSupplierQualification> list = pmsSupplierQualificationService.selectQualificationList(qualification);
        return getDataTable(list);
    }

    @Log(title = "供应商资质", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:qualification:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsSupplierQualification qualification)
    {
        List<PmsSupplierQualification> list = pmsSupplierQualificationService.selectQualificationList(qualification);
        ExcelUtil<PmsSupplierQualification> util = new ExcelUtil<>(PmsSupplierQualification.class);
        util.exportExcel(response, list, "供应商资质数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:qualification:query')")
    @GetMapping(value = "/{qualId}")
    public AjaxResult getInfo(@PathVariable("qualId") Long qualId)
    {
        return AjaxResult.success(pmsSupplierQualificationService.selectQualificationById(qualId));
    }

    @Log(title = "供应商资质", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:qualification:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsSupplierQualification qualification)
    {
        return toAjax(pmsSupplierQualificationService.insertQualification(qualification));
    }

    @Log(title = "供应商资质", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:qualification:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsSupplierQualification qualification)
    {
        return toAjax(pmsSupplierQualificationService.updateQualification(qualification));
    }

    @Log(title = "供应商资质", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:qualification:remove')")
    @DeleteMapping("/{qualIds}")
    public AjaxResult remove(@PathVariable Long[] qualIds)
    {
        return toAjax(pmsSupplierQualificationService.deleteQualificationByIds(qualIds));
    }

    @Log(title = "供应商资质审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:qualification:audit')")
    @PutMapping("/audit/{qualId}")
    public AjaxResult audit(@PathVariable Long qualId, @RequestParam String status,
                            @RequestParam(required = false) String auditRemark)
    {
        return toAjax(pmsSupplierQualificationService.auditQualification(qualId, status, auditRemark));
    }
}
