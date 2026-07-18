package com.ruoyi.dms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
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

    /** 获取AI配置 */
    @PreAuthorize("@ss.hasPermi('dms:ai:config')")
    @GetMapping
    public AjaxResult getConfig()
    {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", getConfigBool("dms.ai.enabled"));
        config.put("apiUrl", getConfigStr("dms.ai.api_url"));
        config.put("apiKey", getConfigStr("dms.ai.api_key"));
        config.put("modelName", getConfigStr("dms.ai.model_name"));
        config.put("repairAssistant", getConfigBool("dms.ai.repair_assistant"));
        config.put("dispatchRecommend", getConfigBool("dms.ai.dispatch_recommend"));
        config.put("knowledgeQa", getConfigBool("dms.ai.knowledge_qa"));
        config.put("monthlyLimit", getConfigStr("dms.ai.monthly_limit"));
        return AjaxResult.success(config);
    }

    /** 更新AI配置 */
    @PreAuthorize("@ss.hasPermi('dms:ai:config')")
    @PutMapping
    public AjaxResult updateConfig(@RequestBody Map<String, Object> config)
    {
        updateConfigByKey("dms.ai.enabled", toString(config.get("enabled")));
        updateConfigByKey("dms.ai.api_url", toString(config.get("apiUrl")));
        updateConfigByKey("dms.ai.api_key", toString(config.get("apiKey")));
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
    }

    private String toString(Object obj)
    {
        if (obj == null) return "";
        if (obj instanceof Boolean) return ((Boolean) obj) ? "true" : "false";
        return obj.toString();
    }
}
