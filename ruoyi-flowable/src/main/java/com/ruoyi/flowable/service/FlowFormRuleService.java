package com.ruoyi.flowable.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 流程表单规则引擎（服务端）
 *
 * 设计要点：
 * 1. 前端配置的规则以结构化 JSON 存储，这里用同一份 JSON 做服务端复算与校验，
 *    前端被绕过、脚本被篡改时仍能拦住脏数据；
 * 2. 表达式使用受限 AST，不允许执行任意代码；
 * 3. 与服务端脚本一致：AI 只产出规则 JSON，必须经过本类的字段/类型/依赖环校验。
 *
 * 规则类型：derive(计算/带出) / visible / readonly / required / options / validate / script
 * 服务端只执行 derive 与 validate，visible/readonly/options 属于纯展示逻辑。
 *
 * @author ruoyi
 */
@Service
public class FlowFormRuleService
{
    private static final Logger log = LoggerFactory.getLogger(FlowFormRuleService.class);

    /** AI模块标识 */
    private static final String AI_MODULE = "flowable";
    /** AI功能标识：表单规则生成 */
    private static final String AI_FEATURE = "form_rules";

    private static final Set<String> RULE_TYPES = Set.of(
            "derive", "visible", "readonly", "required", "options", "validate", "script");

    private static final Set<String> VALIDATORS = Set.of(
            "required", "range", "regex", "crossField", "detailSum", "custom", "unique");

    private static final int MAX_RULES = 300;
    private static final int MAX_EXT_BYTES = 512 * 1024;
    private static final int MAX_RULE_BYTES = 16 * 1024;
    private static final int MAX_NAME_LEN = 60;
    private static final int MAX_DERIVE_PASS = 10;

    @Autowired
    private AiClient aiClient;

    /** 企业工作日历（workDays 规则函数查 biz_hr_workday_calendar，0=工作日 1=休息日 2=法定节假日） */
    @Autowired
    private com.ruoyi.flowable.mapper.FlowWorkdayMapper workdayMapper;

    // ------------------------------------------------------------------
    // 一、保存期校验
    // ------------------------------------------------------------------

    /**
     * 保存前校验并规范化 extJson。
     * 任何非法内容都抛出 ServiceException，避免脏规则进入运行态。
     */
    public String validateForSave(String extJson, String fieldsJson)
    {
        if (StringUtils.isEmpty(extJson))
        {
            return "";
        }
        if (extJson.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_EXT_BYTES)
        {
            throw new ServiceException("表单规则内容超过 512KB，请拆分或精简");
        }
        JSONObject root;
        try
        {
            root = JSON.parseObject(extJson);
        }
        catch (Exception e)
        {
            throw new ServiceException("表单规则不是合法 JSON：" + e.getMessage());
        }
        if (root == null)
        {
            return "";
        }
        if (root.getIntValue("v", 1) != 1)
        {
            throw new ServiceException("表单规则版本不支持：" + root.getIntValue("v", 1));
        }
        JSONArray rules = root.getJSONArray("rules");
        JSONArray scripts = root.getJSONArray("scripts");
        int total = (rules == null ? 0 : rules.size()) + (scripts == null ? 0 : scripts.size());
        if (total > MAX_RULES)
        {
            throw new ServiceException("规则数量超过上限 " + MAX_RULES + " 条");
        }
        FieldDefs defs = FieldDefs.of(fieldsJson);
        Set<String> ids = new HashSet<>();
        if (rules != null)
        {
            for (int i = 0; i < rules.size(); i++)
            {
                validateRule(rules.getJSONObject(i), defs, ids, i);
            }
            ensureNoCycle(rules);
        }
        if (scripts != null)
        {
            for (int i = 0; i < scripts.size(); i++)
            {
                JSONObject s = scripts.getJSONObject(i);
                if (s == null)
                {
                    throw new ServiceException("第 " + (i + 1) + " 个脚本为空");
                }
                String id = s.getString("id");
                if (StringUtils.isEmpty(id))
                {
                    throw new ServiceException("第 " + (i + 1) + " 个脚本缺少 id");
                }
                if (!ids.add(id))
                {
                    throw new ServiceException("脚本 id 重复：" + id);
                }
                String code = s.getString("code");
                if (StringUtils.isNotEmpty(code) && code.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 64 * 1024)
                {
                    throw new ServiceException("脚本「" + s.getString("name") + "」超过 64KB");
                }
            }
        }
        return JSON.toJSONString(root);
    }

    private void validateRule(JSONObject rule, FieldDefs defs, Set<String> ids, int index)
    {
        if (rule == null)
        {
            throw new ServiceException("第 " + (index + 1) + " 条规则为空");
        }
        if (rule.toJSONString().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_RULE_BYTES)
        {
            throw new ServiceException("第 " + (index + 1) + " 条规则超过 16KB");
        }
        String id = rule.getString("id");
        String name = rule.getString("name");
        String type = rule.getString("type");
        if (StringUtils.isEmpty(id))
        {
            throw new ServiceException("第 " + (index + 1) + " 条规则缺少 id");
        }
        if (!ids.add(id))
        {
            throw new ServiceException("规则 id 重复：" + id);
        }
        if (StringUtils.isEmpty(name) || name.length() > MAX_NAME_LEN)
        {
            throw new ServiceException("第 " + (index + 1) + " 条规则的名称不能为空且不超过 " + MAX_NAME_LEN + " 字");
        }
        if (type == null || !RULE_TYPES.contains(type))
        {
            throw new ServiceException("规则「" + name + "」类型不合法：" + type);
        }
        String scope = rule.getString("scope");
        boolean detail = "detail".equals(scope);
        String table = rule.getString("detailTable");
        if (detail && StringUtils.isEmpty(table))
        {
            throw new ServiceException("规则「" + name + "」是明细规则，但未指定明细表");
        }
        // 目标字段
        String target = rule.getString("target");
        if (StringUtils.isEmpty(target))
        {
            throw new ServiceException("规则「" + name + "」未选择目标字段");
        }
        if (detail)
        {
            if (!defs.existsInTable(table, target))
            {
                throw new ServiceException("规则「" + name + "」的目标字段在明细表 " + table + " 中不存在：" + target);
            }
        }
        else if (!defs.exists(target))
        {
            throw new ServiceException("规则「" + name + "」的目标字段不存在：" + target);
        }
        // 触发字段
        JSONArray triggers = rule.getJSONArray("trigger");
        if (triggers != null)
        {
            for (int i = 0; i < triggers.size(); i++)
            {
                String t = triggers.getString(i);
                if (StringUtils.isNotEmpty(t) && !t.contains(".") && !defs.exists(t))
                {
                    throw new ServiceException("规则「" + name + "」引用的触发字段不存在：" + t);
                }
            }
        }
        // 表达式 / 条件引用字段
        if ("derive".equals(type))
        {
            JSONObject expr = rule.getJSONObject("expr");
            JSONArray mapping = rule.getJSONArray("mapping");
            if (expr == null && (mapping == null || mapping.isEmpty()))
            {
                throw new ServiceException("规则「" + name + "」未配置计算公式或带出映射");
            }
            if (expr != null)
            {
                checkRefs(expr, defs, name);
            }
            if (mapping != null)
            {
                for (int i = 0; i < mapping.size(); i++)
                {
                    JSONObject m = mapping.getJSONObject(i);
                    String to = m == null ? null : m.getString("to");
                    if (StringUtils.isEmpty(to) || !defs.exists(to))
                    {
                        throw new ServiceException("规则「" + name + "」的带出目标字段不存在：" + to);
                    }
                }
            }
        }
        if (rule.getJSONObject("when") != null)
        {
            checkRefs(rule.getJSONObject("when"), defs, name);
        }
        if ("validate".equals(type))
        {
            JSONObject action = rule.getJSONObject("action");
            String validator = action == null ? null : action.getString("validator");
            if (validator == null || !VALIDATORS.contains(validator))
            {
                throw new ServiceException("规则「" + name + "」的校验器不合法：" + validator);
            }
            if ("regex".equals(validator) && StringUtils.isNotEmpty(action.getString("pattern")))
            {
                try
                {
                    Pattern.compile(action.getString("pattern"));
                }
                catch (Exception e)
                {
                    throw new ServiceException("规则「" + name + "」的正则表达式不合法：" + e.getMessage());
                }
            }
            checkRefs(action, defs, name);
        }
        if ("options".equals(type))
        {
            JSONObject action = rule.getJSONObject("action");
            if (action != null && StringUtils.isNotEmpty(action.getString("fromField"))
                    && !defs.exists(action.getString("fromField")))
            {
                throw new ServiceException("规则「" + name + "」的选项来源字段不存在：" + action.getString("fromField"));
            }
        }
    }

