package com.ruoyi.ai.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM返回JSON解析工具
 * 兼容 markdown 代码块包裹（```json ... ```）等常见格式；
 * 多级容错：代码块剥离 → 首尾大括号截取 → 解析
 *
 * @author ruoyi
 */
public class AiJsonParser
{
    private static final Logger log = LoggerFactory.getLogger(AiJsonParser.class);

    /**
     * 解析LLM返回的JSON文本（自动剥离markdown代码块、思考文本）
     * 解析失败返回空JSONObject，不抛异常
     */
    public static JSONObject parse(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return new JSONObject();
        }
        String cleaned = text.trim();

        // 1. 剥离markdown代码块
        if (cleaned.startsWith("```"))
        {
            int start = cleaned.indexOf('\n');
            int end = cleaned.lastIndexOf("```");
            if (start > 0 && end > start)
            {
                cleaned = cleaned.substring(start + 1, end).trim();
            }
        }

        // 2. 尝试直接解析
        JSONObject obj = tryParse(cleaned);
        if (obj != null)
        {
            return obj;
        }

        // 3. 容错：截取首个 { 到最后一个 }
        int braceStart = cleaned.indexOf('{');
        int braceEnd = cleaned.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart)
        {
            obj = tryParse(cleaned.substring(braceStart, braceEnd + 1));
            if (obj != null)
            {
                return obj;
            }
        }

        // 4. 容错：内容可能被 max_tokens 截断（无闭合大括号）——记录失败
        log.warn("解析AI返回JSON失败（长度{}，尾部50字：{}）", text.length(),
                text.length() > 50 ? text.substring(text.length() - 50) : text);
        return new JSONObject();
    }

    private static JSONObject tryParse(String s)
    {
        try
        {
            JSONObject obj = JSON.parseObject(s);
            return obj != null ? obj : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
