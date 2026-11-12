package com.ruoyi.kingdee.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.kingdee.apigw.ApigwClient;
import com.kingdee.apigw.ApigwConfig;

import jakarta.annotation.PostConstruct;

/**
 * 金蝶对接模块基础配置
 * <p>
 * 启动时自动将 sys_config 中的金蝶配置同步到官方 SDK 兼容层（ApigwClient）。
 * 前端配置页面保存后会手动触发 syncConfigToSdk()。
 */
@Configuration
@EnableConfigurationProperties(KingdeeProperties.class)
public class KingdeeAutoConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(KingdeeAutoConfiguration.class);

    /** 业务域名（apikingdee.com — 鉴权接口由 SDK 内部硬编码 api.kingdee.com） */
    private static final String BUSINESS_DOMAIN = "https://apikingdee.com";

    private final KingdeeConfigResolver resolver;

    public KingdeeAutoConfiguration(KingdeeConfigResolver resolver)
    {
        this.resolver = resolver;
    }

    @Bean(name = "kingdeeRestTemplate")
    @ConditionalOnMissingBean(name = "kingdeeRestTemplate")
    public RestTemplate kingdeeRestTemplate(KingdeeProperties props)
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeout());
        factory.setReadTimeout(props.getTimeout());
        return new RestTemplate(factory);
    }

    /**
     * Spring 启动后自动初始化 SDK（如果配置已就绪）
     */
    @PostConstruct
    public void initKingdeeSdk()
    {
        try
        {
            String clientId = resolver.getClientId();
            String clientSecret = resolver.getClientSecret();
            String appKey = resolver.getAppKey();
            String appSecret = resolver.getAppSecret();

            if (clientId == null || clientId.isEmpty() || appKey == null || appKey.isEmpty())
            {
                log.warn("金蝶 SDK 跳过自动初始化：clientId 或 appKey 未配置（等前端页面填写后再触发）");
                return;
            }

            ApigwConfig cfg = new ApigwConfig();
            cfg.setClientID(clientId);
            cfg.setClientSecret(clientSecret); // 签名密钥
            cfg.setAppKey(appKey);
            cfg.setAppSecret(appSecret);       // app_signature URL 参数密钥
            cfg.setBaseDomain(BUSINESS_DOMAIN);
            cfg.setRouterAddr(resolver.getRouterAddr());
            cfg.setTimeout(resolver.getTimeout());

            ApigwClient.getInstance().init(cfg);

            boolean hasClientSecret = clientSecret != null && !clientSecret.isEmpty();
            boolean hasAppSecret = appSecret != null && !appSecret.isEmpty()
                && !appSecret.contains("your_real_app_secret_here");
            if (hasClientSecret && hasAppSecret)
            {
                log.info("✅ 金蝶 SDK 启动初始化完成: clientId={}, appKey={}, 两个密钥均已配置", clientId, appKey);
            }
            else
            {
                log.warn("⚠️ 金蝶 SDK 启动初始化完成，但密钥不完整: clientSecret={}, appSecret={}",
                    hasClientSecret ? "已配置" : "未配置",
                    hasAppSecret ? "已配置" : "未配置/占位符");
            }
        }
        catch (Exception e)
        {
            log.warn("金蝶 SDK 启动初始化异常（可忽略，等配置就绪后重试）: {}", e.getMessage());
        }
    }
}
