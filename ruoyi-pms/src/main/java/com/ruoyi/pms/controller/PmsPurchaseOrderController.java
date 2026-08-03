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
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.service.IPmsPurchaseOrderService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 采购订单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/order")
public class PmsPurchaseOrderController extends BaseController
{
    @Autowired
    private IPmsPurchaseOrderService pmsPurchaseOrderService;

    @PreAuthorize("@ss.hasPermi('pms:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsPurchaseOrder order)
    {
        startPage();
        List<PmsPurchaseOrder> list = pmsPurchaseOrderService.selectPurchaseOrderList(order);
        return getDataTable(list);
    }

    @Log(title = "采购订单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:order:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsPurchaseOrder order)
    {
        List<PmsPurchaseOrder> list = pmsPurchaseOrderService.selectPurchaseOrderList(order);
        ExcelUtil<PmsPurchaseOrder> util = new ExcelUtil<>(PmsPurchaseOrder.class);
        util.exportExcel(response, list, "采购订单数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(pmsPurchaseOrderService.selectPurchaseOrderById(orderId));
    }

    @Log(title = "采购订单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:order:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsPurchaseOrder order)
    {
        return toAjax(pmsPurchaseOrderService.insertPurchaseOrder(order));
    }

    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:order:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsPurchaseOrder order)
    {
        return toAjax(pmsPurchaseOrderService.updatePurchaseOrder(order));
    }

    @Log(title = "采购订单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:order:remove')")
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(pmsPurchaseOrderService.deletePurchaseOrderByIds(orderIds));
    }

    @Log(title = "采购订单提交", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:order:edit')")
    @PutMapping("/submit/{orderId}")
    public AjaxResult submit(@PathVariable Long orderId)
    {
        return toAjax(pmsPurchaseOrderService.submitPurchaseOrder(orderId));
    }

    @Log(title = "采购订单审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:order:approve')")
    @PutMapping("/audit/{orderId}")
    public AjaxResult audit(@PathVariable Long orderId, @RequestParam String status,
                            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pmsPurchaseOrderService.auditPurchaseOrder(orderId, status, auditOpinion));
    }
}
