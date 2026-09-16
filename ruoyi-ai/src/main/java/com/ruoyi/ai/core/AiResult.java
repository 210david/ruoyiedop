package com.ruoyi.ai.core;

import java.util.HashMap;
import java.util.Map;

/**
 * AI调用统一返回结果
 * 状态：success成功 / degraded降级 / disabled关闭 / limited限额拦截 / failed失败
 *
 * @author ruoyi
 */
public class AiResult
{
    /** 调用成功 */
    public static final String STATUS_SUCCESS = "0";
    /** 降级（AI失败，业务走备选逻辑） */
    public static final String STATUS_DEGRADED = "1";
    /** 调用失败 */
    public static final String STATUS_FAILED = "2";
    /** 限额拦截 */
    public static final String STATUS_LIMITED = "3";

    private boolean success;
    private String status;
    private String content;
    private String error;
    private long latencyMs;
    /** 是否命中本地缓存（未实际调用模型） */
    private boolean cached;

    public static AiResult ok(String content, long latencyMs)
    {
        AiResult r = new AiResult();
        r.success = true;
        r.status = STATUS_SUCCESS;
        r.content = content;
        r.latencyMs = latencyMs;
        return r;
    }

    public static AiResult degraded(String error)
    {
        AiResult r = new AiResult();
        r.success = false;
        r.status = STATUS_DEGRADED;
        r.error = error;
        return r;
    }

    public static AiResult failed(String error)
    {
        AiResult r = new AiResult();
        r.success = false;
        r.status = STATUS_FAILED;
        r.error = error;
        return r;
    }

    public static AiResult limited()
    {
        AiResult r = new AiResult();
        r.success = false;
        r.status = STATUS_LIMITED;
        r.error = "AI月度调用次数已达上限";
        return r;
    }

    /** 转为前端友好Map */
    public Map<String, Object> toMap()
    {
        Map<String, Object> m = new HashMap<>();
        m.put("success", success);
        m.put("status", status);
        m.put("content", content);
        m.put("error", error);
        m.put("latencyMs", latencyMs);
        m.put("cached", cached);
        return m;
    }

    public boolean isSuccess() { return success; }
    public String getStatus() { return status; }
    public String getContent() { return content; }
    public String getError() { return error; }
    public long getLatencyMs() { return latencyMs; }
    public boolean isCached() { return cached; }
    public void setCached(boolean cached) { this.cached = cached; }
}
