package com.ruoyi.kingdee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.kingdee.domain.KdInventory;
import com.ruoyi.kingdee.service.KdInventoryService;
import com.ruoyi.kingdee.sync.KdInventorySyncService;
import com.ruoyi.kingdee.sync.SyncResult;

/**
 * 金蝶库存查询
 */
@RestController
@RequestMapping("/kingdee/inventory")
public class KdInventoryController extends BaseController
{
    @Autowired private KdInventoryService inventoryService;
    @Autowired private KdInventorySyncService syncService;

    @PreAuthorize("@ss.hasPermi('kingdee:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(KdInventory query)
    {
        startPage();
        List<KdInventory> list = inventoryService.selectList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('kingdee:inventory:list')")
    @GetMapping("/categories")
    public AjaxResult categories()
    {
        return success(inventoryService.selectCategoryList());
    }

    @PreAuthorize("@ss.hasPermi('kingdee:inventory:sync')")
    @PostMapping("/sync")
    public AjaxResult sync()
    {
        SyncResult r = syncService.syncAll();
        return success(r);
    }
}
