package com.ruoyi.kingdee.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 金蝶同步结果（含统计 + 样本 + 耗时）
 *
 * @author ruoyi
 */
public class SyncResult
{
    /** 统计：total/inserted/updated/skipped */
    private Map<String, Integer> statistics = new HashMap<>();

    /** 耗时（毫秒） */
    private long elapsedMs;

    /** 处理详情样本（最多 20 条，前端用于展示结果列表） */
    private List<Map<String, String>> samples = new ArrayList<>();

    /** 跳过原因列表（最多 10 条） */
    private List<String> skipReasons = new ArrayList<>();

    public SyncResult()
    {
        statistics.put("total", 0);
        statistics.put("inserted", 0);
        statistics.put("updated", 0);
        statistics.put("skipped", 0);
    }

    public void incInserted() { statistics.merge("inserted", 1, Integer::sum); }
    public void incUpdated()  { statistics.merge("updated", 1, Integer::sum); }
    public void incSkipped()  { statistics.merge("skipped", 1, Integer::sum); }
    public void incTotal()    { statistics.merge("total", 1, Integer::sum); }

    public void addSample(String no, String name, String action, String remark)
    {
        if (samples.size() >= 20) return;
        Map<String, String> m = new HashMap<>();
        m.put("no", no == null ? "" : no);
        m.put("name", name == null ? "" : name);
        m.put("action", action == null ? "" : action);
        m.put("remark", remark == null ? "" : remark);
        samples.add(m);
    }

    public void addSkipReason(String reason)
    {
        if (skipReasons.size() >= 10) return;
        skipReasons.add(reason);
    }

    // ===== Getter/Setter =====
    public Map<String, Integer> getStatistics() { return statistics; }
    public void setStatistics(Map<String, Integer> s) { this.statistics = s; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
    public List<Map<String, String>> getSamples() { return samples; }
    public void setSamples(List<Map<String, String>> samples) { this.samples = samples; }
    public List<String> getSkipReasons() { return skipReasons; }
    public void setSkipReasons(List<String> skipReasons) { this.skipReasons = skipReasons; }
}