    /** 收集节点中所有 field 引用并校验存在性 */
    private void checkRefs(JSONObject node, FieldDefs defs, String ruleName)
    {
        for (String f : collectRefs(node))
        {
            if (!defs.exists(f))
            {
                throw new ServiceException("规则「" + ruleName + "」引用了不存在的字段：" + f);
            }
        }
    }

    private Set<String> collectRefs(JSONObject node)
    {
        Set<String> out = new LinkedHashSet<>();
        collectRefs(node, out);
        return out;
    }

    private void collectRefs(JSONObject node, Set<String> out)
    {
        if (node == null)
        {
            return;
        }
        Object field = node.get("field");
        if (field instanceof String && StringUtils.isNotEmpty((String) field))
        {
            out.add((String) field);
        }
        collectRefs(node.getJSONObject("left"), out);
        collectRefs(node.getJSONObject("right"), out);
        collectRefs(node.getJSONObject("expr"), out);
        collectRefs(node.getJSONObject("condition"), out);
        JSONArray args = node.getJSONArray("args");
        if (args != null)
        {
            for (int i = 0; i < args.size(); i++)
            {
                Object a = args.get(i);
                if (a instanceof JSONObject)
                {
                    collectRefs((JSONObject) a, out);
                }
            }
        }
        JSONArray conditions = node.getJSONArray("conditions");
        if (conditions != null)
        {
            for (int i = 0; i < conditions.size(); i++)
            {
                Object c = conditions.get(i);
                if (c instanceof JSONObject)
                {
                    collectRefs((JSONObject) c, out);
                }
            }
        }
    }

    /** 依赖环检测：只针对 derive 规则的目标字段（表达式引用目标自身属于「保留当前值」惯用法，运行时迭代求值可收敛，不算环） */
    private void ensureNoCycle(JSONArray rules)
    {
        Map<String, Set<String>> edges = new LinkedHashMap<>();
        for (int i = 0; i < rules.size(); i++)
        {
            JSONObject r = rules.getJSONObject(i);
            if (r == null || !"derive".equals(r.getString("type")) || !r.getBooleanValue("enabled", true))
            {
                continue;
            }
            String target = r.getString("target");
            Set<String> refs = new HashSet<>(collectRefs(r.getJSONObject("expr")));
            refs.addAll(collectRefs(r.getJSONObject("when")));
            JSONArray mapping = r.getJSONArray("mapping");
            if (mapping != null)
            {
                for (int k = 0; k < mapping.size(); k++)
                {
                    JSONObject m = mapping.getJSONObject(k);
                    if (m != null && StringUtils.isNotEmpty(m.getString("from")))
                    {
                        refs.add(m.getString("from"));
                    }
                }
            }
            // 自引用剔除：如 if(need_loan_offset=="否", "", loan_amount) 引用目标自身表示「否则保留原值」
            refs.remove(target);
            edges.computeIfAbsent(target, k -> new HashSet<>()).addAll(refs);
        }
        for (String start : edges.keySet())
        {
            if (hasPath(start, start, edges, new HashSet<>()))
            {
                throw new ServiceException("规则存在循环依赖：" + start + " 的计算结果又被其他规则引用，请拆分规则");
            }
        }
    }

