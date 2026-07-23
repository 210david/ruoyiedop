package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.service.IMkOrderService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 订单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/order")
public class MkOrderController extends BaseController
{
    @Autowired
    private IMkOrderService mkOrderService;

    @PreAuthorize("@ss.hasPermi('marketing:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkOrder order)
    {
        startPage();
        List<MkOrder> list = mkOrderService.selectOrderList(order);
        return getDataTable(list);
    }

    @Log(title = "订单管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:order:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkOrder order)
    {
        List<MkOrder> list = mkOrderService.selectOrderList(order);
        ExcelUtil<MkOrder> util = new ExcelUtil<>(MkOrder.class);
        util.exportExcel(response, list, "订单数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(mkOrderService.selectOrderById(orderId));
    }

    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:order:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkOrder order)
    {
        return toAjax(mkOrderService.insertOrder(order));
    }

    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkOrder order)
    {
        return toAjax(mkOrderService.updateOrder(order));
    }

    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:order:remove')")
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(mkOrderService.deleteOrderByIds(orderIds));
    }

    /**
     * 订单发货
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:deliver')")
    @PutMapping("/deliver")
    public AjaxResult deliver(@RequestBody MkOrder order)
    {
        return toAjax(mkOrderService.deliverOrder(order));
    }

    /**
     * 订单确认
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:confirm')")
    @PutMapping("/confirm/{orderId}")
    public AjaxResult confirm(@PathVariable("orderId") Long orderId)
    {
        return toAjax(mkOrderService.confirmOrder(orderId));
    }

    /**
     * 订单签收
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:receive')")
    @PutMapping("/receive/{orderId}")
    public AjaxResult receive(@PathVariable("orderId") Long orderId, @RequestParam(required = false) String receivePerson)
    {
        return toAjax(mkOrderService.receiveOrder(orderId, receivePerson));
    }

    /**
     * 订单完成
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:edit')")
    @PutMapping("/complete/{orderId}")
    public AjaxResult complete(@PathVariable("orderId") Long orderId)
    {
        return toAjax(mkOrderService.completeOrder(orderId));
    }

    /**
     * 订单取消
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:cancel')")
    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable("orderId") Long orderId, @RequestParam(required = false) String cancelReason)
    {
        return toAjax(mkOrderService.cancelOrder(orderId, cancelReason));
    }
}
