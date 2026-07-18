package com.ruoyi.wms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.service.IWmsInventoryService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/inventory")
public class WmsInventoryController extends BaseController
{
    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @PreAuthorize("@ss.hasPermi('wms:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInventory inventory)
    {
        startPage();
        List<WmsInventory> list = wmsInventoryService.selectInventoryList(inventory);
        return getDataTable(list);
    }

    @Log(title = "库存查询", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:inventory:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsInventory inventory)
    {
        List<WmsInventory> list = wmsInventoryService.selectInventoryList(inventory);
        ExcelUtil<WmsInventory> util = new ExcelUtil<>(WmsInventory.class);
        util.exportExcel(response, list, "库存数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:inventory:query')")
    @GetMapping(value = "/{inventoryId}")
    public AjaxResult getInfo(@PathVariable("inventoryId") Long inventoryId)
    {
        return AjaxResult.success(wmsInventoryService.selectInventoryById(inventoryId));
    }

    @Log(title = "库存查询", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:inventory:remove')")
    @DeleteMapping("/{inventoryIds}")
    public AjaxResult remove(@PathVariable Long[] inventoryIds)
    {
        return toAjax(wmsInventoryService.deleteInventoryByIds(inventoryIds));
    }
}
