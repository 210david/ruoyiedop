package com.ruoyi.bi.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.bi.service.IBiMetricService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 决策驾驶舱 Controller
 *
 * 读快照不碰业务大表（PRD v1.2 F2）：
 * 指标值来自 bi_metric_snapshot（每日 01:00 快照任务落库），
 * 预警横幅按规范 6.0 buildAlerts 红橙蓝三级输出。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/bi/dashboard")
public class BiDashboardController extends BaseController
{
    @Autowired
    private IBiMetricService metricService;

    /**
     * 驾驶舱总览：最新快照指标 + 预警横幅 + 近30天趋势
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:view')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        Map<String, Object> data = metricService.getDashboardOverview(30);
        return AjaxResult.success(data);
    }

    /**
     * F3 主题看板（5 域：sales/purchase/production/inventory/quality）
     *
     * 权限标识按域动态校验（PRD F5）：bi:board:{domain}:view，
     * 菜单 SQL 为每个域配置独立 C 菜单与按钮权限。
     */
    @PreAuthorize("@ss.hasPermi('bi:board:' + #domain + ':view')")
    @GetMapping("/board/{domain}")
    public AjaxResult board(@org.springframework.web.bind.annotation.PathVariable String domain)
    {
        Map<String, Object> data = metricService.getBoardData(domain, 30);
        data.put("domain", domain);
        return AjaxResult.success(data);
    }

    /**
     * F6 跨模块价值链视图（销售到现金链 / 供应保障链，节点取快照+上月对比）
     */
    @PreAuthorize("@ss.hasPermi('bi:dashboard:view')")
    @GetMapping("/valueChain")
    public AjaxResult valueChain()
    {
        Map<String, Object> data = metricService.getValueChain();
        return AjaxResult.success(data);
    }

    /**
     * F8 报表导出：最新快照指标全量 Excel（写操作日志，遵循规范 4.3.11）
     */
    @PreAuthorize("@ss.hasPermi('bi:metric:export')")
    @Log(title = "决策驾驶舱", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response)
    {
        List<com.ruoyi.bi.domain.BiDashboardExport> rows = metricService.buildExportRows();
        ExcelUtil<com.ruoyi.bi.domain.BiDashboardExport> util =
                new ExcelUtil<com.ruoyi.bi.domain.BiDashboardExport>(com.ruoyi.bi.domain.BiDashboardExport.class);
        util.exportExcel(response, rows, "决策驾驶舱指标快照");
    }
}
