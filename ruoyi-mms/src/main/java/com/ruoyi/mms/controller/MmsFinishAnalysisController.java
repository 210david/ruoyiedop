package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsFinishAnalysis;
import com.ruoyi.mms.service.IMmsFinishAnalysisService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 工单完工分析报表 Controller
 * 对应设计方案 RPT-05：工单完工分析
 * 指标：完工率、损耗率、收率/成材率、合格率、工时达成率、准时完工率
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/finishAnalysis")
public class MmsFinishAnalysisController extends BaseController
{
    @Autowired
    private IMmsFinishAnalysisService mmsFinishAnalysisService;

    /**
     * 工单完工分析明细列表
     */
    @PreAuthorize("@ss.hasPermi('mms:finishAnalysis:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsFinishAnalysis query)
    {
        startPage();
        List<MmsFinishAnalysis> list = mmsFinishAnalysisService.selectFinishAnalysisList(query);
        return getDataTable(list);
    }

    /**
     * 汇总统计（用于页面顶部统计卡片）
     */
    @PreAuthorize("@ss.hasPermi('mms:finishAnalysis:query')")
    @GetMapping("/summary")
    public AjaxResult summary(MmsFinishAnalysis query)
    {
        MmsFinishAnalysis summary = mmsFinishAnalysisService.selectFinishAnalysisSummary(query);
        return AjaxResult.success(summary);
    }

    /**
     * 导出
     */
    @PreAuthorize("@ss.hasPermi('mms:finishAnalysis:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsFinishAnalysis query)
    {
        List<MmsFinishAnalysis> list = mmsFinishAnalysisService.selectFinishAnalysisList(query);
        ExcelUtil<MmsFinishAnalysis> util = new ExcelUtil<>(MmsFinishAnalysis.class);
        util.exportExcel(response, list, "工单完工分析");
    }
}
