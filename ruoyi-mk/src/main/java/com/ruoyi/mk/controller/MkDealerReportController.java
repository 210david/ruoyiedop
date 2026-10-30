package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkDealerReport;
import com.ruoyi.mk.service.IMkDealerReportService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 经销商专项报表 Controller（P2-02：分经销商销量/返利/账龄/流向汇总 + 导出）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/dealerReport")
public class MkDealerReportController extends BaseController
{
    @Autowired
    private IMkDealerReportService mkDealerReportService;

    /** 分经销商汇总列表 */
    @PreAuthorize("@ss.hasPermi('marketing:dealerReport:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkDealerReport query)
    {
        startPage();
        List<MkDealerReport> list = mkDealerReportService.selectDealerReportList(query);
        return getDataTable(list);
    }

    /** 汇总合计（全量筛选结果，不受分页影响） */
    @PreAuthorize("@ss.hasPermi('marketing:dealerReport:list')")
    @GetMapping("/total")
    public AjaxResult total(MkDealerReport query)
    {
        return AjaxResult.success(mkDealerReportService.selectDealerReportTotal(query));
    }

    /** 页签数量 */
    @PreAuthorize("@ss.hasPermi('marketing:dealerReport:list')")
    @GetMapping("/counts")
    public AjaxResult counts()
    {
        return AjaxResult.success(mkDealerReportService.selectDealerReportCounts());
    }

    /** 导出经销商专项报表 */
    @Log(title = "经销商专项报表", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:dealerReport:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkDealerReport query)
    {
        List<MkDealerReport> list = mkDealerReportService.selectDealerReportList(query);
        ExcelUtil<MkDealerReport> util = new ExcelUtil<>(MkDealerReport.class);
        util.exportExcel(response, list, "经销商专项报表");
    }
}
