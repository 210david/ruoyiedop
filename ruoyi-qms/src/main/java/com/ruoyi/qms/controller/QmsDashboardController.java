package com.ruoyi.qms.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.qms.domain.QmsInspTask;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.service.IQmsInspTaskService;
import com.ruoyi.qms.service.IQmsNcrService;

/**
 * 质量看板 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/dashboard")
public class QmsDashboardController extends BaseController
{
    @Autowired
    private IQmsInspTaskService qmsInspTaskService;

    @Autowired
    private IQmsNcrService qmsNcrService;

    /**
     * 看板汇总数据
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        Map<String, Object> result = new HashMap<>();

        // 检验任务统计
        QmsInspTask inspQuery = new QmsInspTask();
        List<QmsInspTask> inspList = qmsInspTaskService.selectInspTaskList(inspQuery);
        int inspPending = 0, inspInProgress = 0, inspCompleted = 0, inspPass = 0, inspFail = 0;
        for (QmsInspTask task : inspList)
        {
            if ("0".equals(task.getTaskStatus())) inspPending++;
            if ("1".equals(task.getTaskStatus())) inspInProgress++;
            if ("2".equals(task.getTaskStatus())) inspCompleted++;
            if ("1".equals(task.getInspectResult())) inspPass++;
            if ("2".equals(task.getInspectResult())) inspFail++;
        }
        result.put("inspTotal", inspList.size());
        result.put("inspPending", inspPending);
        result.put("inspInProgress", inspInProgress);
        result.put("inspCompleted", inspCompleted);
        result.put("inspPass", inspPass);
        result.put("inspFail", inspFail);
        double passRate = inspCompleted > 0 ? (double) inspPass / inspCompleted * 100 : 0;
        result.put("passRate", Math.round(passRate * 100) / 100.0);

        // NCR统计
        QmsNcr ncrQuery = new QmsNcr();
        List<QmsNcr> ncrList = qmsNcrService.selectNcrList(ncrQuery);
        int ncrOpen = 0, ncrReviewing = 0, ncrDisposing = 0, ncrVerifying = 0, ncrClosed = 0, ncrRejected = 0;
        for (QmsNcr ncr : ncrList)
        {
            if ("0".equals(ncr.getNcrStatus())) ncrOpen++;
            if ("1".equals(ncr.getNcrStatus())) ncrReviewing++;
            if ("2".equals(ncr.getNcrStatus())) ncrDisposing++;
            if ("3".equals(ncr.getNcrStatus())) ncrVerifying++;
            if ("4".equals(ncr.getNcrStatus())) ncrClosed++;
            if ("5".equals(ncr.getNcrStatus())) ncrRejected++;
        }
        result.put("ncrTotal", ncrList.size());
        result.put("ncrOpen", ncrOpen);
        result.put("ncrReviewing", ncrReviewing);
        result.put("ncrDisposing", ncrDisposing);
        result.put("ncrVerifying", ncrVerifying);
        result.put("ncrClosed", ncrClosed);
        result.put("ncrRejected", ncrRejected);

        return AjaxResult.success(result);
    }

    /**
     * 检验类型分布
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/inspTypeDist")
    public AjaxResult inspTypeDist()
    {
        QmsInspTask query = new QmsInspTask();
        List<QmsInspTask> list = qmsInspTaskService.selectInspTaskList(query);
        String[] types = {"IQC", "IPQC", "FQC", "OQC"};
        String[] names = {"来料检验", "过程检验", "成品检验", "出货检验"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < types.length; i++)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", types[i]);
            item.put("name", names[i]);
            int count = 0, passCount = 0, failCount = 0;
            for (QmsInspTask task : list)
            {
                if (types[i].equals(task.getTaskType()))
                {
                    count++;
                    if ("1".equals(task.getInspectResult())) passCount++;
                    if ("2".equals(task.getInspectResult())) failCount++;
                }
            }
            item.put("count", count);
            item.put("passCount", passCount);
            item.put("failCount", failCount);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 缺陷等级分布
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/defectLevelDist")
    public AjaxResult defectLevelDist()
    {
        QmsInspTask query = new QmsInspTask();
        List<QmsInspTask> list = qmsInspTaskService.selectInspTaskList(query);
        String[] levels = {"1", "2", "3", "4"};
        String[] names = {"致命", "严重", "一般", "轻微"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < levels.length; i++)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", levels[i]);
            item.put("name", names[i]);
            int count = 0;
            for (QmsInspTask task : list)
            {
                if (levels[i].equals(task.getDefectLevel()))
                {
                    count++;
                }
            }
            item.put("count", count);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 最近检验任务
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/recentInspTasks")
    public AjaxResult recentInspTasks()
    {
        QmsInspTask query = new QmsInspTask();
        List<QmsInspTask> all = qmsInspTaskService.selectInspTaskList(query);
        int limit = Math.min(10, all.size());
        return AjaxResult.success(all.subList(0, limit));
    }

    /**
     * 最近NCR记录
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/recentNcrs")
    public AjaxResult recentNcrs()
    {
        QmsNcr query = new QmsNcr();
        List<QmsNcr> all = qmsNcrService.selectNcrList(query);
        int limit = Math.min(10, all.size());
        return AjaxResult.success(all.subList(0, limit));
    }
}
