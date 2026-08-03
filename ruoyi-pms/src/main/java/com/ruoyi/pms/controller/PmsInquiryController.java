package com.ruoyi.pms.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.pms.domain.PmsInquiry;
import com.ruoyi.pms.domain.PmsQuotation;
import com.ruoyi.pms.service.IPmsInquiryService;

/**
 * 询价单Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/inquiry")
public class PmsInquiryController extends BaseController
{
    @Autowired
    private IPmsInquiryService pmsInquiryService;

    @PreAuthorize("@ss.hasPermi('pms:inquiry:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsInquiry pmsInquiry)
    {
        startPage();
        List<PmsInquiry> list = pmsInquiryService.selectPmsInquiryList(pmsInquiry);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:export')")
    @Log(title = "询比价管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsInquiry pmsInquiry)
    {
        List<PmsInquiry> list = pmsInquiryService.selectPmsInquiryList(pmsInquiry);
        ExcelUtil<PmsInquiry> util = new ExcelUtil<PmsInquiry>(PmsInquiry.class);
        util.exportExcel(response, list, "询比价数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:query')")
    @GetMapping(value = "/{inquiryId}")
    public AjaxResult getInfo(@PathVariable("inquiryId") Long inquiryId)
    {
        return AjaxResult.success(pmsInquiryService.selectPmsInquiryById(inquiryId));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:add')")
    @Log(title = "询比价管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsInquiry pmsInquiry)
    {
        return toAjax(pmsInquiryService.insertPmsInquiry(pmsInquiry));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "询比价管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsInquiry pmsInquiry)
    {
        return toAjax(pmsInquiryService.updatePmsInquiry(pmsInquiry));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:remove')")
    @Log(title = "询比价管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inquiryIds}")
    public AjaxResult remove(@PathVariable Long[] inquiryIds)
    {
        return toAjax(pmsInquiryService.deletePmsInquiryByIds(inquiryIds));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "询比价管理", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{inquiryId}")
    public AjaxResult publish(@PathVariable Long inquiryId)
    {
        return toAjax(pmsInquiryService.publishInquiry(inquiryId));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "询比价管理", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{inquiryId}")
    public AjaxResult close(@PathVariable Long inquiryId)
    {
        return toAjax(pmsInquiryService.closeInquiry(inquiryId));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "询比价管理", businessType = BusinessType.UPDATE)
    @PutMapping("/compare/{inquiryId}")
    public AjaxResult compare(@PathVariable Long inquiryId)
    {
        return toAjax(pmsInquiryService.compareInquiry(inquiryId));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "询比价管理", businessType = BusinessType.UPDATE)
    @PutMapping("/award/{inquiryId}")
    public AjaxResult award(@PathVariable Long inquiryId, @RequestParam Long supplierId,
                            @RequestParam String supplierName, @RequestParam java.math.BigDecimal amount)
    {
        return toAjax(pmsInquiryService.awardInquiry(inquiryId, supplierId, supplierName, amount));
    }

    @Log(title = "询比价提交审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @PutMapping("/submit/{inquiryId}")
    public AjaxResult submit(@PathVariable Long inquiryId)
    {
        return toAjax(pmsInquiryService.submitInquiry(inquiryId));
    }

    @Log(title = "询比价审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:inquiry:audit')")
    @PutMapping("/audit/{inquiryId}")
    public AjaxResult audit(@PathVariable Long inquiryId, @RequestParam String status,
                            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pmsInquiryService.auditInquiry(inquiryId, status, auditOpinion));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "录入报价", businessType = BusinessType.INSERT)
    @PostMapping("/quotation")
    public AjaxResult addQuotation(@RequestBody PmsQuotation quotation)
    {
        return toAjax(pmsInquiryService.saveQuotation(quotation));
    }

    @PreAuthorize("@ss.hasPermi('pms:inquiry:edit')")
    @Log(title = "删除报价", businessType = BusinessType.DELETE)
    @DeleteMapping("/quotation/{quotationId}")
    public AjaxResult removeQuotation(@PathVariable Long quotationId)
    {
        return toAjax(pmsInquiryService.deleteQuotation(quotationId));
    }
}
