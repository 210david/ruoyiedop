package com.ruoyi.flowable.config;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * 流程表单系统内置字段定义（单一数据源）
 *
 * 背景：参照泛微 e-cology，每个流程表单默认都带这几个系统字段，用户无需手动添加。
 * 这些字段承载平台级语义，不只是展示用：
 *   - 标题(title)        流程标题，发起时写入 Flowable 实例名，列表可快速检索
 *   - 紧急程度(urgency)   档位由 flow_urgency_level 配置表驱动（可自定义名称/排序/封存），
 *                         默认 1普通 / 2紧急 / 3特急，用于待办排序与提醒分级
 *   - 签字意见(signOpinion) 节点办理意见
 *   - 打印次数(printCount)  表单打印计数
 *   - 流程状态(procStatus)  1进行中 2已完成 3已撤销，用于列表状态标识与过滤
 *
 * 本类作为唯一来源：建表时强制补列、字段设计时返回给前端，避免前后端各写一份。
 *
 * @author ruoyi
 */
public class FlowSystemFields
{
    /** 系统字段标识（用于前端置灰不可删） */
    public static final String SYS_FIELD_FLAG = "sysField";

    /** 标题 */
    public static final String FIELD_TITLE = "title";

    /** 紧急程度 */
    public static final String FIELD_URGENCY = "urgency";

    /** 签字意见 */
    public static final String FIELD_SIGN_OPINION = "signOpinion";

    /** 打印次数 */
    public static final String FIELD_PRINT_COUNT = "printCount";

    /** 流程状态 */
    public static final String FIELD_PROC_STATUS = "procStatus";

    /** 系统字段定义（顺序即展示顺序） */
    private static final JSONArray DEFINITIONS = build();

    private static JSONArray build()
    {
        JSONArray arr = new JSONArray();

        JSONObject title = new JSONObject();
        title.put("field", FIELD_TITLE);
        title.put("label", "标题");
        title.put("form", "text");
        title.put("dtype", "text");
        title.put("length", 200);
        title.put("scale", 2);
        title.put("table", "main");
        title.put(SYS_FIELD_FLAG, true);
        arr.add(title);

        JSONObject urgency = new JSONObject();
        urgency.put("field", FIELD_URGENCY);
        urgency.put("label", "紧急程度");
        urgency.put("form", "select");
        urgency.put("dtype", "text");
        urgency.put("length", 20);
        urgency.put("scale", 2);
        urgency.put("table", "main");
        urgency.put(SYS_FIELD_FLAG, true);
        urgency.put("defaultValue", "1");
        JSONArray opts = new JSONArray();
        opts.add(option("普通", "1"));
        opts.add(option("紧急", "2"));
        opts.add(option("特急", "3"));
        urgency.put("selectOptions", opts);
        arr.add(urgency);

        JSONObject sign = new JSONObject();
        sign.put("field", FIELD_SIGN_OPINION);
        sign.put("label", "签字意见");
        sign.put("form", "textarea");
        sign.put("dtype", "text");
        sign.put("length", 2000);
        sign.put("scale", 2);
        sign.put("table", "main");
        sign.put(SYS_FIELD_FLAG, true);
        sign.put("rows", 3);
        arr.add(sign);

        JSONObject printCount = new JSONObject();
        printCount.put("field", FIELD_PRINT_COUNT);
        printCount.put("label", "打印次数");
        printCount.put("form", "number");
        printCount.put("dtype", "int");
        printCount.put("length", 11);
        printCount.put("scale", 0);
        printCount.put("table", "main");
        printCount.put(SYS_FIELD_FLAG, true);
        arr.add(printCount);

        JSONObject procStatus = new JSONObject();
        procStatus.put("field", FIELD_PROC_STATUS);
        procStatus.put("label", "流程状态");
        procStatus.put("form", "select");
        procStatus.put("dtype", "text");
        procStatus.put("length", 20);
        procStatus.put("scale", 2);
        procStatus.put("table", "main");
        procStatus.put(SYS_FIELD_FLAG, true);
        procStatus.put("defaultValue", "1");
        JSONArray statusOpts = new JSONArray();
        statusOpts.add(option("进行中", "1"));
        statusOpts.add(option("已完成", "2"));
        statusOpts.add(option("已撤销", "3"));
        procStatus.put("selectOptions", statusOpts);
        arr.add(procStatus);

        return arr;
    }

    private static JSONObject option(String label, String value)
    {
        JSONObject o = new JSONObject();
        o.put("label", label);
        o.put("value", value);
        return o;
    }

    /** 系统字段定义副本（避免调用方修改常量）——紧急程度使用内置默认档位 */
    public static JSONArray definitions()
    {
        return (JSONArray) DEFINITIONS.clone();
    }

    /**
     * 系统字段定义副本（紧急程度选项由配置表驱动）
     *
     * @param urgencyOptions 紧急程度可选档位 [{label, value}]，为空时回退内置默认档位
     */
    public static JSONArray definitions(JSONArray urgencyOptions)
    {
        JSONArray copy = (JSONArray) DEFINITIONS.clone();
        if (urgencyOptions == null || urgencyOptions.isEmpty())
        {
            return copy;
        }
        for (int i = 0; i < copy.size(); i++)
        {
            JSONObject def = copy.getJSONObject(i);
            if (FIELD_URGENCY.equals(def.getString("field")))
            {
                def.put("selectOptions", urgencyOptions);
                break;
            }
        }
        return copy;
    }

    /** 系统字段名集合 */
    public static List<String> names()
    {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < DEFINITIONS.size(); i++)
        {
            names.add(DEFINITIONS.getJSONObject(i).getString("field"));
        }
        return names;
    }

    /**
     * 把系统字段合并进表单字段列表：
     * 已存在的沿用表单自身配置（不覆盖用户改动，仅补 sysField 标记），缺失的补到主表最前。
     *
     * @param fields 表单原字段列表
     * @return 合并后的字段列表（新列表，不修改入参）
     */
    public static List<JSONObject> merge(List<JSONObject> fields)
    {
        List<JSONObject> origin = fields == null ? new ArrayList<>() : new ArrayList<>(fields);
        java.util.Set<String> exist = new java.util.HashSet<>();
        for (JSONObject f : origin)
        {
            exist.add(f.getString("field"));
        }
        List<JSONObject> built = new ArrayList<>();
        for (int i = 0; i < DEFINITIONS.size(); i++)
        {
            JSONObject def = DEFINITIONS.getJSONObject(i);
            if (exist.contains(def.getString("field")))
            {
                continue;
            }
            built.add(JSONObject.parseObject(def.toJSONString()));
        }
        List<JSONObject> merged = new ArrayList<>(built);
        merged.addAll(origin);
        // 主表字段重排序号：系统字段在最前
        int order = 0;
        for (JSONObject f : merged)
        {
            String table = f.getString("table");
            if (table == null || "main".equals(table))
            {
                f.put("order", ++order);
            }
        }
        return merged;
    }

    /** 是否系统字段 */
    public static boolean isSystemField(String field)
    {
        return field != null && names().contains(field);
    }
}
