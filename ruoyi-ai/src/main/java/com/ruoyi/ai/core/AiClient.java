package com.ruoyi.ai.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.domain.AiCallLog;
import com.ruoyi.ai.service.IAiAuditService;
import com.ruoyi.ai.util.SensitiveDataMasker;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 统一LLM调用客户端（OpenAI兼容格式）
 * 职责：开关校验 → 限额校验 → 脱敏 → 调用 → 审计落库
 * 各业务模块统一通过本客户端调用大模型，禁止自行发起HTTP请求
 *
 * 配置键（sys_config）：
 * ai.enabled / ai.api_url / ai.api_key / ai.model_name / ai.monthly_limit
 * ai.feature.<module>.<feature> 功能级开关
 * ai.prompt.<module>.<feature>   提示词模板（占位符 {input} 等）
 *
 * @author ruoyi
 */
@Component
public class AiClient
{
    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IAiAuditService auditService;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Redis月度计数key前缀 */
    private static final String CALL_COUNT_KEY = "ai:call:";

    /** 异步线程用户上下文（SSE等无SecurityContext的线程，由入口请求线程显式捕获并传入，用于审计落库操作人） */
    private static final ThreadLocal<Long> ASYNC_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ASYNC_USER_NAME = new ThreadLocal<>();

    /** 在异步线程执行AI调用前设置当前用户（入口请求线程捕获后传入） */
    public static void setUserContext(Long userId, String userName)
    {
        ASYNC_USER_ID.set(userId);
        ASYNC_USER_NAME.set(userName);
    }

    /** 异步线程结束后清理，避免线程复用导致串号 */
    public static void clearUserContext()
    {
        ASYNC_USER_ID.remove();
        ASYNC_USER_NAME.remove();
    }

    public static Long getAsyncUserId()
    {
        return ASYNC_USER_ID.get();
    }

    public static String getAsyncUserName()
    {
        return ASYNC_USER_NAME.get();
    }

    // ==================== 对外主入口 ====================

    /**
     * 调用大模型（完整链路：开关→限额→脱敏→调用→审计）
     *
     * @param module  业务模块（dms/qms/pms/mms/wms/mk/hr/safety/platform）
     * @param feature 功能标识（如 repair_assistant / ncr_advice）
     * @param prompt  完整提示词（业务方组装）
     * @return AiResult，业务方按状态渲染（失败/降级时走备选逻辑）
     */
    public AiResult chat(String module, String feature, String prompt)
    {
        return chat(module, feature, prompt, 1000);
    }

    /** 短超时快速失败的调用入口（适用于SQL生成等轻量任务，避免模型服务抖动时长时间挂起） */
    public AiResult chatWithTimeout(String module, String feature, String prompt, int timeoutSeconds)
    {
        TIMEOUT_OVERRIDE.set(timeoutSeconds);
        try
        {
            return chat(module, feature, prompt);
        }
        finally
        {
            TIMEOUT_OVERRIDE.remove();
        }
    }

    /** 线程级超时覆盖（秒）：chatWithTimeout 设置，send() 读取 */
    private static final ThreadLocal<Integer> TIMEOUT_OVERRIDE = new ThreadLocal<>();

    /** 缓存条目上限：超过则清空重建，避免长期运行内存膨胀 */
    private static final int RESPONSE_CACHE_MAX = 300;
    /** 缓存有效期：30分钟兜底（期间即使重复点击也不再调用） */
    private static final long RESPONSE_CACHE_TTL_MS = 30 * 60 * 1000L;
    private final Map<String, Object[]> responseCache = new ConcurrentHashMap<>();

