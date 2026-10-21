package com.ruoyi.qms.controller;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.qms.domain.*;
import com.ruoyi.qms.service.*;

/**
 * 质量看板 Controller（带Redis缓存）
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

    @Autowired
    private IQmsCapaService qmsCapaService;

    @Autowired
    private IQmsComplaintService qmsComplaintService;

    @Autowired
    private IQmsSupplierEvalService qmsSupplierEvalService;

    @Autowired
    private RedisCache redisCache;

    private static final String CACHE_KEY_SUMMARY = "qms:dashboard:summary";
    private static final String CACHE_KEY_TYPE_DIST = "qms:dashboard:inspTypeDist";
    private static final int CACHE_TTL_MINUTES = 5;

    /**
     * 看板汇总数据（Redis缓存5分钟）
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        // 尝试从Redis缓存读取
        Map<String, Object> cached = redisCache.getCacheObject(CACHE_KEY_SUMMARY);
        if (cached != null)
        {
            return AjaxResult.success(cached);
        }

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

        // CAPA统计
        QmsCapa capaQuery = new QmsCapa();
        List<QmsCapa> capaList = qmsCapaService.selectCapaList(capaQuery);
        int capaDraft = 0, capaInProgress = 0, capaVerifying = 0, capaClosed = 0, capaRejected = 0;
        for (QmsCapa capa : capaList)
        {
            if ("0".equals(capa.getCapaStatus())) capaDraft++;
            if ("1".equals(capa.getCapaStatus())) capaInProgress++;
            if ("2".equals(capa.getCapaStatus())) capaVerifying++;
            if ("3".equals(capa.getCapaStatus())) capaClosed++;
            if ("4".equals(capa.getCapaStatus())) capaRejected++;
        }
        result.put("capaTotal", capaList.size());
        result.put("capaDraft", capaDraft);
        result.put("capaInProgress", capaInProgress);
        result.put("capaVerifying", capaVerifying);
        result.put("capaClosed", capaClosed);
        result.put("capaRejected", capaRejected);
        double capaCloseRate = capaList.size() > 0 ? (double) capaClosed / capaList.size() * 100 : 0;
        result.put("capaCloseRate", Math.round(capaCloseRate * 100) / 100.0);

        // 客诉统计
        QmsComplaint complaintQuery = new QmsComplaint();
        List<QmsComplaint> complaintList = qmsComplaintService.selectComplaintList(complaintQuery);
        int complaintOpen = 0, complaintProcessing = 0, complaintClosed = 0;
        for (QmsComplaint c : complaintList)
        {
            if ("0".equals(c.getComplaintStatus())) complaintOpen++;
            if ("1".equals(c.getComplaintStatus())) complaintProcessing++;
            if ("3".equals(c.getComplaintStatus())) complaintClosed++;
        }
        result.put("complaintTotal", complaintList.size());
        result.put("complaintOpen", complaintOpen);
        result.put("complaintProcessing", complaintProcessing);
        result.put("complaintClosed", complaintClosed);

        // 供应商评价统计
        QmsSupplierEval evalQuery = new QmsSupplierEval();
        List<QmsSupplierEval> evalList = qmsSupplierEvalService.selectEvalList(evalQuery);
        int gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0;
        for (QmsSupplierEval e : evalList)
        {
            if ("A".equals(e.getGrade())) gradeA++;
            if ("B".equals(e.getGrade())) gradeB++;
            if ("C".equals(e.getGrade())) gradeC++;
            if ("D".equals(e.getGrade())) gradeD++;
        }
        result.put("evalTotal", evalList.size());
        result.put("gradeA", gradeA);
        result.put("gradeB", gradeB);
        result.put("gradeC", gradeC);
        result.put("gradeD", gradeD);

        result.put("cacheTime", new Date());

        // 写入Redis缓存，5分钟过期
        redisCache.setCacheObject(CACHE_KEY_SUMMARY, result, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return AjaxResult.success(result);
    }

    /**
     * 清除看板缓存
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @DeleteMapping("/cache")
    public AjaxResult clearCache()
    {
        redisCache.deleteObject(CACHE_KEY_SUMMARY);
        redisCache.deleteObject(CACHE_KEY_TYPE_DIST);
        return AjaxResult.success("缓存已清除");
    }

    /**
     * 检验类型分布
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/inspTypeDist")
    public AjaxResult inspTypeDist()
    {
        List<Map<String, Object>> cached = redisCache.getCacheObject(CACHE_KEY_TYPE_DIST);
        if (cached != null)
        {
            return AjaxResult.success(cached);
        }

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
        redisCache.setCacheObject(CACHE_KEY_TYPE_DIST, result, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
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

    /**
     * 最近CAPA记录
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/recentCapas")
    public AjaxResult recentCapas()
    {
        QmsCapa query = new QmsCapa();
        List<QmsCapa> all = qmsCapaService.selectCapaList(query);
        int limit = Math.min(10, all.size());
        return AjaxResult.success(all.subList(0, limit));
    }

    /**
     * 最近客诉记录
     */
    @PreAuthorize("@ss.hasPermi('qms:dashboard:view')")
    @GetMapping("/recentComplaints")
    public AjaxResult recentComplaints()
    {
        QmsComplaint query = new QmsComplaint();
        List<QmsComplaint> all = qmsComplaintService.selectComplaintList(query);
        int limit = Math.min(10, all.size());
        return AjaxResult.success(all.subList(0, limit));
    }
}
