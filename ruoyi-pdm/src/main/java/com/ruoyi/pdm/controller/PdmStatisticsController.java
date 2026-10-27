package com.ruoyi.pdm.controller;

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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pdm.domain.PdmStatistics;
import com.ruoyi.pdm.service.IPdmStatisticsService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 产品设计统计分析 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/statistics")
public class PdmStatisticsController extends BaseController
{
    @Autowired
    private IPdmStatisticsService statisticsService;

    @PreAuthorize("@ss.hasPermi('pdm:statistics:view')")
    @GetMapping("/list")
    public TableDataInfo list(PdmStatistics query)
    {
        startPage();
        List<PdmStatistics> list = statisticsService.selectStatisticsList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('pdm:statistics:view')")
    @GetMapping("/summary")
    public AjaxResult summary(PdmStatistics query)
    {
        return AjaxResult.success(statisticsService.selectStatisticsSummary(query));
    }

    @PreAuthorize("@ss.hasPermi('pdm:statistics:view')")
    @GetMapping("/charts")
    public AjaxResult charts()
    {
        return AjaxResult.success(statisticsService.selectChartsData());
    }

    @PreAuthorize("@ss.hasPermi('pdm:statistics:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PdmStatistics query)
    {
        List<PdmStatistics> list = statisticsService.selectStatisticsList(query);
        ExcelUtil<PdmStatistics> util = new ExcelUtil<>(PdmStatistics.class);
        util.exportExcel(response, list, "产品设计统计数据");
    }
}
