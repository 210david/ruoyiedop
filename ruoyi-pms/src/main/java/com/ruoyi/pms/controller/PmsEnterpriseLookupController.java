package com.ruoyi.pms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.pms.domain.dto.PmsEnterpriseInfoDTO;
import com.ruoyi.pms.service.IPmsEnterpriseLookupService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 企业信息查询配置 Controller
 *
 * 管理企业信息查询API的配置（启用状态、API提供商、API地址、API密钥）。
 * 配置存储在sys_config表中，可在前端页面动态修改，无需重启。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/enterprise")
public class PmsEnterpriseLookupController extends BaseController
{
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IPmsEnterpriseLookupService enterpriseLookupService;

    // ===== 配置键 =====
    private static final String KEY_ENABLED   = "pms.enterprise.lookup.enabled";
    private static final String KEY_PROVIDER  = "pms.enterprise.lookup.provider";
    private static final String KEY_API_URL   = "pms.enterprise.lookup.api.url";
    private static final String KEY_API_KEY   = "pms.enterprise.lookup.api.key";
    private static final String KEY_API_SECRET = "pms.enterprise.lookup.api.secret";

    /**
     * 获取当前配置
     */
    @PreAuthorize("@ss.hasPermi('pms:enterprise:config')")
    @GetMapping("/config")
    public AjaxResult getConfig()
    {
        Map<String, String> config = new HashMap<>();
        config.put("enabled", configService.selectConfigByKey(KEY_ENABLED));
        config.put("provider", configService.selectConfigByKey(KEY_PROVIDER));
        config.put("apiUrl", configService.selectConfigByKey(KEY_API_URL));
        config.put("apiKey", configService.selectConfigByKey(KEY_API_KEY));
        config.put("apiSecret", configService.selectConfigByKey(KEY_API_SECRET));
        return AjaxResult.success(config);
    }

    /**
     * 保存配置
     */
    @PreAuthorize("@ss.hasPermi('pms:enterprise:config')")
    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody Map<String, String> params)
    {
        upsertConfig(KEY_ENABLED,   "是否启用企业信息查询", params.get("enabled"));
        upsertConfig(KEY_PROVIDER,  "企业查询API提供商",   params.get("provider"));
        upsertConfig(KEY_API_URL,   "企业查询API地址",     params.get("apiUrl"));
        upsertConfig(KEY_API_KEY,   "企业查询API密钥",     params.get("apiKey"));
        upsertConfig(KEY_API_SECRET,"企业查询API Secret",  params.get("apiSecret"));
        // 清除配置缓存使新值立即生效
        configService.clearConfigCache();
        configService.loadingConfigCache();
        return AjaxResult.success();
    }

    /**
     * 查询企业信息（供应商表单中的「查询企业」按钮调用）
     */
    @PreAuthorize("@ss.hasPermi('wms:supplier:add')")
    @GetMapping("/lookup")
    public AjaxResult lookup(@RequestParam String keyword)
    {
        PmsEnterpriseInfoDTO info = enterpriseLookupService.lookup(keyword);
        return AjaxResult.success(info);
    }

    /**
     * 测试查询（配置页面中的「测试」按钮调用）
     */
    @PreAuthorize("@ss.hasPermi('pms:enterprise:config')")
    @GetMapping("/test")
    public AjaxResult test(@RequestParam String keyword)
    {
        PmsEnterpriseInfoDTO info = enterpriseLookupService.lookup(keyword);
        return AjaxResult.success(info);
    }

    // ===== 工具方法 =====

    /**
     * 根据key更新或插入配置
     */
    private void upsertConfig(String configKey, String configName, String configValue)
    {
        // 查找现有配置
        SysConfig query = new SysConfig();
        query.setConfigKey(configKey);
        List<SysConfig> list = configService.selectConfigList(query);

        if (list != null && !list.isEmpty())
        {
            // 更新
            SysConfig config = list.get(0);
            config.setConfigValue(configValue);
            config.setUpdateBy(getUsername());
            configService.updateConfig(config);
        }
        else
        {
            // 新增
            SysConfig config = new SysConfig();
            config.setConfigName(configName);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigType("Y");
            config.setCreateBy(getUsername());
            configService.insertConfig(config);
        }
    }
}
