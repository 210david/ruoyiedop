package com.ruoyi.common.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson全局配置
 *
 * 背景：项目升级到Spring Boot 4.x + Jackson 3.x后，
 * Jackson 3.x (tools.jackson包) 不识别旧版 com.fasterxml.jackson.annotation.JsonFormat 注解，
 * 导致带有 @JsonFormat(pattern = "yyyy-MM-dd") 的 Date 字段无法按指定格式反序列化，
 * 前端传入 "2026-08-15" 等短日期格式时抛出 InvalidFormatException，
 * 被 GlobalExceptionHandler 拦截后返回"系统繁忙，请稍后重试"。
 *
 * 解决方案：注册自定义 Date 反序列化器，自动识别多种日期格式，全局兼容所有 Date 字段。
 * 影响范围：ruoyi-safety/pms/qms/wms/dms 等模块共50+个 Domain 类均受益。
 */
@Configuration
public class JacksonConfig
{
    /** 支持的日期格式（按优先级排序） */
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyyMMdd"
    };

    /**
     * 注册自定义Date反序列化模块
     * Spring Boot会自动将所有Module bean注册到ObjectMapper中
     */
    @Bean
    public SimpleModule multiFormatDateModule()
    {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new MultiFormatDateDeserializer());
        return module;
    }

    /**
     * 多格式日期反序列化器
     * 自动尝试多种日期格式，兼容 yyyy-MM-dd 和 yyyy-MM-dd HH:mm:ss 等
     */
    public static class MultiFormatDateDeserializer extends ValueDeserializer<Date>
    {
        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException
        {
            JsonToken token = p.currentToken();

            // 处理数字时间戳（毫秒）
            if (token == JsonToken.VALUE_NUMBER_INT)
            {
                return new Date(p.getLongValue());
            }

            // 处理字符串日期
            if (token == JsonToken.VALUE_STRING)
            {
                String dateStr = p.getValueAsString();
                if (dateStr == null || dateStr.trim().isEmpty())
                {
                    return null;
                }
                dateStr = dateStr.trim();

                // 尝试所有支持的日期格式
                for (String format : DATE_FORMATS)
                {
                    try
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                        sdf.setLenient(false);
                        return sdf.parse(dateStr);
                    }
                    catch (ParseException e)
                    {
                        // 继续尝试下一种格式
                    }
                }

                // 尝试解析为时间戳
                try
                {
                    long timestamp = Long.parseLong(dateStr);
                    return new Date(timestamp);
                }
                catch (NumberFormatException e)
                {
                    // 不是时间戳，继续
                }

                // 所有格式都失败，抛出异常
                throw DatabindException.from(p,
                    "Unsupported date format '" + dateStr + "'. Supported: yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, yyyy/MM/dd, etc.");
            }

            // 处理null值
            if (token == JsonToken.VALUE_NULL)
            {
                return null;
            }

            // 其他类型，交给上下文处理
            return (Date) ctxt.handleUnexpectedToken(Date.class, p);
        }
    }
}
