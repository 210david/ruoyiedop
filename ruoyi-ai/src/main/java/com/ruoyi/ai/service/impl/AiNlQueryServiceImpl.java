package com.ruoyi.ai.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * NL2Query 自然语言查数 Service实现
 * 安全红线：仅白名单表、只读SELECT、强制LIMIT、禁止DDL/DML/多语句
 *
 * @author ruoyi
 */
@Service
public class AiNlQueryServiceImpl implements IAiNlQueryService
{
    private static final Logger log = LoggerFactory.getLogger(AiNlQueryServiceImpl.class);

    /** 单次返回行数上限 */
    private static final int MAX_ROWS = 100;

    /** 危险关键字（DDL/DML/多语句） */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke|call|load_file|into\\s+outfile|into\\s+dumpfile)\\b",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private AiClient aiClient;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> query(String question)
    {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(question) || question.trim().isEmpty())
        {
            result.put("error", "请输入要查询的问题");
            return result;
        }

        // 1. 开关校验
        if (!"true".equalsIgnoreCase(getConfig("ai.nl2query.enabled")))
        {
            result.put("error", "智能查询未开启，请管理员在系统参数中开启 ai.nl2query.enabled 并配置白名单表");
            return result;
        }

        // 2. 白名单
        String tables = getConfig("ai.nl2query.tables");
        if (StringUtils.isEmpty(tables))
        {
            result.put("error", "白名单表未配置（ai.nl2query.tables），请先配置允许查询的表");
            return result;
        }
        List<String> allowed = new ArrayList<>();
        for (String t : tables.split(","))
        {
            if (StringUtils.isNotEmpty(t.trim()))
            {
                allowed.add(t.trim().toLowerCase());
            }
        }

        // 3. LLM生成SQL（限定白名单表结构）
        String schemaDesc = buildSchemaDesc(allowed);
        String prompt = "你是SQL生成助手。根据用户问题生成一条 MySQL 只读查询 SQL。\n"
                + "硬性要求：\n"
                + "1. 只能使用以下白名单表：\n" + schemaDesc + "\n"
                + "2. 只能生成 SELECT 语句，禁止任何增删改\n"
                + "3. 必须带 LIMIT（不超过" + MAX_ROWS + "）\n"
                + "4. 只输出一条SQL，用```sql```包裹，不要输出任何解释文字\n"
                + "5. 如问题超出白名单表范围，输出：```sql\nSELECT '超出可查询范围' AS 提示\n```\n\n"
                + "用户问题：" + question;

        AiResult ai = aiClient.chat("platform", "nl2query", prompt);
        if (!ai.isSuccess())
        {
            result.put("error", "SQL生成失败：" + StringUtils.nvl(ai.getError(), "请稍后重试"));
            return result;
        }

        // 4. 提取并校验SQL
        String sql = extractSql(ai.getContent());
        if (StringUtils.isEmpty(sql))
        {
            result.put("error", "未能从AI回复中解析出SQL");
            return result;
        }
        // 模型自拒占位语句：问题超出白名单范围
        if (sql.toLowerCase().contains("超出可查询范围"))
        {
            result.put("error", "该问题涉及的数据不在可查询范围内（仅白名单表数据可查）");
            return result;
        }
        String validateError = validate(sql, allowed);
        if (validateError != null)
        {
            log.warn("NL2Query SQL校验不通过: {} -> {}", sql, validateError);
            result.put("error", "生成的SQL未通过安全校验：" + validateError);
            result.put("sql", sql);
            return result;
        }

        // 5. 执行查询
        try
        {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            if (rows.size() > MAX_ROWS)
            {
                rows = rows.subList(0, MAX_ROWS);
            }
            List<String> columns = rows.isEmpty() ? new ArrayList<>() : new ArrayList<>(rows.get(0).keySet());
            result.put("columns", columns);
            result.put("rows", rows);
            result.put("sql", sql);
            result.put("total", rows.size());
            return result;
        }
        catch (Exception e)
        {
            log.warn("NL2Query SQL执行失败: {} -> {}", sql, e.getMessage());
            result.put("error", "SQL执行失败：" + abbreviate(e.getMessage(), 200));
            result.put("sql", sql);
            return result;
        }
    }

    // ==================== 安全校验 ====================

    /** 校验：单条SELECT、无危险关键字、仅白名单表、强制LIMIT */
    private String validate(String sql, List<String> allowed)
    {
        String s = sql.trim().replaceAll("\\s+", " ");
        // 去掉末尾分号后不允许再出现分号（多语句）
        String noTrailing = s.endsWith(";") ? s.substring(0, s.length() - 1).trim() : s;
        if (noTrailing.contains(";"))
        {
            return "不允许执行多条语句";
        }
        String lower = noTrailing.toLowerCase();
        if (!lower.startsWith("select") && !lower.startsWith("with"))
        {
            return "仅允许 SELECT 查询";
        }
        if (FORBIDDEN.matcher(noTrailing).find())
        {
            return "包含禁止的关键字（仅允许只读查询）";
        }
        if (!lower.contains(" limit ") && !lower.endsWith(" limit " + MAX_ROWS)
                && !lower.matches(".*\\blimit\\s+\\d+\\s*$"))
        {
            return "缺少 LIMIT 限制，请在结尾添加 LIMIT";
        }
        // 表白名单：提取 from/join 后的表名
        java.util.regex.Matcher m = Pattern.compile("(?:from|join)\\s+([a-z0-9_]+)", Pattern.CASE_INSENSITIVE).matcher(noTrailing);
        boolean foundTable = false;
        while (m.find())
        {
            foundTable = true;
            String table = m.group(1).toLowerCase();
            if (!allowed.contains(table))
            {
                return "表 " + table + " 不在白名单内";
            }
        }
        if (!foundTable)
        {
            return "未识别到查询表";
        }
        return null;
    }

    /** 从LLM回复中提取SQL（```sql```包裹、裸SELECT、或夹杂思考文本时扫描首个SELECT/WITH） */
    private String extractSql(String content)
    {
        if (content == null) return null;
        java.util.regex.Matcher m = Pattern.compile("```sql\\s*(.*?)```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(content);
        if (m.find())
        {
            return m.group(1).trim();
        }
        // 扫描首个 SELECT/WITH 起点（推理模型可能输出思考文字后再给SQL）
        String lower = content.toLowerCase();
        int idx = -1;
        int sel = lower.indexOf("select");
        int wit = lower.indexOf("with ");
        if (sel >= 0 && (wit < 0 || sel < wit)) idx = sel;
        else if (wit >= 0) idx = wit;
        if (idx < 0) return null;
        String tail = content.substring(idx);
        int end = tail.indexOf(';');
        return end > 0 ? tail.substring(0, end).trim() : tail.trim();
    }

    /** 白名单表的字段描述（供LLM理解结构） */
    private String buildSchemaDesc(List<String> tables)
    {
        StringBuilder sb = new StringBuilder();
        for (String table : tables)
        {
            sb.append("- ").append(table).append("：");
            try
            {
                List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                        "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION", table);
                List<String> parts = new ArrayList<>();
                for (Map<String, Object> c : cols)
                {
                    String col = String.valueOf(c.get("COLUMN_NAME"));
                    String comment = String.valueOf(c.get("COLUMN_COMMENT"));
                    parts.add(comment != null && !comment.isEmpty() && !"null".equals(comment) ? col + "(" + comment + ")" : col);
                }
                sb.append(String.join(", ", parts)).append("\n");
            }
            catch (Exception e)
            {
                sb.append("（表结构读取失败）\n");
            }
        }
        return sb.toString();
    }

    private String getConfig(String key)
    {
        String val = configService.selectConfigByKey(key);
        return val != null ? val.trim() : "";
    }

    private String abbreviate(String text, int maxLen)
    {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
