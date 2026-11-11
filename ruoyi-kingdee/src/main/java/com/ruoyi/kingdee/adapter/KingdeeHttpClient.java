package com.ruoyi.kingdee.adapter;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingdee.apigw.ApigwClient;
import com.kingdee.apigw.ApigwConfig;
import com.kingdee.apigw.ApiRequest;
import com.kingdee.apigw.ApiResult;
import com.kingdee.apigw.HttpMethod;
import com.ruoyi.kingdee.config.KingdeeConfigResolver;

/**
 * 金蝶云星辰 HTTP 客户端（SDK 兼容版）
 * <p>
 * 底层使用 com.kingdee.apigw.ApigwClient 官方 SDK 兼容层，
 * 签名算法和请求头由 SDK 统一处理，业务代码只关心 path + body。
 * <p>
 * 每次调用都从 KingdeeConfigResolver 取最新配置，
 * 前端保存后立即调用 {@link #syncConfigToSdk()} 即可生效。
 */
@Component
public class KingdeeHttpClient
{
    private static final Logger log = LoggerFactory.getLogger(KingdeeHttpClient.class);

    /** 业务域名（固定 api.kingdee.com — 鉴权和业务都走这里！之前误以为两个域名，实测 apikingdee.com DNS 不存在！） */
    private static final String BUSINESS_DOMAIN = "https://api.kingdee.com";

    /** 静态 ObjectMapper（Spring Boot classpath 必有 Jackson，但不一定注册为 Bean） */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KingdeeConfigResolver resolver;
    private final ApigwClient sdk = ApigwClient.getInstance();

    public KingdeeHttpClient(KingdeeConfigResolver resolver)
    {
        this.resolver = resolver;
        // 构造时尝试初始化一次（Spring 启动时 sys_config 可能已有值）
        try { syncConfigToSdk(); } catch (Exception e) { log.warn("SDK 初始初始化跳过: {}", e.getMessage()); }
    }

    // ======================== 配置同步 ========================

    /**
     * 将当前最新配置同步到 SDK（每次配置变更后调用）
     */
    public void syncConfigToSdk()
    {
        ApigwConfig cfg = new ApigwConfig();
        cfg.setClientID(resolver.getClientId());
        cfg.setClientSecret(resolver.getClientSecret()); // 签名密钥
        cfg.setAppKey(resolver.getAppKey());
        cfg.setAppSecret(resolver.getAppSecret());       // app_signature URL 参数密钥
        cfg.setBaseDomain(BUSINESS_DOMAIN);
        cfg.setRouterAddr(resolver.getRouterAddr());
        cfg.setTimeout(resolver.getTimeout());
        sdk.init(cfg);
        log.info("✅ 金蝶 SDK 配置已同步: clientID={}, appKey={}, clientSecret={}, appSecret={}",
            cfg.getClientID(), cfg.getAppKey(),
            mask(cfg.getClientSecret()), mask(cfg.getAppSecret()));
    }

    /**
     * 强制清空 SDK token 缓存（配置变更后调用）
     */
    public void invalidateTokenCache()
    {
        sdk.invalidateTokenCache();
    }

    // ======================== 公开 API ========================

    /**
     * POST 调用金蝶业务 API
     *
     * @param path 业务路径（如 /jdy/bd/customer 或 /bd/customer，会自动补齐前缀）
     * @param body 请求体（Map / POJO，会自动序列化为 JSON）
     * @return 金蝶原始响应 JSON 字符串
     */
    public String post(String path, Object body)
    {
        String fullPath = normalizePath(path);
        log.info("金蝶 POST {} body={}", fullPath, body);

        ApiRequest req = new ApiRequest(HttpMethod.POST_BODY, BUSINESS_DOMAIN, fullPath);
        req.setBodyJson(toJsonBytes(body));

        ApiResult result = sdk.send(req);
        log.info("金蝶 POST {} → code={}", fullPath, result.getHttpCode());
        return result.getBody();
    }

    /**
     * POST 调用（带重试）
     */
    public String postWithRetry(String path, Object body)
    {
        int maxRetry = resolver.getRetryTimes();
        String lastError = null;
        for (int attempt = 0; attempt <= maxRetry; attempt++)
        {
            try
            {
                return post(path, body);
            }
            catch (Exception e)
            {
                lastError = e.getMessage();
                log.warn("金蝶 API 调用失败 ({}/{}): {}", attempt, maxRetry, e.getMessage());
                if (attempt < maxRetry)
                {
                    sleep(500L * (attempt + 1));
                }
            }
        }
        throw new RuntimeException("金蝶 API 最终失败 path=" + path + ": " + lastError);
    }