    /**
     * 带提示词指纹缓存的调模型入口（原创设计：数据未变→提示词不变→直接复用上次结果）
     * key = module:feature:MD5(prompt+maxTokens)，命中未过期则返回 cached=true 的结果，不实际调模型
     * 调用失败不缓存，下次点击自动重试
     */
    public AiResult chatCached(String module, String feature, String prompt, int maxTokens)
    {
        // 缓存键使用应用模板覆盖后的最终提示词指纹：修改模板后立即走新键，不命中旧缓存
        String effective = applyTemplateOverride(module, feature, prompt);
        String key = module + ":" + feature + ":"
            + com.ruoyi.common.utils.sign.Md5Utils.hash(effective + "|" + maxTokens);
        Object[] entry = responseCache.get(key);
        if (entry != null && System.currentTimeMillis() - (Long) entry[0] < RESPONSE_CACHE_TTL_MS)
        {
            AiResult cached = (AiResult) entry[1];
            cached.setCached(true);
            return cached;
        }
        AiResult ai = chat(module, feature, prompt, maxTokens);
        if (ai.isSuccess())
        {
            if (responseCache.size() >= RESPONSE_CACHE_MAX)
            {
                responseCache.clear();
            }
            responseCache.put(key, new Object[] { System.currentTimeMillis(), ai });
        }
        return ai;
    }

    /** 带提示词指纹缓存的调模型入口（默认 maxTokens） */
    public AiResult chatCached(String module, String feature, String prompt)
    {
        return chatCached(module, feature, prompt, 1000);
    }

    /**
     * 调用大模型（可指定maxTokens，长文本生成场景如考题/报告用更大值）
     */
    public AiResult chat(String module, String feature, String prompt, int maxTokens)
    {
        long start = System.currentTimeMillis();
        String status = AiResult.STATUS_SUCCESS;
        String error = null;
        String content = null;
        try
        {
            // 0. 自定义模板覆盖：ai.prompt.<module>.<feature> 配置存在时优先生效
            //    模板含 {prompt} 占位符则嵌入业务原始提示词，否则整体替换
            prompt = applyTemplateOverride(module, feature, prompt);

            // 1. 开关校验：平台总开关 + 模块开关 + 功能开关
            if (!isEnabled(module, feature))
            {
                AiResult r = AiResult.degraded("AI功能未开启");
                audit(module, feature, AiResult.STATUS_DEGRADED, 0, prompt, "AI功能未开启");
                return r;
            }

            // 2. 限额校验
            if (isOverLimit())
            {
                AiResult r = AiResult.limited();
                audit(module, feature, AiResult.STATUS_LIMITED, 0, prompt, r.getError());
                return r;
            }

            // 3. 敏感信息脱敏（进入prompt前）
            String maskedPrompt = SensitiveDataMasker.mask(prompt);

            // 4. 调用LLM
            content = doChat(maskedPrompt, maxTokens);
            if (content == null)
            {
                status = AiResult.STATUS_DEGRADED;
                error = "AI调用失败";
                AiResult r = AiResult.degraded(error);
                audit(module, feature, status, System.currentTimeMillis() - start, maskedPrompt, error);
                return r;
            }

            // 5. 成功计数 + 审计
            increaseCount();
            long latency = System.currentTimeMillis() - start;
            audit(module, feature, AiResult.STATUS_SUCCESS, latency, maskedPrompt, null);
            return AiResult.ok(content, latency);
        }
        catch (Exception e)
        {
            log.error("AI调用异常 module={} feature={}", module, feature, e);
            status = AiResult.STATUS_FAILED;
            error = e.getMessage();
            AiResult r = AiResult.failed(error);
            audit(module, feature, status, System.currentTimeMillis() - start, prompt, abbreviate(error, 500));
            return r;
        }
    }

    /**
     * 应用自定义模板覆盖：读取 ai.prompt.<module>.<feature>，
     * 含 {prompt} 占位符则嵌入业务原始提示词（含业务数据），否则整体替换
     */
    private String applyTemplateOverride(String module, String feature, String prompt)
    {
        String tpl = getConfig("ai.prompt." + module + "." + feature);
        if (StringUtils.isEmpty(tpl) && "knowledge_qa".equals(feature))
        {
            // RAG问答module跟随文档所属模块，回退到注册键
            tpl = getConfig("ai.prompt.dms.knowledge_qa");
        }
        if (StringUtils.isEmpty(tpl))
        {
            return prompt;
        }
        return tpl.contains("{prompt}") ? tpl.replace("{prompt}", prompt) : tpl;
    }

