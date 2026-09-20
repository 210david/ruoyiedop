package com.ruoyi.bid.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.bid.domain.BidQualification;
import com.ruoyi.bid.service.IBidCommonService;

/**
 * 资质资料库 Controller
 */
@RestController
@RequestMapping("/bid/qualification")
public class BidQualificationController extends BaseController
{
    @Autowired
    private IBidCommonService commonService;

    @PreAuthorize("@ss.hasPermi('bid:qualification:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidQualification qualification)
    {
        startPage();
        List<BidQualification> list = commonService.selectQualificationList(qualification);
        return getDataTable(list);
    }

    @Log(title = "资质资料", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bid:qualification:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BidQualification qualification)
    {
        List<BidQualification> list = commonService.selectQualificationList(qualification);
        ExcelUtil<BidQualification> util = new ExcelUtil<>(BidQualification.class);
        util.exportExcel(response, list, "资质资料数据");
    }

    @PreAuthorize("@ss.hasPermi('bid:qualification:query')")
    @GetMapping("/{qualificationId}")
    public AjaxResult getInfo(@PathVariable("qualificationId") Long qualificationId)
    {
        return AjaxResult.success(commonService.selectQualificationById(qualificationId));
    }

    @Log(title = "资质资料", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:qualification:add')")
    @PostMapping
    public AjaxResult add(@org.springframework.validation.annotation.Validated @RequestBody BidQualification qualification)
    {
        return toAjax(commonService.insertQualification(qualification));
    }

    @Log(title = "资质资料", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:qualification:edit')")
    @PutMapping
    public AjaxResult edit(@org.springframework.validation.annotation.Validated @RequestBody BidQualification qualification)
    {
        return toAjax(commonService.updateQualification(qualification));
    }

    @Log(title = "资质资料", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('bid:qualification:remove')")
    @DeleteMapping("/{qualificationIds}")
    public AjaxResult remove(@PathVariable Long[] qualificationIds)
    {
        return toAjax(commonService.deleteQualificationByIds(qualificationIds));
    }
}
