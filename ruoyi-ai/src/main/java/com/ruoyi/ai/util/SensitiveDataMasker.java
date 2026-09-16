package com.ruoyi.ai.util;

import java.util.regex.Pattern;

/**
 * 敏感信息脱敏工具
 * 手机号、身份证、银行卡进入prompt前统一脱敏，防止泄露给外部大模型
 *
 * @author ruoyi
 */
public class SensitiveDataMasker
{
    /** 手机号：1开头11位 */
    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    /** 身份证：15或18位（末位可X） */
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)\\d{15}(?!\\d)|(?<!\\d)\\d{17}[0-9Xx](?!\\d)");
    /** 银行卡：16-19位连续数字 */
    private static final Pattern BANK_CARD = Pattern.compile("(?<!\\d)\\d{16,19}(?!\\d)");

    /**
     * 脱敏文本：保留前3后4，中间以****代替
     */
    public static String mask(String text)
    {
        if (text == null || text.isEmpty())
        {
            return text;
        }
        String result = MOBILE.matcher(text).replaceAll(m -> maskMiddle(m.group()));
        result = ID_CARD.matcher(result).replaceAll(m -> maskMiddle(m.group()));
        result = BANK_CARD.matcher(result).replaceAll(m -> maskMiddle(m.group()));
        return result;
    }

    private static String maskMiddle(String s)
    {
        if (s.length() <= 7)
        {
            return s.charAt(0) + "****" + s.charAt(s.length() - 1);
        }
        return s.substring(0, 3) + "****" + s.substring(s.length() - 4);
    }
}
