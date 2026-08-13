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

    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @GetMapping("/summary")
    public AjaxResult summary(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        // 设置日期筛选参数
        java.util.Map<String, Object> dateParams = new HashMap<>();
        if (startDate != null && !startDate.isEmpty()) dateParams.put("beginTime", startDate);
        if (endDate != null && !endDate.isEmpty()) dateParams.put("endTime", endDate);
        // 检验任务汇总（按日期筛选）
        QmsInspTask taskQuery = new QmsInspTask();
        if (!dateParams.isEmpty()) taskQuery.setParams(dateParams);
        List<QmsInspTask> taskList = inspTaskService.selectInspTaskList(taskQuery);
        result.put("totalInsp", taskList.size());
        result.put("passInsp", taskList.stream().filter(t -> "1".equals(t.getInspectResult())).count());
        result.put("failInsp", taskList.stream().filter(t -> "2".equals(t.getInspectResult())).count());
        // NCR汇总
        QmsNcr ncrQuery = new QmsNcr();
        if (!dateParams.isEmpty()) ncrQuery.setParams(dateParams);
        List<QmsNcr> ncrList = ncrService.selectNcrList(ncrQuery);
        result.put("totalNcr", ncrList.size());
        result.put("openNcr", ncrList.stream().filter(n -> !"4".equals(n.getNcrStatus()) && !"5".equals(n.getNcrStatus())).count());
        // CAPA汇总
        QmsCapa capaQuery = new QmsCapa();
        if (!dateParams.isEmpty()) capaQuery.setParams(dateParams);
        List<QmsCapa> capaList = capaService.selectCapaList(capaQuery);
        result.put("totalCapa", capaList.size());
        result.put("openCapa", capaList.stream().filter(c -> !"3".equals(c.getCapaStatus())).count());
        result.put("closedCapa", capaList.stream().filter(c -> "3".equals(c.getCapaStatus())).count());
        // 客诉汇总
        QmsComplaint complaintQuery = new QmsComplaint();
        if (!dateParams.isEmpty()) complaintQuery.setParams(dateParams);
        List<QmsComplaint> complaintList = complaintService.selectComplaintList(complaintQuery);
        result.put("totalComplaint", complaintList.size());
        result.put("openComplaint", complaintList.stream().filter(c -> !"3".equals(c.getComplaintStatus())).count());
        // 供应商评价汇总
        List<QmsSupplierEval> evalList = supplierEvalService.selectEvalList(new QmsSupplierEval());
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

    @Log(title = "检验日报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportInspDaily")
    public void exportInspDaily(HttpServletResponse response, String date) {
        QmsInspTask query = new QmsInspTask();
        List<QmsInspTask> list = inspTaskService.selectInspTaskList(query);
        ExcelUtil<QmsInspTask> util = new ExcelUtil<>(QmsInspTask.class);
        util.exportExcel(response, list, "检验日报");
    }

    @Log(title = "检验月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportInspMonthly")
    public void exportInspMonthly(HttpServletResponse response, String month) {
        QmsInspTask query = new QmsInspTask();
        List<QmsInspTask> list = inspTaskService.selectInspTaskList(query);
        ExcelUtil<QmsInspTask> util = new ExcelUtil<>(QmsInspTask.class);
        util.exportExcel(response, list, "检验月报");
    }

    @Log(title = "NCR月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportNcrMonthly")
    public void exportNcrMonthly(HttpServletResponse response, String month) {
        List<QmsNcr> list = ncrService.selectNcrList(new QmsNcr());
        ExcelUtil<QmsNcr> util = new ExcelUtil<>(QmsNcr.class);
        util.exportExcel(response, list, "NCR月报");
    }

    @Log(title = "供应商月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportSupplierMonthly")
    public void exportSupplierMonthly(HttpServletResponse response, String month) {
        List<QmsSupplierEval> list = supplierEvalService.selectEvalList(new QmsSupplierEval());
        ExcelUtil<QmsSupplierEval> util = new ExcelUtil<>(QmsSupplierEval.class);
        util.exportExcel(response, list, "供应商质量月报");
    }

    @Log(title = "客诉月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportComplaintMonthly")
    public void exportComplaintMonthly(HttpServletResponse response, String month) {
        List<QmsComplaint> list = complaintService.selectComplaintList(new QmsComplaint());
        ExcelUtil<QmsComplaint> util = new ExcelUtil<>(QmsComplaint.class);
        util.exportExcel(response, list, "客诉月报");
    }

    @Log(title = "CAPA月报", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @PostMapping("/exportCapaMonthly")
    public void exportCapaMonthly(HttpServletResponse response, String month) {
        List<QmsCapa> list = capaService.selectCapaList(new QmsCapa());
        ExcelUtil<QmsCapa> util = new ExcelUtil<>(QmsCapa.class);
        util.exportExcel(response, list, "CAPA月报");
    }
}
