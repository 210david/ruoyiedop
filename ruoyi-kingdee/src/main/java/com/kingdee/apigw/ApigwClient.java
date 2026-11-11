package com.kingdee.apigw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 金蝶 API 网关客户端（SDK 兼容版）
 * <p>
 * 与官方 SDK 类名完全一致，签名算法和请求头构造严格遵循金蝶官方文档：
 * <a href="https://open.jdy.com/">https://open.jdy.com/</a>
 * <p>
 * 本类替代手写的 KingdeeSignUtil + KingdeeHttpClient，底层签名逻辑已通过
 * Node.js 官方示例 100% 验证。
 */
public class ApigwClient
{
    private static final Logger log = LoggerFactory.getLogger(ApigwClient.class);

    /** 鉴权换 token 接口 — 域名**固定 api.kingdee.com**（和业务接口 apikingdee.com 是两套域名！） */
    private static final String AUTH_DOMAIN = "https://api.kingdee.com";
    private static final String AUTH_TOKEN_PATH = "/jdyconnector/app_management/kingdee_auth_token";

    /** 签名涉及的 Header 名（固定值） */
    private static final String SIGN_HEADERS = "X-Api-TimeStamp,X-Api-Nonce";

    /** app-token 提前刷新时间（毫秒） */
    private static final long TOKEN_REFRESH_AHEAD_MS = 60_000L;

    /** token 有效期（金蝶默认 2 小时） */
    private static final long TOKEN_DEFAULT_TTL_MS = 7_200_000L;

    // ======================== 单例 ========================

    private static final ApigwClient INSTANCE = new ApigwClient();

    public static ApigwClient getInstance() { return INSTANCE; }

    private ApigwClient() {}

    // ======================== 状态 ========================

    private volatile ApigwConfig config;

    /** 缓存的 app-token */
    private volatile String cachedAppToken;
    /** token 过期时间戳（毫秒） */
    private volatile long tokenExpireAt;

    // ======================== 初始化 ========================

    /**
     * 初始化客户端配置（每次配置变更后需重新调用）
     */
    public void init(ApigwConfig config)
    {
        this.config = config;
        invalidateTokenCache();
        log.info("ApigwClient 初始化完成: {}", config);
    }

    /**
     * 清空 token 缓存（配置变更后调用）
     */
    public void invalidateTokenCache()
    {
        this.cachedAppToken = null;
        this.tokenExpireAt = 0L;
    }

    /**
     * 获取当前配置（可能为 null）
     */
    public ApigwConfig getConfig()
    {
        return config;
    }

    /** 是否已初始化（需要 clientSecret 和 appSecret 都有） */
    public boolean isInitialized()
    {
        return config != null && config.getClientID() != null
            && config.getClientSecret() != null && config.getAppSecret() != null;
    }

    // ======================== 核心 API ========================

    /**
     * 发送 API 请求（自动签名 + 自动带 app-token）
     *
     * @param request 请求对象
     * @return 响应对象
     */
    public ApiResult send(ApiRequest request)
    {
        ensureInitialized();

        // 如果是业务接口（非换 token），先确保 app-token 有效
        boolean isAuthRequest = AUTH_TOKEN_PATH.equals(request.getPath());
        if (!isAuthRequest)
        {
            ensureAppToken();
            // 自动注入 app-token header（如果调用方没设的话）
            if (!request.getHeaders().containsKey("app-token"))
            {
                request.addHeader("app-token", cachedAppToken);
            }
            // 自动注入 X-GW-Router-Addr
            if (config.getRouterAddr() != null && !request.getHeaders().containsKey("X-GW-Router-Addr"))
            {
                request.addHeader("X-GW-Router-Addr", config.getRouterAddr());
            }
        }

        // 签名 + 发请求
        Map<String, String> signedHeaders = buildSignedHeaders(request);
        signedHeaders.putAll(request.getHeaders()); // 调用方额外加的 header 也要带上

        String urlStr = buildFullUrl(request);
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(config.getTimeout());
            conn.setReadTimeout(config.getTimeout());
            conn.setRequestMethod(toHttpMethodString(request.getMethod()));

            // 设置所有 header
            for (Map.Entry<String, String> e : signedHeaders.entrySet())
            {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }

            // 写 body
            if (request.getBodyJson() != null && request.getBodyJson().length > 0)
            {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream())
                {
                    os.write(request.getBodyJson());
                }
            }

            int httpCode = conn.getResponseCode();
            String body;
            if (httpCode >= 200 && httpCode < 300)
            {
                body = readStream(conn);
            }
            else
            {
                body = readErrorStream(conn);
            }

