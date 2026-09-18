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

    /**
     * 从原文中提取敏感值并建立「脱敏值 -> 原始值」映射。
     * 用途：NL2Query 场景下，问题文本进入大模型前已脱敏，模型生成的 SQL 只含打码值；
     * 执行 SQL 前需将打码值还原为原文，否则等值查询必然查不到数据。
     */
    public static java.util.Map<String, String> maskedToOriginal(String text)
    {
        java.util.Map<String, String> mapping = new java.util.LinkedHashMap<>();
        if (text == null || text.isEmpty())
        {
            return mapping;
        }
        for (Pattern p : new Pattern[]{ ID_CARD, MOBILE, BANK_CARD })
        {
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find())
            {
                String original = m.group();
                String masked = maskMiddle(original);
                if (!masked.equals(original))
                {
                    mapping.putIfAbsent(masked, original);
                }
            }
        }
        return mapping;
    }

    /**
     * 将文本中的脱敏值还原为原始值（仅用于送库执行的SQL，不用于展示/入模型）。
     */
    public static String restore(String text, java.util.Map<String, String> maskedToOriginal)
    {
        if (text == null || text.isEmpty() || maskedToOriginal == null || maskedToOriginal.isEmpty())
        {
            return text;
        }
        for (java.util.Map.Entry<String, String> e : maskedToOriginal.entrySet())
        {
            text = text.replace(e.getKey(), e.getValue());
        }
        return text;
    }
}
