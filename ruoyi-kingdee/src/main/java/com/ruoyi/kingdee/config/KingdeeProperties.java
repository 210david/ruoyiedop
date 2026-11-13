package com.ruoyi.kingdee.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 金蝶云星辰对接配置
 * <p>
 * 参考文档：open.jdy.com 开发指南
 * 鉴权双阶段：先获取 app-token，再带签名调用业务接口
 *
 * @author ruoyi
 */
@ConfigurationProperties(prefix = "kingdee")
public class KingdeeProperties
{
    /** 是否启用金蝶对接 */
    private boolean enabled = false;

    /** API 根地址（固定）— 业务接口真实域名 apikingdee.com，不是 api.kingdee.com */
    private String baseUrl = "https://apikingdee.com/jdy";

    /** 应用ID，创建应用时获得 */
    private String clientId;

    /** 签名密钥（ClientSecret），创建应用时获得，固定不变，用于 X-Api-Signature header */
    private String clientSecret;

    /** 授权Key，用于获取 app-token */
    private String appKey;

    /** 授权密钥（AppSecret），24h 刷新，仅用于计算 app_signature URL 参数 */
    private String appSecret;

    /** 账套所属 IDC 域名（实时授权返回的 domain 字段，如 https://tf.jdy.com/） */
    private String routerAddr;

    /** HTTP 超时（毫秒） */
    private int timeout = 30000;

    /** 接口调用失败重试次数（不含首次） */
    private int retryTimes = 2;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getRouterAddr() { return routerAddr; }
    public void setRouterAddr(String routerAddr) { this.routerAddr = routerAddr; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public int getRetryTimes() { return retryTimes; }
    public void setRetryTimes(int retryTimes) { this.retryTimes = retryTimes; }
}
