package com.ruoyi.fin.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 银行账号加解密单元测试
 *
 * <p>【P0-5】账号不得明文存储，且加密方案必须满足：</p>
 * <ol>
 *   <li>相同明文产生不同密文（GCM 随机 IV，修复了 ECB 的确定性缺陷）</li>
 *   <li>密文被篡改时解密失败（认证标签生效）</li>
 *   <li>尾号提取正确、脱敏格式正确</li>
 * </ol>
 *
 * @author ruoyi
 */
class FinCryptoUtilTest
{
    private FinCryptoUtil util() {
        FinCryptoUtil u = new FinCryptoUtil();
        ReflectionTestUtils.setField(u, "configuredKey", "TestFin@2026Key");
        return u;
    }

    @Test
    @DisplayName("加解密往返一致")
    void testEncryptDecrypt()
    {
        FinCryptoUtil u = util();
        String plain = "6222021234567890123";
        String cipher = u.encrypt(plain);
        assertNotNull(cipher);
        assertNotEquals(plain, cipher, "密文不得等于明文");
        assertEquals(plain, u.decrypt(cipher), "解密须还原原文");
    }

    @Test
    @DisplayName("相同明文两次加密得到不同密文（GCM 随机 IV）")
    void testSamePlainDifferentCipher()
    {
        FinCryptoUtil u = util();
        String plain = "6222021234567890123";
        String c1 = u.encrypt(plain);
        String c2 = u.encrypt(plain);
        assertNotEquals(c1, c2, "GCM 下相同明文必须产生不同密文（ECB 会相同，属缺陷）");

        // 但两次都能正确解回原文
        assertEquals(plain, u.decrypt(c1));
        assertEquals(plain, u.decrypt(c2));
    }

    @Test
    @DisplayName("批量加密不产生重复密文（ECB 缺陷的回归防护）")
    void testNoDuplicateCipher()
    {
        FinCryptoUtil u = util();
        String plain = "6222021234567890123";
        Set<String> ciphers = new HashSet<>();
        for (int i = 0; i < 20; i++)
        {
            ciphers.add(u.encrypt(plain));
        }
        assertEquals(20, ciphers.size(), "20 次加密应产生 20 个不同密文");
    }

    @Test
    @DisplayName("密文被篡改时解密失败（认证标签生效）")
    void testTamperDetected()
    {
        FinCryptoUtil u = util();
        String cipher = u.encrypt("6222021234567890123");

        // 改动密文中间一个字符
        char[] chars = cipher.toCharArray();
        int mid = chars.length / 2;
        chars[mid] = (chars[mid] == 'A') ? 'B' : 'A';
        String tampered = new String(chars);

        assertThrows(Exception.class, () -> u.decrypt(tampered),
                "密文被篡改后必须解密失败，不能静默返回错误数据");
    }

    @Test
    @DisplayName("IV 被篡改同样应失败")
    void testIvTamperDetected()
    {
        FinCryptoUtil u = util();
        String cipher = u.encrypt("6222021234567890123");
        char[] chars = cipher.toCharArray();
        chars[2] = (chars[2] == 'A') ? 'B' : 'A';
        assertThrows(Exception.class, () -> u.decrypt(new String(chars)));
    }

    @Test
    @DisplayName("尾号提取：取后 4 位")
    void testTail()
    {
        FinCryptoUtil u = util();
        assertEquals("0123", u.tail("6222021234567890123"));
        assertEquals("7890", u.tail("6222021234567890"));
    }

    @Test
    @DisplayName("尾号提取：密文也能取到明文尾号")
    void testTailFromCipher()
    {
        FinCryptoUtil u = util();
        String plain = "6222021234567890123";
        String cipher = u.encrypt(plain);
        assertEquals("0123", u.tail(cipher), "从密文也应能提取明文尾号");
    }

    @Test
    @DisplayName("脱敏格式为 ****1234")
    void testMask()
    {
        FinCryptoUtil u = util();
        assertEquals("****0123", u.mask("0123"));
        assertEquals("", u.mask(null));
        assertEquals("", u.mask(""));
    }

    @Test
    @DisplayName("账号格式校验：仅接受 6-32 位数字")
    void testValidate()
    {
        FinCryptoUtil u = util();
        u.validate("123456");
        u.validate("6222021234567890123");
        assertThrows(Exception.class, () -> u.validate("12345"), "不足 6 位应拒绝");
        assertThrows(Exception.class, () -> u.validate("abc123456"), "含字母应拒绝");
        assertThrows(Exception.class, () -> u.validate(null));
    }

    @Test
    @DisplayName("空值处理：encrypt/decrypt 传 null 返回 null")
    void testNullSafe()
    {
        FinCryptoUtil u = util();
        assertEquals(null, u.encrypt(null));
        assertEquals(null, u.encrypt(""));
        assertEquals(null, u.decrypt(null));
        assertEquals(null, u.decrypt(""));
    }

    @Test
    @DisplayName("不同密钥产生不同密文")
    void testDifferentKey()
    {
        FinCryptoUtil u1 = util();
        FinCryptoUtil u2 = new FinCryptoUtil();
        ReflectionTestUtils.setField(u2, "configuredKey", "AnotherFin@2026Key");

        String plain = "6222021234567890123";
        assertNotEquals(u1.encrypt(plain), u2.encrypt(plain), "不同密钥应产生不同密文");
    }

    @Test
    @DisplayName("密钥错误时解密失败而非返回垃圾数据")
    void testWrongKey()
    {
        FinCryptoUtil u1 = util();
        String cipher = u1.encrypt("6222021234567890123");

        FinCryptoUtil u2 = new FinCryptoUtil();
        ReflectionTestUtils.setField(u2, "configuredKey", "WrongFin@2026Key");
        assertThrows(Exception.class, () -> u2.decrypt(cipher),
                "密钥不匹配时 GCM 认证应失败");
    }

    @Test
    @DisplayName("长账号加密后长度可控（Base64 of 16+密文+16）")
    void testCipherLength()
    {
        FinCryptoUtil u = util();
        String cipher = u.encrypt("6222021234567890123");
        // 19 字节明文 + PKCS 补齐到 32 字节，加 12 字节 IV = 44 字节 → Base64 约 60 字符
        // varchar(256) 足够
        assertTrue(cipher.length() < 256, "密文长度须小于 256，实际 " + cipher.length());
    }
}