package com.ruoyi.fin.util;

import com.ruoyi.fin.exception.FinException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 财务敏感字段加解密工具（AES-128-GCM）。
 *
 * <p>【P0-5 约束落地】银行账号等高敏感数据不得明文存储。</p>
 *
 * <p><b>为什么用 GCM 而不是 ECB</b>（2026-10-09 修正）：</p>
 * <ul>
 *   <li>ECB 对相同明文产生相同密文，攻击者可通过密文比对推断出「哪些账号相同」，
 *       且无法检测密文被篡改</li>
 *   <li>GCM 每次加密随机生成 IV，相同账号得到不同密文；并带 128 位认证标签，
 *       密文被改动会解密失败，可发现篡改</li>
 *   <li>存储格式为 {@code Base64(IV ‖ 密文 ‖ 认证标签)}，
 *       前 12 字节为 IV，末 16 字节为标签</li>
 * </ul>
 *
 * <p>其他设计要点：</p>
 * <ol>
 *   <li>密钥来自配置 {@code finance.aes-key}，经环境变量注入，不进 Git 仓库；
 *       未配置时使用默认值并打告警</li>
 *   <li><b>加密后无法 LIKE 查询</b>，故所有需检索/展示的账号字段须另存明文尾号
 *       {@code account_tail}（后 4 位），见 fin_bank_account / fin_partner_bank</li>
 *   <li>导出 Excel 时调用 {@link #mask(String)} 脱敏为 ****1234</li>
 * </ol>
 *
 * @author ruoyi
 */
@Component
public class FinCryptoUtil
{
    private static final Logger log = LoggerFactory.getLogger(FinCryptoUtil.class);

    private static final String ALGORITHM = "AES";

    /** GCM 模式，认证标签 128 位 */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /** IV 长度（GCM 推荐 12 字节 = 96 位） */
    private static final int IV_LENGTH = 12;

    /** 认证标签长度（位） */
    private static final int TAG_LENGTH_BIT = 128;

    /** 账号尾号长度 */
    public static final int TAIL_LENGTH = 4;

    /** 默认密钥，生产环境必须通过环境变量 FIN_AES_KEY 覆盖 */
    private static final String DEFAULT_KEY = "RuoyiFin@2026Key";

    /** 安全随机数生成器，用于每次加密生成新 IV */
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${finance.aes-key:}")
    private String configuredKey;

    /**
     * 取 AES 密钥。未配置时回落到默认值并打告警日志。
     */
    private byte[] keyBytes()
    {
        String key = (configuredKey != null && !configuredKey.trim().isEmpty())
                ? configuredKey.trim() : DEFAULT_KEY;
        if (key.equals(DEFAULT_KEY))
        {
            log.warn("未配置 finance.aes-key，正在使用默认密钥。生产环境必须通过环境变量 FIN_AES_KEY 注入");
        }
        // AES-128 需 16 字节，超长截断、过短补齐
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        byte[] fixed = new byte[16];
        System.arraycopy(raw, 0, fixed, 0, Math.min(raw.length, 16));
        for (int i = raw.length; i < 16; i++)
        {
            fixed[i] = (byte) '0';
        }
        return fixed;
    }

    /**
     * 加密（每次使用随机 IV）。
     *
     * @param plain 明文
     * @return Base64(IV ‖ 密文 ‖ 标签)，入库字段 account_number_encrypt
     */
    public String encrypt(String plain)
    {
        if (plain == null || plain.isEmpty())
        {
            return null;
        }
        try
        {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes(), ALGORITHM),
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("银行账号加密失败", e);
        }
    }

    /**
     * 解密。密文被篡改时认证校验失败会抛异常。
     *
     * @param cipherText Base64(IV ‖ 密文 ‖ 标签)
     * @return 明文
     */
    public String decrypt(String cipherText)
    {
        if (cipherText == null || cipherText.isEmpty())
        {
            return null;
        }
        try
        {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            if (combined.length <= IV_LENGTH + 16)
            {
                throw new IllegalStateException("密文长度异常");
            }
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes(), ALGORITHM),
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] decrypted = cipher.doFinal(combined, IV_LENGTH, combined.length - IV_LENGTH);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            // GCM 认证失败说明密文被篡改或密钥不对，必须显式失败而非返回垃圾数据
            log.error("银行账号解密失败，密文可能被篡改或密钥不匹配", e);
            throw new IllegalStateException("银行账号解密失败", e);
        }
    }

    /**
     * 取账号后 4 位（明文存 account_tail，用于展示与检索）。
     *
     * @param accountNo 银行账号明文或密文均可
     * @return 后 4 位，不足 4 位返回原值
     */
    public String tail(String accountNo)
    {
        if (accountNo == null || accountNo.isEmpty())
        {
            return null;
        }
        String plain = accountNo;
        // GCM 密文必然含 Base64 尾部 '=' 或前段为随机 IV（长度 > 24 视为密文）
        if (accountNo.contains("=") || accountNo.length() > 24)
        {
            try
            {
                plain = decrypt(accountNo);
            }
            catch (Exception e)
            {
                plain = accountNo;
            }
        }
        return plain.length() <= TAIL_LENGTH ? plain
                : plain.substring(plain.length() - TAIL_LENGTH);
    }

    /**
     * 导出脱敏：显示为 ****1234。
     *
     * @param accountTail account_tail 字段值
     * @return 脱敏串
     */
    public String mask(String accountTail)
    {
        if (accountTail == null || accountTail.isEmpty())
        {
            return "";
        }
        return "****" + accountTail;
    }

    /**
     * 校验账号格式，不合法抛业务异常。
     *
     * @param accountNo 账号
     */
    public void validate(String accountNo)
    {
        if (accountNo == null || !accountNo.matches("^[0-9]{6,32}$"))
        {
            throw FinException.accountNoInvalid(accountNo);
        }
    }
}