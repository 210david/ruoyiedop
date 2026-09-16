package com.ruoyi.ai.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.domain.AiCallLog;
import com.ruoyi.ai.service.IAiAuditService;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI中心 Controller
 * 配置管理（含连接测试）+ 用量统计看板 + 调用审计日志查询
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/center")
public class AiCenterController extends AiBaseConfigController
{
    @Autowired
    private IAiAuditService auditService;

    @Autowired
    private AiClient aiClient;

    // ==================== AI配置 ====================

    /**
     * 获取AI配置
     */
    @PreAuthorize("@ss.hasPermi('ai:center:config')")
    @GetMapping("/config")
    public AjaxResult getConfig()
    {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", getConfigBool(mergeKey("ai.enabled")));
        config.put("apiUrl", getConfigStr(mergeKey("ai.api_url")));
        // API Key 保存后不允许查看：不返回任何明文/掩码，仅返回是否已配置
        String apiKey = getConfigStr(mergeKey("ai.api_key"));
        config.put("apiKey", "");
        config.put("apiKeyConfigured", StringUtils.isNotEmpty(apiKey));
        config.put("modelName", getConfigStr(mergeKey("ai.model_name")));
        config.put("monthlyLimit", getConfigStr(mergeKey("ai.monthly_limit")));
        config.put("monthlyUsed", aiClient.getMonthlyUsed());
        // RAG向量检索（可选）：与对话模型解耦，未配置时知识库降级关键词检索
        config.put("embeddingUrl", getConfigStr("ai.embedding_url"));
        String embeddingKey = getConfigStr("ai.embedding_key");
        config.put("embeddingKey", "");
        config.put("embeddingKeyConfigured", StringUtils.isNotEmpty(embeddingKey));
        config.put("embeddingModel", getConfigStr("ai.embedding_model"));
        return AjaxResult.success(config);
    }

    /**
     * 更新AI配置
     */
    @PreAuthorize("@ss.hasPermi('ai:center:config')")
    @PutMapping("/config")
    public AjaxResult updateConfig(@RequestBody Map<String, Object> config)
    {
        String apiUrl = toString(config.get("apiUrl"));
        if (StringUtils.isNotEmpty(apiUrl) && !apiUrl.startsWith("http://") && !apiUrl.startsWith("https://"))
        {
            return AjaxResult.error("API地址必须以 http:// 或 https:// 开头");
        }
        updateConfigByKey("ai.enabled", toString(config.get("enabled")));
        updateConfigByKey("ai.api_url", apiUrl);
        // API Key 保护：空值不覆盖已有密钥
        String apiKey = toString(config.get("apiKey"));
        if (StringUtils.isNotEmpty(apiKey))
        {
            updateConfigByKey("ai.api_key", apiKey);
        }
        updateConfigByKey("ai.model_name", toString(config.get("modelName")));
        updateConfigByKey("ai.monthly_limit", toString(config.get("monthlyLimit")));
        // RAG向量检索（可选）：Key为空时沿用已保存值
        updateConfigByKey("ai.embedding_url", toString(config.get("embeddingUrl")));
        String embeddingKey = toString(config.get("embeddingKey"));
        if (StringUtils.isNotEmpty(embeddingKey))
        {
            updateConfigByKey("ai.embedding_key", embeddingKey);
        }
        updateConfigByKey("ai.embedding_model", toString(config.get("embeddingModel")));
        return AjaxResult.success("配置保存成功");
    }

    /**
     * 测试AI大模型连接
     * apiKey为空时自动使用已保存的密钥（Key保存后不可查看）
     */
    @PreAuthorize("@ss.hasPermi('ai:center:config')")
    @PostMapping("/config/test")
    public AjaxResult testConfig(@RequestBody Map<String, Object> body)
    {
        String apiUrl = toString(body.get("apiUrl"));
        if (StringUtils.isEmpty(apiUrl))
        {
            return AjaxResult.error("请先填写大模型API地址");
        }
        String apiKey = toString(body.get("apiKey"));
        if (StringUtils.isEmpty(apiKey))
        {
            apiKey = getConfigStr(mergeKey("ai.api_key"));
        }
        String modelName = toString(body.get("modelName"));
        AiResult result = aiClient.testConnection(apiUrl, apiKey, modelName);
        return AjaxResult.success(result.toMap());
    }

    /**
     * 保存RAG向量检索配置（分区保存，仅更新embedding三项，不影响大模型配置）
     */
    @PreAuthorize("@ss.hasPermi('ai:center:config')")
    @PutMapping("/config/embedding")
    public AjaxResult updateEmbeddingConfig(@RequestBody Map<String, Object> config)
    {
        String embeddingUrl = toString(config.get("embeddingUrl"));
        if (StringUtils.isNotEmpty(embeddingUrl) && !embeddingUrl.startsWith("http://") && !embeddingUrl.startsWith("https://"))
        {
            return AjaxResult.error("Embedding API地址必须以 http:// 或 https:// 开头");
        }
        updateConfigByKey("ai.embedding_url", embeddingUrl);
        String embeddingKey = toString(config.get("embeddingKey"));
        if (StringUtils.isNotEmpty(embeddingKey))
        {
            updateConfigByKey("ai.embedding_key", embeddingKey);
        }
        updateConfigByKey("ai.embedding_model", toString(config.get("embeddingModel")));
        return AjaxResult.success("RAG配置保存成功，即时生效");
    }

    /**
     * 测试Embedding服务连通性（apiKey留空时自动沿用已保存密钥）
     */
    @PreAuthorize("@ss.hasPermi('ai:center:config')")
    @PostMapping("/config/testEmbedding")
    public AjaxResult testEmbedding(@RequestBody Map<String, Object> body)
    {
        String url = toString(body.get("embeddingUrl"));
        if (StringUtils.isEmpty(url))
        {
            return AjaxResult.error("请先填写Embedding API地址");
        }
        String key = toString(body.get("embeddingKey"));
        if (StringUtils.isEmpty(key))
        {
            key = getConfigStr("ai.embedding_key");
        }
        String model = toString(body.get("embeddingModel"));
        return AjaxResult.success(aiClient.testEmbedding(url, key, model));
    }

    // ==================== 用量统计 ====================

    /**
     * 用量统计看板（默认近30天）
     */
    @PreAuthorize("@ss.hasPermi('ai:center:dashboard')")
    @GetMapping("/usage")
    public AjaxResult usage(@RequestParam(defaultValue = "30") int days)
    {
        Map<String, Object> stat = auditService.selectUsageStat(days);
        stat.put("monthlyUsed", aiClient.getMonthlyUsed());
        stat.put("monthlyLimit", aiClient.getMonthlyLimit());
        return AjaxResult.success(stat);
    }

    // ==================== 调用审计日志 ====================

    /**
     * 查询AI调用审计日志列表
     */
    @PreAuthorize("@ss.hasPermi('ai:center:log:list')")
    @GetMapping("/log/list")
    public TableDataInfo logList(AiCallLog log)
    {
        startPage();
        java.util.List<AiCallLog> list = auditService.selectLogList(log);
        return getDataTable(list);
    }

    /**
     * 按状态统计日志数量（状态标签栏数量徽章用）
     */
    @PreAuthorize("@ss.hasPermi('ai:center:log:list')")
    @GetMapping("/log/statusCounts")
    public AjaxResult logStatusCounts()
    {
        return AjaxResult.success(auditService.selectStatusCounts());
    }
}
