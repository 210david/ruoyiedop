package com.ruoyi.qms.controller;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.qms.domain.*;
import com.ruoyi.qms.service.*;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/report")
public class QmsReportController extends BaseController {
    @Autowired
    private IQmsInspTaskService inspTaskService;
    @Autowired
    private IQmsNcrService ncrService;
    @Autowired
    private IQmsCapaService capaService;
    @Autowired
    private IQmsSupplierEvalService supplierEvalService;
    @Autowired
    private IQmsComplaintService complaintService;
    @Autowired
    private IQmsTraceService qmsTraceService;
    @Autowired
    private IQmsMrService mrService;
    @Autowired
    private IQmsQualityTargetService qualityTargetService;
    @Autowired
    private IQmsAuditPlanService auditPlanService;

    /**
     * 构建日期参数 Map（用于 BaseEntity.params 的 beginTime / endTime）
     */
    private Map<String, Object> buildDateParams(String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        if (startDate != null && !startDate.isEmpty()) params.put("beginTime", startDate);
        if (endDate != null && !endDate.isEmpty()) params.put("endTime", endDate);
        return params;
    }

    /**
     * 构建月度日期参数：month 格式为 yyyy-MM，自动推算该月起止日期
     */
    private Map<String, Object> buildMonthParams(String month) {
        Map<String, Object> params = new HashMap<>();
        if (month != null && !month.isEmpty() && month.length() >= 7) {
            params.put("beginTime", month + "-01");
            // 计算月末
            try {
                int year = Integer.parseInt(month.substring(0, 4));
                int m = Integer.parseInt(month.substring(5, 7));
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(year, m - 1, 1);
                int lastDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
                params.put("endTime", month + "-" + String.format("%02d", lastDay));
            } catch (Exception e) {
                // 解析失败则不限制
            }
        }
        return params;
    }

    /**
     * 构建日报日期参数：date 格式为 yyyy-MM-dd
     */
    private Map<String, Object> buildDayParams(String date) {
        Map<String, Object> params = new HashMap<>();
        if (date != null && !date.isEmpty()) {
            params.put("beginTime", date);
            params.put("endTime", date);
        }
        return params;
    }

    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @GetMapping("/summary")
    public AjaxResult summary(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> dateParams = buildDateParams(startDate, endDate);

        // 检验任务汇总（按日期筛选）
        QmsInspTask taskQuery = new QmsInspTask();
        if (!dateParams.isEmpty()) taskQuery.setParams(dateParams);
        List<QmsInspTask> taskList = inspTaskService.selectInspTaskList(taskQuery);
        result.put("totalInsp", taskList.size());
        result.put("passInsp", taskList.stream().filter(t -> "1".equals(t.getInspectResult())).count());
        result.put("failInsp", taskList.stream().filter(t -> "2".equals(t.getInspectResult())).count());
        // 合格率
        long completedTask = taskList.stream().filter(t -> "2".equals(t.getTaskStatus())).count();
        long passTask = taskList.stream().filter(t -> "1".equals(t.getInspectResult())).count();
        double passRate = completedTask > 0 ? (double) passTask / completedTask * 100 : 0;
        result.put("passRate", Math.round(passRate * 100) / 100.0);

        // NCR汇总
        QmsNcr ncrQuery = new QmsNcr();
        if (!dateParams.isEmpty()) ncrQuery.setParams(dateParams);
        List<QmsNcr> ncrList = ncrService.selectNcrList(ncrQuery);
        result.put("totalNcr", ncrList.size());
        result.put("openNcr", ncrList.stream().filter(n -> !"4".equals(n.getNcrStatus()) && !"5".equals(n.getNcrStatus())).count());
        result.put("closedNcr", ncrList.stream().filter(n -> "4".equals(n.getNcrStatus())).count());

        // CAPA汇总
        QmsCapa capaQuery = new QmsCapa();
        if (!dateParams.isEmpty()) capaQuery.setParams(dateParams);
        List<QmsCapa> capaList = capaService.selectCapaList(capaQuery);
        result.put("totalCapa", capaList.size());
        result.put("openCapa", capaList.stream().filter(c -> !"3".equals(c.getCapaStatus())).count());
        result.put("closedCapa", capaList.stream().filter(c -> "3".equals(c.getCapaStatus())).count());
        double capaCloseRate = capaList.size() > 0 ? (double) capaList.stream().filter(c -> "3".equals(c.getCapaStatus())).count() / capaList.size() * 100 : 0;
        result.put("capaCloseRate", Math.round(capaCloseRate * 100) / 100.0);

        // 客诉汇总
        QmsComplaint complaintQuery = new QmsComplaint();
        if (!dateParams.isEmpty()) complaintQuery.setParams(dateParams);
        List<QmsComplaint> complaintList = complaintService.selectComplaintList(complaintQuery);
        result.put("totalComplaint", complaintList.size());
        result.put("openComplaint", complaintList.stream().filter(c -> !"3".equals(c.getComplaintStatus())).count());
        result.put("closedComplaint", complaintList.stream().filter(c -> "3".equals(c.getComplaintStatus())).count());

        // 供应商评价汇总（修复：增加日期过滤）
        QmsSupplierEval evalQuery = new QmsSupplierEval();
        if (!dateParams.isEmpty()) evalQuery.setParams(dateParams);
        List<QmsSupplierEval> evalList = supplierEvalService.selectEvalList(evalQuery);
        result.put("totalEval", evalList.size());
        result.put("gradeA", evalList.stream().filter(e -> "A".equals(e.getGrade())).count());
        result.put("gradeB", evalList.stream().filter(e -> "B".equals(e.getGrade())).count());
        result.put("gradeC", evalList.stream().filter(e -> "C".equals(e.getGrade())).count());
        result.put("gradeD", evalList.stream().filter(e -> "D".equals(e.getGrade())).count());

        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return AjaxResult.success(result);
    }

