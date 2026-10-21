package com.ruoyi.wms.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.*;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.service.*;

/**
 * 仓库管理(WMS) 开放接口
 * 路径前缀：/openapi/v1/wms
 * 鉴权方式：请求头 X-API-Key（由OpenApiInterceptor统一校验）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/openapi/v1/wms")
public class OpenWmsController extends BaseController
{
    @Autowired
    private IWmsMaterialService materialService;
    @Autowired
    private IWmsSupplierService supplierService;
    @Autowired
    private IWmsWarehouseService warehouseService;
    @Autowired
    private IWmsInboundOrderService inboundOrderService;
    @Autowired
    private IWmsOutboundOrderService outboundOrderService;
    @Autowired
    private IWmsInventoryService inventoryService;
    @Autowired
    private IWmsStatisticsService statisticsService;

    // ==================== 物料 ====================

    @GetMapping("/material/list")
    public TableDataInfo materialList(WmsMaterial query)
    {
        startPage();
        List<WmsMaterial> list = materialService.selectMaterialList(query);
        return getDataTable(list);
    }

    @GetMapping("/material/{materialCode}")
    public AjaxResult materialByCode(@PathVariable String materialCode)
    {
        WmsMaterial material = materialService.selectMaterialByCode(materialCode);
        if (material == null)
        {
            return AjaxResult.error("物料不存在：" + materialCode);
        }
        return AjaxResult.success(material);
    }

    // ==================== 供应商 ====================

    @GetMapping("/supplier/list")
    public TableDataInfo supplierList(WmsSupplier query)
    {
        startPage();
        List<WmsSupplier> list = supplierService.selectSupplierList(query);
        return getDataTable(list);
    }

    @GetMapping("/supplier/{supplierId}")
    public AjaxResult supplierById(@PathVariable Long supplierId)
    {
        return AjaxResult.success(supplierService.selectSupplierById(supplierId));
    }

    // ==================== 仓库结构 ====================

    @GetMapping("/warehouse/list")
    public AjaxResult warehouseList(WmsWarehouse query)
    {
        List<WmsWarehouse> list = warehouseService.selectWarehouseList(query);
        return AjaxResult.success(list);
    }

    @GetMapping("/warehouse/{warehouseId}")
    public AjaxResult warehouseById(@PathVariable Long warehouseId)
    {
        return AjaxResult.success(warehouseService.selectWarehouseById(warehouseId));
    }

    // ==================== 入库单 ====================

    @GetMapping("/inbound/list")
    public TableDataInfo inboundList(WmsInboundOrder query)
    {
        startPage();
        List<WmsInboundOrder> list = inboundOrderService.selectInboundOrderList(query);
        return getDataTable(list);
    }

    @GetMapping("/inbound/{orderId}")
    public AjaxResult inboundById(@PathVariable Long orderId)
    {
        return AjaxResult.success(inboundOrderService.selectInboundOrderById(orderId));
    }

    // ==================== 出库单 ====================

    @GetMapping("/outbound/list")
    public TableDataInfo outboundList(WmsOutboundOrder query)
    {
        startPage();
        List<WmsOutboundOrder> list = outboundOrderService.selectOutboundOrderList(query);
        return getDataTable(list);
    }

    @GetMapping("/outbound/{orderId}")
    public AjaxResult outboundById(@PathVariable Long orderId)
    {
        return AjaxResult.success(outboundOrderService.selectOutboundOrderById(orderId));
    }

    // ==================== 库存 ====================

    @GetMapping("/inventory/list")
    public TableDataInfo inventoryList(WmsInventory query)
    {
        startPage();
        List<WmsInventory> list = inventoryService.selectInventoryList(query);
        return getDataTable(list);
    }

    @GetMapping("/inventory/{inventoryId}")
    public AjaxResult inventoryById(@PathVariable Long inventoryId)
    {
        return AjaxResult.success(inventoryService.selectInventoryById(inventoryId));
    }

    // ==================== 库存预警 ====================

    @GetMapping("/stockAlert/list")
    public TableDataInfo stockAlertList(WmsStatisticsVO query)
    {
        startPage();
        List<WmsStatisticsVO> list = statisticsService.selectStockAlertList(query);
        return getDataTable(list);
    }

    @GetMapping("/stockAlert/summary")
    public AjaxResult stockAlertSummary()
    {
        return AjaxResult.success(statisticsService.selectStockAlertSummary());
    }
}
