package com.ruoyi.mms.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mms.service.IMmsWorkOrderService;
import com.ruoyi.mms.service.IMmsMpsService;
import com.ruoyi.mms.service.IMmsWorkReportService;
import com.ruoyi.mms.service.IMmsAbnormalService;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.mapper.MmsMpsMapper;
import com.ruoyi.mms.mapper.MmsAbnormalMapper;
import com.ruoyi.mms.mapper.MmsWorkReportMapper;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsMps;
import com.ruoyi.mms.domain.MmsAbnormal;
import com.ruoyi.mms.domain.MmsWorkReport;
/**
 * 生产看板 Controller
 *
 * 提供生产运营数据看板API
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/dashboard")
public class MmsDashboardController extends BaseController
{
    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private MmsMpsMapper mpsMapper;

    @Autowired
    private MmsAbnormalMapper abnormalMapper;

    @Autowired
    private MmsWorkReportMapper workReportMapper;

    @Autowired
    private IMmsAbnormalService abnormalService;

    /**
     * 工单统计
     * 按状态分组统计工单数量
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/workorder/stats")
    public AjaxResult workorderStats()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        // 按状态分组统计
        java.util.Map<String, Integer> statusMap = new java.util.LinkedHashMap<>();
        statusMap.put("total", allOrders.size());
        for (MmsWorkOrder wo : allOrders)
        {
            String status = wo.getStatus() == null ? "0" : wo.getStatus();
            statusMap.merge(status, 1, Integer::sum);
        }
        return AjaxResult.success(statusMap);
    }

    /**
     * 计划统计
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/mps/stats")
    public AjaxResult mpsStats()
    {
        MmsMps query = new MmsMps();
        List<MmsMps> allMps = mpsMapper.selectMpsList(query);
        java.util.Map<String, Integer> statusMap = new java.util.LinkedHashMap<>();
        statusMap.put("total", allMps.size());
        for (MmsMps mps : allMps)
        {
            String status = mps.getStatus() == null ? "0" : mps.getStatus();
            statusMap.merge(status, 1, Integer::sum);
        }
        return AjaxResult.success(statusMap);
    }

    /**
     * 异常统计
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/abnormal/stats")
    public AjaxResult abnormalStats()
    {
        MmsAbnormal query = new MmsAbnormal();
        List<MmsAbnormal> allAbnormal = abnormalMapper.selectAbnormalList(query);
        java.util.Map<String, Integer> statusMap = new java.util.LinkedHashMap<>();
        statusMap.put("total", allAbnormal.size());
        for (MmsAbnormal ab : allAbnormal)
        {
            String status = ab.getStatus() == null ? "0" : ab.getStatus();
            statusMap.merge(status, 1, Integer::sum);
        }
        return AjaxResult.success(statusMap);
    }

    /**
     * 报工统计
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/report/stats")
    public AjaxResult reportStats()
    {
        MmsWorkReport query = new MmsWorkReport();
        List<MmsWorkReport> allReports = workReportMapper.selectWorkReportList(query);
        java.util.Map<String, Integer> statusMap = new java.util.LinkedHashMap<>();
        statusMap.put("total", allReports.size());
        for (MmsWorkReport rp : allReports)
        {
            String status = rp.getStatus() == null ? "0" : rp.getStatus();
            statusMap.merge(status, 1, Integer::sum);
        }
        return AjaxResult.success(statusMap);
    }

    /**
     * 综合看板
     * 返回工单、计划、异常、报工的综合统计
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();

        // 工单统计
        MmsWorkOrder woQuery = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(woQuery);
        java.util.Map<String, Object> woStats = new java.util.LinkedHashMap<>();
        woStats.put("total", allOrders.size());
        int woInProduction = 0;
        int woCompleted = 0;
        int woAbnormal = 0;
        for (MmsWorkOrder wo : allOrders)
        {
            String s = wo.getStatus() == null ? "0" : wo.getStatus();
            if ("1".equals(s) || "2".equals(s)) woInProduction++;
            if ("3".equals(s) || "4".equals(s)) woCompleted++;
            if ("5".equals(s) || "6".equals(s)) woAbnormal++;
        }
        woStats.put("inProduction", woInProduction);
        woStats.put("completed", woCompleted);
        woStats.put("abnormal", woAbnormal);
        result.put("workOrder", woStats);

        // 计划统计
        MmsMps mpsQuery = new MmsMps();
        List<MmsMps> allMps = mpsMapper.selectMpsList(mpsQuery);
        java.util.Map<String, Object> mpsStats = new java.util.LinkedHashMap<>();
        mpsStats.put("total", allMps.size());
        int mpsDraft = 0;
        int mpsReleased = 0;
        int mpsExecuted = 0;
        for (MmsMps mps : allMps)
        {
            String s = mps.getStatus() == null ? "0" : mps.getStatus();
            if ("0".equals(s) || "1".equals(s)) mpsDraft++;
            if ("2".equals(s)) mpsReleased++;
            if ("3".equals(s)) mpsExecuted++;
        }
        mpsStats.put("draft", mpsDraft);
        mpsStats.put("released", mpsReleased);
        mpsStats.put("executed", mpsExecuted);
        result.put("mps", mpsStats);

        // 异常统计
        MmsAbnormal abQuery = new MmsAbnormal();
        List<MmsAbnormal> allAbnormal = abnormalMapper.selectAbnormalList(abQuery);
        java.util.Map<String, Object> abStats = new java.util.LinkedHashMap<>();
        abStats.put("total", allAbnormal.size());
        int abPending = 0;
        int abProcessing = 0;
        int abClosed = 0;
        for (MmsAbnormal ab : allAbnormal)
        {
            String s = ab.getStatus() == null ? "0" : ab.getStatus();
            if ("0".equals(s)) abPending++;
            if ("1".equals(s)) abProcessing++;
            if ("2".equals(s)) abClosed++;
        }
        abStats.put("pending", abPending);
        abStats.put("processing", abProcessing);
        abStats.put("closed", abClosed);
        result.put("abnormal", abStats);

        // 报工统计
        MmsWorkReport rpQuery = new MmsWorkReport();
        List<MmsWorkReport> allReports = workReportMapper.selectWorkReportList(rpQuery);
        java.util.Map<String, Object> rpStats = new java.util.LinkedHashMap<>();
        rpStats.put("total", allReports.size());
        int rpPending = 0;
        int rpApproved = 0;
        int rpRejected = 0;
        for (MmsWorkReport rp : allReports)
        {
            String s = rp.getStatus() == null ? "0" : rp.getStatus();
            if ("0".equals(s)) rpPending++;
            if ("1".equals(s)) rpApproved++;
            if ("2".equals(s)) rpRejected++;
        }
        rpStats.put("pending", rpPending);
        rpStats.put("approved", rpApproved);
        rpStats.put("rejected", rpRejected);
        result.put("report", rpStats);

        // 今日停机统计
        java.util.Map<String, Object> dtStats = abnormalService.getDowntimeTodayStats();
        result.put("downtime", dtStats);

        // 未闭环异常数（待响应+处理中）
        int unclosedAbnormal = abPending + abProcessing;
        java.util.Map<String, Object> abnormalSummary = new java.util.LinkedHashMap<>();
        abnormalSummary.put("unclosed", unclosedAbnormal);
        abnormalSummary.put("pending", abPending);
        abnormalSummary.put("processing", abProcessing);
        result.put("abnormalSummary", abnormalSummary);

        return AjaxResult.success(result);
    }
}
