package com.ruoyi.kingdee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.kingdee.domain.KdPurInStock;
import com.ruoyi.kingdee.service.KdPurInStockService;
import com.ruoyi.kingdee.sync.KdPurInStockSyncService;

/**
 * 金蝶采购入库单 控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/kingdee/pur-in-stock")
public class KdPurInStockController extends BaseController
{
    @Autowired
    private KdPurInStockService purInStockService;

    @Autowired
    private KdPurInStockSyncService purInStockSyncService;

    /**
     * 查询采购入库单列表
     */
    @PreAuthorize("@ss.hasPermi('kingdee:purinstock:list')")
    @GetMapping("/list")
    public TableDataInfo list(KdPurInStock query)
    {
        startPage();
        return getDataTable(purInStockService.selectList(query));
    }

    /**
     * 从金蝶同步采购入库单
     */
    @PreAuthorize("@ss.hasPermi('kingdee:purinstock:sync')")
    @Log(title = "金蝶采购入库单", businessType = BusinessType.OTHER)
    @PostMapping("/sync")
    public AjaxResult sync()
    {
        return AjaxResult.success(purInStockSyncService.syncAll());
    }
}