    @Log(title = "追溯报告导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:trace:forward')")
    @PostMapping("/exportPdf/{batchNo}")
    public void exportTracePdf(HttpServletResponse response, @PathVariable String batchNo) {
        try {
            QmsLotGenealogy traceData = qmsTraceService.backwardTrace(batchNo);
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'><style>");
            html.append("body{font-family:SimSun,sans-serif;padding:20px;}");
            html.append("h1{text-align:center;color:#333;}");
            html.append("h2{color:#1a5276;border-bottom:2px solid #2980b9;padding-bottom:4px;}");
            html.append("table{width:100%;border-collapse:collapse;margin:10px 0;}");
            html.append("th,td{border:1px solid #999;padding:6px 10px;font-size:12px;}");
            html.append("th{background:#f0f0f0;}");
            html.append(".trace-node{margin-left:20px;border-left:2px solid #ccc;padding-left:10px;}");
            html.append("</style></head><body>");
            html.append("<h1>质量追溯报告</h1>");
            html.append("<p>批次号：").append(batchNo).append("&nbsp;&nbsp;报告时间：")
               .append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("</p>");
            if (traceData != null) {
                // 基本信息
                html.append("<h2>一、批次基本信息</h2>");
                html.append("<table><tr><th>产出批次号</th><td>").append(safeStr(traceData.getChildBatchNo())).append("</td>");
                html.append("<th>投入批次号</th><td>").append(safeStr(traceData.getParentBatchNo())).append("</td></tr>");
                html.append("<tr><th>物料编码</th><td>").append(safeStr(traceData.getMaterialCode())).append("</td>");
                html.append("<th>物料名称</th><td>").append(safeStr(traceData.getMaterialName())).append("</td></tr>");
                html.append("<tr><th>工单号</th><td>").append(safeStr(traceData.getWorkOrderNo())).append("</td>");
                html.append("<th>工序</th><td>").append(safeStr(traceData.getOpName())).append("</td></tr>");
                html.append("<tr><th>产线</th><td>").append(safeStr(traceData.getLineName())).append("</td>");
                html.append("<th>操作员</th><td>").append(safeStr(traceData.getOperator())).append("</td></tr>");
                html.append("<tr><th>投入数量</th><td>").append(traceData.getQtyIn() != null ? traceData.getQtyIn().toPlainString() : "-").append("</td>");
                html.append("<th>产出数量</th><td>").append(traceData.getQtyOut() != null ? traceData.getQtyOut().toPlainString() : "-").append("</td></tr>");
                html.append("<tr><th>供应商</th><td>").append(safeStr(traceData.getSupplierName())).append("</td>");
                html.append("<th>谱系时间</th><td>").append(traceData.getTraceTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(traceData.getTraceTime()) : "-").append("</td></tr>");
                html.append("</table>");

                // 追溯链
                html.append("<h2>二、追溯链路</h2>");
                html.append(buildTraceTreeHtml(traceData));

                // 关联检验记录
                if (traceData.getInspectTasks() != null && !traceData.getInspectTasks().isEmpty()) {
                    html.append("<h2>三、关联检验记录</h2>");
                    html.append("<table><tr><th>检验单号</th><th>检验类型</th><th>检验结果</th><th>检验人</th><th>检验日期</th></tr>");
                    for (QmsInspTask task : traceData.getInspectTasks()) {
                        html.append("<tr><td>").append(safeStr(task.getTaskNo())).append("</td>");
                        html.append("<td>").append(safeStr(task.getTaskType())).append("</td>");
                        html.append("<td>").append(safeStr(task.getInspectResult())).append("</td>");
                        html.append("<td>").append(safeStr(task.getInspectorName())).append("</td>");
                        html.append("<td>").append(task.getInspectTime() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(task.getInspectTime()) : "-").append("</td></tr>");
                    }
                    html.append("</table>");
                }

                // 关联NCR记录
                if (traceData.getNcrList() != null && !traceData.getNcrList().isEmpty()) {
                    html.append("<h2>四、关联不合格品记录</h2>");
                    html.append("<table><tr><th>NCR编号</th><th>不合格描述</th><th>处置方式</th><th>状态</th></tr>");
                    for (QmsNcr ncr : traceData.getNcrList()) {
                        html.append("<tr><td>").append(safeStr(ncr.getNcrNo())).append("</td>");
                        html.append("<td>").append(safeStr(ncr.getDefectDesc())).append("</td>");
                        html.append("<td>").append(safeStr(ncr.getDisposition())).append("</td>");
                        html.append("<td>").append(safeStr(ncr.getNcrStatus())).append("</td></tr>");
                    }
                    html.append("</table>");
                }
            } else {
                html.append("<p>未找到批次[").append(batchNo).append("]的追溯数据</p>");
            }
            html.append("</body></html>");
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=trace_" + batchNo + ".html");
            java.io.OutputStream os = response.getOutputStream();
            os.write(html.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
        } catch (Exception e) {
            logger.error("导出追溯报告失败", e);
        }
    }

    /** 递归构建追溯树HTML */
    private String buildTraceTreeHtml(QmsLotGenealogy node) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='trace-node'>");
        sb.append("<b>").append(safeStr(node.getChildBatchNo())).append("</b>");
        sb.append(" &larr; <span>").append(safeStr(node.getParentBatchNo())).append("</span>");
        sb.append(" | 物料:").append(safeStr(node.getMaterialName()));
        sb.append(" | 工序:").append(safeStr(node.getOpName()));
        sb.append(" | 数量:").append(node.getQtyOut() != null ? node.getQtyOut().toPlainString() : "-");
        if (node.getBreakFlag() != null && node.getBreakFlag() == 1) {
            sb.append(" <span style='color:red;'>&#9888;断点(").append(safeStr(node.getBreakReason())).append(")</span>");
        }
        if (node.getChildren() != null) {
            for (QmsLotGenealogy child : node.getChildren()) {
                sb.append(buildTraceTreeHtml(child));
            }
        }
        sb.append("</div>");
        return sb.toString();
    }