    /**
     * 使用提示词模板调用（模板存sys_config：ai.prompt.<module>.<feature>，可热更新）
     * 模板中 {xxx} 占位符由params填充；模板未配置时使用defaultPrompt
     */
    public AiResult chatWithTemplate(String module, String feature, String defaultPrompt, java.util.Map<String, String> params)
    {
        String template = getConfig("ai.prompt." + module + "." + feature);
        if (StringUtils.isEmpty(template))
        {
            template = defaultPrompt;
        }
        if (params != null)
        {
            for (java.util.Map.Entry<String, String> e : params.entrySet())
            {
                template = template.replace("{" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
            }
        }
        return chat(module, feature, template);
    }

    /**
     * 连接测试（AI中心配置页用）：不走开关/限额，直接试连
     */
    public AiResult testConnection(String apiUrl, String apiKey, String modelName)
    {
        if (StringUtils.isEmpty(apiUrl) || StringUtils.isEmpty(apiKey))
        {
            return AiResult.failed("API地址或API Key未配置，请先填写完整");
        }
        if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://"))
        {
            return AiResult.failed("API地址格式不正确：必须以 http:// 或 https:// 开头");
        }

        long start = System.currentTimeMillis();
        try
        {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", StringUtils.isNotEmpty(modelName) ? modelName : "deepseek-chat");
            requestBody.put("temperature", 0);
            requestBody.put("max_tokens", 20);
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "连接测试，请只回复：OK");
            messages.add(message);
            requestBody.put("messages", messages);

            HttpResponse<String> response = send(apiUrl, apiKey, requestBody);
            long latency = System.currentTimeMillis() - start;

            if (response.statusCode() == 200)
            {
                JSONObject respJson = JSON.parseObject(response.body());
                JSONArray choices = respJson.getJSONArray("choices");
                if (choices == null || choices.isEmpty())
                {
                    return AiResult.failed("服务返回200但未包含模型回复，请确认该地址是聊天补全接口（/chat/completions）");
                }
                JSONObject msg = choices.getJSONObject(0).getJSONObject("message");
                String reply = msg != null ? msg.getString("content") : null;
                if (StringUtils.isEmpty(reply))
                {
                    String reasoning = msg != null ? msg.getString("reasoning_content") : null;
                    reply = StringUtils.isNotEmpty(reasoning)
                            ? "连接正常（推理模型：回答在思考链中生成，已省略）" : "连接正常，模型已响应";
                }
                AiResult r = AiResult.ok(reply, latency);
                return r;
            }
            return AiResult.failed(buildTestError(apiUrl, response.statusCode(), response.body()));
        }
        catch (java.net.http.HttpTimeoutException e)
        {
            return AiResult.failed("连接超时（45秒无响应）：请检查API地址是否可达、网络/代理是否正常；推理模型响应较慢时可稍后重试");
        }
        catch (java.net.UnknownHostException e)
        {
            return AiResult.failed("无法解析API地址中的域名（" + extractHost(apiUrl) + "）：请检查域名拼写是否正确");
        }
        catch (java.net.ConnectException e)
        {
            return AiResult.failed("无法连接到服务器：" + extractHost(apiUrl) + " 拒绝或无法建立连接，请检查网络和端口");
        }
        catch (Exception e)
        {
            log.warn("AI连接测试失败: {}", e.getMessage());
            return AiResult.failed("连接异常：" + abbreviate(e.getMessage(), 200));
        }
    }

