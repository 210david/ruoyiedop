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
import com.ruoyi.kingdee.domain.KdSalOutbound;
import com.ruoyi.kingdee.service.KdSalOutboundService;
import com.ruoyi.kingdee.sync.KdSalOutboundSyncService;

/**
 * 金蝶销售出库单 控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/kingdee/sal-outbound")
public class KdSalOutboundController extends BaseController
{
    @Autowired
    private KdSalOutboundService salOutboundService;

    @Autowired
    private KdSalOutboundSyncService salOutboundSyncService;

    /**
     * 查询销售出库单列表
     */
    @PreAuthorize("@ss.hasPermi('kingdee:saloutbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(KdSalOutbound query)
    {
        startPage();
        return getDataTable(salOutboundService.selectList(query));
    }

    /**
     * 从金蝶同步销售出库单
     */
    @PreAuthorize("@ss.hasPermi('kingdee:saloutbound:sync')")
    @Log(title = "金蝶销售出库单", businessType = BusinessType.OTHER)
    @PostMapping("/sync")
    public AjaxResult sync()
    {
        return AjaxResult.success(salOutboundSyncService.syncAll());
    }
}
