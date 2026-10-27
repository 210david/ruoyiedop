package com.ruoyi.dms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.dms.domain.*;
import com.ruoyi.dms.service.*;

/**
 * 设备管理(DMS) 开放接口
 * 路径前缀：/openapi/v1/dms
 * 鉴权方式：请求头 X-API-Key（由OpenApiInterceptor统一校验）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/openapi/v1/dms")
public class OpenDmsController extends BaseController
{
    @Autowired
    private IDmsEquipmentService equipmentService;
    @Autowired
    private IDmsWorkOrderService workOrderService;
    @Autowired
    private IDmsSparePartService sparePartService;
    @Autowired
    private IDmsEquipmentCategoryService categoryService;

    // ==================== 设备台账 ====================

    @GetMapping("/equipment/list")
    public TableDataInfo equipmentList(DmsEquipment query)
    {
        startPage();
        List<DmsEquipment> list = equipmentService.selectEquipmentList(query);
        return getDataTable(list);
    }

    @GetMapping("/equipment/{equipmentId}")
    public AjaxResult equipmentById(@PathVariable Long equipmentId)
    {
        return AjaxResult.success(equipmentService.selectEquipmentById(equipmentId));
    }

    // ==================== 设备分类 ====================

    @GetMapping("/category/list")
    public AjaxResult categoryList(DmsEquipmentCategory query)
    {
        return AjaxResult.success(categoryService.selectCategoryList(query));
    }

    @GetMapping("/category/{categoryId}")
    public AjaxResult categoryById(@PathVariable Long categoryId)
    {
        return AjaxResult.success(categoryService.selectCategoryById(categoryId));
    }

    // ==================== 工单 ====================

    @GetMapping("/workorder/list")
    public TableDataInfo workOrderList(DmsWorkOrder query)
    {
        startPage();
        List<DmsWorkOrder> list = workOrderService.selectWorkOrderList(query);
        return getDataTable(list);
    }

    @GetMapping("/workorder/{orderId}")
    public AjaxResult workOrderById(@PathVariable Long orderId)
    {
        return AjaxResult.success(workOrderService.selectWorkOrderById(orderId));
    }

    // ==================== 备件 ====================

    @GetMapping("/sparepart/list")
    public TableDataInfo sparePartList(DmsSparePart query)
    {
        startPage();
        List<DmsSparePart> list = sparePartService.selectSparePartList(query);
        return getDataTable(list);
    }

    @GetMapping("/sparepart/{partId}")
    public AjaxResult sparePartById(@PathVariable Long partId)
    {
        return AjaxResult.success(sparePartService.selectSparePartById(partId));
    }

    // ==================== 备件出入库记录 ====================

    @GetMapping("/sparepart/record/list")
    public TableDataInfo sparePartRecordList(DmsSparePartRecord query)
    {
        startPage();
        List<DmsSparePartRecord> list = sparePartService.selectRecordList(query);
        return getDataTable(list);
    }

    // ==================== 备件库存预警 ====================

    @GetMapping("/sparepart/alert/list")
    public TableDataInfo sparePartAlertList(DmsSparePart query)
    {
        startPage();
        List<DmsSparePart> list = sparePartService.selectStockAlertList(query);
        return getDataTable(list);
    }
}
