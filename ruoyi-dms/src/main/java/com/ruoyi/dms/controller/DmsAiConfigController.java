package com.ruoyi.dms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;

/**
 * AI配置管理
 * 读写 sys_config 表中 dms.ai.* 系列参数
 */
@RestController
@RequestMapping("/dms/ai/config")
public class DmsAiConfigController extends BaseController
{
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private com.ruoyi.ai.core.AiClient aiClient;

    /** 获取AI配置 */
    @PreAuthorize("@ss.hasPermi('dms:ai:config')")
    @GetMapping
    public AjaxResult getConfig()
    {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", getConfigBool("dms.ai.enabled"));
        config.put("apiUrl", getConfigStr("dms.ai.api_url"));
        // API Key 保存后不允许查看：不返回任何明文/掩码，仅返回是否已配置
        String apiKey = getConfigStr("dms.ai.api_key");
        config.put("apiKey", "");
        config.put("apiKeyConfigured", StringUtils.isNotEmpty(apiKey));
        config.put("modelName", getConfigStr("dms.ai.model_name"));
        config.put("repairAssistant", getConfigBool("dms.ai.repair_assistant"));
        config.put("dispatchRecommend", getConfigBool("dms.ai.dispatch_recommend"));
        config.put("knowledgeQa", getConfigBool("dms.ai.knowledge_qa"));
        config.put("monthlyLimit", getConfigStr("dms.ai.monthly_limit"));
        return AjaxResult.success(config);
    }

    /**
     * 测试AI大模型连接
     * 前端传入当前表单的 apiUrl/apiKey/modelName，支持保存前先验证
     * apiKey为空或为脱敏掩码时自动使用已保存的密钥
     */
    @PreAuthorize("@ss.hasPermi('dms:ai:config')")
    @PostMapping("/test")
    public AjaxResult testConfig(@RequestBody Map<String, Object> body)
    {
        String apiUrl = toString(body.get("apiUrl"));
        if (StringUtils.isEmpty(apiUrl))
        {
            return AjaxResult.error("请先填写大模型API地址");
        }
        String apiKey = toString(body.get("apiKey"));
        if (StringUtils.isEmpty(apiKey) || apiKey.contains("****"))
        {
            // 掩码或未填写时回退到已保存密钥
            apiKey = getConfigStr("dms.ai.api_key");
        }
        String modelName = toString(body.get("modelName"));
        com.ruoyi.ai.core.AiResult result = aiClient.testConnection(apiUrl, apiKey, modelName);
        return AjaxResult.success(result.toMap());
    }

    /** 更新AI配置 */
    @PreAuthorize("@ss.hasPermi('dms:ai:config')")
    @PutMapping
    public AjaxResult updateConfig(@RequestBody Map<String, Object> config)
    {
        // API地址格式校验
        String apiUrl = toString(config.get("apiUrl"));
        if (StringUtils.isNotEmpty(apiUrl) && !apiUrl.startsWith("http://") && !apiUrl.startsWith("https://"))
        {
            return AjaxResult.error("API地址必须以 http:// 或 https:// 开头");
        }

        updateConfigByKey("dms.ai.enabled", toString(config.get("enabled")));
        updateConfigByKey("dms.ai.api_url", apiUrl);
        // API Key 保护：空值或脱敏掩码不覆盖已有密钥
        String apiKey = toString(config.get("apiKey"));
        if (StringUtils.isNotEmpty(apiKey) && !apiKey.contains("****"))
        {
            updateConfigByKey("dms.ai.api_key", apiKey);
        }
        updateConfigByKey("dms.ai.model_name", toString(config.get("modelName")));
        updateConfigByKey("dms.ai.repair_assistant", toString(config.get("repairAssistant")));
        updateConfigByKey("dms.ai.dispatch_recommend", toString(config.get("dispatchRecommend")));
        updateConfigByKey("dms.ai.knowledge_qa", toString(config.get("knowledgeQa")));
        updateConfigByKey("dms.ai.monthly_limit", toString(config.get("monthlyLimit")));
        return AjaxResult.success("配置保存成功");
    }

    private String getConfigStr(String key)
    {
        String val = configService.selectConfigByKey(key);
        return val != null ? val : "";
    }

    private boolean getConfigBool(String key)
    {
        return "true".equalsIgnoreCase(configService.selectConfigByKey(key));
    }

    /**
     * 按configKey更新配置值
     * 先查出完整记录获取configId，再调用updateConfig
     */
    private void updateConfigByKey(String key, String value)
    {
        SysConfig query = new SysConfig();
        query.setConfigKey(key);
        List<SysConfig> list = configService.selectConfigList(query);
        if (list != null && !list.isEmpty())
        {
            SysConfig update = list.get(0);
            update.setConfigValue(value);
            update.setUpdateBy(getUsername());
            configService.updateConfig(update);
        }
        else if (StringUtils.isNotEmpty(value))
        {
            // 配置项缺失时自动补建，避免保存静默丢失
            SysConfig insert = new SysConfig();
            insert.setConfigName(defaultConfigName(key));
            insert.setConfigKey(key);
            insert.setConfigValue(value);
            insert.setConfigType("Y");
            insert.setCreateBy(getUsername());
            configService.insertConfig(insert);
        }
    }

    /** 缺失配置项补建时的默认参数名 */
    private String defaultConfigName(String key)
    {
        switch (key)
        {
            case "dms.ai.enabled": return "AI功能总开关";
            case "dms.ai.api_url": return "AI大模型API地址";
            case "dms.ai.api_key": return "AI大模型API密钥";
            case "dms.ai.model_name": return "AI大模型名称";
            case "dms.ai.repair_assistant": return "智能报修助手开关";
            case "dms.ai.dispatch_recommend": return "智能派工推荐开关";
            case "dms.ai.knowledge_qa": return "维修知识问答开关";
            case "dms.ai.monthly_limit": return "AI月度调用上限";
            default: return "AI配置-" + key;
        }
    }

    /** API Key 脱敏：保留前4位，其余以****代替 */
    private String maskKey(String key)
    {
        if (key.length() <= 4) return "****";
        return key.substring(0, 4) + "****";
    }

    private String toString(Object obj)
    {
        if (obj == null) return "";
        if (obj instanceof Boolean) return ((Boolean) obj) ? "true" : "false";
        return obj.toString();
    }
}
