package com.ruoyi.kingdee.adapter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.kingdee.config.KingdeeConfigResolver;

import jakarta.annotation.PostConstruct;

/**
 * 金蝶 appSecret 自动刷新器
 * <p>
 * 签名规则与换 app-token 接口完全一致（属于鉴权类接口，不需要 app-token header）：
 * <pre>
 *   POST https://api.kingdee.com/jdyconnector/app_management/push_app_authorize
 *     ?outerInstanceId=xxx&app_key=xxx&app_signature=Base64(HMAC-SHA256(appSecret, appKey))
 *   + 7 个标准金蝶签名 header
 *   + POST body: {}
 * </pre>
 * 返回格式（和业务接口不同！）：
 * <pre>
 *   { "code": 200, "msg": "查询要推送的用户授权成功", "data": [ { ... } ] }
 * </pre>
 */
@Component
public class AppSecretRefresher
{
    private static final Logger log = LoggerFactory.getLogger(AppSecretRefresher.class);

    private static final String AUTH_DOMAIN = "https://api.kingdee.com";
    private static final String PATH = "/jdyconnector/app_management/push_app_authorize";

    private final KingdeeConfigResolver resolver;
    private final KingdeeHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile long lastSuccessAt = 0;

    public AppSecretRefresher(KingdeeConfigResolver resolver, KingdeeHttpClient httpClient)
    {
        this.resolver = resolver;
        this.httpClient = httpClient;
    }

    // ==================== 签名工具 ====================

