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
import com.ruoyi.mk.domain.MkShipment;
import com.ruoyi.mk.service.IMkShipmentService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 发货管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/shipment")
public class MkShipmentController extends BaseController
{
    @Autowired
    private IMkShipmentService mkShipmentService;

    @PreAuthorize("@ss.hasPermi('marketing:shipment:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkShipment shipment)
    {
        startPage();
        List<MkShipment> list = mkShipmentService.selectShipmentList(shipment);
        return getDataTable(list);
    }

    @Log(title = "发货管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:shipment:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkShipment shipment)
    {
        List<MkShipment> list = mkShipmentService.selectShipmentList(shipment);
        ExcelUtil<MkShipment> util = new ExcelUtil<>(MkShipment.class);
        util.exportExcel(response, list, "发货管理数据");
    }

    /**
     * 获取存在进行中发货单的订单ID列表
     * 进行中 = 状态为待发货(0)、已发货(1)
     */
    @PreAuthorize("@ss.hasPermi('marketing:shipment:list')")
    @GetMapping("/inProgressOrderIds")
    public AjaxResult getInProgressOrderIds()
    {
        return AjaxResult.success(mkShipmentService.selectInProgressShipmentOrderIds());
    }

    @PreAuthorize("@ss.hasPermi('marketing:shipment:query')")
    @GetMapping(value = "/{shipmentId}")
    public AjaxResult getInfo(@PathVariable("shipmentId") Long shipmentId)
    {
        return AjaxResult.success(mkShipmentService.selectShipmentById(shipmentId));
    }

    @Log(title = "发货管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:shipment:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkShipment shipment)
    {
        return toAjax(mkShipmentService.insertShipment(shipment));
    }

    @Log(title = "发货管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:shipment:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkShipment shipment)
    {
        return toAjax(mkShipmentService.updateShipment(shipment));
    }

    @Log(title = "发货管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:shipment:remove')")
    @DeleteMapping("/{shipmentIds}")
    public AjaxResult remove(@PathVariable Long[] shipmentIds)
    {
        return toAjax(mkShipmentService.deleteShipmentByIds(shipmentIds));
    }

    /**
     * 确认发货
     */
    @Log(title = "发货管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:shipment:deliver')")
    @PutMapping("/confirm")
    public AjaxResult confirm(@RequestBody MkShipment shipment)
    {
        return toAjax(mkShipmentService.confirmShipment(shipment));
    }

    /**
     * 签收
     */
    @Log(title = "发货管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:shipment:receive')")
    @PutMapping("/receive/{shipmentId}")
    public AjaxResult receive(@PathVariable("shipmentId") Long shipmentId)
    {
        return toAjax(mkShipmentService.receiveShipment(shipmentId));
    }
}
