package com.ruoyi.ai.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
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

    /** SQL执行超时（秒） */
    private static final int EXEC_TIMEOUT_SECONDS = 15;

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

    /** NL2Query 专用模板：15秒 queryTimeout，超时由MySQL驱动真正终止语句并归还连接（防连接池泄漏耗尽） */
    private org.springframework.jdbc.core.JdbcTemplate nlJdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void initNlJdbcTemplate()
    {
        nlJdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(jdbcTemplate.getDataSource());
        nlJdbcTemplate.setQueryTimeout(EXEC_TIMEOUT_SECONDS);
    }

    @Override
    public Map<String, Object> query(String question)
    {
        String userInfo = null;
        try
        {
            com.ruoyi.common.core.domain.entity.SysUser u = com.ruoyi.common.utils.SecurityUtils.getLoginUser().getUser();
            userInfo = "账号:" + u.getUserName() + "，昵称:" + u.getNickName()
                    + (u.getDept() != null ? "，部门:" + u.getDept().getDeptName() : "");
        }
        catch (Exception ignore) { }
        return queryWithStages(question, getLoginPermissions(), userInfo, null);
    }

    @Override
    public Map<String, Object> queryWithStages(String question, Set<String> userPerms, String userInfo, BiConsumer<String, String> stage)
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
            result.put("error", "智能查询未开启，请管理员在【AI中心-配置】中开启智能查数");
            return result;
        }

        // 2. 可查表集合 = 业务模块前缀放行 - 黑名单（黑名单优先级最高）
        //    系统表(sys_)/工作流表(flow_)额外要求用户具备对应管理权限，按登录人权限动态裁剪
        List<String> allowed = resolveAllowedTables(userPerms);

        // 3. 两阶段生成：先选相关表（紧凑清单），再仅对相关表生成SQL（大幅减小提示词，提速且避免连接被重置）
        fireStage(stage, "tables", "start");
        List<String> related = selectRelatedTables(question, allowed);
        fireStage(stage, "tables", "done");
        String schemaDesc = buildSchemaDesc(related);
        String prompt = "你是SQL生成助手。根据用户问题生成一条 MySQL 只读查询 SQL。\n"
                + "硬性要求：\n"
                + "1. 只能使用以下可查表（格式：表名：字段名(中文注释)）：\n" + schemaDesc + "\n"
                + "2. 只能生成 SELECT 语句，禁止任何增删改；只能使用上表中列出的真实字段名，禁止编造或猜测字段；不确定字段是否存在时宁可不查该列\n"
                + "3. 查询列必须使用字段的中文注释作为别名（AS 中文名），例如 fault_count AS 故障次数；"
                + "函数计算列也要起直观中文名，例如 COUNT(*) AS 工单数量；没有中文注释的字段才保留原名；"
                + "禁止 SELECT *\n"
                + "4. 结果面向普通业务用户（不懂技术、只关心业务）：优先展示人能看懂的信息——"
                + "涉及用户/操作人/审批人时，必须 JOIN 用户表展示昵称(user_name对应的nick_name)和部门名称(dept_name)，不要只给英文账号；"
                + "涉及设备/物料/客户/供应商时用名称而非编码，编码列可与名称列并存但名称列在前；"
                + "状态/类型等代码值如有对应中文名请一并翻译为中文或附中文名列\n"
                + "5. 必须带 LIMIT（不超过" + MAX_ROWS + "）\n"
                + "6. 只输出一条SQL，用```sql```包裹，不要输出任何解释文字\n"
                + "7. 如问题超出可查表范围，输出：```sql\nSELECT '超出可查询范围' AS 提示\n```\n"
                + (userInfo != null ? "8. 当前登录用户信息：" + userInfo + "。凡涉及\"我/当前用户/我的部门\"的问题，直接使用该信息（如需其余属性可查sys_user表 user_name='" 
                    + (userInfo.contains("账号:") ? userInfo.split("账号:")[1].split("，")[0] : "") + "'），不要反问\n" : "")
                + "\n用户问题：" + question;

        fireStage(stage, "sql", "start");
        AiResult ai = aiClient.chatWithTimeout("platform", "nl2query", prompt, 12);
        if (!ai.isSuccess())
        {
            fireStage(stage, "sql", "done");
            result.put("error", "SQL生成失败：" + StringUtils.nvl(ai.getError(), "请稍后重试"));
            return result;
        }
        fireStage(stage, "sql", "done");

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

        // 5. 执行查询（失败自动修复重试一次）
        fireStage(stage, "exec", "start");
        List<Map<String, Object>> rows;
        String execSql = sql;
        try
        {
            rows = executeQuery(execSql);
        }
        catch (Exception e)
        {
            log.warn("NL2Query SQL执行失败，尝试AI自动修复: {} -> {}", execSql, e.getMessage());
            String fixed = repairSqlWithAi(question, execSql, e.getMessage(), allowed);
            if (fixed == null)
            {
                fireStage(stage, "exec", "done");
                result.put("error", "SQL执行失败：" + abbreviate(e.getMessage(), 200));
                result.put("sql", execSql);
                return result;
            }
            String fixError = validate(fixed, allowed);
            if (fixError != null)
            {
                result.put("error", "SQL执行失败：" + abbreviate(e.getMessage(), 200));
                result.put("sql", execSql);
                return result;
            }
            try
            {
                execSql = fixed;
                rows = executeQuery(execSql);
            }
            catch (Exception e2)
            {
                fireStage(stage, "exec", "done");
                result.put("error", "SQL执行失败：" + abbreviate(e2.getMessage(), 200));
                result.put("sql", execSql);
                return result;
            }
        }
        fireStage(stage, "exec", "done");
        if (rows.size() > MAX_ROWS)
        {
            rows = rows.subList(0, MAX_ROWS);
        }
        // 兜底：结果列仍为原始字段名时，映射为数据库中文注释
        Map<String, String> commentMap = buildColumnCommentMap(execSql, allowed);
        if (!rows.isEmpty() && !commentMap.isEmpty())
        {
            List<Map<String, Object>> renamed = new ArrayList<>();
            for (Map<String, Object> row : rows)
            {
                Map<String, Object> r = new java.util.LinkedHashMap<>();
                for (Map.Entry<String, Object> e : row.entrySet())
                {
                    String col = e.getKey();
                    r.put(commentMap.containsKey(col.toLowerCase()) ? commentMap.get(col.toLowerCase()) : col, e.getValue());
                }
                renamed.add(r);
            }
            rows = renamed;
        }
        List<String> columns = rows.isEmpty() ? new ArrayList<>() : new ArrayList<>(rows.get(0).keySet());
        result.put("columns", columns);
        result.put("rows", rows);
        result.put("sql", execSql);
        result.put("total", rows.size());
        return result;
    }

    /** 阶段回调触发（stage为null时静默跳过） */
    private void fireStage(BiConsumer<String, String> stage, String name, String status)
    {
        if (stage != null)
        {
            try
            {
                stage.accept(name, status);
            }
            catch (Exception ignore) { }
        }
    }

    /** 执行只读查询：queryTimeout 由MySQL驱动强制终止超时语句，连接立即归还池中 */
    private List<Map<String, Object>> executeQuery(String sql)
    {
        return nlJdbcTemplate.queryForList(sql);
    }

    // ==================== 元数据缓存（避免每次查询执行200+次information_schema查询） ====================

    /** 缓存有效期：5分钟 */
    private static final long META_CACHE_TTL = 5 * 60 * 1000L;

    /** 元数据缓存：key = 表名，value = [表注释, 字段描述串] */
    private volatile Map<String, String[]> metaCache;

    private volatile long metaCacheAt = 0;

    /** 获取（带缓存的）库表元数据：表名 -> [表注释, 字段描述] */
    private Map<String, String[]> getMetaCache()
    {
        long now = System.currentTimeMillis();
        Map<String, String[]> cache = metaCache;
        if (cache != null && now - metaCacheAt < META_CACHE_TTL)
        {
            return cache;
        }
        synchronized (this)
        {
            if (metaCache != null && System.currentTimeMillis() - metaCacheAt < META_CACHE_TTL)
            {
                return metaCache;
            }
            Map<String, String[]> fresh = new HashMap<>();
            try
            {
                // 表清单+注释：单次查询
                Map<String, String> tableComments = new HashMap<>();
                for (Map<String, Object> t : jdbcTemplate.queryForList(
                        "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()"))
                {
                    String comment = String.valueOf(t.get("TABLE_COMMENT"));
                    tableComments.put(String.valueOf(t.get("TABLE_NAME")).toLowerCase(),
                            comment != null && !"null".equals(comment) ? comment : "");
                }
                // 全部字段+注释：单次查询
                Map<String, List<String>> colsByTable = new HashMap<>();
                for (Map<String, Object> c : jdbcTemplate.queryForList(
                        "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME, ORDINAL_POSITION"))
                {
                    String table = String.valueOf(c.get("TABLE_NAME")).toLowerCase();
                    String col = String.valueOf(c.get("COLUMN_NAME"));
                    String comment = String.valueOf(c.get("COLUMN_COMMENT"));
                    String part = (comment != null && !comment.isEmpty() && !"null".equals(comment))
                            ? col + "(" + comment + ")" : col;
                    colsByTable.computeIfAbsent(table, k -> new ArrayList<>()).add(part);
                }
                for (Map.Entry<String, String> e : tableComments.entrySet())
                {
                    List<String> cols = colsByTable.getOrDefault(e.getKey(), new ArrayList<>());
                    fresh.put(e.getKey(), new String[]{ e.getValue(), String.join(", ", cols) });
                }
            }
            catch (Exception e)
            {
                log.warn("NL2Query 元数据缓存加载失败: {}", e.getMessage());
                if (cache != null) { return cache; }
            }
            metaCache = fresh;
            metaCacheAt = System.currentTimeMillis();
            return fresh;
        }
    }

    /** 阶段一：让AI从紧凑表清单（表名+注释）中挑选与问题相关的表（最多8张） */
    private List<String> selectRelatedTables(String question, List<String> allowed)
    {
        Map<String, String[]> meta = getMetaCache();
        StringBuilder listDesc = new StringBuilder();
        for (String table : allowed)
        {
            String[] m = meta.get(table);
            String comment = m != null ? m[0] : "";
            listDesc.append(table).append(comment != null && !comment.isEmpty() ? "(" + comment + ")" : "").append("\n");
        }
        try
        {
            String prompt = "从以下数据表清单中，选出回答用户问题最相关的表（不超过8张）。\n"
                    + "只输出逗号分隔的表名，不要任何解释。\n\n表清单：\n" + listDesc + "\n用户问题：" + question;
            // 模型服务偶发抖动时自动重试一次
            AiResult ai = aiClient.chatWithTimeout("platform", "nl2query", prompt, 12);
            if (!ai.isSuccess())
            {
                ai = aiClient.chatWithTimeout("platform", "nl2query", prompt, 12);
            }
            if (ai.isSuccess())
            {
                List<String> picked = new ArrayList<>();
                for (String t : ai.getContent().replaceAll("```", "").split("[,，\\s]+"))
                {
                    String name = t.trim().toLowerCase();
                    if (allowed.contains(name) && !picked.contains(name))
                    {
                        picked.add(name);
                    }
                    if (picked.size() >= 8)
                    {
                        break;
                    }
                }
                if (!picked.isEmpty())
                {
                    return picked;
                }
            }
        }
        catch (Exception e)
        {
            log.warn("NL2Query 相关表挑选失败，退回全量表: {}", e.getMessage());
        }
        // 兜底：挑选失败时退回原全量模式
        return allowed;
    }

    /** 执行失败时，把数据库错误回喂给AI修正SQL（一次机会） */
    private String repairSqlWithAi(String question, String sql, String dbError, List<String> allowed)
    {
        String prompt = "你之前根据用户问题生成的MySQL只读查询SQL执行失败。请修正后重新输出。\n"
                + "可查表结构：\n" + buildSchemaDesc(allowed) + "\n"
                + "用户问题：" + question + "\n"
                + "原SQL：\n" + sql + "\n"
                + "数据库错误：" + abbreviate(dbError, 300) + "\n"
                + "要求：只能使用表结构中列出的真实字段名，修正后只输出一条SQL，用```sql```包裹，不要任何解释。";
        AiResult ai = aiClient.chatWithTimeout("platform", "nl2query", prompt, 12);
        if (!ai.isSuccess())
        {
            return null;
        }
        return extractSql(ai.getContent());
    }

    // ==================== 可查表解析（黑名单模式） ====================

    /** 默认放行域：业务模块 + 系统管理 + 工作流（qrtz_/gen_ 等纯技术表天然不可查） */
    private static final String[] ALLOWED_PREFIXES = { "dms_", "wms_", "mk_", "pms_", "qms_", "mms_", "biz_", "ai_", "sys_", "flow_" };

    /** 系统日志表例外：虽命中 *_log 排除规则，但对管理查询有价值，显式放行 */
    private static final Set<String> LOG_TABLE_ALLOW = new java.util.HashSet<>(java.util.Arrays.asList(
            "sys_oper_log", "sys_logininfor", "sys_job_log", "sys_data_change_log"));

    /** 凭据类字段：任何 SQL 引用即拒绝（防 AI 拼出密码/密钥查询） */
    private static final Pattern FORBIDDEN_COLUMNS = Pattern.compile(
            "\\b(password|pwd|secret|api_key|apikey|access_token|secret_key|private_key|salt)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * 解析当前用户可查表：库中放行前缀表 - 黑名单（ai.nl2query.blockedTables，逗号分隔）。
     * 黑名单优先级最高；新增业务表自动可查，新增敏感表需显式加入黑名单。
     * 权限联动：sys_ 表需具备 system:/monitor: 权限；flow_ 表需具备 flow:/workflow: 权限。
     */
    private List<String> resolveAllowedTables(Set<String> userPerms)
    {
        Set<String> blocked = new java.util.HashSet<>();
        for (String t : getConfig("ai.nl2query.blockedTables").split(","))
        {
            if (StringUtils.isNotEmpty(t.trim()))
            {
                blocked.add(t.trim().toLowerCase());
            }
        }
        // 排除日志/快照/技术表（系统日志白名单例外）
        Pattern skip = Pattern.compile("(_log$|_log_|audit_log|approve_log|status_log|change_log|version_log|snapshot|number_rule|_prefix|trace_sn|push_log|collection_config|code_rule|stat_daily)");
        List<String> allowed = new ArrayList<>();
        try
        {
            List<Map<String, Object>> tabs = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE()");
            for (Map<String, Object> t : tabs)
            {
                String name = String.valueOf(t.get("TABLE_NAME")).toLowerCase();
                boolean biz = false;
                for (String p : ALLOWED_PREFIXES)
                {
                    if (name.startsWith(p)) { biz = true; break; }
                }
                if (!biz || blocked.contains(name) || (skip.matcher(name).find() && !LOG_TABLE_ALLOW.contains(name)))
                {
                    continue;
                }
                // 权限联动裁剪：管理员权限串为 *:*:* 全通过
                if (name.startsWith("sys_") && !hasAnyPerm(userPerms, "system:", "monitor:"))
                {
                    continue;
                }
                if (name.startsWith("flow_") && !hasAnyPerm(userPerms, "flow:", "workflow:"))
                {
                    continue;
                }
                allowed.add(name);
            }
        }
        catch (Exception e)
        {
            log.warn("NL2Query 可查表解析失败: {}", e.getMessage());
        }
        if (allowed.isEmpty())
        {
            // 兜底：走旧版显式白名单配置
            for (String t : getConfig("ai.nl2query.tables").split(","))
            {
                if (StringUtils.isNotEmpty(t.trim()))
                {
                    allowed.add(t.trim().toLowerCase());
                }
            }
        }
        return allowed;
    }

    /** 当前登录用户的权限串集合（与 @PreAuthorize 同源；未登录/异常返回空集） */
    private Set<String> getLoginPermissions()
    {
        try
        {
            return com.ruoyi.common.utils.SecurityUtils.getLoginUser().getPermissions();
        }
        catch (Exception e)
        {
            log.warn("NL2Query 获取登录人权限失败: {}", e.getMessage());
            return java.util.Collections.emptySet();
        }
    }

    /** 是否具备任一指定前缀的权限（管理员 *:*:* 直接通过） */
    private boolean hasAnyPerm(Set<String> perms, String... prefixes)
    {
        if (perms == null || perms.isEmpty())
        {
            return false;
        }
        if (perms.contains("*:*:*"))
        {
            return true;
        }
        for (String p : prefixes)
        {
            for (String perm : perms)
            {
                if (perm != null && perm.startsWith(p))
                {
                    return true;
                }
            }
        }
        return false;
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
        if (FORBIDDEN_COLUMNS.matcher(noTrailing).find())
        {
            return "包含凭据类敏感字段（密码/密钥不可查询）";
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
                return "表 " + table + " 不在可查范围内";
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

    /** 白名单表的字段描述（供LLM理解结构，走元数据缓存） */
    private String buildSchemaDesc(List<String> tables)
    {
        Map<String, String[]> meta = getMetaCache();
        StringBuilder sb = new StringBuilder();
        for (String table : tables)
        {
            String[] m = meta.get(table);
            sb.append("- ").append(table).append(m != null && !m[0].isEmpty() ? "(" + m[0] + ")" : "").append("：")
              .append(m != null ? m[1] : "（表结构读取失败）").append("\n");
        }
        return sb.toString();
    }

    /** 兜底列名映射：SQL 中出现的原始字段名 -> 数据库中文注释（仅含注释的） */
    private Map<String, String> buildColumnCommentMap(String sql, List<String> tables)
    {
        Map<String, String> map = new HashMap<>();
        // 提取 SQL 中引用的字段名（含别名前的原名）
        java.util.regex.Matcher m = Pattern.compile("\\b([a-z][a-z0-9_]*)\\b", Pattern.CASE_INSENSITIVE).matcher(sql);
        List<String> refs = new ArrayList<>();
        while (m.find())
        {
            refs.add(m.group(1).toLowerCase());
        }
        if (refs.isEmpty())
        {
            return map;
        }
        for (String table : tables)
        {
            try
            {
                List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                        "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_COMMENT IS NOT NULL "
                        + "AND COLUMN_COMMENT != ''", table);
                for (Map<String, Object> c : cols)
                {
                    String col = String.valueOf(c.get("COLUMN_NAME")).toLowerCase();
                    String comment = String.valueOf(c.get("COLUMN_COMMENT")).trim();
                    // SQL 中确实引用了该字段，且结果列可能就是它（未被 AS 改名）
                    if (refs.contains(col) && !comment.isEmpty() && !"null".equals(comment))
                    {
                        map.put(col, comment);
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("NL2Query 列注释读取失败: {} -> {}", table, e.getMessage());
            }
        }
        return map;
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
