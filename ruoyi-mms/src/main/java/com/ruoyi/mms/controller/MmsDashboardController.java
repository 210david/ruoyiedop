package com.ruoyi.mms.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
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

    /** 工单状态标签映射 */
    private static final Map<String, String> WO_STATUS_MAP = new LinkedHashMap<>();
    static {
        WO_STATUS_MAP.put("0", "新建");
        WO_STATUS_MAP.put("1", "已下达");
        WO_STATUS_MAP.put("2", "执行中");
        WO_STATUS_MAP.put("3", "已完工");
        WO_STATUS_MAP.put("4", "已关闭");
        WO_STATUS_MAP.put("5", "已暂停");
        WO_STATUS_MAP.put("6", "已作废");
    }

    /** 异常类型标签映射 */
    private static final Map<String, String> AB_TYPE_MAP = new LinkedHashMap<>();
    static {
        AB_TYPE_MAP.put("0", "设备");
        AB_TYPE_MAP.put("1", "物料");
        AB_TYPE_MAP.put("2", "质量");
        AB_TYPE_MAP.put("3", "安全");
        AB_TYPE_MAP.put("4", "其他");
    }

    /** 工单来源类型映射 */
    private static final Map<String, String> WO_SOURCE_MAP = new LinkedHashMap<>();
    static {
        WO_SOURCE_MAP.put("1", "计划生成");
        WO_SOURCE_MAP.put("2", "订单直转");
        WO_SOURCE_MAP.put("3", "手工创建");
    }

    /** 工单类型映射 */
    private static final Map<String, String> WO_ORDER_TYPE_MAP = new LinkedHashMap<>();
    static {
        WO_ORDER_TYPE_MAP.put("0", "生产");
        WO_ORDER_TYPE_MAP.put("1", "返工");
        WO_ORDER_TYPE_MAP.put("2", "外协");
    }

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
        BigDecimal totalPlanQty = BigDecimal.ZERO;
        BigDecimal totalFinishedQty = BigDecimal.ZERO;
        BigDecimal totalQualifiedQty = BigDecimal.ZERO;
        BigDecimal totalDefectQty = BigDecimal.ZERO;
        for (MmsWorkOrder wo : allOrders)
        {
            String s = wo.getStatus() == null ? "0" : wo.getStatus();
            if ("1".equals(s) || "2".equals(s)) woInProduction++;
            if ("3".equals(s) || "4".equals(s)) woCompleted++;
            if ("5".equals(s) || "6".equals(s)) woAbnormal++;
            if (wo.getPlanQty() != null) totalPlanQty = totalPlanQty.add(wo.getPlanQty());
            if (wo.getFinishedQty() != null) totalFinishedQty = totalFinishedQty.add(wo.getFinishedQty());
            if (wo.getQualifiedQty() != null) totalQualifiedQty = totalQualifiedQty.add(wo.getQualifiedQty());
            if (wo.getDefectQty() != null) totalDefectQty = totalDefectQty.add(wo.getDefectQty());
        }
        woStats.put("inProduction", woInProduction);
        woStats.put("completed", woCompleted);
        woStats.put("abnormal", woAbnormal);
        woStats.put("totalPlanQty", totalPlanQty);
        woStats.put("totalFinishedQty", totalFinishedQty);
        woStats.put("totalQualifiedQty", totalQualifiedQty);
        woStats.put("totalDefectQty", totalDefectQty);
        // 完工率
        double finishRate = totalPlanQty.compareTo(BigDecimal.ZERO) > 0
            ? totalFinishedQty.divide(totalPlanQty, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0;
        woStats.put("finishRate", Math.round(finishRate * 100) / 100.0);
        // 合格率
        double yieldRate = totalFinishedQty.compareTo(BigDecimal.ZERO) > 0
            ? totalQualifiedQty.divide(totalFinishedQty, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100 : 0;
        woStats.put("yieldRate", Math.round(yieldRate * 100) / 100.0);
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
        BigDecimal totalGoodQty = BigDecimal.ZERO;
        BigDecimal totalDefectReportQty = BigDecimal.ZERO;
        for (MmsWorkReport rp : allReports)
        {
            String s = rp.getStatus() == null ? "0" : rp.getStatus();
            if ("0".equals(s)) rpPending++;
            if ("1".equals(s)) rpApproved++;
            if ("2".equals(s)) rpRejected++;
            if (rp.getGoodQty() != null) totalGoodQty = totalGoodQty.add(rp.getGoodQty());
            if (rp.getDefectQty() != null) totalDefectReportQty = totalDefectReportQty.add(rp.getDefectQty());
        }
        rpStats.put("pending", rpPending);
        rpStats.put("approved", rpApproved);
        rpStats.put("rejected", rpRejected);
        rpStats.put("totalGoodQty", totalGoodQty);
        rpStats.put("totalDefectQty", totalDefectReportQty);
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

    /**
     * 工单状态分布（饼图）
     * 按状态分组统计工单数量，返回状态名和数量
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/workorder/statusDist")
    public AjaxResult workorderStatusDist()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        Map<String, Integer> statusCount = new LinkedHashMap<>();
        for (MmsWorkOrder wo : allOrders)
        {
            String s = wo.getStatus() == null ? "0" : wo.getStatus();
            statusCount.merge(s, 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : WO_STATUS_MAP.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", entry.getKey());
            item.put("name", entry.getValue());
            item.put("count", statusCount.getOrDefault(entry.getKey(), 0));
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 工单月度趋势（折线+柱状图）
     * 按月统计工单创建数量和完工数量
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/workorder/trend")
    public AjaxResult workorderTrend()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM");
        // 按月汇总
        Map<String, int[]> monthMap = new TreeMap<>();
        for (MmsWorkOrder wo : allOrders)
        {
            if (wo.getCreateTime() == null) continue;
            String month = monthFmt.format(wo.getCreateTime());
            int[] vals = monthMap.computeIfAbsent(month, k -> new int[2]);
            vals[0]++; // 创建数
            if (wo.getActualFinish() != null) vals[1]++; // 完工数
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : monthMap.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("createdCount", entry.getValue()[0]);
            item.put("finishedCount", entry.getValue()[1]);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 产品产量TOP10（横向柱状图）
     * 按产品统计完工数量排名
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/product/top10")
    public AjaxResult productTop10()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        Map<String, BigDecimal> productQtyMap = new LinkedHashMap<>();
        Map<String, String> productNameMap = new HashMap<>();
        for (MmsWorkOrder wo : allOrders)
        {
            if (wo.getProductCode() == null) continue;
            BigDecimal qty = wo.getFinishedQty() != null ? wo.getFinishedQty() : BigDecimal.ZERO;
            productQtyMap.merge(wo.getProductCode(), qty, BigDecimal::add);
            if (wo.getProductName() != null) productNameMap.put(wo.getProductCode(), wo.getProductName());
        }
        // 排序取TOP10
        List<Map.Entry<String, BigDecimal>> sorted = new ArrayList<>(productQtyMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(10, sorted.size());
        for (int i = 0; i < limit; i++)
        {
            Map.Entry<String, BigDecimal> entry = sorted.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productCode", entry.getKey());
            item.put("productName", productNameMap.getOrDefault(entry.getKey(), entry.getKey()));
            item.put("finishedQty", entry.getValue());
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 异常类型分布（饼图）
     * 按异常类型统计数量
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/abnormal/typeDist")
    public AjaxResult abnormalTypeDist()
    {
        MmsAbnormal query = new MmsAbnormal();
        List<MmsAbnormal> allAbnormal = abnormalMapper.selectAbnormalList(query);
        Map<String, Integer> typeCount = new LinkedHashMap<>();
        for (MmsAbnormal ab : allAbnormal)
        {
            String t = ab.getAbnormalType() == null ? "4" : ab.getAbnormalType();
            typeCount.merge(t, 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : AB_TYPE_MAP.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", entry.getKey());
            item.put("name", entry.getValue());
            item.put("count", typeCount.getOrDefault(entry.getKey(), 0));
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 异常严重等级分布（柱状图）
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/abnormal/severityDist")
    public AjaxResult abnormalSeverityDist()
    {
        MmsAbnormal query = new MmsAbnormal();
        List<MmsAbnormal> allAbnormal = abnormalMapper.selectAbnormalList(query);
        int general = 0, serious = 0, urgent = 0;
        for (MmsAbnormal ab : allAbnormal)
        {
            String s = ab.getSeverity() == null ? "0" : ab.getSeverity();
            if ("0".equals(s)) general++;
            if ("1".equals(s)) serious++;
            if ("2".equals(s)) urgent++;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> item1 = new LinkedHashMap<>();
        item1.put("name", "一般"); item1.put("count", general);
        result.add(item1);
        Map<String, Object> item2 = new LinkedHashMap<>();
        item2.put("name", "严重"); item2.put("count", serious);
        result.add(item2);
        Map<String, Object> item3 = new LinkedHashMap<>();
        item3.put("name", "紧急"); item3.put("count", urgent);
        result.add(item3);
        return AjaxResult.success(result);
    }

    /**
     * 报工产量趋势（折线+柱状图）
     * 按日报工合格数和不良数
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/report/trend")
    public AjaxResult reportTrend()
    {
        MmsWorkReport query = new MmsWorkReport();
        List<MmsWorkReport> allReports = workReportMapper.selectWorkReportList(query);
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, BigDecimal[]> dayMap = new TreeMap<>();
        for (MmsWorkReport rp : allReports)
        {
            if (rp.getReportTime() == null) continue;
            String day = dayFmt.format(rp.getReportTime());
            BigDecimal[] vals = dayMap.computeIfAbsent(day, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (rp.getGoodQty() != null) vals[0] = vals[0].add(rp.getGoodQty());
            if (rp.getDefectQty() != null) vals[1] = vals[1].add(rp.getDefectQty());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(30, dayMap.size());
        int skip = dayMap.size() - limit;
        int idx = 0;
        for (Map.Entry<String, BigDecimal[]> entry : dayMap.entrySet())
        {
            if (idx++ < skip) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("goodQty", entry.getValue()[0]);
            item.put("defectQty", entry.getValue()[1]);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 工单来源分析（饼图）
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/workorder/sourceDist")
    public AjaxResult workorderSourceDist()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        Map<String, Integer> sourceCount = new LinkedHashMap<>();
        for (MmsWorkOrder wo : allOrders)
        {
            String s = wo.getSourceType() == null ? "3" : wo.getSourceType();
            sourceCount.merge(s, 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : WO_SOURCE_MAP.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", entry.getKey());
            item.put("name", entry.getValue());
            item.put("count", sourceCount.getOrDefault(entry.getKey(), 0));
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 产能单元负荷统计（横向柱状图）
     * 按产能单元统计工单数和计划/完工数量
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/resource/load")
    public AjaxResult resourceLoad()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        Map<String, Object[]> resourceMap = new LinkedHashMap<>();
        for (MmsWorkOrder wo : allOrders)
        {
            String name = wo.getResourceName();
            if (name == null || name.isEmpty()) continue;
            Object[] vals = resourceMap.computeIfAbsent(name, k -> new Object[]{0, BigDecimal.ZERO, BigDecimal.ZERO});
            vals[0] = (Integer) vals[0] + 1;
            if (wo.getPlanQty() != null) vals[1] = ((BigDecimal) vals[1]).add(wo.getPlanQty());
            if (wo.getFinishedQty() != null) vals[2] = ((BigDecimal) vals[2]).add(wo.getFinishedQty());
        }
        // 按计划数量降序
        List<Map.Entry<String, Object[]>> sorted = new ArrayList<>(resourceMap.entrySet());
        sorted.sort((a, b) -> ((BigDecimal) b.getValue()[1]).compareTo((BigDecimal) a.getValue()[1]));
        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(10, sorted.size());
        for (int i = 0; i < limit; i++)
        {
            Map.Entry<String, Object[]> entry = sorted.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("resourceName", entry.getKey());
            item.put("workOrderCount", entry.getValue()[0]);
            item.put("planQty", entry.getValue()[1]);
            item.put("finishedQty", entry.getValue()[2]);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 工单类型分布（饼图）
     */
    @PreAuthorize("@ss.hasPermi('mms:dashboard:view')")
    @GetMapping("/workorder/typeDist")
    public AjaxResult workorderTypeDist()
    {
        MmsWorkOrder query = new MmsWorkOrder();
        List<MmsWorkOrder> allOrders = workOrderMapper.selectWorkOrderList(query);
        Map<String, Integer> typeCount = new LinkedHashMap<>();
        for (MmsWorkOrder wo : allOrders)
        {
            String t = wo.getOrderType() == null ? "0" : wo.getOrderType();
            typeCount.merge(t, 1, Integer::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : WO_ORDER_TYPE_MAP.entrySet())
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", entry.getKey());
            item.put("name", entry.getValue());
            item.put("count", typeCount.getOrDefault(entry.getKey(), 0));
            result.add(item);
        }
        return AjaxResult.success(result);
    }
}
