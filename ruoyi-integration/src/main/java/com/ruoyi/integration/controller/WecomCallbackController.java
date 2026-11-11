package com.ruoyi.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.integration.wecom.WecomCallbackCrypto;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 企业微信回调接口（应用管理后台「接收消息服务器URL」）
 *
 * 用途：
 *   1. GET 验证：企业微信后台保存回调 URL 时发送 echostr，验签解密后原样回显，
 *      验证通过后应用后台即解锁「企业可信IP」配置；
 *   2. POST 接收：预留消息推送入口，当前验签解密后仅应答 success，
 *      后续免登 / 事件推送在此扩展业务处理。
 *
 * 配置依赖（系统管理-参数设置，凭据只写不回显）：
 *   integration.wecom.callbackToken         回调配置页的 Token
 *   integration.wecom.callbackEncodingAesKey 回调配置页的 EncodingAESKey（43 位）
 *   integration.wecom.corpId                企业 ID（复用现有配置）
 *
 * 后台填写的 URL 形如：http(s)://<后端地址>/integration/wecom/callback
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/integration/wecom")
public class WecomCallbackController
{
    private static final String KEY_CALLBACK_TOKEN = "integration.wecom.callbackToken";

    private static final String KEY_CALLBACK_AES_KEY = "integration.wecom.callbackEncodingAesKey";

    private static final String KEY_CORP_ID = "integration.wecom.corpId";

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private WecomCallbackCrypto callbackCrypto;

    /**
     * 回调 URL 验证（企业微信后台保存配置时触发）
     *
     * 验签通过且 echostr 解密成功时，原样返回解密明文；否则返回空串视为验证失败。
     */
    @GetMapping(value = "/callback", produces = "text/plain;charset=UTF-8")
    public String verify(@RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr)
    {
        String token = configService.selectConfigByKey(KEY_CALLBACK_TOKEN);
        String aesKey = configService.selectConfigByKey(KEY_CALLBACK_AES_KEY);
        String corpId = configService.selectConfigByKey(KEY_CORP_ID);
        if (StringUtils.isAnyEmpty(token, aesKey, corpId))
        {
            return "";
        }
        if (!callbackCrypto.verifySignature(token, timestamp, nonce, echostr, msgSignature))
        {
            return "";
        }
        String plain = callbackCrypto.decrypt(aesKey, echostr, corpId);
        return plain == null ? "" : plain;
    }

    /**
     * 接收企业微信推送消息（预留）
     *
     * 当前仅完成验签解密并应答 success；具体业务处理后续按需求扩展。
     */
    @PostMapping(value = "/callback", produces = "text/plain;charset=UTF-8")
    public String receive(@RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestBody String encryptedBody)
    {
        String token = configService.selectConfigByKey(KEY_CALLBACK_TOKEN);
        String aesKey = configService.selectConfigByKey(KEY_CALLBACK_AES_KEY);
        String corpId = configService.selectConfigByKey(KEY_CORP_ID);
        if (StringUtils.isAnyEmpty(token, aesKey, corpId))
        {
            return "success";
        }
        // 密文格式：<xml><Encrypt><![CDATA[...]]></Encrypt></xml>，签名对 Encrypt 字段计算
        String cipherText = extractEncrypt(encryptedBody);
        if (StringUtils.isEmpty(cipherText))
        {
            return "success";
        }
        if (!callbackCrypto.verifySignature(token, timestamp, nonce, cipherText, msgSignature))
        {
            return "success";
        }
        String plain = callbackCrypto.decrypt(aesKey, cipherText, corpId);
        if (plain != null)
        {
            // TODO: 后续免登 / 事件推送业务处理入口
        }
        return "success";
    }

    /** 从回调 POST 的 XML 中提取 Encrypt 密文字段 */
    private String extractEncrypt(String xmlBody)
    {
        if (StringUtils.isEmpty(xmlBody))
        {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("<Encrypt><!\\[CDATA\\[(.*?)\\]\\]></Encrypt>").matcher(xmlBody);
        return matcher.find() ? matcher.group(1) : null;
    }
}
