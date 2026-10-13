package com.ruoyi.hr.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.vo.HrStatisticsVO;
import com.ruoyi.hr.service.IHrStatisticsService;

/**
 * 人力资源统计报表 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/hr/statistics")
public class HrStatisticsController extends BaseController
{
    @Autowired
    private IHrStatisticsService hrStatisticsService;

    /**
     * 概览统计
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(hrStatisticsService.selectOverview());
    }

    /**
     * 在册人员按部门分布
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/headcountByOrg")
    public AjaxResult headcountByOrg()
    {
        return AjaxResult.success(hrStatisticsService.selectHeadcountByOrg());
    }

    /**
     * 人员流动（按月统计入职/离职）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/flowTrend")
    public AjaxResult flowTrend(HrStatisticsVO query)
    {
        return AjaxResult.success(hrStatisticsService.selectFlowTrend(query));
    }

    /**
     * 出勤月报（按部门聚合）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/attendanceByOrg")
    public AjaxResult attendanceByOrg(HrStatisticsVO query)
    {
        return AjaxResult.success(hrStatisticsService.selectAttendanceByOrg(query));
    }

    /**
     * 出勤趋势（按月）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/attendanceTrend")
    public AjaxResult attendanceTrend(HrStatisticsVO query)
    {
        return AjaxResult.success(hrStatisticsService.selectAttendanceTrend(query));
    }

    /**
     * 人工成本（按月）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/costByMonth")
    public AjaxResult costByMonth(HrStatisticsVO query)
    {
        return AjaxResult.success(hrStatisticsService.selectCostByMonth(query));
    }

    /**
     * 人工成本（按部门，指定月）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/costByOrg")
    public AjaxResult costByOrg(HrStatisticsVO query)
    {
        return AjaxResult.success(hrStatisticsService.selectCostByOrg(query));
    }

    /**
     * 薪酬发放台账（批次）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:list')")
    @GetMapping("/payrollLedger")
    public AjaxResult payrollLedger(HrStatisticsVO query)
    {
        return AjaxResult.success(hrStatisticsService.selectPayrollLedger(query));
    }

    /**
     * 导出报表 Excel（reportType: headcountOrg/flow/attendance/cost/ledger）
     */
    @PreAuthorize("@ss.hasPermi('hr:statistics:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrStatisticsVO query)
    {
        String reportType = query.getReportType();
        List<HrStatisticsVO> list;
        String sheetName;
        if ("flow".equals(reportType))
        {
            list = hrStatisticsService.selectFlowTrend(query);
            sheetName = "人员流动报表";
        }
        else if ("attendance".equals(reportType))
        {
            list = hrStatisticsService.selectAttendanceByOrg(query);
            sheetName = "出勤月报";
        }
        else if ("cost".equals(reportType))
        {
            list = hrStatisticsService.selectCostByOrg(query);
            sheetName = "人工成本月报";
        }
        else if ("ledger".equals(reportType))
        {
            list = hrStatisticsService.selectPayrollLedger(query);
            sheetName = "薪酬发放台账";
        }
        else
        {
            list = hrStatisticsService.selectHeadcountByOrg();
            sheetName = "在册人员报表";
        }
        ExcelUtil<HrStatisticsVO> util = new ExcelUtil<HrStatisticsVO>(HrStatisticsVO.class);
        util.exportExcel(response, list, sheetName);
    }
}
