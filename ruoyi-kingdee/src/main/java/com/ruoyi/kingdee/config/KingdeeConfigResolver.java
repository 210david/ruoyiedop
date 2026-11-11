package com.ruoyi.kingdee.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SecureKeyUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 金蝶云星辰 配置解析器
 * <p>
 * 配置优先级（从高到低）：
 * 1. 数据库 sys_config 表（key 前缀 kingdee.*）
 * 2. application.yml（KingdeeProperties）默认值
 * <p>
 * 这样前端页面保存后立即生效，不需要重启服务。
 *
 * @author ruoyi
 */
@Component
public class KingdeeConfigResolver
{
    // sys_config key
    public static final String K_ENABLED      = "kingdee.enabled";
    public static final String K_BASE_URL     = "kingdee.base_url";
    public static final String K_CLIENT_ID    = "kingdee.client_id";
    public static final String K_CLIENT_SECRET = "kingdee.client_secret"; // 固定签名密钥（加密存）
    public static final String K_APP_KEY      = "kingdee.app_key";
    public static final String K_APP_SECRET   = "kingdee.app_secret";    // 24h 刷新密钥（加密存）
    public static final String K_ROUTER_ADDR  = "kingdee.router_addr";
    public static final String K_TIMEOUT      = "kingdee.timeout";
    public static final String K_RETRY        = "kingdee.retry_times";
    public static final String K_SYNC_CRON    = "kingdee.sync_cron";

    private static final String[] SECRET_KEYS = { K_CLIENT_SECRET, K_APP_SECRET };

    private final KingdeeProperties props;
    private final ISysConfigService configService;

    public KingdeeConfigResolver(KingdeeProperties props, ISysConfigService configService)
    {
        this.props = props;
        this.configService = configService;
    }

    // ==================== 公开读取接口 ====================

    public boolean isEnabled()
    {
        String v = getDb(K_ENABLED);
        return StringUtils.isNotEmpty(v) ? "true".equalsIgnoreCase(v) : props.isEnabled();
    }

    public String getBaseUrl()
    {
        return firstNonEmpty(getDb(K_BASE_URL), props.getBaseUrl());
    }

    public String getClientId()
    {
        return firstNonEmpty(getDb(K_CLIENT_ID), props.getClientId());
    }

    /** ClientSecret — 固定签名密钥（创建应用时获得），自动解密 */
    public String getClientSecret()
    {
        String dbVal = getDb(K_CLIENT_SECRET);
        if (StringUtils.isNotEmpty(dbVal))
        {
            try { return SecureKeyUtils.decrypt(dbVal); }
            catch (Exception e) { return dbVal; }
        }
        return props.getClientSecret();
    }

    public String getAppKey()
    {
        return firstNonEmpty(getDb(K_APP_KEY), props.getAppKey());
    }

    /** app_secret 自动解密返回明文 */
    public String getAppSecret()
    {
        String dbVal = getDb(K_APP_SECRET);
        if (StringUtils.isNotEmpty(dbVal))
        {
            // 存库时加密过，这里解密
            try { return SecureKeyUtils.decrypt(dbVal); }
            catch (Exception e) { return dbVal; } // 兼容未加密的老数据
        }
        return props.getAppSecret();
    }

    public String getRouterAddr()
    {
        return firstNonEmpty(getDb(K_ROUTER_ADDR), props.getRouterAddr());
    }

    public int getTimeout()
    {
        String v = getDb(K_TIMEOUT);
        return StringUtils.isNotEmpty(v) ? parseInt(v, props.getTimeout()) : props.getTimeout();
    }

    public int getRetryTimes()
    {
        String v = getDb(K_RETRY);
        return StringUtils.isNotEmpty(v) ? parseInt(v, props.getRetryTimes()) : props.getRetryTimes();
    }

    /**
     * 组装完整配置快照（给前端回显 + 给 HttpClient 用）
     */
    public Map<String, Object> snapshot(boolean maskSecret)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("enabled", isEnabled());
        m.put("baseUrl", getBaseUrl());
        m.put("clientId", getClientId());
        String cs = getClientSecret();
        m.put("clientSecret", maskSecret ? mask(cs) : cs);
        m.put("clientSecretConfigured", StringUtils.isNotEmpty(cs));
        m.put("appKey", getAppKey());
        String secret = getAppSecret();
        m.put("appSecret", maskSecret ? mask(secret) : secret);
        m.put("appSecretConfigured", StringUtils.isNotEmpty(getAppSecret()));
        m.put("routerAddr", getRouterAddr());
        m.put("timeout", getTimeout());
        m.put("retryTimes", getRetryTimes());
        return m;
    }

    // ==================== 内部工具 ====================

    private String getDb(String key)
    {
        try { return configService.selectConfigByKey(key); }
        catch (Exception e) { return null; }
    }

    private static String firstNonEmpty(String... candidates)
    {
        for (String c : candidates)
        {
            if (StringUtils.isNotEmpty(c)) return c;
        }
        return null;
    }

    private static int parseInt(String s, int def)
    {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private static String mask(String s)
    {
        if (s == null || s.length() <= 4) return "****";
        return s.substring(0, 2) + "****" + s.substring(s.length() - 2);
    }

    public static boolean isSecretKey(String key)
    {
        for (String s : SECRET_KEYS) if (s.equals(key)) return true;
        return false;
    }

    /** 将配置写入 sys_config 表（不存在则新增，存在则更新），敏感值自动加密 */
    public void save(String key, String value, String operator)
    {
        if (isSecretKey(key) && StringUtils.isNotEmpty(value))
        {
            value = SecureKeyUtils.encrypt(value);
        }
        updateOrInsert(key, value, operator);
    }

    private void updateOrInsert(String key, String value, String operator)
    {
        SysConfig query = new SysConfig();
        query.setConfigKey(key);
        List<SysConfig> list = configService.selectConfigList(query);
        SysConfig target = null;
        if (list != null)
        {
            for (SysConfig c : list)
            {
                if (key.equals(c.getConfigKey())) { target = c; break; }
            }
        }
        if (target != null)
        {
            target.setConfigValue(value);
            target.setUpdateBy(operator);
            configService.updateConfig(target);
        }
        else if (StringUtils.isNotEmpty(value))
        {
            SysConfig insert = new SysConfig();
            insert.setConfigName(defaultName(key));
            insert.setConfigKey(key);
            insert.setConfigValue(value);
            insert.setConfigType("Y");
            insert.setCreateBy(operator);
            configService.insertConfig(insert);
        }
    }

    private static String defaultName(String key)
    {
        switch (key)
        {
            case K_ENABLED:     return "金蝶-功能开关";
            case K_BASE_URL:    return "金蝶-API根地址";
            case K_CLIENT_ID:    return "金蝶-应用ID";
            case K_CLIENT_SECRET: return "金蝶-签名密钥(ClientSecret)";
            case K_APP_KEY:      return "金蝶-授权Key";
            case K_APP_SECRET:   return "金蝶-授权密钥(AppSecret)";
            case K_ROUTER_ADDR: return "金蝶-路由地址";
            case K_TIMEOUT:     return "金蝶-超时(ms)";
            case K_RETRY:       return "金蝶-重试次数";
            default: return "金蝶配置-" + key;
        }
    }
}
