package com.ruoyi.ai.controller;

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
import com.ruoyi.common.utils.StringUtils;

/**
 * AI中心基础支撑：配置读写辅助Controller
 * 供AiCenterController与提示词模板管理共用的配置操作（内部辅助方法）
 *
 * @author ruoyi
 */
public abstract class AiBaseConfigController extends BaseController
{
    @Autowired
    protected ISysConfigService configService;

    /** 读配置，空返回"" */
    protected String getConfigStr(String key)
    {
        String val = configService.selectConfigByKey(key);
        return val != null ? val : "";
    }

    protected boolean getConfigBool(String key)
    {
        return "true".equalsIgnoreCase(configService.selectConfigByKey(key));
    }

    /** 新键优先读，为空回退旧键（dms.ai.* 迁移兼容） */
    protected String mergeKey(String newKey)
    {
        if (configService.selectConfigByKey(newKey) == null
                || StringUtils.isEmpty(configService.selectConfigByKey(newKey)))
        {
            String oldKey = "dms." + newKey;
            String oldVal = configService.selectConfigByKey(oldKey);
            if (StringUtils.isNotEmpty(oldVal))
            {
                return oldKey;
            }
        }
        return newKey;
    }

    /** 按configKey更新配置值 */
    protected void updateConfigByKey(String key, String value)
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
    protected String defaultConfigName(String key)
    {
        switch (key)
        {
            case "ai.enabled": return "AI功能总开关";
            case "ai.api_url": return "AI大模型API地址";
            case "ai.api_key": return "AI大模型API密钥";
            case "ai.model_name": return "AI大模型名称";
            case "ai.monthly_limit": return "AI月度调用上限";
            default: return "AI配置-" + key;
        }
    }

    protected String toString(Object obj)
    {
        if (obj == null) return "";
        if (obj instanceof Boolean) return ((Boolean) obj) ? "true" : "false";
        return obj.toString();
    }
}
