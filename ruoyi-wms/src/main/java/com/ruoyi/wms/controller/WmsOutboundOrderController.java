package com.ruoyi.wms.controller;

import java.math.BigDecimal;
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
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.service.IWmsOutboundOrderService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/outbound")
public class WmsOutboundOrderController extends BaseController
{
    @Autowired
    private IWmsOutboundOrderService wmsOutboundOrderService;

    @PreAuthorize("@ss.hasPermi('wms:outbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsOutboundOrder order)
    {
        startPage();
        List<WmsOutboundOrder> list = wmsOutboundOrderService.selectOutboundOrderList(order);
        return getDataTable(list);
    }

    @Log(title = "出库管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:outbound:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsOutboundOrder order)
    {
        List<WmsOutboundOrder> list = wmsOutboundOrderService.selectOutboundOrderList(order);
        ExcelUtil<WmsOutboundOrder> util = new ExcelUtil<>(WmsOutboundOrder.class);
        util.exportExcel(response, list, "出库单数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:outbound:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(wmsOutboundOrderService.selectOutboundOrderById(orderId));
    }

    @Log(title = "出库管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:outbound:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsOutboundOrder order)
    {
        return toAjax(wmsOutboundOrderService.insertOutboundOrder(order));
    }

    @Log(title = "出库管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:outbound:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsOutboundOrder order)
    {
        return toAjax(wmsOutboundOrderService.updateOutboundOrder(order));
    }

    @Log(title = "出库管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:outbound:remove')")
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(wmsOutboundOrderService.deleteOutboundOrderByIds(orderIds));
    }

    @Log(title = "出库提交", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:outbound:edit')")
    @PutMapping("/submit/{orderId}")
    public AjaxResult submit(@PathVariable Long orderId)
    {
        return toAjax(wmsOutboundOrderService.submitOutboundOrder(orderId));
    }

    @Log(title = "出库拣货", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:outbound:edit')")
    @PutMapping("/pick/{orderId}/{detailId}")
    public AjaxResult pick(@PathVariable Long orderId, @PathVariable Long detailId,
                           @RequestParam BigDecimal pickQty)
    {
        return toAjax(wmsOutboundOrderService.doPick(orderId, detailId, pickQty));
    }

    @Log(title = "出库复核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:outbound:edit')")
    @PutMapping("/check/{orderId}/{detailId}")
    public AjaxResult check(@PathVariable Long orderId, @PathVariable Long detailId,
                            @RequestParam BigDecimal actualQty)
    {
        return toAjax(wmsOutboundOrderService.doCheck(orderId, detailId, actualQty));
    }
}
