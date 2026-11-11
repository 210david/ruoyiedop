package com.ruoyi.flowable.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * 流程编号生成（参照泛微 e-cology 的「流程编号规则」）
 *
 * 规则存在流程模型 metaInfo.processNoRule 里（前端「流程 → 高级设置」配置）：
 * <pre>
 * {
 *   "enabled": true,
 *   "sep": "-",
 *   "reset": "year",
 *   "allowEdit": false,
 *   "segments": [
 *     { "type": "text",   "value": "HT" },
 *     { "type": "date",   "format": "yyyyMMdd" },
 *     { "type": "serial", "len": 4, "start": 1, "step": 1 },
 *     { "type": "field",  "field": "bm" }
 *   ]
 * }
 * </pre>
 * 编号 = 各段按分隔符拼接（空段跳过）。段类型：
 * <ul>
 * <li>text  固定文本（流程缩写）</li>
 * <li>date  日期，格式见 {@link #formatDate(String)}</li>
 * <li>serial 流水号，支持位数、起始编号、步长</li>
 * <li>field 表单字段值（如部门、申请人），取不到则跳过该段</li>
 * </ul>
 *
 * 兼容旧版规则（prefix / dateFmt / seqLen 三段式）：{@link #normalized(JSONObject)} 会换算成 segments。
 *
 * 流水号落在 flow_no_serial 计数器表（no_key = 流程Key|周期，周期由 reset 决定）：
 * 首次取号写入 start，之后每次 +step，跨周期自动从 start 重新开始，
 * 避免用「count(*)+1」导致删除单据后编号重复。
 *
 * @author ruoyi
 */
@Service
public class FlowProcessNoService
{
    private static final Logger log = LoggerFactory.getLogger(FlowProcessNoService.class);

    /** 计数器表名 */
    private static final String SERIAL_TABLE = "flow_no_serial";

    /** 单机部署下的取号互斥锁，保证同一 JVM 内「取号 + 读取」不被并发穿插 */
    private static final Object SEQ_LOCK = new Object();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.flowable.engine.RepositoryService repositoryService;

    /**
     * 读取流程模型配置的编号规则
     *
     * @param processKey 流程定义 Key（= 模型 Key）
     * @return 规则对象；未配置或未启用时返回 null（不生成编号）
     */
    public JSONObject ruleOf(String processKey)
    {
        if (processKey == null || processKey.trim().isEmpty())
        {
            return null;
        }
        try
        {
            org.flowable.engine.repository.Model model = null;
            for (org.flowable.engine.repository.Model m : repositoryService.createModelQuery()
                    .modelKey(processKey).list())
            {
                if (model == null || (m.getVersion() != null && model.getVersion() != null
                        && m.getVersion() > model.getVersion()))
                {
                    model = m;
                }
            }
            if (model == null)
            {
                return null;
            }
            String metaInfo = model.getMetaInfo();
            JSONObject meta = JSONObject.parseObject(metaInfo == null || metaInfo.trim().isEmpty() ? "{}" : metaInfo);
            String ruleJson = meta.getString("processNoRule");
            if (ruleJson == null || ruleJson.trim().isEmpty())
            {
                return null;
            }
            JSONObject rule = JSONObject.parseObject(ruleJson);
            if (rule == null || !rule.getBooleanValue("enabled"))
            {
                return null;
            }
            return rule;
        }
        catch (Exception e)
        {
            log.warn("读取流程编号规则失败（{}）：{}", processKey, e.getMessage());
            return null;
        }
    }

    /** 规则是否允许发起时手工修改编号 */
    public boolean allowEdit(String processKey)
    {
        JSONObject rule = ruleOf(processKey);
        return rule != null && rule.getBooleanValue("allowEdit");
    }

    /**
     * 生成下一个流程编号
     *
     * @param processKey 流程定义 Key
     * @return 编号；规则未启用或取号失败时返回 null（发起流程不因此中断）
     */
    public String next(String processKey)
    {
        return next(processKey, null);
    }

    /**
     * 生成下一个流程编号（可带流程变量，用于「表单字段」段，如部门名称）
     */
    public String next(String processKey, Map<String, Object> vars)
    {
        JSONObject rule = ruleOf(processKey);
        if (rule == null)
        {
            return null;
        }
        try
        {
            JSONObject r = normalized(rule);
            List<JSONObject> segs = segments(r);
            long seq = 0L;
            JSONObject serial = findSerial(segs);
            if (serial != null)
            {
                seq = nextSeq(processKey + "|" + periodOf(r.getString("reset")),
                        startOf(serial), stepOf(serial));
            }
            return build(r, segs, vars, seq);
        }
        catch (Exception e)
        {
            // 计数器表缺失等异常：不阻断发起，只是这张单子没有编号
            log.warn("生成流程编号失败（{}）：{}", processKey, e.getMessage());
            return null;
        }
    }

    /**
     * 编号预览：按规则算出「下一单的真实编号」，但**不消耗流水号**，供高级设置页展示。
     *
     * @param processKey 流程 Key（可为空，规则由前端传）
     * @param ruleOverride 前端正在编辑的规则（未保存也能预览）；为空时读模型里的规则
     * @param sampleVars 表单字段段的示例值（如 {bm: '研发部'}），可为空
     */
    public JSONObject preview(String processKey, JSONObject ruleOverride, Map<String, Object> sampleVars)
    {
        JSONObject out = new JSONObject();
        JSONObject rule = ruleOverride != null ? ruleOverride : ruleOf(processKey);
        if (rule == null || !rule.getBooleanValue("enabled"))
        {
            out.put("enabled", false);
            return out;
        }
        try
        {
            JSONObject r = normalized(rule);
            List<JSONObject> segs = segments(r);
            JSONObject serial = findSerial(segs);
            long start = serial == null ? 0L : startOf(serial);
            long step = serial == null ? 1L : stepOf(serial);
            String key = (processKey == null ? "" : processKey) + "|" + periodOf(r.getString("reset"));
            long current = currentSeq(key);
            // 起始编号是「下限」：还没发过单从起始编号开始；发过单则接着计数器走，
            // 但起始编号被调大时下一次立即跳到新起点（与真实取号规则保持一致）
            long next = current <= 0L ? (start <= 0L ? 1L : start) : Math.max(current + step, start);
            out.put("enabled", true);
            out.put("preview", build(r, segs, sampleVars, next));
            out.put("nextSeq", next);
            out.put("current", current);
            out.put("noKey", key);
            out.put("started", current > 0L);
            // 把归一化后的规则一并返回：发起页据此判断能否手工改编号、默认值显示什么
            out.put("rule", r);
            out.put("allowEdit", r.getBooleanValue("allowEdit"));
        }
        catch (Exception e)
        {
            log.warn("预览流程编号失败：{}", e.getMessage());
            out.put("enabled", true);
            out.put("preview", "");
        }
        return out;
    }

    /** 当前计数器值（不存在返回 0，表示还没发过单） */
    private long currentSeq(String noKey)
    {
        try
        {
            List<Long> list = jdbcTemplate.queryForList(
                    "SELECT `seq` FROM `" + SERIAL_TABLE + "` WHERE `no_key` = ?", Long.class, noKey);
            return list.isEmpty() || list.get(0) == null ? 0L : list.get(0);
        }
        catch (Exception e)
        {
            // 表还没建：当作没发过单，预览按起始编号显示
            return 0L;
        }
    }

    // ==================== 规则归一化 ====================

    /**
     * 把规则换算成统一结构（含 segments、reset）。
     * 旧版规则只有 prefix / dateFmt / seqLen，这里折算成三段，保证历史模型照常发号；
     * 旧版带的 sep（段间分隔符）也折算成「固定文本」段，编号格式与升级前完全一致。
     */
    public JSONObject normalized(JSONObject rule)
    {
        JSONObject r = new JSONObject();
        if (rule == null)
        {
            return r;
        }
        r.put("enabled", rule.getBooleanValue("enabled"));
        // 分隔符不再是独立配置项，统一由「固定文本」段承担，这里只用来兼容老规则
        r.put("sep", "");
        String reset = rule.getString("reset");
        r.put("reset", reset == null || reset.trim().isEmpty() ? "year" : reset.trim());
        r.put("allowEdit", rule.getBooleanValue("allowEdit"));

        JSONArray segs = rule.getJSONArray("segments");
        if (segs != null && !segs.isEmpty())
        {
            r.put("segments", withSep(segs, rule.getString("sep")));
            return r;
        }
        // 旧版三段式：前缀 + 日期 + 流水号
        JSONArray out = new JSONArray();
        String prefix = rule.getString("prefix");
        if (prefix != null && !prefix.trim().isEmpty())
        {
            JSONObject t = new JSONObject();
            t.put("type", "text");
            t.put("value", prefix.trim());
            out.add(t);
        }
        String dateFmt = rule.getString("dateFmt");
        if (dateFmt != null && !dateFmt.trim().isEmpty() && !"none".equalsIgnoreCase(dateFmt))
        {
            JSONObject d = new JSONObject();
            d.put("type", "date");
            d.put("format", dateFmt.trim());
            out.add(d);
        }
        JSONObject s = new JSONObject();
        s.put("type", "serial");
        int len = rule.getIntValue("seqLen");
        s.put("len", len < 1 ? 4 : Math.min(len, 12));
        s.put("start", 1);
        s.put("step", 1);
        out.add(s);
        r.put("segments", withSep(out, rule.getString("sep")));
        return r;
    }

    /**
     * 旧规则兼容：把 sep 折算成段与段之间的「固定文本」段。
     * 新规则不再写 sep（空），直接返回原数组。
     */
    private JSONArray withSep(JSONArray segs, String sep)
    {
        if (sep == null || sep.isEmpty() || segs == null || segs.size() < 2)
        {
            return segs;
        }
        JSONArray out = new JSONArray();
        for (int i = 0; i < segs.size(); i++)
        {
            if (i > 0)
            {
                JSONObject t = new JSONObject();
                t.put("type", "text");
                t.put("value", sep);
                out.add(t);
            }
            out.add(segs.get(i));
        }
        return out;
    }

    private List<JSONObject> segments(JSONObject r)
    {
        List<JSONObject> list = new ArrayList<>();
        JSONArray arr = r.getJSONArray("segments");
        if (arr == null)
        {
            return list;
        }
        for (int i = 0; i < arr.size(); i++)
        {
            JSONObject o = arr.getJSONObject(i);
            if (o != null && o.getString("type") != null)
            {
                list.add(o);
            }
        }
        return list;
    }

    private JSONObject findSerial(List<JSONObject> segs)
    {
        for (JSONObject s : segs)
        {
            if ("serial".equals(s.getString("type")))
            {
                return s;
            }
        }
        return null;
    }

    private long startOf(JSONObject serial)
    {
        long v = serial == null ? 1L : serial.getLongValue("start");
        return v <= 0L ? 1L : v;
    }

    private long stepOf(JSONObject serial)
    {
        long v = serial == null ? 1L : serial.getLongValue("step");
        return v <= 0L ? 1L : v;
    }

    /**
     * 按规则拼出编号（seq 由调用方给：真实发号给计数器值，预览给「下一个」值）。
     * 段与段**直接拼接**，需要分隔符就在规则里放一个内容为「-」的固定文本段（与泛微一致）。
     */
    private String build(JSONObject r, List<JSONObject> segs, Map<String, Object> vars, long seq)
    {
        List<String> parts = new ArrayList<>();
        // 流水号段只认第一个：重复添加的流水号段不再拼一遍，避免出现 CS-2026-0001-0001 这种怪号
        boolean serialUsed = false;
        for (JSONObject s : segs)
        {
            String type = s.getString("type");
            String v = "";
            if ("serial".equals(type) && serialUsed)
            {
                continue;
            }
            if ("text".equals(type))
            {
                v = s.getString("value");
            }
            else if ("date".equals(type))
            {
                v = formatDate(s.getString("format"));
            }
            else if ("serial".equals(type))
            {
                serialUsed = true;
                v = pad(seq, seqLenOf(s));
            }
            else if ("field".equals(type))
            {
                String field = s.getString("field");
                Object o = vars == null || field == null ? null : vars.get(field);
                String raw = o == null ? "" : String.valueOf(o).trim();
                v = raw;
                // 选择框字段的代字映射：{"研发部":"YF"}，没配代字的选项用原值
                JSONObject codes = s.getJSONObject("codes");
                if (codes != null && !raw.isEmpty())
                {
                    String code = codes.getString(raw);
                    if (code != null && !code.trim().isEmpty())
                    {
                        v = code.trim();
                    }
                }
            }
            if (v != null && !v.trim().isEmpty())
            {
                parts.add(v.trim());
            }
        }
        return String.join("", parts);
    }

    private int seqLenOf(JSONObject serial)
    {
        int len = serial == null ? 4 : serial.getIntValue("len");
        if (len <= 0)
        {
            len = 4;
        }
        return Math.min(len, 12);
    }

    /** 支持 yyyy / yy / yyyyMM / yyyyMMdd / yyyyMMddHHmmss / MMdd 等；非法格式退化为空段 */
    private String formatDate(String fmt)
    {
        if (fmt == null || fmt.trim().isEmpty() || "none".equalsIgnoreCase(fmt))
        {
            return "";
        }
        try
        {
            return new SimpleDateFormat(fmt.trim()).format(new Date());
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /** 周期标识：编号流水号的重置维度 */
    private String periodOf(String reset)
    {
        String mode = reset == null ? "year" : reset.trim().toLowerCase();
        String fmt;
        switch (mode)
        {
            case "day":
                fmt = "yyyyMMdd";
                break;
            case "month":
                fmt = "yyyyMM";
                break;
            case "quarter":
                Calendar c = Calendar.getInstance();
                return c.get(Calendar.YEAR) + "Q" + ((c.get(Calendar.MONTH) / 3) + 1);
            case "none":
                return "ALL";
            case "year":
            default:
                fmt = "yyyy";
                break;
        }
        return new SimpleDateFormat(fmt).format(new Date());
    }

    /** 取号：首次写入起始编号，之后按步长自增（INSERT ... ON DUPLICATE KEY UPDATE 保证原子性） */
    private long nextSeq(String noKey, long start, long step)
    {
        synchronized (SEQ_LOCK)
        {
            jdbcTemplate.update("INSERT INTO `" + SERIAL_TABLE
                    + "` (`no_key`, `seq`, `update_time`) VALUES (?, ?, NOW())"
                    + " ON DUPLICATE KEY UPDATE `seq` = `seq` + ?, `update_time` = NOW()",
                    noKey, start, step);
            Long seq = jdbcTemplate.queryForObject(
                    "SELECT `seq` FROM `" + SERIAL_TABLE + "` WHERE `no_key` = ?", Long.class, noKey);
            long v = seq == null ? start : seq;
            // 起始编号被调大（如从 1 改成 100）：下一单直接从新起点开始，
            // 不必等旧计数器追上，也不会与已发出的号重复
            if (v < start)
            {
                jdbcTemplate.update("UPDATE `" + SERIAL_TABLE
                        + "` SET `seq` = ?, `update_time` = NOW() WHERE `no_key` = ?", start, noKey);
                v = start;
            }
            return v;
        }
    }

    /** 流水号补零 */
    private String pad(long seq, int len)
    {
        StringBuilder sb = new StringBuilder(String.valueOf(seq));
        while (sb.length() < len)
        {
            sb.insert(0, '0');
        }
        return sb.toString();
    }
}
