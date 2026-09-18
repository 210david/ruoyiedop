package com.ruoyi.ai.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.domain.AiTool;
import com.ruoyi.ai.mapper.AiToolMapper;
import com.ruoyi.ai.service.IAiNlQueryService;
import com.ruoyi.ai.service.IAiToolRouteService;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI工具路由 Service实现（方案3：Text-to-API）
 *
 * 流程：用户问题 → LLM单次调用选工具+填参数（强制JSON） → 校验（工具白名单+参数Schema）
 *      → SQL模板参数化执行（进程内、只读、强制LIMIT） → 返回结构化结果
 * 降级：无匹配工具/路由失败时转发NL2SQL两阶段流程（长尾兜底）
 *
 * 安全红线：SQL模板来自管理员注册的ai_tool表（非LLM生成）；LLM只能选工具和填参数，
 *          无法注入SQL文本；参数全部走PreparedStatement占位符绑定，模板本身强制只含SELECT。
 *
 * @author ruoyi
 */
@Service
public class AiToolRouteServiceImpl implements IAiToolRouteService
{
    private static final Logger log = LoggerFactory.getLogger(AiToolRouteServiceImpl.class);

    @Autowired
    private AiToolMapper toolMapper;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private IAiNlQueryService nlQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 工具清单缓存（60秒，避免每次路由都查库） */
    private volatile List<AiTool> toolCache;
    private volatile long toolCacheAt = 0;

    @Override
    public Map<String, Object> query(String question)
    {
        return query(question, null);
    }

    @Override
    public Map<String, Object> query(String question, java.util.function.BiConsumer<String, String> stage)
    {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(question) || question.trim().isEmpty())
        {
            result.put("error", "请输入要查询的问题");
            return result;
        }
        fireStage(stage, "tables", "start");
        List<AiTool> tools = getEnabledToolsForUser();
        fireStage(stage, "tables", "done");
        if (tools.isEmpty())
        {
            // 无可用工具：直接走NL2SQL
            return nlQueryService.query(question, stage);
        }

        // 1. LLM路由：单次调用，输出 {"tool":"xxx","args":{...}}
        fireStage(stage, "sql", "start");
        JSONObject route = routeWithAi(question, tools);
        if (route != null && !"none".equals(route.getString("tool")))
        {
            fireStage(stage, "sql", "done");
        }
        if (route == null)
        {
            log.warn("ToolRoute 路由失败（LLM调用未返回有效JSON），降级NL2SQL: {}", question);
        }
        if (route == null || "none".equals(route.getString("tool")))
        {
            // 无匹配工具：降级NL2SQL
            return nlQueryService.query(question, stage);
        }

        // 2. 工具白名单校验（防幻觉：LLM返回的工具名必须真实存在）
        String toolName = route.getString("tool");
        AiTool target = null;
        for (AiTool t : tools)
        {
            if (t.getToolName().equals(toolName))
            {
                target = t;
                break;
            }
        }
        if (target == null)
        {
            log.warn("ToolRoute LLM返回未注册工具: {}，降级NL2SQL", toolName);
            return nlQueryService.query(question, stage);
        }

        // 3. 参数校验 + 绑定（只接受Schema声明过的参数，类型转换，超限裁剪）
        JSONObject rawArgs = route.getJSONObject("args");
        Map<String, Object> args = validateAndBindArgs(target, rawArgs != null ? rawArgs : new JSONObject());
        if (args.containsKey("__error__"))
        {
            result.put("error", args.get("__error__"));
            return result;
        }