    private boolean hasPath(String current, String goal, Map<String, Set<String>> edges, Set<String> visited)
    {
        if (!visited.add(current))
        {
            return false;
        }
        Set<String> next = edges.get(current);
        if (next == null)
        {
            return false;
        }
        for (String n : next)
        {
            if (n.equals(goal))
            {
                return true;
            }
            if (edges.containsKey(n) && hasPath(n, goal, edges, visited))
            {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // 二、提交期复算与校验
    // ------------------------------------------------------------------

    /**
     * 服务端复算 + 校验。derived 的值直接写回 variables，校验失败抛 ServiceException。
     *
     * @param extJson    表单扩展 JSON
     * @param fieldsJson 字段定义
     * @param variables  流程变量（主表字段 + detailData）
     */
    public void applyAndValidate(String extJson, String fieldsJson, Map<String, Object> variables)
    {
        applyAndValidate(extJson, fieldsJson, variables, false);
    }

    /**
     * 服务端复算与校验。
     *
     * @param skipValidate true 时仅复算 derive（合计/大写等），不做必填与校验拦截——
     *                     用于退回发起人节点的「暂存」：允许未填完，但计算值要随变量落库
     */
    public void applyAndValidate(String extJson, String fieldsJson, Map<String, Object> variables, boolean skipValidate)
    {
        if (StringUtils.isEmpty(extJson) || variables == null)
        {
            return;
        }
        JSONObject root;
        try
        {
            root = JSON.parseObject(extJson);
        }
        catch (Exception e)
        {
            log.warn("表单规则 JSON 解析失败，跳过服务端复算：{}", e.getMessage());
            return;
        }
        JSONArray rules = root == null ? null : root.getJSONArray("rules");
        if (rules == null || rules.isEmpty())
        {
            return;
        }
        FieldDefs defs = FieldDefs.of(fieldsJson);
        EvalCtx ctx = new EvalCtx(variables);
        ctx.defs = defs;

        // 1) 计算型规则迭代求值（最多 10 轮，避免互相依赖导致的死循环）
        for (int pass = 0; pass < MAX_DERIVE_PASS; pass++)
        {
            boolean changed = false;
            for (int i = 0; i < rules.size(); i++)
            {
                JSONObject r = rules.getJSONObject(i);
                if (r == null || !r.getBooleanValue("enabled", true) || !"derive".equals(r.getString("type")))
                {
                    continue;
                }
                if (applyDerive(r, ctx, defs))
                {
                    changed = true;
                }
            }
            if (!changed)
            {
                break;
            }
            if (pass == MAX_DERIVE_PASS - 1)
            {
                log.warn("表单规则计算达到最大迭代次数，可能存在互相依赖");
            }
        }

        // 2) 聚合隐藏/必填状态
        Set<String> hidden = new HashSet<>();
        Set<String> required = new HashSet<>();
        for (int i = 0; i < rules.size(); i++)
        {
            JSONObject r = rules.getJSONObject(i);
            if (r == null || !r.getBooleanValue("enabled", true))
            {
                continue;
            }
            String type = r.getString("type");
            if (!"visible".equals(type) && !"required".equals(type))
            {
                continue;
            }
            if (!evalCondition(r.getJSONObject("when"), ctx))
            {
                continue;
            }
            JSONObject action = r.getJSONObject("action");
            if ("visible".equals(type))
            {
                if (action != null && Boolean.FALSE.equals(action.getBoolean("visible")))
                {
                    hidden.add(r.getString("target"));
                }
            }
            else if (action == null || !Boolean.FALSE.equals(action.getBoolean("required")))
            {
                required.add(r.getString("target"));
            }
        }

        // 3) 校验（暂存场景跳过：允许未填完，仅保证计算值落库）
        List<String> errors = new ArrayList<>();
        if (skipValidate)
        {
            return;
        }
        for (int i = 0; i < rules.size(); i++)
        {
            JSONObject r = rules.getJSONObject(i);
            if (r == null || !r.getBooleanValue("enabled", true) || !"validate".equals(r.getString("type")))
            {
                continue;
            }
            if ("detail".equals(r.getString("scope")))
            {
                validateDetailRule(r, ctx, errors);
            }
            else if (defs.isDetailField(r.getString("target")))
            {
                // 目标是明细字段但规则未标 scope=detail：按行校验（与前端引擎口径一致）
                validateDetailFieldRule(r, ctx, errors, defs.tableOf(r.getString("target")));
            }
            else
            {
                validateMainRule(r, ctx, hidden, errors);
            }
        }
        // 动态必填：条件成立但值为空也拦截
        for (String f : required)
        {
            if (!hidden.contains(f) && isEmpty(ctx.values.get(f)))
            {
                errors.add("请填写" + defs.label(f));
            }
        }
        if (!errors.isEmpty())
        {
            throw new ServiceException(errors.get(0));
        }
    }

    private boolean applyDerive(JSONObject rule, EvalCtx ctx, FieldDefs defs)
    {
        String target = rule.getString("target");
        String mode = rule.getString("mode");
        boolean ifEmpty = "ifEmpty".equals(mode);
        if ("detail".equals(rule.getString("scope")))
        {
            String table = rule.getString("detailTable");
            List<Map<String, Object>> rows = ctx.rows(table);
            if (rows == null)
            {
                return false;
            }
            boolean changed = false;
            for (Map<String, Object> row : rows)
            {
                if (!evalCondition(rule.getJSONObject("when"), ctx.withRow(row, table)))
                {
                    continue;
                }
                Object v = evalExpr(rule.getJSONObject("expr"), ctx.withRow(row, table));
                if (v == null)
                {
                    continue;
                }
                if (ifEmpty && !isEmpty(row.get(target)))
                {
                    continue;
                }
                if (!sameValue(row.get(target), v))
                {
                    row.put(target, v);
                    changed = true;
                }
            }
            return changed;
        }
        if (!evalCondition(rule.getJSONObject("when"), ctx))
        {
            return false;
        }
        JSONArray mapping = rule.getJSONArray("mapping");
        if (mapping != null && !mapping.isEmpty())
        {
            return applyMapping(rule, ctx);
        }
        Object v = evalExpr(rule.getJSONObject("expr"), ctx);
        if (v == null)
        {
            return false;
        }
        if (ifEmpty && !isEmpty(ctx.values.get(target)))
        {
            return false;
        }
        if (sameValue(ctx.values.get(target), v))
        {
            return false;
        }
        ctx.values.put(target, v);
        return true;
    }

    /** 浏览字段带出：从 ${source}__extra 或同名原始字段中取数 */
    private boolean applyMapping(JSONObject rule, EvalCtx ctx)
    {
        String source = rule.getString("source");
        JSONArray mapping = rule.getJSONArray("mapping");
        Map<String, Object> src = new HashMap<>();
        Object extra = ctx.values.get(source + "__extra");
        if (extra instanceof Map)
        {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) extra).entrySet())
            {
                if (e.getKey() != null)
                {
                    src.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
        }
        else if (extra instanceof String && StringUtils.isNotEmpty((String) extra) && ((String) extra).startsWith("{"))
        {
            try
            {
                src.putAll(JSON.parseObject((String) extra));
            }
            catch (Exception ignore)
            {
                // 附加信息不是合法 JSON：忽略，退回原始字段映射
            }
        }
        boolean changed = false;
        for (int i = 0; i < mapping.size(); i++)
        {
            JSONObject m = mapping.getJSONObject(i);
            if (m == null)
            {
                continue;
            }
            String from = m.getString("from");
            String to = m.getString("to");
            if (StringUtils.isEmpty(to))
            {
                continue;
            }
            Object v = src.get(from);
            if (v == null)
            {
                v = ctx.values.get(from);
            }
            if (v == null)
            {
                continue;
            }
            if ("ifEmpty".equals(m.getString("mode")) && !isEmpty(ctx.values.get(to)))
            {
                continue;
            }
            if (!sameValue(ctx.values.get(to), v))
            {
                ctx.values.put(to, v);
                changed = true;
            }
        }
        return changed;
    }

    private void validateMainRule(JSONObject rule, EvalCtx ctx, Set<String> hidden, List<String> errors)
    {
        String target = rule.getString("target");
        if (hidden.contains(target))
        {
            return;
        }
        if (!evalCondition(rule.getJSONObject("when"), ctx))
        {
            return;
        }
        JSONObject action = rule.getJSONObject("action");
        if (action == null)
        {
            return;
        }
        String validator = action.getString("validator");
        // 行上下文时目标取行值（明细字段按行校验复用本方法），否则取主表值
        Object v = ctx.row != null && ctx.row.containsKey(target) ? ctx.row.get(target) : ctx.values.get(target);
        String message = StringUtils.isNotEmpty(rule.getString("message"))
                ? rule.getString("message") : "「" + target + "」校验未通过";
        boolean ok = true;
        if ("required".equals(validator))
        {
            ok = !isEmpty(v);
        }
        else if ("range".equals(validator))
        {
            // 空值跳过（与前端引擎一致），空值约束由必填规则负责
            BigDecimal n = toNum(v);
            BigDecimal min = toNum(action.get("min"));
            BigDecimal max = toNum(action.get("max"));
            ok = isEmpty(v) || (n != null && (min == null || n.compareTo(min) >= 0) && (max == null || n.compareTo(max) <= 0));
        }
        else if ("regex".equals(validator))
        {
            String pattern = action.getString("pattern");
            ok = isEmpty(v) || StringUtils.isEmpty(pattern) || (v != null && Pattern.compile(pattern).matcher(String.valueOf(v)).find());
        }
        else if ("crossField".equals(validator))
        {
            if (isEmpty(v))
            {
                // 空值跳过比较（空值约束由必填规则负责），避免空字段误报「不能大于/必须等于」
                ok = true;
            }
            else
            {
                ok = compare(action.getString("cmp"), evalOperand(action.getJSONObject("left"), ctx),
                        evalOperand(action.getJSONObject("right"), ctx));
            }
        }
        else if ("custom".equals(validator))
        {
            ok = boolOf(evalOperand(action.getJSONObject("expr"), ctx));
        }
        else if ("detailSum".equals(validator))
        {
            BigDecimal total = detailSum(ctx, action.getString("table"), action.getString("field"));
            BigDecimal max = toNum(action.get("max"));
            BigDecimal min = toNum(action.get("min"));
            ok = total != null && (max == null || total.compareTo(max) <= 0) && (min == null || total.compareTo(min) >= 0);
        }
        if (!ok)
        {
            errors.add(message);
        }
    }

    /** 目标是明细字段的校验规则：按行执行与主表一致的校验器，错误带行号（与前端引擎口径一致） */
    private void validateDetailFieldRule(JSONObject rule, EvalCtx ctx, List<String> errors, String table)
    {
        List<Map<String, Object>> rows = ctx.rows(table);
        if (rows == null || rows.isEmpty())
        {
            return;
        }
        for (int i = 0; i < rows.size(); i++)
        {
            EvalCtx rowCtx = ctx.withRow(rows.get(i), table);
            int before = errors.size();
            validateMainRule(rule, rowCtx, Collections.emptySet(), errors);
            if (errors.size() > before)
            {
                errors.set(before, "第 " + (i + 1) + " 行：" + errors.get(before));
            }
        }
    }

    private void validateDetailRule(JSONObject rule, EvalCtx ctx, List<String> errors)
    {
        String table = rule.getString("detailTable");
        List<Map<String, Object>> rows = ctx.rows(table);
        if (rows == null || rows.isEmpty())
        {
            return;
        }
        JSONObject action = rule.getJSONObject("action");
        String validator = action == null ? null : action.getString("validator");
        String message = StringUtils.isNotEmpty(rule.getString("message"))
                ? rule.getString("message") : "明细校验未通过";
        for (int i = 0; i < rows.size(); i++)
        {
            Map<String, Object> row = rows.get(i);
            EvalCtx rowCtx = ctx.withRow(row, table);
            if (!evalCondition(rule.getJSONObject("when"), rowCtx))
            {
                continue;
            }
            String target = rule.getString("target");
            Object v = row.get(target);
            boolean ok = true;
            if ("required".equals(validator))
            {
                ok = !isEmpty(v);
            }
            else if ("range".equals(validator))
            {
                BigDecimal n = toNum(v);
                BigDecimal min = toNum(action.get("min"));
                BigDecimal max = toNum(action.get("max"));
                ok = n != null && (min == null || n.compareTo(min) >= 0) && (max == null || n.compareTo(max) <= 0);
            }
            else if ("regex".equals(validator))
            {
                String pattern = action.getString("pattern");
                ok = StringUtils.isEmpty(pattern) || (v != null && Pattern.compile(pattern).matcher(String.valueOf(v)).find());
            }
            else if ("custom".equals(validator))
            {
                ok = boolOf(evalOperand(action.getJSONObject("expr"), rowCtx));
            }
            if (!ok)
            {
                errors.add("第 " + (i + 1) + " 行：" + message);
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // 三、表达式求值（受限 AST，无代码执行）
    // ------------------------------------------------------------------

    private Object evalOperand(JSONObject node, EvalCtx ctx)
    {
        return evalExpr(node, ctx);
    }

    private Object evalExpr(JSONObject node, EvalCtx ctx)
    {
        if (node == null)
        {
            return null;
        }
        if (node.containsKey("value"))
        {
            return node.get("value");
        }
        if (node.containsKey("field"))
        {
            String f = node.getString("field");
            if (f == null)
            {
                return null;
            }
            if (ctx.row != null && ctx.row.containsKey(f))
            {
                return ctx.row.get(f);
            }
            int dot = f.indexOf('.');
            if (dot > 0)
            {
                List<Map<String, Object>> rows = ctx.rows(f.substring(0, dot));
                String col = f.substring(dot + 1);
                if (rows != null && !rows.isEmpty())
                {
                    return rows.get(0).get(col);
                }
                return null;
            }
            return ctx.values.get(f);
        }
        String fn = node.getString("fn");
        if (StringUtils.isNotEmpty(fn))
        {
            JSONArray args = node.getJSONArray("args");
            // 兼容 AI/用户配置的 sum(明细字段) 单参数写法：明细字段值不在主表，直接 sum 永远得 0，
            // 归一化为 detailSum(明细表, 字段)，与前端引擎口径一致
            if ("sum".equals(fn) && args != null && args.size() == 1 && ctx.defs != null)
            {
                JSONObject a0 = args.getJSONObject(0);
                String mf = a0 == null ? null : a0.getString("field");
                if (StringUtils.isNotEmpty(mf) && ctx.defs.isDetailField(mf))
                {
                    return callFunction("detailSum", java.util.Arrays.asList(ctx.defs.tableOf(mf), mf), ctx);
                }
            }
            List<Object> vals = new ArrayList<>();
            if (args != null)
            {
                for (int i = 0; i < args.size(); i++)
                {
                    Object a = args.get(i);
                    vals.add(a instanceof JSONObject ? evalExpr((JSONObject) a, ctx) : a);
                }
            }
            return callFunction(fn, vals, ctx);
        }
        return null;
    }

    private Object callFunction(String fn, List<Object> args, EvalCtx ctx)
    {
        switch (fn)
        {
            case "add":
                return numOp(args, "+");
            case "sub":
                return numOp(args, "-");
            case "mul":
                return numOp(args, "*");
            case "div":
                return numOp(args, "/");
            case "mod":
                return numOp(args, "%");
            case "round":
            {
                BigDecimal n = argNum(args, 0);
                int scale = args.size() > 1 ? argNum(args, 1).intValue() : 0;
                return n == null ? null : n.setScale(scale, RoundingMode.HALF_UP);
            }
            case "floor":
            {
                BigDecimal n = argNum(args, 0);
                return n == null ? null : n.setScale(0, RoundingMode.FLOOR);
            }
            case "ceil":
            {
                BigDecimal n = argNum(args, 0);
                return n == null ? null : n.setScale(0, RoundingMode.CEILING);
            }
            case "abs":
            {
                BigDecimal n = argNum(args, 0);
                return n == null ? null : n.abs();
            }
            case "min":
            case "max":
            {
                BigDecimal r = null;
                for (Object a : args)
                {
                    BigDecimal n = toNum(a);
                    if (n == null)
                    {
                        continue;
                    }
                    if (r == null || ("min".equals(fn) ? n.compareTo(r) < 0 : n.compareTo(r) > 0))
                    {
                        r = n;
                    }
                }
                return r;
            }
            case "if":
                return boolOf(arg(args, 0)) ? arg(args, 1) : arg(args, 2);
            case "and":
            {
                for (Object a : args)
                {
                    if (!boolOf(a))
                    {
                        return false;
                    }
                }
                return true;
            }
            case "or":
            {
                for (Object a : args)
                {
                    if (boolOf(a))
                    {
                        return true;
                    }
                }
                return false;
            }
            case "not":
                return !boolOf(arg(args, 0));
            case "concat":
            {
                StringBuilder sb = new StringBuilder();
                for (Object a : args)
                {
                    sb.append(a == null ? "" : String.valueOf(a));
                }
                return sb.toString();
            }
            case "len":
                return arg(args, 0) == null ? 0 : String.valueOf(arg(args, 0)).length();
            case "trim":
                return arg(args, 0) == null ? "" : String.valueOf(arg(args, 0)).trim();
            case "upper":
                return arg(args, 0) == null ? "" : String.valueOf(arg(args, 0)).toUpperCase();
            case "lower":
                return arg(args, 0) == null ? "" : String.valueOf(arg(args, 0)).toLowerCase();
            case "today":
                return LocalDate.now().toString();
            case "now":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "dateDiff":
            {
                LocalDate d1 = toDate(arg(args, 0));
                LocalDate d2 = toDate(arg(args, 1));
                return d1 == null || d2 == null ? null : (d2.toEpochDay() - d1.toEpochDay());
            }
            case "dateAdd":
            {
                LocalDate d = toDate(arg(args, 0));
                BigDecimal days = toNum(arg(args, 1));
                return d == null || days == null ? null : d.plusDays(days.longValue()).toString();
            }
            case "sum":
            case "avg":
            {
                BigDecimal total = BigDecimal.ZERO;
                int n = 0;
                for (Object a : args)
                {
                    BigDecimal v = toNum(a);
                    if (v != null)
                    {
                        total = total.add(v);
                        n++;
                    }
                }
                if ("sum".equals(fn))
                {
                    return total;
                }
                return n == 0 ? null : total.divide(BigDecimal.valueOf(n), 6, RoundingMode.HALF_UP);
            }
            case "detailSum":
            {
                // 明细数据缺失（rows 为 null）时返回 null 让 applyDerive 跳过写入，
                // 避免把前端已算好的合计覆盖成 0；空明细列表求和为 0 属合法结果
                List<Map<String, Object>> sumRows = ctx.rows(str(arg(args, 0)));
                if (sumRows == null)
                {
                    return null;
                }
                BigDecimal total = BigDecimal.ZERO;
                for (Map<String, Object> row : sumRows)
                {
                    BigDecimal v = toNum(row.get(str(arg(args, 1))));
                    if (v != null)
                    {
                        total = total.add(v);
                    }
                }
                return total;
            }
            case "detailCount":
            {
                List<Map<String, Object>> rows = ctx.rows(str(arg(args, 0)));
                return rows == null ? 0 : rows.size();
            }
            case "rmbUpper":
            {
                // 金额 → 人民币大写（与前端 utils/rmb.js 口径一致）
                BigDecimal n = toNum(arg(args, 0));
                return n == null ? "" : rmbUpper(n);
            }
            case "hoursDiff":
            {
                // 时间相差小时（支持 yyyy-MM-dd HH:mm / yyyy-MM-dd，日期按 00:00 计）；与前端 engine.js 口径一致
                LocalDateTime t1 = toDateTime(arg(args, 0));
                LocalDateTime t2 = toDateTime(arg(args, 1));
                if (t1 == null || t2 == null)
                {
                    return null;
                }
                long ms = java.time.Duration.between(t1, t2).toMillis();
                return BigDecimal.valueOf(ms).divide(BigDecimal.valueOf(3600000L), 2, RoundingMode.HALF_UP);
            }
            case "workDays":
            {
                // 工作日天数（含首尾两天，跳过休息日/法定节假日、含调休补班日；
                // 区间无日历数据的日期按「剔除周六日」回退口径处理）；与前端 engine.js 口径一致
                LocalDate d1 = toDate(arg(args, 0));
                LocalDate d2 = toDate(arg(args, 1));
                if (d1 == null || d2 == null)
                {
                    return null;
                }
                if (d2.isBefore(d1))
                {
                    return BigDecimal.ZERO;
                }
                Map<String, String> cal = workdayTypeMap(d1, d2);
                long count = 0;
                for (LocalDate d = d1; !d.isAfter(d2); d = d.plusDays(1))
                {
                    String type = cal.get(d.toString());
                    if ("0".equals(type))
                    {
                        count++;
                    }
                    else if (!"1".equals(type) && !"2".equals(type))
                    {
                        // 无日历数据回退：周六日不计
                        int dow = d.getDayOfWeek().getValue(); // 1=周一 ... 7=周日
                        if (dow <= 5)
                        {
                            count++;
                        }
                    }
                }
                return BigDecimal.valueOf(count);
            }
            default:
                log.warn("未知的规则函数：{}", fn);
                return null;
        }
    }

    /** 查询区间内的企业日历标记 {日期: 类型}（查不到/异常时返回空 Map，走回退口径） */
    private Map<String, String> workdayTypeMap(LocalDate start, LocalDate end)
    {
        try
        {
            List<Map<String, Object>> rows = workdayMapper.selectDateTypeRange(start.toString(), end.toString());
            Map<String, String> map = new HashMap<>();
            if (rows != null)
            {
                for (Map<String, Object> r : rows)
                {
                    Object dt = r.get("dt");
                    Object type = r.get("dateType");
                    if (dt != null && type != null)
                    {
                        map.put(String.valueOf(dt), String.valueOf(type));
                    }
                }
            }
            return map;
        }
        catch (Exception e)
        {
            log.warn("查询工作日历失败，workDays 按剔除周末口径计算：{}", e.getMessage());
            return new HashMap<>();
        }
    }

    /** 宽松解析日期时间：yyyy-MM-dd HH:mm[:ss] 或 yyyy-MM-dd（按 00:00 计） */
    private static LocalDateTime toDateTime(Object v)
    {
        if (v == null)
        {
            return null;
        }
        String s = String.valueOf(v).trim();
        if (s.length() < 8)
        {
            return null;
        }
        try
        {
            if (s.length() >= 16)
            {
                return LocalDateTime.parse(s.substring(0, s.length() >= 19 ? 19 : 16).replace(' ', 'T'));
            }
            return LocalDate.parse(s.substring(0, 10)).atStartOfDay();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static final String[] RMB_DIG = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] RMB_UNIT = {"", "拾", "佰", "仟"};
    private static final String[] RMB_BIG = {"", "万", "亿", "兆"};

    /** 4 位一段的中文大写（段内零的处理） */
    private String rmbSegUpper(String s)
    {
        StringBuilder out = new StringBuilder();
        boolean zero = false;
        for (int i = 0; i < s.length(); i++)
        {
            int d = s.charAt(i) - '0';
            int pos = s.length() - 1 - i;
            if (d == 0)
            {
                zero = out.length() > 0;
            }
            else
            {
                if (zero)
                {
                    out.append('零');
                    zero = false;
                }
                out.append(RMB_DIG[d]).append(RMB_UNIT[pos]);
            }
        }
        return out.toString();
    }

    /** 金额数字 → 中文大写金额，如 123456.78 → 壹拾贰万叁仟肆佰伍拾陆圆柒角捌分 */
    private String rmbUpper(BigDecimal n)
    {
        if (n == null)
        {
            return "";
        }
        if (n.compareTo(BigDecimal.ZERO) == 0)
        {
            return "零圆整";
        }
        String neg = n.compareTo(BigDecimal.ZERO) < 0 ? "负" : "";
        String abs = n.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
        int dot = abs.indexOf('.');
        String intStr = dot > 0 ? abs.substring(0, dot) : abs;
        String decStr = dot > 0 ? abs.substring(dot + 1) : "";

        // 整数部分按 4 位分段（万/亿/兆）
        java.util.List<String> parts = new ArrayList<>();
        String s = intStr;
        while (s.length() > 4)
        {
            parts.add(0, s.substring(s.length() - 4));
            s = s.substring(0, s.length() - 4);
        }
        parts.add(0, s);

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.size(); i++)
        {
            String seg = rmbSegUpper(parts.get(i));
            if (seg.isEmpty())
            {
                continue;
            }
            // 本段数值不足 1000 且前面已有段：跨段补零（如 100000010 → 壹亿零壹拾）
            if (out.length() > 0 && Long.parseLong(parts.get(i)) < 1000)
            {
                out.append('零');
            }
            out.append(seg).append(RMB_BIG[parts.size() - 1 - i]);
        }

        int jiao = decStr.charAt(0) - '0';
        int fen = decStr.length() > 1 ? decStr.charAt(1) - '0' : 0;
        // 整数为 0 时省略「圆」：0.05 → 伍分、0.5 → 伍角
        if ("0".equals(intStr))
        {
            if (jiao == 0 && fen == 0)
            {
                return neg + "零圆整";
            }
            if (jiao > 0)
            {
                out.append(RMB_DIG[jiao]).append('角');
            }
            else if (fen > 0)
            {
                out.append('零');
            }
            if (fen > 0)
            {
                out.append(RMB_DIG[fen]).append('分');
            }
            return neg + out;
        }
        out.append('圆');
        if (jiao == 0 && fen == 0)
        {
            return neg + out + "整";
        }
        if (jiao > 0)
        {
            out.append(RMB_DIG[jiao]).append('角');
        }
        else if (fen > 0)
        {
            out.append('零');
        }
        if (fen > 0)
        {
            out.append(RMB_DIG[fen]).append('分');
        }
        return neg + out.toString();
    }

    private boolean evalCondition(JSONObject c, EvalCtx ctx)
    {
        if (c == null)
        {
            return true;
        }
        String op = c.getString("op");
        if ("and".equals(op) || "or".equals(op))
        {
            JSONArray conditions = c.getJSONArray("conditions");
            if (conditions == null || conditions.isEmpty())
            {
                return true;
            }
            for (int i = 0; i < conditions.size(); i++)
            {
                boolean r = evalCondition(conditions.getJSONObject(i), ctx);
                if ("and".equals(op) && !r)
                {
                    return false;
                }
                if ("or".equals(op) && r)
                {
                    return true;
                }
            }
            return "and".equals(op);
        }
        if ("not".equals(op))
        {
            return !evalCondition(c.getJSONObject("condition"), ctx);
        }
        return compare(c.getString("cmp"), evalExpr(c.getJSONObject("left"), ctx), evalExpr(c.getJSONObject("right"), ctx));
    }

    private boolean compare(String cmp, Object left, Object right)
    {
        if (cmp == null)
        {
            return true;
        }
        switch (cmp)
        {
            case "isEmpty":
                return isEmpty(left);
            case "isNotEmpty":
                return !isEmpty(left);
            case "eq":
                return sameValue(left, right);
            case "ne":
                return !sameValue(left, right);
            case "contains":
                return left != null && right != null && String.valueOf(left).contains(String.valueOf(right));
            case "notContains":
                return left == null || right == null || !String.valueOf(left).contains(String.valueOf(right));
            case "regex":
                return left != null && right != null && Pattern.compile(String.valueOf(right)).matcher(String.valueOf(left)).find();
            case "in":
            case "notIn":
            {
                boolean hit = false;
                if (right instanceof List)
                {
                    for (Object o : (List<?>) right)
                    {
                        if (sameValue(left, o))
                        {
                            hit = true;
                            break;
                        }
                    }
                }
                else if (right != null)
                {
                    for (String s : String.valueOf(right).split(","))
                    {
                        if (sameValue(left, s.trim()))
                        {
                            hit = true;
                            break;
                        }
                    }
                }
                return "in".equals(cmp) ? hit : !hit;
            }
            case "between":
            {
                if (!(right instanceof List) || ((List<?>) right).size() < 2)
                {
                    return false;
                }
                List<?> range = (List<?>) right;
                return compare("gte", left, range.get(0)) && compare("lte", left, range.get(1));
            }
            default:
                break;
        }
        BigDecimal nl = toNum(left);
        BigDecimal nr = toNum(right);
        if (nl != null && nr != null)
        {
            int c = nl.compareTo(nr);
            switch (cmp)
            {
                case "gt":
                    return c > 0;
                case "gte":
                    return c >= 0;
                case "lt":
                    return c < 0;
                case "lte":
                    return c <= 0;
                default:
                    return false;
            }
        }
        LocalDate dl = toDate(left);
        LocalDate dr = toDate(right);
        if (dl != null && dr != null)
        {
            int c = dl.compareTo(dr);
            switch (cmp)
            {
                case "gt":
                    return c > 0;
                case "gte":
                    return c >= 0;
                case "lt":
                    return c < 0;
                case "lte":
                    return c <= 0;
                default:
                    return false;
            }
        }
        int c = String.valueOf(left == null ? "" : left).compareTo(String.valueOf(right == null ? "" : right));
        switch (cmp)
        {
            case "gt":
                return c > 0;
            case "gte":
                return c >= 0;
            case "lt":
                return c < 0;
            case "lte":
                return c <= 0;
            default:
                return false;
        }
    }

    private boolean sameValue(Object a, Object b)
    {
        if (a == null || b == null)
        {
            return a == b || (isEmpty(a) && isEmpty(b));
        }
        BigDecimal na = toNum(a);
        BigDecimal nb = toNum(b);
        if (na != null && nb != null)
        {
            return na.compareTo(nb) == 0;
        }
        return String.valueOf(a).trim().equals(String.valueOf(b).trim());
    }

    private BigDecimal detailSum(EvalCtx ctx, String table, String field)
    {
        List<Map<String, Object>> rows = ctx.rows(table);
        if (rows == null)
        {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> row : rows)
        {
            BigDecimal v = toNum(row.get(field));
            if (v != null)
            {
                total = total.add(v);
            }
        }
        return total;
    }

    // ------------------------------------------------------------------
    // 四、基础工具
    // ------------------------------------------------------------------

    private static Object arg(List<Object> args, int i)
    {
        return args != null && args.size() > i ? args.get(i) : null;
    }

    private static BigDecimal argNum(List<Object> args, int i)
    {
        return toNum(arg(args, i));
    }

    private static String str(Object o)
    {
        return o == null ? null : String.valueOf(o);
    }

    private static BigDecimal toNum(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof BigDecimal)
        {
            return (BigDecimal) v;
        }
        if (v instanceof Number)
        {
            return new BigDecimal(String.valueOf(v));
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty())
        {
            return null;
        }
        try
        {
            return new BigDecimal(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static Object numOp(List<Object> args, String op)
    {
        BigDecimal a = argNum(args, 0);
        BigDecimal b = argNum(args, 1);
        if (a == null || b == null)
        {
            return null;
        }
        switch (op)
        {
            case "+":
                return a.add(b);
            case "-":
                return a.subtract(b);
            case "*":
                return a.multiply(b);
            case "/":
                return b.compareTo(BigDecimal.ZERO) == 0 ? null : a.divide(b, 6, RoundingMode.HALF_UP);
            case "%":
                return b.compareTo(BigDecimal.ZERO) == 0 ? null : a.remainder(b);
            default:
                return null;
        }
    }

    private static boolean boolOf(Object v)
    {
        if (v == null)
        {
            return false;
        }
        if (v instanceof Boolean)
        {
            return (Boolean) v;
        }
        if (v instanceof Number)
        {
            return ((Number) v).doubleValue() != 0;
        }
        String s = String.valueOf(v).trim();
        return !(s.isEmpty() || "0".equals(s) || "false".equalsIgnoreCase(s) || "null".equalsIgnoreCase(s));
    }

    private static boolean isEmpty(Object v)
    {
        if (v == null)
        {
            return true;
        }
        if (v instanceof String)
        {
            return ((String) v).trim().isEmpty();
        }
        if (v instanceof List)
        {
            return ((List<?>) v).isEmpty();
        }
        if (v instanceof Map)
        {
            return ((Map<?, ?>) v).isEmpty();
        }
        return false;
    }

    private static LocalDate toDate(Object v)
    {
        if (v == null)
        {
            return null;
        }
        String s = String.valueOf(v).trim();
        if (s.length() < 8)
        {
            return null;
        }
        try
        {
            if (s.length() >= 19)
            {
                return LocalDateTime.parse(s.substring(0, 19).replace(' ', 'T')).toLocalDate();
            }
            if (s.length() == 10)
            {
                return LocalDate.parse(s);
            }
            if (s.length() >= 10)
            {
                return LocalDate.parse(s.substring(0, 10));
            }
        }
        catch (Exception ignore)
        {
            return null;
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 五、AI 生成规则（复用统一 AiClient，只出 JSON，默认草稿）
    // ------------------------------------------------------------------

    public JSONObject aiGenerate(String fieldsJson, String extJson, String requirement)
    {
        if (StringUtils.isEmpty(requirement))
        {
            throw new ServiceException("请先描述你的规则需求");
        }
        if (aiClient == null || !aiClient.isEnabled(AI_MODULE, AI_FEATURE))
        {
            throw new ServiceException("AI 规则生成未开启，请联系管理员在 AI 配置中启用 flowable/form_rules");
        }
        FieldDefs defs = FieldDefs.of(fieldsJson);
        StringBuilder fieldLines = new StringBuilder();
        for (JSONObject f : defs.all)
        {
            fieldLines.append("- ").append(f.getString("field"))
                    .append("（").append(f.getString("label")).append("，")
                    .append(StringUtils.isNotEmpty(f.getString("dtype")) ? f.getString("dtype") : "text")
                    .append("，").append("main".equals(f.getString("table")) || StringUtils.isEmpty(f.getString("table"))
                            ? "主表" : f.getString("table"))
                    .append("）\n");
        }
        String prompt = "你是企业流程表单规则专家。请把用户需求转换成表单规则 JSON 数组。\n"
                + "【表单字段】\n" + fieldLines
                + "\n【已有规则】\n" + (StringUtils.isEmpty(extJson) ? "[]" : extJson)
                + "\n【用户需求】\n" + requirement
                + "\n\n【输出要求】\n"
                + "只输出 JSON 数组，不要 markdown 代码块、不要解释。元素结构：\n"
                + "{ \"id\": \"rule_1\", \"type\": \"derive|visible|readonly|required|options|validate\",\n"
                + "  \"name\": \"规则名称\", \"target\": \"目标字段\",\n"
                + "  \"trigger\": [\"触发字段\"], \"when\": {条件}, \"expr\": {表达式}, \"action\": {...}, \"message\": \"提示语\" }\n"
                + "条件格式：{ \"op\": \"and\", \"conditions\": [ { \"left\": {\"field\":\"x\"}, \"cmp\": \"gt\", \"right\": {\"value\": 1} } ] }\n"
                + "校验类规则（validate）的 action 必须包含 validator 字段，可选值：required（必填）、range（数值范围，配 min/max）、regex（正则，配 pattern）、crossField（两字段比较）、detailSum（明细合计）；禁止省略 validator。\n"
                + "字段间大小比较必须用 crossField，action 格式：{ \"validator\": \"crossField\", \"cmp\": \"lt|lte|gt|gte|eq|ne\", \"left\": {\"field\":\"字段甲\"}, \"right\": {\"field\":\"字段乙\"} }；与固定值比较时 right 用 {\"value\": 数值}；非负校验用 range 且 min 设为 0。\n"
                + "表达式格式：{ \"fn\": \"mul\", \"args\": [{\"field\":\"a\"},{\"value\":2}] }，可用函数 add/sub/mul/div/round/sum/if/dateDiff（自然日相差天数）/hoursDiff（相差小时，用于加班时长）/workDays(开始,结束)（工作日天数，自动跳过休息日和法定节假日、含调休补班，用于请假天数，开始晚于结束返回0）/rmbUpper（金额转人民币大写，如合计大写 = rmbUpper(合计小写)）、detailSum(表名,字段)（明细列求和）、detailCount(表名)（明细行数）。\n"
                + "字段名只能取自上方清单，禁止编造字段；无法确认时返回空数组 []。\n"
                + "id 为规则唯一标识，必须填写，按 rule_1、rule_2 递增编号，不得重复。";
        AiResult result = aiClient.chat(AI_MODULE, AI_FEATURE, prompt, 2500);
        if (result == null || !result.isSuccess())
        {
            throw new ServiceException("AI 规则生成失败："
                    + (result == null || StringUtils.isEmpty(result.getError()) ? "模型未返回内容" : result.getError()));
        }
        JSONArray rules = parseJsonArray(result.getContent());
        // 模型输出不可信：统一重写规则 id（时间戳批号+序号），确保与已有规则、历史生成批次均不冲突
        long batch = System.currentTimeMillis();
        for (int i = 0; i < rules.size(); i++)
        {
            JSONObject r = rules.getJSONObject(i);
            if (r == null)
            {
                continue;
            }
            r.put("id", "ai_" + batch + "_" + (i + 1));
        }
        JSONObject root = new JSONObject();
        root.put("v", 1);
        root.put("rules", rules);
        // 用保存期同一套校验兜底，模型输出不可信
        validateForSave(root.toJSONString(), fieldsJson);
        return root;
    }

    private JSONArray parseJsonArray(String content)
    {
        if (StringUtils.isEmpty(content))
        {
            throw new ServiceException("AI 未返回规则内容");
        }
        String s = content.trim();
        if (s.startsWith("```"))
        {
            int first = s.indexOf('\n');
            int last = s.lastIndexOf("```");
            if (first >= 0 && last > first)
            {
                s = s.substring(first + 1, last).trim();
            }
        }
        try
        {
            return JSON.parseArray(s);
        }
        catch (Exception e)
        {
            int l = s.indexOf('[');
            int r = s.lastIndexOf(']');
            if (l >= 0 && r > l)
            {
                try
                {
                    return JSON.parseArray(s.substring(l, r + 1));
                }
                catch (Exception ignore)
                {
                    // 继续抛出统一错误
                }
            }
            throw new ServiceException("AI 返回的内容不是合法规则 JSON，请调整描述后重试");
        }
    }

    // ------------------------------------------------------------------
    // 内部类
    // ------------------------------------------------------------------

    /** 字段定义索引 */
    static class FieldDefs
    {
        final List<JSONObject> all = new ArrayList<>();
        final Map<String, JSONObject> byName = new HashMap<>();
        final Map<String, Map<String, JSONObject>> byTable = new HashMap<>();

        static FieldDefs of(String fieldsJson)
        {
            FieldDefs d = new FieldDefs();
            if (StringUtils.isEmpty(fieldsJson))
            {
                return d;
            }
            JSONArray arr;
            try
            {
                arr = JSON.parseArray(fieldsJson);
            }
            catch (Exception e)
            {
                return d;
            }
            if (arr == null)
            {
                return d;
            }
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject f = arr.getJSONObject(i);
                if (f == null || StringUtils.isEmpty(f.getString("field")))
                {
                    continue;
                }
                if (StringUtils.isEmpty(f.getString("table")))
                {
                    f.put("table", "main");
                }
                d.all.add(f);
                d.byName.putIfAbsent(f.getString("field"), f);
                d.byTable.computeIfAbsent(f.getString("table"), k -> new LinkedHashMap<>())
                        .put(f.getString("field"), f);
            }
            return d;
        }

        boolean exists(String field)
        {
            if (StringUtils.isEmpty(field))
            {
                return false;
            }
            if (field.contains("."))
            {
                String[] p = field.split("\\.", 2);
                return existsInTable(p[0], p[1]);
            }
            return byName.containsKey(field);
        }

        boolean existsInTable(String table, String field)
        {
            Map<String, JSONObject> m = byTable.get(table);
            return m != null && m.containsKey(field);
        }

        String label(String field)
        {
            JSONObject f = byName.get(field);
            return f == null || StringUtils.isEmpty(f.getString("label")) ? field : f.getString("label");
        }

        /** 字段归属表（main 表示主表；null 表示字段不存在） */
        String tableOf(String field)
        {
            JSONObject f = byName.get(field);
            return f == null ? null : f.getString("table");
        }

        /** 字段是否为明细表字段 */
        boolean isDetailField(String field)
        {
            String t = tableOf(field);
            return t != null && !"main".equals(t);
        }
    }

    /** 求值上下文：主表值 + 当前明细行 */
    static class EvalCtx
    {
        final Map<String, Object> values;
        Map<String, Object> row;
        String table;
        /** 字段定义（可选）：用于 sum(明细字段) 单参数写法的归一化 */
        FieldDefs defs;

        EvalCtx(Map<String, Object> values)
        {
            this.values = values;
        }

        EvalCtx withRow(Map<String, Object> row, String table)
        {
            EvalCtx c = new EvalCtx(values);
            c.row = row;
            c.table = table;
            c.defs = defs;
            return c;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows(String table)
        {
            if (table == null)
            {
                return null;
            }
            if (row != null && table.equals(this.table))
            {
                // 行上下文里读表数据仍取整表，保持 detailSum 等聚合可用
            }
            Object d = values.get("detailData");
            if (d instanceof Map)
            {
                Object v = ((Map<String, Object>) d).get(table);
                return v instanceof List ? (List<Map<String, Object>>) v : null;
            }
            if (d instanceof String && ((String) d).startsWith("{"))
            {
                try
                {
                    JSONObject o = JSON.parseObject((String) d);
                    JSONArray a = o.getJSONArray(table);
                    if (a == null)
                    {
                        return null;
                    }
                    List<Map<String, Object>> out = new ArrayList<>();
                    for (int i = 0; i < a.size(); i++)
                    {
                        JSONObject r = a.getJSONObject(i);
                        out.add(r == null ? new HashMap<>() : new HashMap<>(r));
                    }
                    return out;
                }
                catch (Exception ignore)
                {
                    return null;
                }
            }
            return null;
        }
    }
}
