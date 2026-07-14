package com.ruoyi.wms.controller;

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
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.service.IWmsWarehouseService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/warehouse")
public class WmsWarehouseController extends BaseController
{
    @Autowired
    private IWmsWarehouseService wmsWarehouseService;

    // ==================== 统一CRUD ====================

    @PreAuthorize("@ss.hasPermi('wms:warehouse:list')")
    @GetMapping("/list")
    public AjaxResult list(WmsWarehouse warehouse)
    {
        List<WmsWarehouse> list = wmsWarehouseService.selectWarehouseList(warehouse);
        return AjaxResult.success(list);
    }

    @Log(title = "仓库管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:warehouse:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsWarehouse warehouse)
    {
        List<WmsWarehouse> list = wmsWarehouseService.selectWarehouseList(warehouse);
        ExcelUtil<WmsWarehouse> util = new ExcelUtil<>(WmsWarehouse.class);
        util.exportExcel(response, list, "仓库数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:warehouse:query')")
    @GetMapping(value = "/{warehouseId}")
    public AjaxResult getInfo(@PathVariable("warehouseId") Long warehouseId)
    {
        return AjaxResult.success(wmsWarehouseService.selectWarehouseById(warehouseId));
    }

    @Log(title = "仓库管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:warehouse:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsWarehouse warehouse)
    {
        return toAjax(wmsWarehouseService.insertWarehouse(warehouse));
    }

    @Log(title = "仓库管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:warehouse:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsWarehouse warehouse)
    {
        return toAjax(wmsWarehouseService.updateWarehouse(warehouse));
    }

    @Log(title = "仓库管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:warehouse:remove')")
    @DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds)
    {
        return toAjax(wmsWarehouseService.deleteWarehouseByIds(warehouseIds));
    }

    /** 生成下一个编码 */
    @PreAuthorize("@ss.hasPermi('wms:warehouse:add')")
    @GetMapping("/genCode")
    public AjaxResult genCode(@RequestParam String nodeType, @RequestParam(required = false) Long parentId)
    {
        WmsWarehouse param = new WmsWarehouse();
        param.setNodeType(nodeType);
        param.setParentId(parentId);
        return AjaxResult.success(wmsWarehouseService.generateNextCode(param));
    }

    // ==================== 兼容接口（供其他模块调用） ====================

    /** 查询仓库列表（仅仓库层级） */
    @PreAuthorize("@ss.hasPermi('wms:warehouse:list')")
    @GetMapping("/warehouseList")
    public TableDataInfo warehouseList(WmsWarehouse warehouse)
    {
        warehouse.setNodeType("1");
        startPage();
        List<WmsWarehouse> list = wmsWarehouseService.selectWarehouseList(warehouse);
        return getDataTable(list);
    }

    /** 查询仓区列表（仅仓区层级，按仓库筛选） */
    @PreAuthorize("@ss.hasPermi('wms:area:list')")
    @GetMapping("/area/list")
    public TableDataInfo areaList(WmsWarehouse warehouse)
    {
        warehouse.setNodeType("2");
        // 兼容：前端传 warehouseId 作为父仓库筛选条件
        if (warehouse.getWarehouseId() != null)
        {
            warehouse.setParentId(warehouse.getWarehouseId());
            warehouse.setWarehouseId(null);
        }
        startPage();
        List<WmsWarehouse> list = wmsWarehouseService.selectWarehouseList(warehouse);
        return getDataTable(list);
    }

    /** 查询仓位列表（仅仓位层级，按仓库筛选） */
    @PreAuthorize("@ss.hasPermi('wms:location:list')")
    @GetMapping("/location/list")
    public TableDataInfo locationList(WmsWarehouse warehouse)
    {
        warehouse.setNodeType("3");
        // 兼容：前端传 warehouseId，仓位是仓库的孙节点，使用 FIND_IN_SET 查询
        // warehouseId 保持不变，mapper 中会通过 FIND_IN_SET(warehouseId, ancestors) 过滤
        startPage();
        List<WmsWarehouse> list = wmsWarehouseService.selectWarehouseList(warehouse);
        return getDataTable(list);
    }
}