    /** 安全字符串转换 */
    private String safeStr(Object obj) {
        return obj != null ? obj.toString() : "-";
    }

    // ==================== 检验日报/月报 ====================

    @Log(title = "检验日报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportInspDaily")
    public void exportInspDaily(HttpServletResponse response, String date) {
        QmsInspTask query = new QmsInspTask();
        Map<String, Object> params = buildDayParams(date);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsInspTask> list = inspTaskService.selectInspTaskList(query);
        ExcelUtil<QmsInspTask> util = new ExcelUtil<>(QmsInspTask.class);
        util.exportExcel(response, list, "检验日报_" + (date != null ? date : "全部"));
    }

    @Log(title = "检验月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportInspMonthly")
    public void exportInspMonthly(HttpServletResponse response, String month) {
        QmsInspTask query = new QmsInspTask();
        Map<String, Object> params = buildMonthParams(month);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsInspTask> list = inspTaskService.selectInspTaskList(query);
        ExcelUtil<QmsInspTask> util = new ExcelUtil<>(QmsInspTask.class);
        util.exportExcel(response, list, "检验月报_" + (month != null ? month : "全部"));
    }

    // ==================== NCR月报 ====================

    @Log(title = "NCR月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportNcrMonthly")
    public void exportNcrMonthly(HttpServletResponse response, String month) {
        QmsNcr query = new QmsNcr();
        Map<String, Object> params = buildMonthParams(month);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsNcr> list = ncrService.selectNcrList(query);
        ExcelUtil<QmsNcr> util = new ExcelUtil<>(QmsNcr.class);
        util.exportExcel(response, list, "NCR月报_" + (month != null ? month : "全部"));
    }

    // ==================== 供应商月报 ====================

