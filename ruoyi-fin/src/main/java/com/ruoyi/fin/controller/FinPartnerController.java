package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinPartner;
import com.ruoyi.fin.service.IFinPartnerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 财务往来单位 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/partner")
public class FinPartnerController extends BaseController
{
    @Autowired
    private IFinPartnerService partnerService;

    @PreAuthorize("@ss.hasPermi('fin:partner:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinPartner partner)
    {
        startPage();
        List<FinPartner> list = partnerService.selectPartnerList(partner);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('fin:partner:query')")
    @GetMapping("/{partnerId}")
    public AjaxResult getInfo(@PathVariable("partnerId") Long partnerId)
    {
        return AjaxResult.success(partnerService.selectPartnerById(partnerId));
    }

    @Log(title = "财务往来单位", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:partner:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FinPartner partner)
    {
        return toAjax(partnerService.insertPartner(partner));
    }

    @Log(title = "财务往来单位", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:partner:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FinPartner partner)
    {
        return toAjax(partnerService.updatePartner(partner));
    }

    @Log(title = "财务往来单位", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:partner:remove')")
    @DeleteMapping("/{partnerIds}")
    public AjaxResult remove(@PathVariable Long[] partnerIds)
    {
        return toAjax(partnerService.deletePartnerByIds(partnerIds));
    }

    @Log(title = "财务往来单位", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:partner:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinPartner partner)
    {
        List<FinPartner> list = partnerService.selectPartnerList(partner);
        ExcelUtil<FinPartner> util = new ExcelUtil<>(FinPartner.class);
        util.exportExcel(response, list, "财务往来单位数据");
    }
}