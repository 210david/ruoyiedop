package com.kingdee.apigw;

/**
 * 金蝶 API 网关客户端配置（与官方 SDK 类名完全一致）
 * <p>
 * 金蝶有两个独立密钥，千万别搞混：
 * <ul>
 *   <li><b>clientSecret</b> — 创建应用时获得，固定不变，用于 X-Api-Signature header 签名（所有接口）</li>
 *   <li><b>appSecret</b> — 授权后获得，每 24h 刷新，仅用于计算 app_signature URL 参数（换 token 接口）</li>
 * </ul>
 */
public class ApigwConfig
{
    /** 应用 ClientID（必填） */
    private String clientID;

    /** 应用 ClientSecret — 签名密钥（固定不变，创建应用时获得） */
    private String clientSecret;

    /** 授权 AppKey（必填，换 app-token 用） */
    private String appKey;

    /** 授权 AppSecret — app_signature URL 参数密钥（24h 刷新，授权后获得） */
    private String appSecret;

    /** 业务域名，默认 https://apikingdee.com */
    private String baseDomain = "https://apikingdee.com";

    /** 业务根路径，默认 /jdy */
    private String apiRootPath = "/jdy";

    /** 网关路由地址（授权后获得） */
    private String routerAddr;

    /** HTTP 超时（毫秒），默认 30000 */
    private int timeout = 30000;

    /** 鉴权版本，固定 2.0 */
    private String authVersion = "2.0";

    public String getClientID() { return clientID; }
    public void setClientID(String clientID) { this.clientID = clientID; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getBaseDomain() { return baseDomain; }
    public void setBaseDomain(String baseDomain) { this.baseDomain = baseDomain; }

    public String getApiRootPath() { return apiRootPath; }
    public void setApiRootPath(String apiRootPath) { this.apiRootPath = apiRootPath; }

    public String getRouterAddr() { return routerAddr; }
    public void setRouterAddr(String routerAddr) { this.routerAddr = routerAddr; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public String getAuthVersion() { return authVersion; }
    public void setAuthVersion(String authVersion) { this.authVersion = authVersion; }

    @Override
    public String toString()
    {
        return "ApigwConfig{clientID=" + clientID + ", appKey=" + appKey
            + ", baseDomain=" + baseDomain + ", timeout=" + timeout + "}";
    }
}