    /**
     * GET 调用金蝶业务 API
     *
     * @param path 业务路径
     * @param queryParams URL 查询参数
     * @return 金蝶原始响应 JSON 字符串
     */
    public String get(String path, java.util.Map<String, String> queryParams)
    {
        String fullPath = normalizePath(path);
        ApiRequest req = new ApiRequest(HttpMethod.GET, BUSINESS_DOMAIN, fullPath);
        if (queryParams != null)
        {
            queryParams.forEach(req::addQuery);
        }

        ApiResult result = sdk.send(req);
        log.info("金蝶 GET {} params={} → code={}", fullPath, queryParams, result.getHttpCode());
        return result.getBody();
    }

    /**
     * GET 调用（带重试）
     */
    public String getWithRetry(String path, java.util.Map<String, String> queryParams)
    {
        int maxRetry = resolver.getRetryTimes();
        String lastError = null;
        for (int attempt = 0; attempt <= maxRetry; attempt++)
        {
            try
            {
                return get(path, queryParams);
            }
            catch (Exception e)
            {
                lastError = e.getMessage();
                log.warn("金蝶 GET API 调用失败 ({}/{}): {}", attempt, maxRetry, e.getMessage());
                if (attempt < maxRetry)
                {
                    sleep(500L * (attempt + 1));
                }
            }
        }
        throw new RuntimeException("金蝶 GET API 最终失败 path=" + path + ": " + lastError);
    }

    /**
     * 获取 SDK 实例（高级用法：直接操作 SDK）
     */
    public ApigwClient getSdk()
    {
        return sdk;
    }

    /**
     * 测试连通性（尝试换一次 app-token）
     *
     * @return "OK" 或错误描述
     */
    public String testConnection()
    {
        try
        {
            syncConfigToSdk();
            String token = sdk.getAppToken();
            if (token != null && !token.isEmpty())
            {
                return "OK: app-token 获取成功 (len=" + token.length() + ")";
            }
            return "FAIL: app-token 为空";
        }
        catch (Exception e)
        {
            return "FAIL: " + e.getMessage();
        }
    }

    // ======================== 工具 ========================

    /**
     * 路径标准化：自动补齐 /jdy/v2 前缀（金蝶云星辰业务接口路径都是 /jdy/v2/模块/接口）
     * 支持传入 "/bd/customer" → "/jdy/v2/bd/customer"
     * 也支持传入完整路径 "/jdy/v2/bd/customer" → 原样保留
     * 鉴权接口 /jdyconnector 不要加前缀
     */
    private static String normalizePath(String path)
    {
        if (path == null) return "/jdy/v2";
        String p = path.startsWith("/") ? path : "/" + path;
        if (p.startsWith("/jdyconnector"))
        {
            return p; // 鉴权接口原样
        }
        if (p.startsWith("/jdy/"))
        {
            // 已经有 /jdy/ 前缀，检查有没有 /v2/
            if (!p.startsWith("/jdy/v2/"))
            {
                // /jdy/xxx → /jdy/v2/xxx
                p = "/jdy/v2" + p.substring(4);
            }
            return p;
        }
        if (p.startsWith("/jdy"))
        {
            // /jdy 或 /jdyxxx
            return p;
        }
        // 无前缀 → 加 /jdy/v2
        return "/jdy/v2" + p;
    }

    private byte[] toJsonBytes(Object obj)
    {
        if (obj == null) return new byte[0];
        if (obj instanceof byte[]) return (byte[]) obj;
        if (obj instanceof String) return ((String) obj).getBytes(StandardCharsets.UTF_8);
        try
        {
            return MAPPER.writeValueAsBytes(obj);
        }
        catch (Exception e)
        {
            throw new RuntimeException("JSON 序列化失败: " + e.getMessage(), e);
        }
    }

    private static void sleep(long ms)
    {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private static String mask(String s)
    {
        if (s == null || s.isEmpty()) return "未配置";
        if (s.length() <= 8) return s.substring(0, 4) + "****";
        return s.substring(0, 8) + "****" + s.substring(s.length() - 4);
    }
}
