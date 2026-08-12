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

    @PreAuthorize("@ss.hasPermi('qms:report:list')")
    @GetMapping("/summary")
    public AjaxResult summary(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        // 检验任务汇总
        QmsInspTask taskQuery = new QmsInspTask();
        List<QmsInspTask> taskList = inspTaskService.selectInspTaskList(taskQuery);
        result.put("totalInsp", taskList.size());
        result.put("passInsp", taskList.stream().filter(t -> "1".equals(t.getInspectResult())).count());
        result.put("failInsp", taskList.stream().filter(t -> "2".equals(t.getInspectResult())).count());
        // NCR汇总
        List<QmsNcr> ncrList = ncrService.selectNcrList(new QmsNcr());
        result.put("totalNcr", ncrList.size());
        result.put("openNcr", ncrList.stream().filter(n -> !"4".equals(n.getNcrStatus()) && !"5".equals(n.getNcrStatus())).count());
        // CAPA汇总
        List<QmsCapa> capaList = capaService.selectCapaList(new QmsCapa());
        result.put("totalCapa", capaList.size());
        result.put("openCapa", capaList.stream().filter(c -> !"3".equals(c.getCapaStatus())).count());
        result.put("closedCapa", capaList.stream().filter(c -> "3".equals(c.getCapaStatus())).count());
        // 客诉汇总
        List<QmsComplaint> complaintList = complaintService.selectComplaintList(new QmsComplaint());
        result.put("totalComplaint", complaintList.size());
        result.put("openComplaint", complaintList.stream().filter(c -> !"3".equals(c.getComplaintStatus())).count());
        // 供应商评价汇总
        List<QmsSupplierEval> evalList = supplierEvalService.selectEvalList(new QmsSupplierEval());
        result.put("totalEval", evalList.size());
        result.put("gradeA", evalList.stream().filter(e -> "A".equals(e.getGrade())).count());
        result.put("gradeB", evalList.stream().filter(e -> "B".equals(e.getGrade())).count());
        result.put("gradeC", evalList.stream().filter(e -> "C".equals(e.getGrade())).count());
        result.put("gradeD", evalList.stream().filter(e -> "D".equals(e.getGrade())).count());
        return AjaxResult.success(result);
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