    private static String hmacSha256Hex(String key, String data)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hex = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hex) sb.append(String.format("%02x", b));
            return sb.toString();
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String doubleUrlEncode(String s)
    {
        return urlEncodeOnce(urlEncodeOnce(s));
    }

    private static String singleUrlEncode(String s)
    {
        return urlEncodeOnce(s);
    }

    /**
     * 单次 URL 编码 — 金蝶官方要求：
     * 1. + 替换为 %2B（URLEncoder 默认输出 +）
     * 2. %xx 里的十六进制字母必须大写（URLEncoder 默认小写）
     */
    private static String urlEncodeOnce(String s)
    {
        try
        {
            String encoded = java.net.URLEncoder.encode(s, StandardCharsets.UTF_8)
                .replace("+", "%2B")
                .replace("*", "%2A")
                .replace("%7E", "~");
            // 手动把 %xx 里的十六进制字母转大写
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < encoded.length())
            {
                if (encoded.charAt(i) == '%' && i + 2 < encoded.length())
                {
                    sb.append('%');
                    sb.append(Character.toUpperCase(encoded.charAt(i + 1)));
                    sb.append(Character.toUpperCase(encoded.charAt(i + 2)));
                    i += 3;
                }
                else
                {
                    sb.append(encoded.charAt(i));
                    i++;
                }
            }
            return sb.toString();
        }
        catch (Exception e) { return s; }
    }

    private static String calcAppSignature(String appSecret, String appKey)
    {
        // 金蝶官方：HMAC-SHA256 → 16进制字符串 → 对 hex 字符串做 Base64（不是对 raw bytes！）
        String hex = hmacSha256Hex(appSecret, appKey);
        return java.util.Base64.getEncoder().encodeToString(hex.getBytes(StandardCharsets.UTF_8));
    }

    private static String calcSignature(String clientSecret, String method, String path,
                                        Map<String, String> params, String nonce, String timestamp)
    {
        TreeMap<String, String> sorted = new TreeMap<>(params == null ? new HashMap<>() : params);
        StringBuilder ps = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet())
        {
            if (ps.length() > 0) ps.append('&');
            ps.append(e.getKey()).append('=').append(doubleUrlEncode(e.getValue()));
        }

        String pathEnc = singleUrlEncode(path);
        String hs = "x-api-nonce:" + nonce + "\nx-api-timestamp:" + timestamp;
        String text = method + "\n" + pathEnc + "\n" + ps + "\n" + hs + "\n";

        String hex = hmacSha256Hex(clientSecret, text);
        // 金蝶官方：HMAC-SHA256 → 16进制字符串 → 对 hex 字符串做 Base64（输出 88 字符，不是 44！）
        return java.util.Base64.getEncoder().encodeToString(hex.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 刷新逻辑 ====================

    public Map<String, Object> refreshNow()
    {
        Map<String, Object> result = new HashMap<>();
        long start = System.currentTimeMillis();

        String outerInstanceId = resolver.getOuterInstanceId();
        if (StringUtils.isEmpty(outerInstanceId))
        {
            result.put("ok", false);
            result.put("error", "未配置 outerInstanceId");
            return result;
        }

        try
        {
            String appKey = resolver.getAppKey();
            String appSecret = resolver.getAppSecret();
            String clientSecret = resolver.getClientSecret();
            String clientId = resolver.getClientId();

            if (StringUtils.isEmpty(appSecret) || StringUtils.isEmpty(clientSecret))
            {
                result.put("ok", false);
                result.put("error", "appSecret 或 clientSecret 未配置");
                return result;
            }

            log.info("🔄 [appSecret 刷新] 开始 appKey={}, outerInstanceId={}", appKey, outerInstanceId);

            String nonce = String.valueOf(System.currentTimeMillis());
            String ts = String.valueOf(System.currentTimeMillis());
            String appSig = calcAppSignature(appSecret, appKey);

            // URL params（按 ASCII 升序）
            Map<String, String> allParams = new TreeMap<>();
            allParams.put("app_key", appKey);
            allParams.put("app_signature", appSig);
            allParams.put("outerInstanceId", outerInstanceId);

            // 构造完整 URL（params 在 URL 里只做单次 encode）
            StringBuilder urlSb = new StringBuilder(AUTH_DOMAIN).append(PATH).append('?');
            boolean first = true;
            for (Map.Entry<String, String> e : allParams.entrySet())
            {
                if (!first) urlSb.append('&');
                urlSb.append(e.getKey()).append('=').append(singleUrlEncode(e.getValue()));
                first = false;
            }
            String fullUrl = urlSb.toString();

            // 7 个签名 header
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json;charset=UTF-8");
            headers.put("X-Api-ClientID", clientId);
            headers.put("X-Api-Auth-Version", "2.0");
            headers.put("X-Api-TimeStamp", ts);
            headers.put("X-Api-Nonce", nonce);
            headers.put("X-Api-SignHeaders", "X-Api-TimeStamp,X-Api-Nonce");
            headers.put("X-Api-Signature", calcSignature(clientSecret, "POST", PATH, allParams, nonce, ts));

            // 发请求
            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(resolver.getTimeout());
            conn.setReadTimeout(resolver.getTimeout());
            conn.setDoOutput(true);
            headers.forEach(conn::setRequestProperty);
            try (var os = conn.getOutputStream()) { os.write("{}".getBytes(StandardCharsets.UTF_8)); }

            int code = conn.getResponseCode();
            String body;
            try (var is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream())
            {
                body = is == null ? "" : new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            conn.disconnect();

            log.info("金蝶 push_app_authorize → HTTP {}", code);
            log.debug("响应体: {}", body);

            // 解析返回：{code:200, msg:"...", data:[...]}
            Map<String, Object> resp = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            Integer respCode = (Integer) resp.get("code");
            String msg = String.valueOf(resp.getOrDefault("msg", ""));

            if (respCode == null || respCode != 200)
            {
                log.error("❌ [appSecret 刷新] 接口报错: code={}, msg={}", respCode, msg);
                result.put("ok", false);
                result.put("error", msg);
                return result;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) resp.get("data");
            if (data == null || data.isEmpty())
            {
                result.put("ok", false);
                result.put("error", "金蝶返回 data 为空");
                return result;
            }

            Map<String, Object> auth = data.get(0);
            String newAppSecret = String.valueOf(auth.getOrDefault("appSecret", ""));
            String newAppKey = String.valueOf(auth.getOrDefault("appKey", ""));
            String newDomain = String.valueOf(auth.getOrDefault("domain", ""));
            String accountId = String.valueOf(auth.getOrDefault("accountId", ""));
            String accountName = String.valueOf(auth.getOrDefault("accountName", ""));
            String companyName = String.valueOf(auth.getOrDefault("agreementCompanyName", ""));
            Object expires = auth.get("instanceExpiresTime");
            int status = auth.get("status") instanceof Number ? ((Number) auth.get("status")).intValue() : -1;

            if (StringUtils.isEmpty(newAppSecret))
            {
                result.put("ok", false);
                result.put("error", "金蝶返回 appSecret 为空");
                return result;
            }

            // 对比并持久化
            boolean changed = !newAppSecret.equals(appSecret);
            if (changed)
            {
                resolver.save(KingdeeConfigResolver.K_APP_SECRET, newAppSecret, "appSecret刷新器");
                log.info("✅ [appSecret 刷新] appSecret 已更新");
            }
            else
            {
                log.info("✅ [appSecret 刷新] appSecret 未变化（仍是最新）");
            }

            if (StringUtils.isNotEmpty(newAppKey) && !newAppKey.equals(appKey))
            {
                resolver.save(KingdeeConfigResolver.K_APP_KEY, newAppKey, "appSecret刷新器");
            }

            String curRouter = resolver.getRouterAddr();
            if (StringUtils.isNotEmpty(newDomain) && !newDomain.equals(curRouter))
            {
                resolver.save(KingdeeConfigResolver.K_ROUTER_ADDR, newDomain, "appSecret刷新器");
            }

            // 通知 SDK + 清 token
            httpClient.syncConfigToSdk();
            com.kingdee.apigw.ApigwClient.getInstance().invalidateTokenCache();

            lastSuccessAt = System.currentTimeMillis();
            result.put("ok", true);
            result.put("changed", changed);
            result.put("accountId", accountId);
            result.put("accountName", accountName);
            result.put("companyName", companyName);
            result.put("routerAddr", newDomain);
            result.put("status", status);
            result.put("instanceExpiresTime", expires);
            result.put("elapsedMs", System.currentTimeMillis() - start);
            return result;
        }
        catch (Exception e)
        {
            log.error("❌ [appSecret 刷新] 异常", e);
            result.put("ok", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    // ==================== 定时 ====================

    @Scheduled(cron = "0 0 2,14 * * ?")
    public void scheduledRefresh()
    {
        if (!resolver.isEnabled() || StringUtils.isEmpty(resolver.getOuterInstanceId())) return;
        Map<String, Object> r = refreshNow();
        log.info("[appSecret 定时刷新] ok={}, changed={}", r.get("ok"), r.get("changed"));
    }

    @PostConstruct
    public void startupSelfCheck()
    {
        new Thread(() -> {
            try { Thread.sleep(30000); } catch (InterruptedException ignored) { return; }
            if (!resolver.isEnabled() || StringUtils.isEmpty(resolver.getOuterInstanceId())) return;
            log.info("🧐 [appSecret 启动自检] 自动刷新...");
            Map<String, Object> r = refreshNow();
            log.info("[appSecret 启动自检] ok={}, changed={}", r.get("ok"), r.get("changed"));
        }, "appSecret-refresher-selfcheck").start();
    }

    public long getLastSuccessAt() { return lastSuccessAt; }
}
