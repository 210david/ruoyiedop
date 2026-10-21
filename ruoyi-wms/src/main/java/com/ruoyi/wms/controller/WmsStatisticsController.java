package com.ruoyi.wms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wms.domain.vo.WmsStatisticsVO;
import com.ruoyi.wms.service.IWmsStatisticsService;

/**
 * 仓库管理统计报表 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/wms/statistics")
public class WmsStatisticsController extends BaseController
{
    @Autowired
    private IWmsStatisticsService wmsStatisticsService;

    /**
     * 概览统计
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(wmsStatisticsService.selectOverview());
    }

    /**
     * 按仓库统计库存分布
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/inventoryByWarehouse")
    public AjaxResult inventoryByWarehouse()
    {
        return AjaxResult.success(wmsStatisticsService.selectInventoryByWarehouse());
    }

    /**
     * 按物料类型统计库存分布
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/inventoryByMaterialType")
    public AjaxResult inventoryByMaterialType()
    {
        return AjaxResult.success(wmsStatisticsService.selectInventoryByMaterialType());
    }

    /**
     * 出入库趋势（按日统计）
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/inboundOutboundTrend")
    public AjaxResult inboundOutboundTrend(WmsStatisticsVO query)
    {
        return AjaxResult.success(wmsStatisticsService.selectInboundOutboundTrend(query));
    }

    /**
     * 入库按类型统计
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/inboundByType")
    public AjaxResult inboundByType(WmsStatisticsVO query)
    {
        return AjaxResult.success(wmsStatisticsService.selectInboundByType(query));
    }

    /**
     * 出库按类型统计
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/outboundByType")
    public AjaxResult outboundByType(WmsStatisticsVO query)
    {
        return AjaxResult.success(wmsStatisticsService.selectOutboundByType(query));
    }

    /**
     * 物料库存排行 Top N
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/materialTop")
    public AjaxResult materialTop(@RequestParam(defaultValue = "10") Integer count)
    {
        WmsStatisticsVO query = new WmsStatisticsVO();
        query.setCount(count);
        return AjaxResult.success(wmsStatisticsService.selectMaterialTop(query));
    }

    /**
     * 库存预警（低于安全库存下限）
     */
    @PreAuthorize("@ss.hasPermi('wms:statistics:list')")
    @GetMapping("/stockAlert")
    public AjaxResult stockAlert()
    {
        return AjaxResult.success(wmsStatisticsService.selectStockAlert());
    }
}
