package com.ruoyi.common.utils;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * 敏感配置项对称加密工具（AES-128/ECB/PKCS5Padding）
 * 用途：sys_config 中 API Key 等敏感值的密文存储
 *
 * 密文格式：ENC:Base64(密文)
 * - 加密：encrypt()，输入明文，输出带 ENC: 前缀的密文
 * - 解密：decrypt()，输入密文（带前缀）返回明文；输入无前缀明文时原样返回（存量兼容）
 * - 解密失败返回原文（容错，避免密钥变更导致功能不可用，但会记录告警日志）
 *
 * 密钥来源：ruoyi.fileEncryptionKey（与文件加密共用），未配置时使用默认密钥。
 * 生产环境建议通过环境变量覆盖，且定期轮换（注意：轮换前需先用旧密钥解密回明文再重新加密）。
 *
 * @author ruoyi
 */
public class SecureKeyUtils
{
    private static final Logger log = LoggerFactory.getLogger(SecureKeyUtils.class);

    /** 密文前缀标识：带此前缀的值才尝试解密 */
    public static final String CIPHER_PREFIX = "ENC:";

    private static final String ALGORITHM = "AES";

    /** 默认密钥（配置未指定时兜底，生产环境必须配置） */
    private static final String DEFAULT_KEY = "EDOP2026SecureKey";

    private static SecretKeySpec getSecretKey()
    {
        String key = RuoYiConfig.getFileEncryptionKey();
        if (key == null || key.length() < 16)
        {
            key = DEFAULT_KEY;
        }
        byte[] keyBytes = new byte[16];
        byte[] src = key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 16));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /** 加密：明文 → ENC:Base64 */
    public static String encrypt(String plain)
    {
        if (plain == null || plain.isEmpty())
        {
            return plain;
        }
        // 已是密文则原样返回（幂等）
        if (plain.startsWith(CIPHER_PREFIX))
        {
            return plain;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return CIPHER_PREFIX + java.util.Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            log.error("敏感配置加密失败", e);
            return plain;
        }
    }

    /** 解密：ENC:Base64 → 明文；无前缀明文原样返回（存量兼容）；主密钥失败时回退默认密钥（兼容早期用默认密钥加密的存量数据） */
    public static String decrypt(String value)
    {
        if (value == null || value.isEmpty() || !value.startsWith(CIPHER_PREFIX))
        {
            return value;
        }
        String body = value.substring(CIPHER_PREFIX.length());
        String plain = tryDecrypt(body, getSecretKey());
        if (plain == null)
        {
            plain = tryDecrypt(body, getDefaultSecretKey());
        }
        if (plain == null)
        {
            log.warn("敏感配置解密失败（密钥不匹配），按原值返回");
            return value;
        }
        return plain;
    }

    private static String tryDecrypt(String body, SecretKeySpec key)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decoded = java.util.Base64.getDecoder().decode(body);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 默认密钥KeySpec（迁移期配置未就绪时可能用默认密钥加密过，解密需兼容） */
    private static SecretKeySpec getDefaultSecretKey()
    {
        byte[] keyBytes = new byte[16];
        byte[] src = DEFAULT_KEY.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 16));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /** 是否为密文格式 */
    public static boolean isEncrypted(String value)
    {
        return value != null && value.startsWith(CIPHER_PREFIX);
    }
}
