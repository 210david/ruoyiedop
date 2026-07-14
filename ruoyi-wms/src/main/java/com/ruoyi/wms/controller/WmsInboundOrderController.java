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
import com.ruoyi.wms.domain.WmsInboundOrder;
import com.ruoyi.wms.service.IWmsInboundOrderService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/inbound")
public class WmsInboundOrderController extends BaseController
{
    @Autowired
    private IWmsInboundOrderService wmsInboundOrderService;

    @PreAuthorize("@ss.hasPermi('wms:inbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInboundOrder order)
    {
        startPage();
        List<WmsInboundOrder> list = wmsInboundOrderService.selectInboundOrderList(order);
        return getDataTable(list);
    }

    @Log(title = "入库管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:inbound:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsInboundOrder order)
    {
        List<WmsInboundOrder> list = wmsInboundOrderService.selectInboundOrderList(order);
        ExcelUtil<WmsInboundOrder> util = new ExcelUtil<>(WmsInboundOrder.class);
        util.exportExcel(response, list, "入库单数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:inbound:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(wmsInboundOrderService.selectInboundOrderById(orderId));
    }

    @Log(title = "入库管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:inbound:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsInboundOrder order)
    {
        return toAjax(wmsInboundOrderService.insertInboundOrder(order));
    }

    @Log(title = "入库管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:inbound:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsInboundOrder order)
    {
        return toAjax(wmsInboundOrderService.updateInboundOrder(order));
    }

    @Log(title = "入库管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:inbound:remove')")
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(wmsInboundOrderService.deleteInboundOrderByIds(orderIds));
    }

    @Log(title = "入库提交", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:inbound:edit')")
    @PutMapping("/submit/{orderId}")
    public AjaxResult submit(@PathVariable Long orderId)
    {
        return toAjax(wmsInboundOrderService.submitInboundOrder(orderId));
    }

    @Log(title = "入库收货", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:inbound:edit')")
    @PutMapping("/receive/{orderId}/{detailId}")
    public AjaxResult receive(@PathVariable Long orderId, @PathVariable Long detailId,
                              @RequestParam BigDecimal receivedQty)
    {
        return toAjax(wmsInboundOrderService.doReceive(orderId, detailId, receivedQty));
    }

    @Log(title = "入库上架", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:inbound:edit')")
    @PutMapping("/putaway/{orderId}/{detailId}")
    public AjaxResult putaway(@PathVariable Long orderId, @PathVariable Long detailId,
                              @RequestParam Long locationId, @RequestParam BigDecimal putawayQty)
    {
        return toAjax(wmsInboundOrderService.doPutaway(orderId, detailId, locationId, putawayQty));
    }
}
