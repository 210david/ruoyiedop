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
     * 订单发货（支持多次发货）
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:deliver')")
    @PutMapping("/deliver")
    public AjaxResult deliver(@RequestBody MkOrder order)
    {
        return toAjax(mkOrderService.deliverOrder(order));
    }

    /**
     * 订单提交（草稿/已驳回 → 待审核）
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:edit')")
    @PutMapping("/submit/{orderId}")
    public AjaxResult submit(@PathVariable("orderId") Long orderId)
    {
        return toAjax(mkOrderService.submitOrder(orderId));
    }

    /**
     * 订单作废（草稿/待审核/已审核/部分发货 → 已作废）
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:cancel')")
    @PutMapping("/void/{orderId}")
    public AjaxResult voidOrder(@PathVariable("orderId") Long orderId, @RequestParam(required = false) String voidReason)
    {
        return toAjax(mkOrderService.voidOrder(orderId, voidReason));
    }

    /**
     * 订单审核（待审核 → 已审核/已驳回）
     */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:order:approve')")
    @PutMapping("/audit/{orderId}")
    public AjaxResult audit(@PathVariable("orderId") Long orderId, @RequestParam String status,
                            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(mkOrderService.auditOrder(orderId, status, auditOpinion));
    }
}