            // 如果 token 失效，强制刷新后重试一次
            if (!isAuthRequest && (httpCode == 401 || (body != null && body.contains("\"errcode\":401"))))
            {
                log.warn("金蝶返回 401，强制刷新 app-token 后重试 path={}", request.getPath());
                forceRefreshToken();
                request.addHeader("app-token", cachedAppToken);
                signedHeaders = buildSignedHeaders(request);
                signedHeaders.putAll(request.getHeaders());
                // 递归重试（只一次，避免死循环）
                return doSend(request, signedHeaders);
            }

            return new ApiResult(httpCode, body, readResponseHeaders(conn));
        }
        catch (Exception e)
        {
            log.error("金蝶 API 请求异常 path={}: {}", request.getPath(), e.getMessage(), e);
            return new ApiResult(0, "{\"errcode\":-1,\"description\":\"" + e.getMessage() + "\"}", null);
        }
        finally
        {
            if (conn != null) conn.disconnect();
        }
    }

    private ApiResult doSend(ApiRequest request, Map<String, String> signedHeaders)
    {
        // 简化版重试，避免 401 无限循环
        String urlStr = buildFullUrl(request);
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(config.getTimeout());
            conn.setReadTimeout(config.getTimeout());
            conn.setRequestMethod(toHttpMethodString(request.getMethod()));
            for (Map.Entry<String, String> e : signedHeaders.entrySet())
            {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
            if (request.getBodyJson() != null)
            {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) { os.write(request.getBodyJson()); }
            }
            int httpCode = conn.getResponseCode();
            String body = (httpCode >= 200 && httpCode < 300) ? readStream(conn) : readErrorStream(conn);
            return new ApiResult(httpCode, body, readResponseHeaders(conn));
        }
        catch (Exception e)
        {
            return new ApiResult(0, "{\"errcode\":-1,\"description\":\"" + e.getMessage() + "\"}", null);
        }
        finally
        {
            if (conn != null) conn.disconnect();
        }
    }

    // ======================== app-token 管理 ========================

    /**
     * 显式获取 app-token（带缓存）
     */
    public synchronized String getAppToken()
    {
        ensureAppToken();
        return cachedAppToken;
    }

    private void ensureAppToken()
    {
        long now = System.currentTimeMillis();
        if (cachedAppToken != null && (tokenExpireAt - TOKEN_REFRESH_AHEAD_MS) > now)
        {
            return;
        }
        refreshAppToken();
    }

    private void forceRefreshToken()
    {
        cachedAppToken = null;
        refreshAppToken();
    }

    /**
     * 换 app-token（双阶段鉴权第一步）
     * GET https://api.kingdee.com/jdyconnector/app_management/kingdee_auth_token
     *   ?app_key=xxx&app_signature=Base64(HMAC-SHA256(appSecret, appKey))
     */
    private synchronized void refreshAppToken()
    {
        ensureInitialized();

        String appKey = config.getAppKey();
        String appSecret = config.getAppSecret();

        if (appKey == null || appSecret == null)
        {
            throw new IllegalStateException("ApigwClient 未配置 appKey 或 appSecret");
        }

        // 1. app_signature URL 参数 — 用 **appSecret** 作密钥（注意：不是 clientSecret！）
        String appSignature = calcAppSignature(appSecret, appKey);

        // 2. 构建请求 — 鉴权域名固定 AUTH_DOMAIN（和业务接口 baseDomain 是两套独立域名！）
        ApiRequest authReq = new ApiRequest(HttpMethod.GET, AUTH_DOMAIN, AUTH_TOKEN_PATH);
        authReq.addQuery("app_key", appKey);
        authReq.addQuery("app_signature", appSignature);

        log.info("正在获取金蝶 app-token ... appKey={}", appKey);

        ApiResult result = doSendInternal(authReq);

        if (!result.isSuccess())
        {
            log.error("金蝶 app-token HTTP 错误: code={} body={}", result.getHttpCode(), result.getBody());
            throw new RuntimeException("金蝶 app-token 获取失败 HTTP " + result.getHttpCode());
        }

        String body = result.getBody();
        Integer errcode = extractJsonInt(body, "errcode");
        if (errcode == null || errcode != 0)
        {
            String desc = extractJsonString(body, "description");
            log.error("金蝶 app-token 业务错误: errcode={} desc={} body={}", errcode, desc, body);
            throw new RuntimeException("金蝶 app-token 接口报错 errcode=" + errcode + ", desc=" + desc);
        }

        // 从 data 对象里取 "app-token"（key 带中划线）
        String token = extractAppTokenFromData(body);
        if (token == null || token.isEmpty())
        {
            throw new IllegalStateException("金蝶 app-token 为空: " + body);
        }

        cachedAppToken = token;
        tokenExpireAt = System.currentTimeMillis() + TOKEN_DEFAULT_TTL_MS;
        log.info("✅ 金蝶 app-token 获取成功，有效期 2h");
    }

    /** 内部发送（用于换 token，不走自动带 app-token 逻辑） */
    private ApiResult doSendInternal(ApiRequest request)
    {
        // 自己算签名 header
        Map<String, String> signedHeaders = buildSignedHeaders(request);
        signedHeaders.putAll(request.getHeaders());

        String urlStr = buildFullUrl(request);
        HttpURLConnection conn = null;
        try
        {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(config.getTimeout());
            conn.setReadTimeout(config.getTimeout());
            conn.setRequestMethod(toHttpMethodString(request.getMethod()));
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("User-Agent", "kingdee-apigw-sdk/1.0");
            for (Map.Entry<String, String> e : signedHeaders.entrySet())
            {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
            if (request.getBodyJson() != null)
            {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) { os.write(request.getBodyJson()); }
            }
            int httpCode = conn.getResponseCode();
            String body = (httpCode >= 200 && httpCode < 300) ? readStream(conn) : readErrorStream(conn);
            return new ApiResult(httpCode, body, readResponseHeaders(conn));
        }
        catch (Exception e)
        {
            return new ApiResult(0, "{\"errcode\":-1,\"description\":\"" + e.getMessage() + "\"}", null);
        }
        finally
        {
            if (conn != null) conn.disconnect();
        }
    }

    // ======================== 签名核心 ========================

    /**
     * 构建 7 个必填 Header（自动签名）
     */
    private Map<String, String> buildSignedHeaders(ApiRequest request)
    {
        long tsMillis = System.currentTimeMillis();
        String timestamp = request.getTimeStamp() != null ? request.getTimeStamp() : String.valueOf(tsMillis);
        String nonce = request.getNonce() != null ? request.getNonce() : generateNonce();

        // 收集 query params
        Map<String, String> params = new TreeMap<>(request.getQuerys());

        // 签名原文（5 段 + 4 个 \n + 末尾一个 \n）
        String methodStr = request.getMethod() == HttpMethod.POST_BODY ? "POST" : request.getMethod().name();
        String path = request.getPath();
        String signature = sign(config.getClientSecret(), methodStr, path, params, nonce, timestamp);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");
        headers.put("X-Api-ClientID", config.getClientID());
        headers.put("X-Api-Auth-Version", config.getAuthVersion());
        headers.put("X-Api-TimeStamp", timestamp);
        headers.put("X-Api-Nonce", nonce);
        headers.put("X-Api-SignHeaders", SIGN_HEADERS);
        headers.put("X-Api-Signature", signature);
        return headers;
    }

    /**
     * 计算 X-Api-Signature
     * <p>
     * 签名原文 = 段1\n段2\n段3\n段4\n段5\n
     * 段1: HTTP 方法大写
     * 段2: path 做一次 URL encode
     * 段3: params 按 key ASCII 升序拼，每个 key 和 value 做两次 URL encode（空则空串）
     * 段4: x-api-nonce:xxx（header 名小写）
     * 段5: x-api-timestamp:xxx（header 名小写）
     * <p>
     * 加密: HMAC-SHA256(clientSecret, 签名原文) → hex 字符串 → Base64
     */
    String sign(String clientSecret, String method, String path,
                Map<String, String> params, String nonce, String timestamp)
    {
        String seg1 = method.toUpperCase();
        String seg2 = urlEncodeOnce(path);
        String seg3 = buildParamsDoubleEncoded(params);
        String seg4 = "x-api-nonce:" + nonce;
        String seg5 = "x-api-timestamp:" + timestamp;

        String signStr = seg1 + "\n" + seg2 + "\n" + seg3 + "\n" + seg4 + "\n" + seg5 + "\n";

        if (log.isDebugEnabled())
        {
            log.debug("=== 金蝶签名原文 ===");
            log.debug("seg1(method): [{}]", seg1);
            log.debug("seg2(path)  : [{}]", seg2);
            log.debug("seg3(params): [{}]", seg3);
            log.debug("seg4(nonce) : [{}]", seg4);
            log.debug("seg5(ts)    : [{}]", seg5);
            log.debug("full        : {}", signStr.replace("\n", "\\n"));
        }

        byte[] raw = hmacSha256(clientSecret, signStr);
        String hex = bytesToHex(raw);
        String signature = Base64.getEncoder().encodeToString(hex.getBytes(StandardCharsets.UTF_8));

        log.debug("金蝶签名 path={} sig={}", path, signature);
        return signature;
    }

    /**
     * 计算 app_signature（用于换 app-token 接口的 URL params）
     * = Base64(hex(HMAC-SHA256(appSecret, appKey)))
     */
    String calcAppSignature(String appSecret, String appKey)
    {
        byte[] raw = hmacSha256(appSecret, appKey);
        String hex = bytesToHex(raw);
        return Base64.getEncoder().encodeToString(hex.getBytes(StandardCharsets.UTF_8));
    }

    // ======================== URL 构造 ========================

    private String buildFullUrl(ApiRequest request)
    {
        StringBuilder url = new StringBuilder();
        String domain = request.getDomain() != null ? request.getDomain() : config.getBaseDomain();
        url.append(domain);

        String path = request.getPath();
        if (path != null)
        {
            url.append(path);
        }

        if (request.getQuerys() != null && !request.getQuerys().isEmpty())
        {
            // params 按 key 升序排列（金蝶要求）
            Map<String, String> sorted = new TreeMap<>(request.getQuerys());
            boolean first = true;
            for (Map.Entry<String, String> e : sorted.entrySet())
            {
                url.append(first ? '?' : '&');
                url.append(urlEncodeOnce(e.getKey()));
                url.append('=');
                url.append(urlEncodeOnce(e.getValue() == null ? "" : e.getValue()));
                first = false;
            }
        }
        return url.toString();
    }

    // ======================== 工具方法 ========================

    private String buildParamsDoubleEncoded(Map<String, String> params)
    {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet())
        {
            if (sb.length() > 0) sb.append('&');
            sb.append(doubleUrlEncode(e.getKey()));
            sb.append('=');
            sb.append(doubleUrlEncode(e.getValue() == null ? "" : e.getValue()));
        }
        return sb.toString();
    }

    private static String doubleUrlEncode(String s) { return urlEncodeOnce(urlEncodeOnce(s)); }

    private static String urlEncodeOnce(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8")
                .replace("+", "%2B")
                .replace("*", "%2A")
                .replace("%7E", "~");
        }
        catch (Exception e) { return s; }
    }

    private static byte[] hmacSha256(String secret, String data)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("HMAC-SHA256 加密失败: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String generateNonce()
    {
        // 13 位数字 nonce（兼容金蝶示例格式）
        String hex = UUID.randomUUID().toString().replace("-", "").substring(0, 13);
        return String.valueOf(Long.parseLong(hex, 16));
    }

    private void ensureInitialized()
    {
        if (!isInitialized())
        {
            throw new IllegalStateException("ApigwClient 未初始化，请先调用 init(ApigwConfig)");
        }
    }

    private static String toHttpMethodString(HttpMethod m)
    {
        if (m == HttpMethod.POST_BODY) return "POST";
        return m.name();
    }

    private static String readStream(HttpURLConnection conn) throws Exception
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static String readErrorStream(HttpURLConnection conn) throws Exception
    {
        if (conn.getErrorStream() == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static Map<String, String> readResponseHeaders(HttpURLConnection conn)
    {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> e : conn.getHeaderFields().entrySet())
        {
            if (e.getKey() != null && e.getValue() != null && !e.getValue().isEmpty())
            {
                headers.put(e.getKey(), e.getValue().get(0));
            }
        }
        return headers;
    }

    // ======================== JSON 提取工具 ========================

    private static Integer extractJsonInt(String json, String key)
    {
        if (json == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static String extractJsonString(String json, String key)
    {
        if (json == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /** 从响应 data 对象中提取 app-token（key 带中划线） */
    private static String extractAppTokenFromData(String json)
    {
        int dataIdx = json.indexOf("\"data\"");
        if (dataIdx < 0) return null;
        int braceStart = json.indexOf('{', dataIdx);
        if (braceStart < 0) return null;
        int depth = 0, braceEnd = -1;
        for (int i = braceStart; i < json.length(); i++)
        {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') { depth--; if (depth == 0) { braceEnd = i; break; } }
        }
        if (braceEnd < 0) return null;
        String dataObj = json.substring(braceStart, braceEnd + 1);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "\"app-token\"\\s*:\\s*\"([^\"]*)\"").matcher(dataObj);
        return m.find() ? m.group(1) : null;
    }
}