        // 4. 执行SQL模板（参数化占位，只读）
        fireStage(stage, "exec", "start");
        try
        {
            String sql = target.getSqlTemplate();
            List<Map<String, Object>> rows = executeParameterized(sql, args);
            fireStage(stage, "exec", "done");
            List<String> columns = rows.isEmpty() ? new ArrayList<>() : new ArrayList<>(rows.get(0).keySet());
            // AI结论生成：模板直答同样产出"说人话"的总结（失败静默降级为纯表格）
            fireStage(stage, "summary", "start");
            String summary = nlQueryService.summarizeResult(question, columns, rows);
            fireStage(stage, "summary", "done");
            if (StringUtils.isNotEmpty(summary))
            {
                result.put("summary", summary);
            }
            // 空结果语义区分：执行成功但0行 ≠ 查询失败；有AI结论时用结论替代通用文案
            if (rows.isEmpty())
            {
                result.put("empty", Boolean.TRUE);
                result.put("emptyReason", StringUtils.isNotEmpty(summary) ? summary
                        : "查询执行成功，但没有查到匹配数据——查询对象可能尚无关联业务记录"
                        + "（如物料未入库、设备无工单记录、单据尚未产生）。可点击【查看 SQL】核对查询范围，"
                        + "或到对应业务模块确认数据是否已录入。");
            }
            result.put("columns", columns);
            result.put("rows", rows);
            result.put("total", rows.size());
            result.put("tool", target.getToolName());
            result.put("sql", sql);
            return result;
        }
        catch (Exception e)
        {
            fireStage(stage, "exec", "done");
            log.warn("ToolRoute SQL执行失败 tool={} err={}", target.getToolName(), e.getMessage());
            // 模板执行失败也降级NL2SQL，保证用户始终有结果
            return nlQueryService.query(question, stage);
        }
    }

    /** 阶段回调触发（stage为null时静默跳过） */
    private void fireStage(java.util.function.BiConsumer<String, String> stage, String name, String status)
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

    // ==================== LLM路由 ====================

    /** 按用户权限过滤后的启用工具清单（含60秒缓存） */
    private List<AiTool> getEnabledToolsForUser()
    {
        long now = System.currentTimeMillis();
        List<AiTool> cache = toolCache;
        if (cache == null || now - toolCacheAt > 60_000L)
        {
            try
            {
                cache = toolMapper.selectEnabledToolList();
                toolCache = cache;
                toolCacheAt = now;
            }
            catch (Exception e)
            {
                log.warn("ToolRoute 工具清单加载失败: {}", e.getMessage());
                return new ArrayList<>();
            }
        }
        // 权限过滤：配置了permKey的工具要求用户具备该权限（管理员*:*:*全通过）
        List<AiTool> allowed = new ArrayList<>();
        java.util.Set<String> perms = getLoginPermissions();
        for (AiTool t : cache)
        {
            if (StringUtils.isEmpty(t.getPermKey()) || perms.contains("*:*:*"))
            {
                allowed.add(t);
                continue;
            }
            for (String p : perms)
            {
                if (p != null && p.startsWith(t.getPermKey()))
                {
                    allowed.add(t);
                    break;
                }
            }
        }
        return allowed;
    }

    private java.util.Set<String> getLoginPermissions()
    {
        try
        {
            return com.ruoyi.common.utils.SecurityUtils.getLoginUser().getPermissions();
        }
        catch (Exception e)
        {
            return java.util.Collections.emptySet();
        }
    }

    /** 日志截断 */
    private String abbreviateLog(String s, int max)
    {
        return s == null ? "null" : (s.length() <= max ? s : s.substring(0, max) + "...");
    }

    /** LLM单次路由调用：组装工具清单提示词，解析JSON输出 */
    private JSONObject routeWithAi(String question, List<AiTool> tools)
    {
        StringBuilder menu = new StringBuilder();
        for (AiTool t : tools)
        {
            menu.append("{\"name\":\"").append(t.getToolName())
                .append("\",\"description\":\"").append(t.getDescription().replace("\"", "'"))
                .append("\",\"params\":").append(StringUtils.isNotEmpty(t.getParamSchema()) ? t.getParamSchema() : "[]")
                .append("}\n");
        }
        String prompt = "你是查询路由器。根据用户问题，从可用工具中选择一个并填写参数。\n"
                + "可用工具：\n" + menu + "\n"
                + "只输出JSON，格式：{\"tool\":\"工具名\",\"args\":{参数名:参数值}}\n"
                + "规则：\n"
                + "1. 问题与所有工具都不匹配时输出：{\"tool\":\"none\",\"args\":{}}\n"
                + "2. 只使用工具参数中声明的参数名，未提到的参数不要输出\n"
                + "3. 用户说\"本月/这个月\"时month填当前月，\"上个月\"填上月\n"
                + "4. 不要输出任何解释文字\n"
                + "\n用户问题：" + question;
        try
        {
            AiResult ai = aiClient.chatWithTimeout("platform", "tool_route", prompt, 8);
            if (!ai.isSuccess())
            {
                log.warn("ToolRoute LLM路由调用失败: {}", ai.getError());
                return null;
            }
            log.info("ToolRoute LLM原始回复: {}", abbreviateLog(ai.getContent(), 300));
            String content = ai.getContent().trim()
                    .replaceAll("(?s)^```(json)?", "").replaceAll("(?s)```$", "").trim();
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start)
            {
                return JSON.parseObject(content.substring(start, end + 1));
            }
        }
        catch (Exception e)
        {
            log.warn("ToolRoute LLM路由失败: {}", e.getMessage());
        }
        return null;
    }

    // ==================== 参数校验与执行 ====================

    /** 校验并绑定参数：只接受Schema声明的参数，类型转换，数值上限裁剪 */
    private Map<String, Object> validateAndBindArgs(AiTool tool, JSONObject rawArgs)
    {
        Map<String, Object> bound = new HashMap<>();
        JSONArray schema;
        try
        {
            schema = JSON.parseArray(StringUtils.isNotEmpty(tool.getParamSchema()) ? tool.getParamSchema() : "[]");
        }
        catch (Exception e)
        {
            schema = new JSONArray();
        }
        // 未声明的参数直接忽略（防注入面）；缺失的可选参数补null（SQL中 IS NULL 分支放行）
        for (int i = 0; i < schema.size(); i++)
        {
            JSONObject spec = schema.getJSONObject(i);
            String name = spec.getString("name");
            String type = StringUtils.nvl(spec.getString("type"), "string");
            Object value = rawArgs.get(name);
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty()))
            {
                boolean required = Boolean.TRUE.equals(spec.getBoolean("required"));
                if (required)
                {
                    Map<String, Object> err = new HashMap<>();
                    err.put("__error__", "缺少必填参数：" + name + "（" + StringUtils.nvl(spec.getString("desc"), "") + "）");
                    return err;
                }
                // LIMIT类数值参数缺失时给默认值10（LIMIT NULL在MySQL中非法）
                if ("number".equals(type)
                        && (name.toLowerCase().contains("top") || name.toLowerCase().contains("limit")))
                {
                    bound.put(name, 10);
                    continue;
                }
                bound.put(name, null);
                continue;
            }
            if ("number".equals(type))
            {
                try
                {
                    int num = (int) Double.parseDouble(String.valueOf(value));
                    // 数值参数安全上限：LIMIT类最大100
                    if (name.toLowerCase().contains("top") || name.toLowerCase().contains("limit"))
                    {
                        num = Math.min(Math.max(num, 1), 100);
                    }
                    bound.put(name, num);
                }
                catch (NumberFormatException e)
                {
                    bound.put(name, null);
                }
            }
            else
            {
                bound.put(name, String.valueOf(value).trim());
            }
        }
        return bound;
    }

    /** 参数化执行：NamedParameter风格 :name → JDBC ? 绑定（PreparedStatement防注入） */
    private List<Map<String, Object>> executeParameterized(String sql, Map<String, Object> args) throws Exception
    {
        List<String> names = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)").matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            String name = m.group(1);
            if (!args.containsKey(name))
            {
                // SQL引用了未绑定参数：替换为字面NULL，保持模板可执行
                m.appendReplacement(sb, "NULL");
            }
            else
            {
                names.add(name);
                m.appendReplacement(sb, "?");
            }
        }
        m.appendTail(sb);
        Object[] values = new Object[names.size()];
        for (int i = 0; i < names.size(); i++)
        {
            values[i] = args.get(names.get(i));
        }
        org.springframework.jdbc.core.JdbcTemplate tpl = new JdbcTemplate(jdbcTemplate.getDataSource());
        tpl.setQueryTimeout(15);
        return tpl.queryForList(sb.toString(), values);
    }
}
