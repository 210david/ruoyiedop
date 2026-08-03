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
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.service.IPmsPurchaseRequestService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 采购申请 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/request")
public class PmsPurchaseRequestController extends BaseController
{
    @Autowired
    private IPmsPurchaseRequestService pmsPurchaseRequestService;

    @PreAuthorize("@ss.hasPermi('pms:request:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsPurchaseRequest request)
    {
        startPage();
        List<PmsPurchaseRequest> list = pmsPurchaseRequestService.selectPurchaseRequestList(request);
        return getDataTable(list);
    }

    @Log(title = "采购申请", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:request:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsPurchaseRequest request)
    {
        List<PmsPurchaseRequest> list = pmsPurchaseRequestService.selectPurchaseRequestList(request);
        ExcelUtil<PmsPurchaseRequest> util = new ExcelUtil<>(PmsPurchaseRequest.class);
        util.exportExcel(response, list, "采购申请数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:request:query')")
    @GetMapping(value = "/{requestId}")
    public AjaxResult getInfo(@PathVariable("requestId") Long requestId)
    {
        return AjaxResult.success(pmsPurchaseRequestService.selectPurchaseRequestById(requestId));
    }

    @Log(title = "采购申请", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:request:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsPurchaseRequest request)
    {
        return toAjax(pmsPurchaseRequestService.insertPurchaseRequest(request));
    }

    @Log(title = "采购申请", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:request:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsPurchaseRequest request)
    {
        return toAjax(pmsPurchaseRequestService.updatePurchaseRequest(request));
    }

    @Log(title = "采购申请", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:request:remove')")
    @DeleteMapping("/{requestIds}")
    public AjaxResult remove(@PathVariable Long[] requestIds)
    {
        return toAjax(pmsPurchaseRequestService.deletePurchaseRequestByIds(requestIds));
    }

    @Log(title = "采购申请提交", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:request:edit')")
    @PutMapping("/submit/{requestId}")
    public AjaxResult submit(@PathVariable Long requestId)
    {
        return toAjax(pmsPurchaseRequestService.submitPurchaseRequest(requestId));
    }

    @Log(title = "采购申请审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:request:approve')")
    @PutMapping("/audit/{requestId}")
    public AjaxResult audit(@PathVariable Long requestId, @RequestParam String status,
                            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pmsPurchaseRequestService.auditPurchaseRequest(requestId, status, auditOpinion));
    }
}
