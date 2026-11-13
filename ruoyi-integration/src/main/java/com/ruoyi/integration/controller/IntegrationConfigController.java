package com.ruoyi.integration.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 第三方集成配置管理接口（钉钉 / 企业微信）
 *
 * 配置持久化复用 sys_config，按键读写；凭据（AppSecret / Secret）只写不回显。
 * 通过 connectorType 参数区分连接器，未传时默认 dingtalk，保证历史前端调用行为不变。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/integration/attendance")
public class IntegrationConfigController extends BaseController
{
    private static final String CONNECTOR_WECOM = "wecom";

    private static final String KEY_APP_KEY = "integration.dingtalk.appKey";

    private static final String KEY_APP_SECRET = "integration.dingtalk.appSecret";

    private static final String KEY_ENABLED = "integration.dingtalk.enabled";

    private static final String KEY_MATCH_TYPE = "integration.dingtalk.matchType";

    private static final String KEY_MOCK_ENABLED = "integration.dingtalk.mockEnabled";

    /** 企业微信配置键 */
    private static final String KEY_WECOM_CORP_ID = "integration.wecom.corpId";

    private static final String KEY_WECOM_SECRET = "integration.wecom.secret";

    private static final String KEY_WECOM_ENABLED = "integration.wecom.enabled";

    private static final String KEY_WECOM_MATCH_TYPE = "integration.wecom.matchType";

    private static final String KEY_WECOM_MOCK_ENABLED = "integration.wecom.mockEnabled";

    /** 企业微信回调配置键（用于接收消息服务器URL验证） */
    private static final String KEY_WECOM_CALLBACK_TOKEN = "integration.wecom.callbackToken";

    private static final String KEY_WECOM_CALLBACK_AES_KEY = "integration.wecom.callbackEncodingAesKey";

    /** 前端占位符：出现该值视为"未修改" */
    private static final String SECRET_PLACEHOLDER = "******";

    @Autowired
    private ISysConfigService configService;

    private boolean isWecom(String connectorType)
    {
        return CONNECTOR_WECOM.equalsIgnoreCase(connectorType);
    }