    @Log(title = "供应商质量月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportSupplierMonthly")
    public void exportSupplierMonthly(HttpServletResponse response, String month) {
        QmsSupplierEval query = new QmsSupplierEval();
        Map<String, Object> params = buildMonthParams(month);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsSupplierEval> list = supplierEvalService.selectEvalList(query);
        ExcelUtil<QmsSupplierEval> util = new ExcelUtil<>(QmsSupplierEval.class);
        util.exportExcel(response, list, "供应商质量月报_" + (month != null ? month : "全部"));
    }

    // ==================== 客诉月报 ====================

    @Log(title = "客诉月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportComplaintMonthly")
    public void exportComplaintMonthly(HttpServletResponse response, String month) {
        QmsComplaint query = new QmsComplaint();
        Map<String, Object> params = buildMonthParams(month);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsComplaint> list = complaintService.selectComplaintList(query);
        ExcelUtil<QmsComplaint> util = new ExcelUtil<>(QmsComplaint.class);
        util.exportExcel(response, list, "客诉月报_" + (month != null ? month : "全部"));
    }

    // ==================== CAPA月报 ====================

    @Log(title = "CAPA月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportCapaMonthly")
    public void exportCapaMonthly(HttpServletResponse response, String month) {
        QmsCapa query = new QmsCapa();
        Map<String, Object> params = buildMonthParams(month);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsCapa> list = capaService.selectCapaList(query);
        ExcelUtil<QmsCapa> util = new ExcelUtil<>(QmsCapa.class);
        util.exportExcel(response, list, "CAPA月报_" + (month != null ? month : "全部"));
    }

    // ==================== 管理评审(MR)报表 ====================

    @Log(title = "管理评审月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportMrMonthly")
    public void exportMrMonthly(HttpServletResponse response, String month) {
        QmsMr query = new QmsMr();
        Map<String, Object> params = buildMonthParams(month);
        if (!params.isEmpty()) query.setParams(params);
        List<QmsMr> list = mrService.selectMrList(query);
        ExcelUtil<QmsMr> util = new ExcelUtil<>(QmsMr.class);
        util.exportExcel(response, list, "管理评审月报_" + (month != null ? month : "全部"));
    }

    // ==================== 质量目标达成报表 ====================

    @Log(title = "质量目标达成报表", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportQualityTarget")
    public void exportQualityTarget(HttpServletResponse response, Integer year) {
        QmsQualityTarget query = new QmsQualityTarget();
        if (year != null) query.setTargetYear(year);
        List<QmsQualityTarget> list = qualityTargetService.selectTargetList(query);
        ExcelUtil<QmsQualityTarget> util = new ExcelUtil<>(QmsQualityTarget.class);
        util.exportExcel(response, list, "质量目标达成报表_" + (year != null ? year : "全部"));
    }

    /**
     * 质量目标达成汇总（供前端展示）
     */
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @GetMapping("/targetSummary")
    public AjaxResult targetSummary(Integer year) {
        QmsQualityTarget query = new QmsQualityTarget();
        if (year != null) query.setTargetYear(year);
        List<QmsQualityTarget> list = qualityTargetService.selectTargetList(query);
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("achieved", list.stream().filter(t -> "1".equals(t.getAchievement())).count());
        result.put("notAchieved", list.stream().filter(t -> "0".equals(t.getAchievement())).count());
        result.put("targets", list);
        return AjaxResult.success(result);
    }

    // ==================== 审核计划报表 ====================

    @Log(title = "审核计划报表", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportAuditPlan")
    public void exportAuditPlan(HttpServletResponse response, Integer year) {
        QmsAuditPlan query = new QmsAuditPlan();
        if (year != null) query.setAuditYear(year);
        List<QmsAuditPlan> list = auditPlanService.selectAuditPlanList(query);
        ExcelUtil<QmsAuditPlan> util = new ExcelUtil<>(QmsAuditPlan.class);
        util.exportExcel(response, list, "审核计划报表_" + (year != null ? year : "全部"));
    }

    /**
     * 审核计划汇总（供前端展示）
     */
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @GetMapping("/auditSummary")
    public AjaxResult auditSummary(Integer year) {
        QmsAuditPlan query = new QmsAuditPlan();
        if (year != null) query.setAuditYear(year);
        List<QmsAuditPlan> list = auditPlanService.selectAuditPlanList(query);
        Map<String, Object> result = new HashMap<>();
        result.put("total", list.size());
        result.put("planned", list.stream().filter(a -> "0".equals(a.getPlanStatus())).count());
        result.put("inProgress", list.stream().filter(a -> "1".equals(a.getPlanStatus())).count());
        result.put("completed", list.stream().filter(a -> "2".equals(a.getPlanStatus())).count());
        result.put("cancelled", list.stream().filter(a -> "3".equals(a.getPlanStatus())).count());
        result.put("auditPlans", list);
        return AjaxResult.success(result);
    }
}