    /**
     * 测试Embedding服务连通性：发送最小向量化请求，返回维度与耗时
     */
    public Map<String, Object> testEmbedding(String apiUrl, String apiKey, String model)
    {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(apiUrl) || StringUtils.isEmpty(model))
        {
            result.put("success", false);
            result.put("error", "Embedding API地址或模型名称未填写");
            return result;
        }
        if (StringUtils.isEmpty(apiKey))
        {
            // 未单独填写Key时沿用对话模型密钥（与运行时行为一致）
            apiKey = getSecretConfig("ai.api_key");
        }
        if (StringUtils.isEmpty(apiKey))
        {
            result.put("success", false);
            result.put("error", "Embedding API Key未填写（且对话模型密钥也未配置）");
            return result;
        }
        long start = System.currentTimeMillis();
        try
        {
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("input", "连接测试");
            HttpResponse<String> response = send(apiUrl, apiKey, body);
            long latency = System.currentTimeMillis() - start;
            result.put("latencyMs", latency);
            if (response.statusCode() == 200)
            {
                JSONObject resp = JSON.parseObject(response.body());
                JSONArray data = resp.getJSONArray("data");
                if (data != null && !data.isEmpty() && data.getJSONObject(0).getJSONArray("embedding") != null)
                {
                    result.put("success", true);
                    result.put("dims", data.getJSONObject(0).getJSONArray("embedding").size());
                    return result;
                }
                result.put("success", false);
                result.put("error", "服务返回200但未包含向量数据，请确认该地址是 /embeddings 向量化接口");
                return result;
            }
            if (response.statusCode() == 401 || response.statusCode() == 403)
            {
                result.put("success", false);
                result.put("error", "鉴权失败(" + statusCodeStr(response.statusCode()) + ")：Embedding Key无效或无权限");
                return result;
            }
            if (response.statusCode() == 404)
            {
                String hint = apiUrl.endsWith("/embeddings") ? "请核对地址与模型名称" : "地址一般以 /embeddings 结尾，当前地址缺少该路径";
                result.put("success", false);
                result.put("error", "Embedding接口不存在(404)：" + hint);
                return result;
            }
            if (response.statusCode() == 400)
            {
                result.put("success", false);
                result.put("error", "请求被拒绝(400)：通常是模型名称不正确，请核对服务商控制台");
                return result;
            }
            result.put("success", false);
            result.put("error", "服务返回异常状态(HTTP " + statusCodeStr(response.statusCode()) + ")");
            return result;
        }
        catch (java.net.http.HttpTimeoutException e)
        {
            result.put("success", false);
            result.put("error", "连接超时（45秒无响应），请检查网络或稍后重试");
            return result;
        }
        catch (java.net.UnknownHostException e)
        {
            result.put("success", false);
            result.put("error", "无法解析域名，请检查Embedding API地址拼写");
            return result;
        }
        catch (Exception e)
        {
            log.warn("Embedding连接测试失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "连接异常：" + abbreviate(e.getMessage(), 200));
            return result;
        }
    }

    private String statusCodeStr(int code) { return String.valueOf(code); }

    // ==================== 内部实现 ====================

