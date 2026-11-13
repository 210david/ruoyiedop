package com.ruoyi.integration.wecom;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * 企业微信回调消息加解密工具（官方加解密协议实现，等价于微信提供的 WXBizMsgCrypt）。
 *
 * 协议要点：
 *   - EncodingAESKey 为 43 位字符串，尾部补一个 '=' 后 Base64 解码得到 32 字节 AES-256 密钥；
 *   - 签名算法：sha1(sort(token, timestamp, nonce, 密文))；
 *   - 密文解密：AES-256-CBC / NoPadding，IV 取密钥前 16 字节；
 *   - 明文结构：16 字节随机串 + 4 字节消息长度（网络字节序）+ 消息体 + receiveid（企业微信场景为 corpId）。
 *
 * 当前用途：
 *   1. 回调 URL 配置时的 GET echo 验证（解密 echostr 后原样回显）；
 *   2. 预留 POST 消息推送的验签解密，供后续免登 / 事件推送扩展使用。
 *
 * @author ruoyi
 */
@Component
public class WecomCallbackCrypto
{
    private static final Logger log = LoggerFactory.getLogger(WecomCallbackCrypto.class);

    /** AES 块大小 */
    private static final int AES_BLOCK_SIZE = 16;

    /**
     * 校验回调签名：sha1(sort(token, timestamp, nonce, cipherText)) 与 msgSignature 比对
     *
     * @return true 签名合法
     */
    public boolean verifySignature(String token, String timestamp, String nonce, String cipherText, String msgSignature)
    {
        if (StringUtils.isAnyEmpty(token, timestamp, nonce, cipherText, msgSignature))
        {
            return false;
        }
        try
        {
            String[] items = { token, timestamp, nonce, cipherText };
            Arrays.sort(items);
            StringBuilder sb = new StringBuilder();
            for (String item : items)
            {
                sb.append(item);
            }
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest)
            {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equals(msgSignature);
        }
        catch (Exception e)
        {
            log.error("企业微信回调签名计算失败", e);
            return false;
        }
    }

    /**
     * 解密回调密文（echostr 或消息体密文）
     *
     * @param encodingAesKey 后台回调配置的 EncodingAESKey（43 位）
     * @param cipherText     密文（Base64）
     * @param corpId         企业 ID，用于校验明文尾部 receiveid
     * @return 解密后的明文；解密失败或 receiveid 不匹配返回 null
     */
    public String decrypt(String encodingAesKey, String cipherText, String corpId)
    {
        if (StringUtils.isAnyEmpty(encodingAesKey, cipherText, corpId) || encodingAesKey.length() != 43)
        {
            return null;
        }
        try
        {
            byte[] aesKey = Base64.getDecoder().decode(encodingAesKey + "=");
            byte[] encrypted = Base64.getDecoder().decode(cipherText);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, AES_BLOCK_SIZE));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] plain = cipher.doFinal(encrypted);

            // 明文结构：16 字节随机串 + 4 字节消息长度（网络字节序）+ 消息体 + receiveid + PKCS7 填充（块 32）
            int msgLen = ((plain[AES_BLOCK_SIZE] & 0xFF) << 24)
                    | ((plain[AES_BLOCK_SIZE + 1] & 0xFF) << 16)
                    | ((plain[AES_BLOCK_SIZE + 2] & 0xFF) << 8)
                    | (plain[AES_BLOCK_SIZE + 3] & 0xFF);
            if (msgLen < 0 || AES_BLOCK_SIZE + 4 + msgLen > plain.length)
            {
                log.error("企业微信回调密文长度非法: msgLen={}", msgLen);
                return null;
            }
            // 剔除尾部 PKCS7 填充字节（填充块大小 32），否则 receiveid 会带上填充导致比对失败
            int pad = plain[plain.length - 1] & 0xFF;
            if (pad < 1 || pad > 32 || AES_BLOCK_SIZE + 4 + msgLen + pad > plain.length)
            {
                log.error("企业微信回调密文填充非法: pad={}", pad);
                return null;
            }
            String msg = new String(plain, AES_BLOCK_SIZE + 4, msgLen, StandardCharsets.UTF_8);
            String receiveId = new String(plain, AES_BLOCK_SIZE + 4 + msgLen,
                    plain.length - AES_BLOCK_SIZE - 4 - msgLen - pad, StandardCharsets.UTF_8);
            if (!corpId.equals(receiveId))
            {
                log.error("企业微信回调 receiveid 不匹配: expected={}, actual={}", corpId, receiveId);
                return null;
            }
            return msg;
        }
        catch (Exception e)
        {
            log.error("企业微信回调密文解密失败: {}", e.getMessage());
            return null;
        }
    }
}
