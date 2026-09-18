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
import com.ruoyi.ai.util.SensitiveDataMasker;
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
    public Map<String, Object> query(String question, java.util.function.BiConsumer<String, String> stage)
    {
        String userInfo = null;
        try
        {
            com.ruoyi.common.core.domain.entity.SysUser u = com.ruoyi.common.utils.SecurityUtils.getLoginUser().getUser();
            userInfo = "账号:" + u.getUserName() + "，昵称:" + u.getNickName()
                    + (u.getDept() != null ? "，部门:" + u.getDept().getDeptName() : "");
        }
        catch (Exception ignore) { }
        return queryWithStages(question, getLoginPermissions(), userInfo, stage);
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
        // 脱敏还原映射：问题进入大模型前会被脱敏（身份证/手机号/银行卡保留前3后4），
        // AI生成的SQL只能拿到打码值，直接等值查询必然查不到；执行前需还原为原文
        Map<String, String> sensitiveMap = SensitiveDataMasker.maskedToOriginal(question);

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
                + "5. 严格按用户问题确定查询范围：只添加用户问题明确提到的过滤条件，"
                + "禁止自行增加用户未提及的时间范围（如\"本月\"）、状态过滤或其他条件；"
                + "排名/统计/分布类问题统计全部有效数据即可（del_flag 有效数据过滤除外）；"
                + "是非类问题（\"有没有/是否存在/是否做过\"）应到对应业务记录表（工单/故障/出入库/审批记录等）"
                + "用 COUNT(*) 计数直接回答有无，禁止改查设备/物料/客户等主档信息表\n"
                + "6. 必须带 LIMIT（不超过" + MAX_ROWS + "）\n"
                + "7. 只输出一条SQL，用```sql```包裹，不要输出任何解释文字\n"
                + "8. 如问题超出可查表范围，输出：```sql\nSELECT '超出可查询范围' AS 提示\n```\n"
                + (userInfo != null ? "8. 当前登录用户信息：" + userInfo + "。凡涉及\"我/当前用户/我的部门\"的问题，直接使用该信息（如需其余属性可查sys_user表 user_name='" 
                    + (userInfo.contains("账号:") ? userInfo.split("账号:")[1].split("，")[0] : "") + "'），不要反问\n" : "")
                + "9. 已知数据口径（必须遵守，防止跨表ID错配）："
                + "HR员工表 biz_hr_employee 的 org_id 字段实际存储的是 sys_dept.dept_id（员工归属部门以 sys_dept 为准），"
                + "统计部门/组织及其人数时必须用 sys_dept 关联 biz_hr_employee（e.org_id = d.dept_id），"
                + "禁止用 biz_hr_organization.org_id 去关联 biz_hr_employee.org_id（两者是不同的ID体系，关联必为空）；"
                + "org_level 等层级字段也只对 biz_hr_organization 自身有意义，如需部门层级请使用 sys_dept 的 parent_id/ancestors\n"
                + "\n用户问题：" + question;

        fireStage(stage, "sql", "start");
        AiResult ai = chatWithRetry(prompt, 12);
        if (!ai.isSuccess())
        {
            fireStage(stage, "sql", "done");
            result.put("error", "SQL生成失败：" + StringUtils.nvl(ai.getError(), "请稍后重试"));
            return result;
        }
        fireStage(stage, "sql", "done");

        // 4. 提取并校验SQL
        String sql = restoreSensitive(extractSql(ai.getContent()), sensitiveMap);
        if (StringUtils.isEmpty(sql))
        {
            result.put("error", "未能从AI回复中解析出SQL");
            return result;
        }
        // 模型自拒占位语句：问题超出白名单范围
        if (sql.toLowerCase().contains("超出可查询范围"))
        {
            // 修复：阶段一选表偏差时，用关键词重选表重试一次（选表失败是"超出范围"误判的主因）
            if (related.size() < allowed.size())
            {
                log.warn("NL2Query 选表偏差(related={})，关键词重选后重试", related);
                List<String> repicked = pickTablesByKeyword(question, allowed, 12);
                String fullPrompt = "你是SQL生成助手。根据用户问题生成一条 MySQL 只读查询 SQL。\n"
                        + "硬性要求：\n"
                        + "1. 只能使用以下可查表（格式：表名：字段名(中文注释)）：\n" + buildSchemaDesc(repicked) + "\n"
                        + "2. 只能生成 SELECT 语句，禁止任何增删改；只能使用上表中列出的真实字段名，禁止编造或猜测字段；不确定字段是否存在时宁可不查该列\n"
                        + "3. 查询列必须使用字段的中文注释作为别名（AS 中文名），例如 fault_count AS 故障次数；"
                        + "禁止 SELECT *\n"
                        + "4. 必须带 LIMIT（不超过" + MAX_ROWS + "）\n"
                        + "5. 只输出一条SQL，用```sql```包裹，不要输出任何解释文字\n"
                        + "6. 如问题确实超出可查表范围，输出：```sql\nSELECT '超出可查询范围' AS 提示\n```\n"
                        + "\n用户问题：" + question;
                AiResult retry = chatWithRetry(fullPrompt, 12);
                if (retry.isSuccess())
                {
                    String retrySql = restoreSensitive(extractSql(retry.getContent()), sensitiveMap);
                    if (StringUtils.isNotEmpty(retrySql) && !retrySql.toLowerCase().contains("超出可查询范围"))
                    {
                        retrySql = ensureLimit(retrySql);
                        String retryErr = validate(retrySql, allowed);
                        if (retryErr == null)
                        {
                            sql = retrySql;
                        }
                    }
                }
            }
            if (sql.toLowerCase().contains("超出可查询范围"))
            {
                result.put("error", "该问题涉及的数据不在可查询范围内（仅白名单表数据可查）");
                return result;
            }
        }
        // 4.1 自动补LIMIT：AI偶发遗漏时直接追加（表白名单/只读校验在后，补LIMIT是安全收窄，避免死路报错）
        sql = ensureLimit(sql);
        String validateError = validate(sql, allowed);
        // 4.2 幻觉表名重试：AI引用了不存在的表时（选表失败是主因），用关键词重选表重新生成一次
        if (validateError != null && validateError.startsWith("AI引用了不存在的表"))
        {
            log.warn("NL2Query 模型幻觉表名，关键词重选后重试: {}", validateError);
            if (related.size() < allowed.size())
            {
                List<String> repicked = pickTablesByKeyword(question, allowed, 12);
                repicked.addAll(related);
                String retryPrompt = "你是SQL生成助手。根据用户问题生成一条 MySQL 只读查询 SQL。\n"
                        + "硬性要求：\n"
                        + "1. 只能使用以下可查表（格式：表名：字段名(中文注释)）：\n" + buildSchemaDesc(repicked) + "\n"
                        + "2. 只能生成 SELECT 语句，禁止任何增删改；只能使用上表中列出的真实字段名，禁止编造或猜测字段或表名（表名必须逐字取自清单）；不确定字段是否存在时宁可不查该列\n"
                        + "3. 查询列必须使用字段的中文注释作为别名（AS 中文名），例如 fault_count AS 故障次数；"
                        + "函数计算列也要起直观中文名，例如 COUNT(*) AS 工单数量；没有中文注释的字段才保留原名；"
                        + "禁止 SELECT *\n"
                        + "4. 必须带 LIMIT（不超过" + MAX_ROWS + "）\n"
                        + "5. 只输出一条SQL，用```sql```包裹，不要输出任何解释文字\n"
                        + "\n用户问题：" + question;
                AiResult retry = chatWithRetry(retryPrompt, 12);
                if (retry.isSuccess())
                {
                    String retrySql = restoreSensitive(extractSql(retry.getContent()), sensitiveMap);
                    if (StringUtils.isNotEmpty(retrySql))
                    {
                        retrySql = ensureLimit(retrySql);
                        String retryErr = validate(retrySql, allowed);
                        if (retryErr == null)
                        {
                            sql = retrySql;
                            validateError = null;
                        }
                    }
                }
            }
        }
        if (validateError != null)
        {
            log.warn("NL2Query SQL校验不通过: {} -> {}", sql, validateError);
            result.put("error", "生成的SQL未通过安全校验：" + validateError);
            result.put("sql", sql);
            return result;
        }

        // 5. 执行查询（失败自动修复重试一次；先做完整性预检，AI输出截断的残缺SQL不送数据库执行）
        fireStage(stage, "exec", "start");
        List<Map<String, Object>> rows = null;
        String execSql = sql;
        String integrity = checkSqlIntegrity(execSql);
        Exception firstErr = null;
        if (integrity == null)
        {
            try
            {
                rows = executeQuery(execSql);
            }
            catch (Exception e)
            {
                firstErr = e;
            }
        }
        else
        {
            firstErr = new ServiceException(integrity);
        }
        if (firstErr != null)
        {
            String dbErr = friendlyDbError(firstErr);
            log.error("NL2Query SQL执行失败，尝试AI自动修复: {} -> {}", execSql, dbErr);
            String fixed = restoreSensitive(repairSqlWithAi(question, execSql, dbErr, allowed), sensitiveMap);
            if (fixed == null)
            {
                fireStage(stage, "exec", "done");
                result.put("error", "SQL执行失败：" + dbErr);
                result.put("sql", execSql);
                return result;
            }
            fixed = ensureLimit(fixed);
            String fixError = validate(fixed, allowed);
            if (fixError != null)
            {
                result.put("error", "SQL执行失败：" + dbErr);
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
                log.error("NL2Query 修复后SQL仍执行失败: {} -> {}", execSql, friendlyDbError(e2));
                result.put("error", "SQL执行失败：" + friendlyDbError(e2));
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
        // 字典翻译：结果列命中"字段注释含字典/内联枚举"时，把代码值替换为字典中文名
        translateDictValues(rows, execSql);
        // 空结果语义区分：执行成功但0行 ≠ 查询失败，明确告知用户"数据不存在"而非"问法有问题"
        List<String> columns = rows.isEmpty() ? new ArrayList<>() : new ArrayList<>(rows.get(0).keySet());
        // AI结论生成：把结果回喂给大模型，产出面向业务用户的"说人话"回答（失败静默降级为纯表格）
        fireStage(stage, "summary", "start");
        String summary = summarizeResult(question, columns, rows);
        fireStage(stage, "summary", "done");
        if (StringUtils.isNotEmpty(summary))
        {
            result.put("summary", summary);
        }
        if (rows.isEmpty())
        {
            result.put("empty", Boolean.TRUE);
            result.put("emptyReason", StringUtils.isNotEmpty(summary) ? summary
                    : "查询执行成功，但没有查到匹配数据——查询对象可能尚无关联业务记录"
                    + "（如物料未入库、设备无工单/故障记录、单据尚未产生）。可点击【查看 SQL】核对查询范围，"
                    + "或到对应业务模块确认数据是否已录入。");
        }
        result.put("columns", columns);
        result.put("rows", rows);
        result.put("sql", execSql);
        result.put("total", rows.size());
        return result;
    }

    /**
     * AI结论生成：把查询结果回喂给大模型，产出面向业务用户的"说人话"回答。
     * NL2SQL与工具路由两条链路复用；失败静默返回null（前端降级为纯表格展示，不影响查询本身）。
     */
    @Override
    public String summarizeResult(String question, List<String> columns, List<Map<String, Object>> rows)
    {
        try
        {
            StringBuilder data = new StringBuilder();
            data.append("列：").append(String.join(" | ", columns)).append("\n");
            int total = rows == null ? 0 : rows.size();
            int n = Math.min(total, 15);
            for (int i = 0; i < n; i++)
            {
                List<String> vals = new ArrayList<>();
                for (Object v : rows.get(i).values())
                {
                    String s = v == null ? "空" : String.valueOf(v);
                    if (s.length() > 40) { s = s.substring(0, 40) + "…"; }
                    vals.add(s);
                }
                data.append("行").append(i + 1).append("：").append(String.join(" | ", vals)).append("\n");
            }
            if (total > n)
            {
                data.append("（共").append(total).append("行，仅展示前").append(n).append("行）\n");
            }
            if (n == 0)
            {
                data.append("（查询结果为空，0行）\n");
            }
            String prompt = "你是数据分析助手。用户提问：" + question + "\n"
                    + "查询得到的表格数据：\n" + data + "\n"
                    + "请用中文直接回答用户的问题：1~3句话，口语化、面向不懂技术的业务人员；"
                    + "直接给结论（有没有/是多少/排名如何），点出关键数字；"
                    + "不要罗列列名、不要复述表格结构、不要使用任何markdown符号。\n"
                    + (n == 0 ? "结果为空时：明确说\"没有查到相关记录\"，并用一句话给出最可能的原因（如该对象尚无相关业务发生）。" : "");
            AiResult ai = chatWithRetry(prompt, 12);
            if (!ai.isSuccess() || ai.getContent() == null)
            {
                return null;
            }
            String text = ai.getContent().trim();
            // 兜底清洗：去掉模型偶发包裹的```或引号
            if (text.startsWith("```"))
            {
                text = text.replaceAll("^```[a-z]*\\s*", "").replaceAll("```\\s*$", "").trim();
            }
            if (text.startsWith("\"") && text.endsWith("\"") && text.length() > 1)
            {
                text = text.substring(1, text.length() - 1).trim();
            }
            if (text.length() > 300)
            {
                text = text.substring(0, 300) + "…";
            }
            return text.isEmpty() ? null : text;
        }
        catch (Exception e)
        {
            log.warn("NL2Query 结论生成失败（降级纯表格）: {}", e.getMessage());
            return null;
        }
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
            String prompt = "从以下数据表清单中，选出回答用户问题最相关的业务数据表（不超过8张）。\n"
                    + "注意：ai_knowledge_*/ai_qa_history 是AI系统内部表（存的是用户提问和文档切片，不是业务数据），"
                    + "除非用户明确要查『AI问答记录/知识库文档』，否则不要选它们；"
                    + "业务数据在 dms_/wms_/mk_/pms_/qms_/mms_/biz_/sys_/flow_ 前缀表中。\n"
                    + "只输出逗号分隔的表名，不要任何解释。\n\n表清单：\n" + listDesc + "\n用户问题：" + question;
            // 模型服务偶发抖动时自动重试（chatWithRetry 内置2次重试）
            AiResult ai = chatWithRetry(prompt, 12);
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
            log.warn("NL2Query 相关表挑选失败，退回本地关键词匹配: {}", e.getMessage());
        }
        // 兜底：挑选失败时用本地关键词匹配（表名+注释与问题字符重叠度打分），避免全量表撑爆提示词
        return pickTablesByKeyword(question, allowed, 8);
    }

    /** 本地关键词选表兜底：问题与表名/表注释的字符重叠打分，取前N张 */
    private List<String> pickTablesByKeyword(String question, List<String> allowed, int topN)
    {
        Map<String, String[]> meta = getMetaCache();
        String q = question.toLowerCase();
        List<Map.Entry<String, Integer>> scored = new ArrayList<>();
        for (String table : allowed)
        {
            String[] m = meta.get(table);
            String nameAndComment = table + (m != null && m[0] != null ? m[0] : "");
            int score = 0;
            // 滑动2元组重叠计分（中文友好）
            for (int i = 0; i + 2 <= nameAndComment.length(); i++)
            {
                if (q.contains(nameAndComment.substring(i, i + 2)))
                {
                    score++;
                }
            }
            if (score > 0)
            {
                scored.add(new java.util.AbstractMap.SimpleEntry<>(table, score));
            }
        }
        scored.sort((a, b) -> b.getValue() - a.getValue());
        List<String> picked = new ArrayList<>();
        for (Map.Entry<String, Integer> e : scored)
        {
            picked.add(e.getKey());
            if (picked.size() >= topN)
            {
                break;
            }
        }
        if (picked.isEmpty())
        {
            // 完全无匹配时至少取前缀分组代表表，避免全量
            picked.addAll(allowed.subList(0, Math.min(topN, allowed.size())));
        }
        return picked;
    }

    /** SQL完整性预检：AI输出偶发被截断（token超限/连接中断），残缺SQL直接执行只会得到含混的语法错误，预检后直接进AI修复流程 */
    private String checkSqlIntegrity(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return "生成的SQL为空，可能AI输出被截断";
        }
        long quotes = sql.chars().filter(c -> c == '\'').count();
        if (quotes % 2 != 0)
        {
            return "生成的SQL不完整（引号未闭合，AI输出可能被截断）";
        }
        long parens = 0;
        for (char c : sql.toCharArray())
        {
            if (c == '(') { parens++; }
            else if (c == ')') { parens--; }
        }
        if (parens != 0)
        {
            return "生成的SQL不完整（括号不配对，AI输出可能被截断）";
        }
        String tail = sql.trim();
        if (tail.endsWith(",") || tail.endsWith("+") || tail.endsWith("(") || tail.endsWith("="))
        {
            return "生成的SQL不完整（以运算符/逗号结尾，AI输出可能被截断）";
        }
        return null;
    }

    /** 提取数据库报错根因：BadSqlGrammarException 的长消息前面全是SQL文本，真实原因（如Unknown column）在根因异常里，直接截断会把关键信息截掉 */    private String friendlyDbError(Exception e)
    {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t)
        {
            t = t.getCause();
        }
        String msg = t.getMessage();
        if (msg == null || msg.isEmpty())
        {
            msg = e.getMessage();
        }
        return abbreviate(msg, 200);
    }

    /** 执行失败时，把数据库错误回喂给AI修正SQL（一次机会） */
    private String repairSqlWithAi(String question, String sql, String dbError, List<String> allowed)
    {
        // 修复阶段只喂"SQL已引用表 + 问题关键词匹配表"的结构，避免全量schema撑爆prompt导致AI服务连接重置（与两阶段生成同思路）
        List<String> related = new ArrayList<>();
        java.util.regex.Matcher m = Pattern.compile("(?:from|join)\\s+([a-z0-9_]+)", Pattern.CASE_INSENSITIVE).matcher(sql);
        while (m.find())
        {
            String t = m.group(1).toLowerCase();
            if (allowed.contains(t) && !related.contains(t))
            {
                related.add(t);
            }
        }
        for (String t : pickTablesByKeyword(question, allowed, 6))
        {
            if (!related.contains(t))
            {
                related.add(t);
            }
        }
        if (related.size() > 12)
        {
            related = new ArrayList<>(related.subList(0, 12));
        }
        String prompt = "你之前根据用户问题生成的MySQL只读查询SQL执行失败。请修正后重新输出。\n"
                + "可查表结构：\n" + buildSchemaDesc(related) + "\n"
                + "用户问题：" + question + "\n"
                + "原SQL：\n" + sql + "\n"
                + "数据库错误：" + dbError + "\n"
                + "要求：只能使用表结构中列出的真实字段名，修正后只输出一条SQL，用```sql```包裹，不要任何解释。";
        AiResult ai = chatWithRetry(prompt, 12);
        if (!ai.isSuccess())
        {
            return null;
        }
        return extractSql(ai.getContent());
    }

    // ==================== 可查表解析（黑名单模式） ====================

    /** 默认放行域：业务模块 + 系统管理 + 工作流（qrtz_/gen_ 等纯技术表天然不可查） */
    private static final String[] ALLOWED_PREFIXES = { "dms_", "wms_", "mk_", "pms_", "qms_", "mms_", "biz_", "sys_", "flow_" };

    /** 系统日志表例外：虽命中 *_log 排除规则，但对管理查询有价值，显式放行 */
    private static final Set<String> LOG_TABLE_ALLOW = new java.util.HashSet<>(java.util.Arrays.asList(
            "sys_oper_log", "sys_logininfor", "sys_job_log", "sys_data_change_log",
            "wms_inventory_log"));

    /**
     * 人员展示类系统表例外（用户明确要求）：用户表/组织部门表/岗位表等不加入黑名单，
     * 登录即可用于 JOIN 展示人员姓名、部门、岗位；敏感字段仍由 FORBIDDEN_COLUMNS SQL 级硬拦截。
     */
    private static final Set<String> HR_SYS_TABLE_ALLOW = new java.util.HashSet<>(java.util.Arrays.asList(
            "sys_user", "sys_dept", "sys_post", "sys_role",
            "sys_user_role", "sys_user_post", "sys_role_dept"));

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
                // 权限联动裁剪：管理员权限串为 *:*:* 全通过；
                // 人员展示类 sys_ 表（sys_user/sys_dept/sys_post 等）按用户要求放开，登录即可查（用于JOIN展示姓名/部门/岗位）
                if (name.startsWith("sys_") && !HR_SYS_TABLE_ALLOW.contains(name)
                        && !hasAnyPerm(userPerms, "system:", "monitor:"))
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
        if (!lower.replaceAll("\\s+", " ").contains(" limit ")
                && !lower.endsWith(" limit " + MAX_ROWS)
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
                // 区分两类情形：库中根本不存在 = 模型幻觉表名（自动重试）；真实存在但被黑名单/权限裁剪 = 真实越界
                if (!getMetaCache().containsKey(table))
                {
                    log.warn("NL2Query 模型幻觉表名: {}", table);
                    return "AI引用了不存在的表 " + table + "（模型幻觉，需自动重试）";
                }
                return "表 " + table + " 不在可查范围内";
            }
        }
        if (!foundTable)
        {
            return "未识别到查询表";
        }
        return null;
    }

    /**
     * 带自动重试的AI调用：外部大模型接口偶发抖动（连接重置/超时/瞬时5xx）时自动重试，
     * 共尝试最多 1+RETRY_TIMES 次，每次间隔 RETRY_INTERVAL_MS 毫秒，避免用户直接看到"AI调用失败"。
     */
    private AiResult chatWithRetry(String prompt, int maxTokens)
    {
        final int retryTimes = 2;
        final long retryIntervalMs = 1000;
        AiResult ai = aiClient.chatWithTimeout("platform", "nl2query", prompt, maxTokens);
        for (int i = 0; i < retryTimes && !ai.isSuccess(); i++)
        {
            log.warn("NL2Query AI调用失败(第{}次重试): {}", i + 1, StringUtils.nvl(ai.getError(), "未知错误"));
            try
            {
                Thread.sleep(retryIntervalMs);
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt();
                break;
            }
            ai = aiClient.chatWithTimeout("platform", "nl2query", prompt, maxTokens);
        }
        return ai;
    }

    /** 将AI生成SQL中的脱敏值还原为用户原始输入值（仅用于送库执行，不用于展示） */    private String restoreSensitive(String sql, Map<String, String> sensitiveMap)
    {
        if (sql == null || sensitiveMap == null || sensitiveMap.isEmpty())
        {
            return sql;
        }
        String restored = SensitiveDataMasker.restore(sql, sensitiveMap);
        if (!restored.equals(sql))
        {
            log.info("NL2Query SQL脱敏值已还原后执行");
        }
        return restored;
    }

    /** 从LLM回复中提取SQL（```sql```包裹、裸SELECT、或夹杂思考文本时扫描首个SELECT/WITH） */    private String extractSql(String content)
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

    /** 自动补LIMIT：AI偶发遗漏LIMIT时追加默认上限（表白名单/只读校验在validate中，补LIMIT是安全收窄）。
     * 注意：必须先把换行/制表符归一化为空格再检测，否则AI把LIMIT单独放一行（换行结尾）时会被误判为缺失，导致 "LIMIT 100 LIMIT 100" 语法错误 */
    private String ensureLimit(String sql)
    {
        if (sql == null)
        {
            return sql;
        }
        String normalized = sql.toLowerCase().replaceAll("\\s+", " ");
        if (normalized.contains(" limit "))
        {
            return sql;
        }
        String s = sql.trim();
        if (s.endsWith(";"))
        {
            s = s.substring(0, s.length() - 1).trim();
        }
        return s + " LIMIT " + MAX_ROWS;
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

    // ==================== 字典翻译（结果代码值 -> 字典中文名） ====================

    /** 字典数据缓存有效期：5分钟 */
    private static final long DICT_CACHE_TTL = 5 * 60 * 1000L;

    /** 字典数据缓存：dict_type -> (dict_value -> dict_label)，按dict_sort保序 */
    private volatile Map<String, Map<String, String>> dictCache;

    private volatile long dictCacheAt = 0;

    /** 列字典映射缓存有效期：5分钟 */
    private static final long COL_DICT_TTL = 5 * 60 * 1000L;

    /**
     * 列字典映射缓存："table.column" -> [zh名称, 值->中文映射Map, 是否权威映射]
     * 权威映射（authoritative=true）：字段注释含"字典 dms_xxx"或内联枚举（至少2组键值），按覆盖值直接翻译；
     * 非权威映射（authoritative=false）：注释无提示时按命名约定推导的字典
     *   （表名_列名 / 前缀_去下划线中段_列名 / 前缀_列名），仅在字典100%覆盖结果值时才翻译（防误译）。
     */
    private volatile Map<String, Object[]> colDictCache;

    private volatile long colDictCacheAt = 0;

    /** 获取（带缓存的）字典数据：dict_type -> value->label */
    private Map<String, Map<String, String>> getDictCache()
    {
        long now = System.currentTimeMillis();
        Map<String, Map<String, String>> cache = dictCache;
        if (cache != null && now - dictCacheAt < DICT_CACHE_TTL)
        {
            return cache;
        }
        synchronized (this)
        {
            if (dictCache != null && System.currentTimeMillis() - dictCacheAt < DICT_CACHE_TTL)
            {
                return dictCache;
            }
            Map<String, Map<String, String>> fresh = new HashMap<>();
            try
            {
                for (Map<String, Object> d : jdbcTemplate.queryForList(
                        "SELECT dict_type, dict_value, dict_label FROM sys_dict_data ORDER BY dict_type, dict_sort"))
                {
                    String type = String.valueOf(d.get("dict_type"));
                    String value = String.valueOf(d.get("dict_value"));
                    String label = String.valueOf(d.get("dict_label"));
                    if (label == null || "null".equals(label) || label.isEmpty()) { continue; }
                    fresh.computeIfAbsent(type, k -> new java.util.LinkedHashMap<>()).put(value, label);
                }
            }
            catch (Exception e)
            {
                log.warn("NL2Query 字典数据加载失败: {}", e.getMessage());
                if (cache != null) { return cache; }
            }
            dictCache = fresh;
            dictCacheAt = System.currentTimeMillis();
            return fresh;
        }
    }

    /** 获取（带缓存的）列->字典映射："table.column" -> [zh名称, 值->中文映射] */
    private Map<String, Object[]> getColDictCache()
    {
        long now = System.currentTimeMillis();
        Map<String, Object[]> cache = colDictCache;
        if (cache != null && now - colDictCacheAt < COL_DICT_TTL)
        {
            return cache;
        }
        synchronized (this)
        {
            if (colDictCache != null && System.currentTimeMillis() - colDictCacheAt < COL_DICT_TTL)
            {
                return colDictCache;
            }
            Map<String, Object[]> fresh = new HashMap<>();
            try
            {
                Map<String, Map<String, String>> dicts = getDictCache();
                java.util.regex.Pattern dm = Pattern.compile("字典\\s*([a-z][a-z0-9_]*)", Pattern.CASE_INSENSITIVE);
                // 内联枚举：数字代码+紧随中文/字母名称，如 "0正常 1响应超时"；"（1-5星）"这类区间描述不匹配（代码后紧跟非名称分隔符）
                java.util.regex.Pattern em = Pattern.compile("\\b(\\d{1,3})\\s*([\\u4e00-\\u9fa5A-Za-z]{2,10})");
                for (Map<String, Object> c : jdbcTemplate.queryForList(
                        "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND COLUMN_COMMENT IS NOT NULL AND COLUMN_COMMENT != ''"))
                {
                    String table = String.valueOf(c.get("TABLE_NAME")).toLowerCase();
                    String col = String.valueOf(c.get("COLUMN_NAME")).toLowerCase();
                    String comment = String.valueOf(c.get("COLUMN_COMMENT"));
                    comment = comment.trim();
                    if ("null".equals(comment) || comment.isEmpty()) { continue; }
                    // 中文主名：取括号/全角括号前的部分（如"工单状态（字典 dms_order_status）"->"工单状态"）
                    String zh = comment.split("[（(]")[0].trim();
                    Map<String, String> vm = null;
                    java.util.regex.Matcher dmm = dm.matcher(comment);
                    if (dmm.find())
                    {
                        Map<String, String> d = dicts.get(dmm.group(1).toLowerCase());
                        if (d != null && !d.isEmpty()) { vm = d; }
                    }
                    if (vm == null)
                    {
                        Map<String, String> inline = new java.util.LinkedHashMap<>();
                        java.util.regex.Matcher emm = em.matcher(comment);
                        boolean ok = true;
                        while (emm.find())
                        {
                            String key = emm.group(1);
                            String label = emm.group(2);
                            if (inline.containsKey(key)) { ok = false; break; }
                            inline.put(key, label);
                        }
                        if (ok && inline.size() >= 2) { vm = inline; }
                    }
                    if (vm != null)
                    {
                        fresh.put(table + "." + col, new Object[]{ zh, vm, Boolean.TRUE });
                        continue;
                    }
                    // 命名约定兜底：注释无提示时按 表名_列名 / 前缀_去下划线中段_列名 / 前缀_列名 推导字典
                    String prefix = table.split("_")[0];
                    String rest = table.contains("_") ? table.substring(table.indexOf('_') + 1) : "";
                    String[] conventionDicts = {
                            table + "_" + col,
                            rest.isEmpty() ? null : prefix + "_" + rest.replace("_", "") + "_" + col,
                            table.replace("_", "") + "_" + col,
                            prefix + "_" + col
                    };
                    Map<String, String> conv = null;
                    for (String dt : conventionDicts)
                    {
                        if (dt == null) { continue; }
                        Map<String, String> d = dicts.get(dt);
                        if (d != null && !d.isEmpty()) { conv = d; break; }
                    }
                    if (conv != null)
                    {
                        fresh.put(table + "." + col, new Object[]{ zh, conv, Boolean.FALSE });
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("NL2Query 列字典映射加载失败: {}", e.getMessage());
                if (cache != null) { return cache; }
            }
            colDictCache = fresh;
            colDictCacheAt = System.currentTimeMillis();
            return fresh;
        }
    }

    /**
     * 字典翻译：把结果集中代码值替换为字典中文名。
     * 列匹配规则：结果列名（中文别名或原始字段名）与 SQL 引用表中"带字典映射的列"的中文主名/字段名匹配；
     * 多候选时按"值覆盖率"择优，覆盖率相同或无匹配则不翻译（避免误译）。
     */
    private void translateDictValues(List<Map<String, Object>> rows, String sql)
    {
        if (rows == null || rows.isEmpty()) { return; }
        try
        {
            // SQL 引用的表（与 validate 同源正则）
            java.util.Set<String> refTables = new java.util.HashSet<>();
            java.util.regex.Matcher m = Pattern.compile("(?:from|join)\\s+([a-z0-9_]+)", Pattern.CASE_INSENSITIVE).matcher(sql);
            while (m.find())
            {
                refTables.add(m.group(1).toLowerCase());
            }
            if (refTables.isEmpty()) { return; }
            Map<String, Object[]> colDict = getColDictCache();
            // 候选：引用表中所有带字典映射的列 {column全名 -> [zh, 值映射]}
            Map<String, Object[]> candidates = new HashMap<>();
            for (Map.Entry<String, Object[]> e : colDict.entrySet())
            {
                String table = e.getKey().split("\\.")[0];
                if (refTables.contains(table))
                {
                    candidates.put(e.getKey(), e.getValue());
                }
            }
            if (candidates.isEmpty()) { return; }
            // 每个结果列择优匹配一个字典映射，然后整列翻译
            Map<String, Map<String, String>> dictMaps = new HashMap<>();
            for (String col : rows.get(0).keySet())
            {
                java.util.Set<String> values = new java.util.HashSet<>();
                for (Map<String, Object> row : rows)
                {
                    Object v = row.get(col);
                    if (v != null) { values.add(String.valueOf(v).trim()); }
                }
                if (values.isEmpty()) { continue; }
                String bestKey = null;
                int bestScore = 0;
                for (Map.Entry<String, Object[]> cand : candidates.entrySet())
                {
                    String zh = (String) cand.getValue()[0];
                    String colName = cand.getKey().split("\\.")[1];
                    boolean authoritative = Boolean.TRUE.equals(cand.getValue()[2]);
                    // 名称匹配：中文主名与结果列名互相包含（均需长度>1），或原始字段名直等
                    boolean byZh = zh.equals(col) || (zh.length() > 1 && col.length() > 1
                            && (zh.contains(col) || col.contains(zh)));
                    boolean byCol = colName.equals(col.toLowerCase());
                    if (!byZh && !byCol) { continue; }
                    @SuppressWarnings("unchecked")
                    Map<String, String> vm = (Map<String, String>) cand.getValue()[1];
                    int cover = 0;
                    for (String v : values)
                    {
                        if (vm.containsKey(v)) { cover++; }
                    }
                    // 评分：权威映射（注释声明/内联枚举）覆盖即用；约定推导映射必须100%覆盖结果值才允许
                    int score;
                    if (authoritative)
                    {
                        score = cover > 0 ? 1000 + cover : 0;
                    }
                    else
                    {
                        score = cover == values.size() ? 500 + cover : 0;
                    }
                    if (score > bestScore)
                    {
                        bestScore = score;
                        bestKey = cand.getKey();
                    }
                }
                if (bestKey != null)
                {
                    @SuppressWarnings("unchecked")
                    Map<String, String> vm = (Map<String, String>) candidates.get(bestKey)[1];
                    dictMaps.put(col, vm);
                }
            }
            if (dictMaps.isEmpty()) { return; }
            for (Map<String, Object> row : rows)
            {
                for (Map.Entry<String, Map<String, String>> e : dictMaps.entrySet())
                {
                    String col = e.getKey();
                    Object v = row.get(col);
                    if (v == null) { continue; }
                    String label = e.getValue().get(String.valueOf(v).trim());
                    if (label != null)
                    {
                        row.put(col, label);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.warn("NL2Query 字典翻译失败: {}", e.getMessage());
        }
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