    /** 实际调用LLM，返回content或null */
    private String doChat(String prompt, int maxTokens)
    {
        try
        {
            String apiUrl = getApiUrl();
            String apiKey = getSecretConfig("ai.api_key");
            String modelName = getConfig("ai.model_name");

            if (StringUtils.isEmpty(apiUrl) || StringUtils.isEmpty(apiKey))
            {
                log.warn("AI大模型API地址或Key未配置");
                return null;
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", StringUtils.isNotEmpty(modelName) ? modelName : "deepseek-chat");
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", maxTokens);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            // 提示词注入防护：分隔符内为数据非指令
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            HttpResponse<String> response = send(apiUrl, apiKey, requestBody);
            if (response.statusCode() == 200)
            {
                JSONObject respJson = JSON.parseObject(response.body());
                JSONArray choices = respJson.getJSONArray("choices");
                if (choices != null && !choices.isEmpty())
                {
                    return choices.getJSONObject(0).getJSONObject("message").getString("content");
                }
            }
            else
            {
                log.error("AI大模型API调用失败, HTTP {}: {}", response.statusCode(), response.body());
            }
        }
        catch (Exception e)
        {
            log.error("AI大模型API调用异常", e);
        }
        return null;
    }

    private HttpResponse<String> send(String apiUrl, String apiKey, JSONObject requestBody) throws Exception
    {
        Integer override = TIMEOUT_OVERRIDE.get();
        return send(apiUrl, apiKey, requestBody, override != null ? override : 45);
    }

    private HttpResponse<String> send(String apiUrl, String apiKey, JSONObject requestBody, int timeoutSeconds) throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** 平台总开关 + 模块开关 + 功能开关（三级） */
    public boolean isEnabled(String module, String feature)
    {
        if (!"true".equalsIgnoreCase(getConfig("ai.enabled")))
        {
            return false;
        }
        // 模块级开关（未配置视为开启，默认放行）
        if ("false".equalsIgnoreCase(getConfig("ai.module." + module + ".enabled")))
        {
            return false;
        }
        // 功能级开关（兼容旧键 dms.ai.<feature>）
        String featureVal = getConfigWithFallback("ai.feature." + module + "." + feature, "dms.ai." + feature);
        if (StringUtils.isNotEmpty(featureVal) && !"true".equalsIgnoreCase(featureVal))
        {
            return false;
        }
        return true;
    }

    /** 月度限额校验 */
    public boolean isOverLimit()
    {
        int monthlyLimit = parseLimit(getConfig("ai.monthly_limit"));
        if (monthlyLimit <= 0)
        {
            return false;
        }
        Integer used = redisCache.getCacheObject(countKey());
        return used != null && used >= monthlyLimit;
    }

    /** 当前月度已用次数 */
    public int getMonthlyUsed()
    {
        Integer used = redisCache.getCacheObject(countKey());
        return used == null ? 0 : used;
    }

    /** 月度限额值（0=不限） */
    public int getMonthlyLimit()
    {
        return parseLimit(getConfig("ai.monthly_limit"));
    }

    private void increaseCount()
    {
        int monthlyLimit = getMonthlyLimit();
        if (monthlyLimit > 0)
        {
            String key = countKey();
            Integer used = redisCache.getCacheObject(key);
            redisCache.setCacheObject(key, used == null ? 1 : used + 1, 45, TimeUnit.DAYS);
        }
    }

    private String countKey()
    {
        return CALL_COUNT_KEY + java.time.YearMonth.now();
    }

    private String getApiUrl()
    {
        String url = getConfig("ai.api_url");
        if (StringUtils.isNotEmpty(url) && !url.endsWith("/chat/completions"))
        {
            // 容错：用户只填到 /v1 时自动补全
            while (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            if (url.endsWith("/v1"))
            {
                url = url + "/chat/completions";
            }
        }
        return url;
    }

    /** 读配置，新键优先，未配置时回退旧键（dms.ai.*迁移兼容） */
    private String getConfigWithFallback(String newKey, String oldKey)
    {
        String val = getConfig(newKey);
        return StringUtils.isNotEmpty(val) ? val : getConfig(oldKey);
    }

    /** 读敏感配置（API Key等）：密文自动解密（ENC:前缀），明文原样返回（存量兼容） */
    private String getSecretConfig(String key)
    {
        return com.ruoyi.common.utils.SecureKeyUtils.decrypt(getConfig(key));
    }

    /** 启动完成后自动迁移：将存量明文密钥加密为 ENC: 密文落库（幂等）；同时把早期默认密钥加密的密文用当前配置密钥重加密统一 */
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void migratePlainKeysToEncrypted()
    {
        try
        {
            String[] secretKeys = { "ai.api_key", "ai.embedding_key" };
            for (String key : secretKeys)
            {
                String raw = getConfig(key);
                if (StringUtils.isEmpty(raw))
                {
                    continue;
                }
                if (!com.ruoyi.common.utils.SecureKeyUtils.isEncrypted(raw))
                {
                    configService.updateConfigByKeyForced(key, com.ruoyi.common.utils.SecureKeyUtils.encrypt(raw));
                    log.info("AI密钥已自动加密存储: {}", key);
                }
                else
                {
                    // 密文但非当前配置密钥加密（迁移期默认密钥产物）：解密后用当前密钥重加密
                    String plain = com.ruoyi.common.utils.SecureKeyUtils.decrypt(raw);
                    if (plain != null && !plain.startsWith(com.ruoyi.common.utils.SecureKeyUtils.CIPHER_PREFIX))
                    {
                        configService.updateConfigByKeyForced(key, com.ruoyi.common.utils.SecureKeyUtils.encrypt(plain));
                        log.info("AI密钥已统一为当前配置密钥加密: {}", key);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.warn("AI密钥加密迁移失败（不影响启动，将在下次保存时加密）: {}", e.getMessage());
        }
    }

    private String getConfig(String key)
    {
        String val = configService.selectConfigByKey(key);
        return val != null ? val.trim() : "";
    }

    private int parseLimit(String val)
    {
        try
        {
            return StringUtils.isEmpty(val) ? 0 : Integer.parseInt(val);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /** 审计落库（异步容错：审计失败不影响业务） */
    private void audit(String module, String feature, String status, long latencyMs, String prompt, String error)
    {
        try
        {
            AiCallLog logEntry = new AiCallLog();
            logEntry.setModule(module);
            logEntry.setFeature(feature);
            logEntry.setStatus(status);
            logEntry.setDurationMs((int) latencyMs);
            logEntry.setPromptDigest(abbreviateKeepTail(prompt, 500));
            logEntry.setErrorMsg(abbreviate(error, 500));
            auditService.insertLog(logEntry);
        }
        catch (Exception e)
        {
            log.warn("AI审计落库失败: {}", e.getMessage());
        }
    }

    /** 按HTTP状态码生成可读的测试失败原因与修复建议 */
    private String buildTestError(String apiUrl, int statusCode, String body)
    {
        String apiMsg = "";
        try
        {
            JSONObject respJson = JSON.parseObject(body);
            if (respJson != null && respJson.getJSONObject("error") != null)
            {
                apiMsg = StringUtils.defaultString(respJson.getJSONObject("error").getString("message"));
            }
        }
        catch (Exception ignore) {}

        StringBuilder sb = new StringBuilder();
        switch (statusCode)
        {
            case 404:
                sb.append("API地址不存在(404)：地址路径不完整或有误。");
                if (apiUrl.endsWith("/v1") || apiUrl.endsWith("/v1/") || apiUrl.endsWith("/"))
                {
                    String u = apiUrl;
                    while (u.endsWith("/")) u = u.substring(0, u.length() - 1);
                    sb.append("当前地址缺少补全路径，一般应形如：").append(u).append("/chat/completions。");
                }
                else
                {
                    sb.append("请到模型服务商文档核对完整的聊天接口地址。");
                }
                break;
            case 401:
            case 403:
                sb.append("鉴权失败(").append(statusCode).append(")：API Key无效、已过期或无权访问该模型，请重新获取并粘贴完整Key。");
                break;
            case 429:
                sb.append("调用受限(429)：余额不足或请求频率超限，请检查服务商账户余额与配额。");
                break;
            case 400:
                sb.append("请求被拒绝(400)：通常是模型名称不正确，请核对服务商控制台的模型名称。");
                break;
            default:
                sb.append("服务返回异常状态(HTTP ").append(statusCode).append(")，请确认API地址是否为服务商官方聊天接口地址。");
        }
        if (StringUtils.isNotEmpty(apiMsg))
        {
            sb.append(" 服务商说明：").append(abbreviate(apiMsg, 120));
        }
        else if (StringUtils.isNotEmpty(body) && (body.contains("<html") || body.contains("<HTML")))
        {
            sb.append("（该地址返回的是网页而非API响应，通常是地址错误或被网关拦截）");
        }
        return sb.toString();
    }

    private String extractHost(String url)
    {
        try
        {
            return URI.create(url).getHost();
        }
        catch (Exception e)
        {
            return url;
        }
    }

    private String abbreviate(String text, int maxLen)
    {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    /** 审计摘要：超长时把尾部（用户问题所在）置顶展示，后接省略号与提示词头部摘要，确保用户问题第一眼可见 */
    private String abbreviateKeepTail(String text, int maxLen)
    {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        // 固定前缀与分隔符共约30字符，需从预算中扣除，确保总长不超maxLen（数据库varchar上限）
        final String head = "[用户问题]";
        final String mid = " ...[提示词开头]";
        int budget = maxLen - head.length() - mid.length();
        int tailLen = Math.min(200, budget / 2);
        int headLen = budget - tailLen;
        return head + text.substring(text.length() - tailLen) + mid + text.substring(0, headLen);
    }
}