    /**
     * 查询集成配置（凭据不回显，仅返回是否已配置）
     *
     * @param connectorType 连接器类型（dingtalk 默认 / wecom）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:config')")
    @GetMapping("/config")
    public AjaxResult getConfig(String connectorType)
    {
        AjaxResult ajax = success();
        ajax.put("connectorType", isWecom(connectorType) ? CONNECTOR_WECOM : "dingtalk");
        if (isWecom(connectorType))
        {
            ajax.put("corpId", configService.selectConfigByKey(KEY_WECOM_CORP_ID));
            ajax.put("enabled", "true".equals(configService.selectConfigByKey(KEY_WECOM_ENABLED)));
            ajax.put("matchType", configService.selectConfigByKey(KEY_WECOM_MATCH_TYPE));
            ajax.put("mockEnabled", "true".equals(configService.selectConfigByKey(KEY_WECOM_MOCK_ENABLED)));
            String secret = configService.selectConfigByKey(KEY_WECOM_SECRET);
            ajax.put("hasSecret", StringUtils.isNotEmpty(secret));
            ajax.put("secretPlaceholder", SECRET_PLACEHOLDER);
            ajax.put("callbackToken", configService.selectConfigByKey(KEY_WECOM_CALLBACK_TOKEN));
            String callbackAesKey = configService.selectConfigByKey(KEY_WECOM_CALLBACK_AES_KEY);
            ajax.put("hasCallbackAesKey", StringUtils.isNotEmpty(callbackAesKey));
            ajax.put("callbackUrl", "/integration/wecom/callback");
            return ajax;
        }
        ajax.put("appKey", configService.selectConfigByKey(KEY_APP_KEY));
        ajax.put("enabled", "true".equals(configService.selectConfigByKey(KEY_ENABLED)));
        ajax.put("matchType", configService.selectConfigByKey(KEY_MATCH_TYPE));
        ajax.put("mockEnabled", "true".equals(configService.selectConfigByKey(KEY_MOCK_ENABLED)));
        String secret = configService.selectConfigByKey(KEY_APP_SECRET);
        ajax.put("hasSecret", StringUtils.isNotEmpty(secret));
        ajax.put("secretPlaceholder", SECRET_PLACEHOLDER);
        return ajax;
    }

    /**
     * 保存集成配置
     *
     * 钉钉：appKey / appSecret（留空不修改）/ enabled / matchType / mockEnabled
     * 企业微信：corpId / secret（留空不修改）/ enabled / matchType / mockEnabled
     *
     * @param body 配置项，connectorType 用于区分连接器（可选，默认 dingtalk）
     */
    @PreAuthorize("@ss.hasPermi('integration:attendance:config')")
    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody Map<String, String> body)
    {
        return isWecom(body.get("connectorType")) ? saveWecomConfig(body) : saveDingtalkConfig(body);
    }

    /**
     * 保存钉钉配置（保持原有行为不变）
     */
    private AjaxResult saveDingtalkConfig(Map<String, String> body)
    {
        String appKey = body.get("appKey");
        if (StringUtils.isNotEmpty(appKey))
        {
            configService.updateConfigByKeyForced(KEY_APP_KEY, appKey.trim());
        }
        String appSecret = body.get("appSecret");
        if (StringUtils.isNotEmpty(appSecret) && !SECRET_PLACEHOLDER.equals(appSecret))
        {
            configService.updateConfigByKeyForced(KEY_APP_SECRET, appSecret.trim());
        }
        String enabled = body.get("enabled");
        if (StringUtils.isNotEmpty(enabled))
        {
            configService.updateConfigByKeyForced(KEY_ENABLED, "true".equals(enabled) ? "true" : "false");
        }
        String matchType = body.get("matchType");
        if (StringUtils.isNotEmpty(matchType))
        {
            if (!"mobile".equals(matchType) && !"jobNumber".equals(matchType) && !"auto".equals(matchType))
            {
                return error("匹配策略不合法：仅支持 mobile / jobNumber / auto");
            }
            configService.updateConfigByKeyForced(KEY_MATCH_TYPE, matchType);
        }
        String mockEnabled = body.get("mockEnabled");
        if (StringUtils.isNotEmpty(mockEnabled))
        {
            configService.updateConfigByKeyForced(KEY_MOCK_ENABLED, "true".equals(mockEnabled) ? "true" : "false");
        }
        return success("保存成功");
    }

    /**
     * 保存企业微信配置
     *
     * 匹配策略默认 mobile（手机号优先、工号兜底）：
     * 企业微信 2022-06-20 起新建自建应用不再返回手机号，因此手机号未命中时会自动用
     * alias（别名）/ userid 对员工工号兜底。
     */
    private AjaxResult saveWecomConfig(Map<String, String> body)
    {
        String corpId = body.get("corpId");
        if (StringUtils.isNotEmpty(corpId))
        {
            configService.updateConfigByKeyForced(KEY_WECOM_CORP_ID, corpId.trim());
        }
        String secret = body.get("secret");
        if (StringUtils.isNotEmpty(secret) && !SECRET_PLACEHOLDER.equals(secret))
        {
            configService.updateConfigByKeyForced(KEY_WECOM_SECRET, secret.trim());
        }
        String enabled = body.get("enabled");
        if (StringUtils.isNotEmpty(enabled))
        {
            configService.updateConfigByKeyForced(KEY_WECOM_ENABLED, "true".equals(enabled) ? "true" : "false");
        }
        String matchType = body.get("matchType");
        if (StringUtils.isNotEmpty(matchType))
        {
            if (!"mobile".equals(matchType) && !"jobNumber".equals(matchType) && !"auto".equals(matchType))
            {
                return error("匹配策略不合法：仅支持 mobile / jobNumber / auto");
            }
            configService.updateConfigByKeyForced(KEY_WECOM_MATCH_TYPE, matchType);
        }
        String mockEnabled = body.get("mockEnabled");
        if (StringUtils.isNotEmpty(mockEnabled))
        {
            configService.updateConfigByKeyForced(KEY_WECOM_MOCK_ENABLED, "true".equals(mockEnabled) ? "true" : "false");
        }
        String callbackToken = body.get("callbackToken");
        if (StringUtils.isNotEmpty(callbackToken))
        {
            configService.updateConfigByKeyForced(KEY_WECOM_CALLBACK_TOKEN, callbackToken.trim());
        }
        String callbackAesKey = body.get("callbackEncodingAesKey");
        if (StringUtils.isNotEmpty(callbackAesKey) && !SECRET_PLACEHOLDER.equals(callbackAesKey))
        {
            String trimmed = callbackAesKey.trim();
            if (trimmed.length() != 43)
            {
                return error("EncodingAESKey 长度须为 43 位字符（企业微信后台回调配置页提供）");
            }
            configService.updateConfigByKeyForced(KEY_WECOM_CALLBACK_AES_KEY, trimmed);
        }
        return success("保存成功");
    }
}
