package com.ruoyi.ai.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.domain.AiNavIntent;
import com.ruoyi.ai.mapper.AiNavIntentMapper;
import com.ruoyi.ai.service.IAiNavService;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysMenuService;

/**
 * AI导航 Service实现
 *
 * 链路：问题 → 权限菜单索引 → 意图过滤(权限) → 规则命中 → LLM兜底 → 组装actions
 * 安全红线：path 只能由 menu_id + 登录人菜单树计算，绝不接受模型或前端传入的路径字符串
 *
 * @author ruoyi
 */
@Service
public class AiNavServiceImpl implements IAiNavService
{
    private static final Logger log = LoggerFactory.getLogger(AiNavServiceImpl.class);

    /** 单次最多返回入口数 */
    private static final int MAX_ACTIONS = 3;

    /** 低于该置信度不展示，避免把用户带到错误页面 */
    private static final double MIN_CONFIDENCE = 0.55;

    /** 进入提示词的最大候选数（控制 token） */
    private static final int MAX_CANDIDATES = 40;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private AiNavIntentMapper navMapper;

    /** 读取文档绑定的功能入口（P2 唯一事实来源） */
    @Autowired
    private com.ruoyi.ai.mapper.AiKnowledgeDocMapper docNavMapper;

    @Autowired
    private AiClient aiClient;

    // ==================== 对外入口 ====================

    @Override
    public List<Map<String, Object>> resolve(String question)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        if (!navEnabled() || StringUtils.isEmpty(question) || question.trim().isEmpty())
        {
            return out;
        }
        try
        {
            Map<Long, Map<String, Object>> index = currentUserMenuIndex();
            if (index.isEmpty())
            {
                return out;
            }
            List<AiNavIntent> allowed = filterByPermission(index);
            if (allowed.isEmpty())
            {
                return out;
            }

            // ① 规则命中（零模型成本）
            List<Map<String, Object>> hit = matchByRule(question, allowed, index);
            if (!hit.isEmpty())
            {
                return dedup(hit);
            }

            // ② 规则未命中 → LLM 兜底
            List<Map<String, Object>> byAi = routeByAi(question, allowed, index);
            return dedup(byAi);
        }
        catch (Exception e)
        {
            log.warn("AI导航解析失败（不影响主答案）: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> resolveBySources(String question, List<Map<String, Object>> sources)
    {
        if (!navEnabled())
        {
            return new ArrayList<>();
        }
        try
        {
            Map<Long, Map<String, Object>> index = currentUserMenuIndex();
            if (index.isEmpty())
            {
                return new ArrayList<>();
            }

            // 优先：文档绑定菜单（确定性最高，不依赖模型）
            List<Map<String, Object>> bound = resolveByDocBinding(sources, index);
            if (!bound.isEmpty())
            {
                return dedup(bound);
            }
        }
        catch (Exception e)
        {
            log.warn("AI导航(文档绑定)解析失败: {}", e.getMessage());
        }
        // 兜底：走通用语义解析
        return resolve(question);
    }

    /** 按来源文档的 nav_menu_id 直连（P2 核心：管理员标过的归属页面，绝不靠模型猜） */
    /**
     * 按来源文档绑定的功能入口直连（P2 核心）
     *
     * 数据来源是 ai_knowledge_doc.nav_menu_id —— 管理员上传手册时标定的「归属页面」，
     * 属于确定性映射，不依赖模型猜测。
     * 说明：早期设计曾在 ai_nav_intent 上冗余 doc_id，两处并存会导致配置分散，
     * 这里统一以 ai_knowledge_doc.nav_menu_id 为唯一事实来源。
     */
    private List<Map<String, Object>> resolveByDocBinding(List<Map<String, Object>> sources,
                                                          Map<Long, Map<String, Object>> index)
    {
        List<Map<String, Object>> out = new ArrayList<>();
        if (sources == null || sources.isEmpty())
        {
            return out;
        }
        java.util.Set<Long> seenMenu = new java.util.HashSet<>();
        for (Map<String, Object> s : sources)
        {
            Object docIdObj = s.get("docId");
            if (docIdObj == null)
            {
                continue;
            }
            try
            {
                Long docId = ((Number) docIdObj).longValue();
                Long navMenuId = docNavMapper.selectNavMenuIdByDocId(docId);
                if (navMenuId == null || !index.containsKey(navMenuId) || !seenMenu.add(navMenuId))
                {
                    continue;
                }
                Map<String, Object> menu = index.get(navMenuId);
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("intentKey", "doc_" + docId);
                a.put("title", menu.get("title"));
                a.put("path", menu.get("path"));
                a.put("breadcrumb", menu.get("breadcrumb"));
                a.put("icon", menu.get("icon"));
                a.put("moduleCode", menu.get("moduleCode"));
                a.put("query", new HashMap<String, Object>());
                a.put("confidence", 1.0);
                a.put("matchedBy", "doc");
                out.add(a);
            }
            catch (Exception ignore)
            {
                // 单条文档异常不影响其他来源
            }
        }
        return out;
    }

    // ==================== 权限菜单索引 ====================

    @Override
    public Map<Long, Map<String, Object>> currentUserMenuIndex()
    {
        Map<Long, Map<String, Object>> index = new LinkedHashMap<>();
        try
        {
            List<SysMenu> menus = menuService.selectMenuTreeByUserId(SecurityUtils.getUserId());
            walk(menus, "", null, 0, index);
        }
        catch (Exception e)
        {
            log.warn("菜单索引构建失败: {}", e.getMessage());
        }
        return index;
    }

    /**
     * 递归构建 menuId → {path, title, breadcrumb, icon, moduleCode}
     *
     * 路径规则与 sys_menu 的 getRouterPath 保持一致：
     * - 一级目录：path = "/" + menu.path
     * - 子级节点：拼接父路径 + menu.path
     * - 只把「C（菜单）类型且配置了 component 的叶子节点」作为可跳转目标
     */
    private void walk(List<SysMenu> menus, String parentPath, String parentCrumb, int level,
                      Map<Long, Map<String, Object>> index)
    {
        if (menus == null || menus.isEmpty())
        {
            return;
        }
        for (SysMenu m : menus)
        {
            if (m == null)
            {
                continue;
            }
            String name = StringUtils.nvl(m.getMenuName(), "");
            String crumb = StringUtils.isEmpty(parentCrumb) ? name : parentCrumb + " / " + name;

            List<SysMenu> children = m.getChildren();
            if (children != null && !children.isEmpty())
            {
                // 目录节点：一级加 "/"，其余拼接
                String dirPath = (level == 0) ? "/" + stripSlash(m.getPath())
                        : joinPath(parentPath, m.getPath());
                walk(children, dirPath, crumb, level + 1, index);
                continue;
            }

            // 叶子节点：只收 C 类型 + 有组件（外链、纯目录、按钮全部排除）
            if (!UserConstants.TYPE_MENU.equals(m.getMenuType()))
            {
                continue;
            }
            if (StringUtils.isEmpty(m.getComponent()))
            {
                continue;
            }
            String fullPath = joinPath(parentPath, m.getPath());
            if (StringUtils.isEmpty(fullPath) || "/".equals(fullPath))
            {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("menuId", m.getMenuId());
            item.put("path", fullPath);
            item.put("title", name);
            item.put("breadcrumb", crumb);
            item.put("icon", m.getIcon());
            item.put("moduleCode", moduleOf(fullPath));
            index.put(m.getMenuId(), item);
        }
    }

    /** 拼接路径（与前端 getNormalPath 口径一致：去重斜杠、去尾部斜杠） */
    private String joinPath(String parent, String path)
    {
        String p = StringUtils.nvl(path, "").trim();
        // 外链不作为站内导航目标
        if (p.startsWith("http://") || p.startsWith("https://"))
        {
            return "";
        }
        if (p.isEmpty())
        {
            return "";
        }
        if (StringUtils.isEmpty(parent))
        {
            return p.startsWith("/") ? p : "/" + p;
        }
        String base = parent.endsWith("/") ? parent.substring(0, parent.length() - 1) : parent;
        String rel = p.startsWith("/") ? p.substring(1) : p;
        if (!base.startsWith("/"))
        {
            base = "/" + base;
        }
        return base + "/" + rel;
    }

    private String stripSlash(String path)
    {
        String p = StringUtils.nvl(path, "").trim();
        while (p.startsWith("/"))
        {
            p = p.substring(1);
        }
        while (p.endsWith("/"))
        {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    /** 取路径首段作为模块编码，如 /wms/inbound/order → wms */
    private String moduleOf(String path)
    {
        if (StringUtils.isEmpty(path))
        {
            return "";
        }
        String p = stripSlash(path);
        int idx = p.indexOf('/');
        return idx > 0 ? p.substring(0, idx) : p;
    }

    // ==================== 意图过滤与匹配 ====================

    /** 权限过滤：目标菜单不在当前用户菜单树里 → 该意图不参与推荐（不泄露菜单存在性） */
    private List<AiNavIntent> filterByPermission(Map<Long, Map<String, Object>> index)
    {
        List<AiNavIntent> all = navMapper.selectEnabledList();
        List<AiNavIntent> allowed = new ArrayList<>();
        if (all == null)
        {
            return allowed;
        }
        for (AiNavIntent it : all)
        {
            if (it.getMenuId() != null && index.containsKey(it.getMenuId()))
            {
                allowed.add(it);
            }
        }
        return allowed;
    }

    /** 规则匹配：问题中是否出现标题或别名（零模型成本，实测命中约 79%） */
    private List<Map<String, Object>> matchByRule(String question, List<AiNavIntent> intents,
                                                  Map<Long, Map<String, Object>> index)
    {
        String q = question.toLowerCase();
        List<Map<String, Object>> hits = new ArrayList<>();
        for (AiNavIntent it : intents)
        {
            double score = 0;
            // 标题命中优先（置信度更高）
            if (StringUtils.isNotEmpty(it.getTitle()) && q.contains(it.getTitle().toLowerCase()))
            {
                score = 0.9;
            }
            else if (StringUtils.isNotEmpty(it.getAliases()))
            {
                for (String a : it.getAliases().split(","))
                {
                    String t = a.trim().toLowerCase();
                    // 单字别名（如"库"）误命中率高，要求至少两个字符
                    if (t.length() >= 2 && q.contains(t))
                    {
                        score = 0.8;
                        break;
                    }
                }
            }
            if (score > 0)
            {
                Map<String, Object> a = buildAction(it, index);
                if (a != null)
                {
                    a.put("confidence", score);
                    a.put("matchedBy", "rule");
                    hits.add(a);
                }
            }
        }
        return hits;
    }

    /**
     * LLM 路由：只在候选意图里选 intentKey，不允许输出路径
     *
     * 两级路由（候选量大时必须）：
     *   候选 ≤ MAX_CANDIDATES 时单级调用，一次选到具体功能；
     *   超过时先按模块分组选出「最相关的 2~3 个模块」，再只在这些模块内选具体功能。
     * 原因：意图扩充到上百条后，把所有候选塞进提示词会导致 token 暴涨、
     *      模型在大列表里选错的概率显著上升（尤其"质量检验"这类重名词）。
     */
    private List<Map<String, Object>> routeByAi(String question, List<AiNavIntent> intents,
                                                Map<Long, Map<String, Object>> index)
    {
        List<AiNavIntent> candidates = intents;
        if (candidates.size() > MAX_CANDIDATES)
        {
            candidates = narrowByModule(question, intents);
            if (candidates.isEmpty())
            {
                return new ArrayList<>();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (AiNavIntent it : candidates)
        {
            sb.append("{\"k\":\"").append(it.getIntentKey())
                    .append("\",\"t\":\"").append(StringUtils.nvl(it.getTitle(), ""))
                    .append("\",\"a\":\"").append(StringUtils.nvl(it.getAliases(), ""))
                    .append("\",\"e\":\"").append(StringUtils.nvl(it.getExamples(), ""))
                    .append("\"}\n");
        }
        String prompt = "你是系统菜单导航器。根据用户问题，判断他想去哪个功能页面。\n"
                + "候选功能（k=标识, t=名称, a=别名, e=示例问句）：\n" + sb
                + "\n只输出JSON：{\"k\":\"功能标识\",\"c\":置信度0到1}\n"
                + "规则：\n"
                + "1. 用户问题与任何功能都不相关（如闲聊、纯查数据）时输出 {\"k\":\"none\",\"c\":0}\n"
                + "2. 仅当用户明确表达「去哪里、在哪、怎么操作、怎么新增、怎么发起、怎么录入」"
                + "这类「找页面/找入口」的意图时才选\n"
                + "3. 只从候选里选，不要编造标识\n"
                + "4. 不要输出任何解释文字\n"
                + "\n用户问题：" + question;

        try
        {
            AiResult ai = aiClient.chatWithTimeout("platform", "ai_nav_route", prompt, 8);
            if (!ai.isSuccess() || StringUtils.isEmpty(ai.getContent()))
            {
                return new ArrayList<>();
            }
            JSONObject obj = parseJson(ai.getContent());
            if (obj == null)
            {
                return new ArrayList<>();
            }
            String key = obj.getString("k");
            double conf = obj.getDoubleValue("c");
            if (StringUtils.isEmpty(key) || "none".equalsIgnoreCase(key) || conf < MIN_CONFIDENCE)
            {
                return new ArrayList<>();
            }
            List<Map<String, Object>> out = new ArrayList<>();
            for (AiNavIntent it : candidates)
            {
                if (it.getIntentKey().equals(key))
                {
                    Map<String, Object> a = buildAction(it, index);
                    if (a != null)
                    {
                        a.put("confidence", conf);
                        a.put("matchedBy", "ai");
                        out.add(a);
                    }
                    break;
                }
            }
            return out;
        }
        catch (Exception e)
        {
            log.warn("AI导航路由失败（降级为无入口）: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 从模型输出中抠出 JSON（兼容 ```json 代码块与前后解释文字） */
    /**
     * 两级路由第一级：先收敛到相关模块，再在模块内选具体功能
     *
     * 策略：关键词优先（零成本），未命中再用 LLM 选模块。
     * 模块关键词来自该模块下所有意图的 title/aliases，不需要额外维护模块词典。
     */
    private List<AiNavIntent> narrowByModule(String question, List<AiNavIntent> intents)
    {
        String q = StringUtils.nvl(question, "").toLowerCase();
        if (q.isEmpty())
        {
            return new ArrayList<>();
        }

        // 按模块分组
        Map<String, List<AiNavIntent>> byModule = new LinkedHashMap<>();
        for (AiNavIntent it : intents)
        {
            String mod = StringUtils.nvl(it.getModuleCode(), "other");
            byModule.computeIfAbsent(mod, k -> new ArrayList<>()).add(it);
        }
        if (byModule.size() <= 1)
        {
            return intents;
        }

        // 关键词匹配：统计每个模块被命中的意图数，命中说明该模块与问题相关
        Map<String, Integer> score = new HashMap<>();
        for (AiNavIntent it : intents)
        {
            String mod = StringUtils.nvl(it.getModuleCode(), "other");
            if (hitInQuestion(q, it))
            {
                score.merge(mod, 1, Integer::sum);
            }
        }

        List<String> picked = new ArrayList<>();
        if (!score.isEmpty())
        {
            picked.addAll(score.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList()));
        }

        // 关键词无命中 → LLM 选模块（候选只有十几个，成本极低）
        if (picked.isEmpty())
        {
            picked = pickModulesByAi(question, byModule);
        }
        if (picked.isEmpty())
        {
            return new ArrayList<>();
        }

        List<AiNavIntent> narrowed = new ArrayList<>();
        for (String mod : picked)
        {
            List<AiNavIntent> list = byModule.get(mod);
            if (list != null)
            {
                narrowed.addAll(list);
            }
        }
        if (narrowed.size() > MAX_CANDIDATES)
        {
            narrowed = narrowed.subList(0, MAX_CANDIDATES);
        }
        return narrowed;
    }

    /** 单条意图是否命中问题（标题或别名包含于问题） */
    private boolean hitInQuestion(String q, AiNavIntent it)
    {
        if (StringUtils.isNotEmpty(it.getTitle()) && q.contains(it.getTitle().toLowerCase()))
        {
            return true;
        }
        if (StringUtils.isNotEmpty(it.getAliases()))
        {
            for (String a : it.getAliases().split(","))
            {
                String t = a.trim().toLowerCase();
                if (t.length() >= 2 && q.contains(t))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** LLM 选模块：候选只有模块名与功能名摘要，成本可控 */
    private List<String> pickModulesByAi(String question, Map<String, List<AiNavIntent>> byModule)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<AiNavIntent>> e : byModule.entrySet())
        {
            sb.append("- ").append(e.getKey()).append("：");
            List<AiNavIntent> list = e.getValue();
            for (int i = 0; i < list.size() && i < 12; i++)
            {
                if (i > 0)
                {
                    sb.append("、");
                }
                sb.append(StringUtils.nvl(list.get(i).getTitle(), ""));
            }
            sb.append("\n");
        }
        String prompt = "你是系统模块分类器。判断用户想去哪个业务模块的页面。\n"
                + "可选模块及其包含的功能：\n" + sb
                + "\n只输出JSON：{\"modules\":[\"模块编码1\",\"模块编码2\"],\"c\":置信度0到1}\n"
                + "规则：\n"
                + "1. 最多选 3 个最可能相关的模块\n"
                + "2. 完全不相关时输出 {\"modules\":[],\"c\":0}\n"
                + "3. 只使用上面列出的模块编码，不要编造\n"
                + "4. 不要输出任何解释文字\n"
                + "\n用户问题：" + question;
        try
        {
            AiResult ai = aiClient.chatWithTimeout("platform", "ai_nav_module", prompt, 8);
            if (!ai.isSuccess() || StringUtils.isEmpty(ai.getContent()))
            {
                return new ArrayList<>();
            }
            JSONObject obj = parseJson(ai.getContent());
            if (obj == null)
            {
                return new ArrayList<>();
            }
            double conf = obj.getDoubleValue("c");
            if (conf < MIN_CONFIDENCE)
            {
                return new ArrayList<>();
            }
            List<String> out = new ArrayList<>();
            com.alibaba.fastjson2.JSONArray arr = obj.getJSONArray("modules");
            if (arr != null)
            {
                for (int i = 0; i < arr.size() && i < 3; i++)
                {
                    String mod = arr.getString(i);
                    if (StringUtils.isNotEmpty(mod) && byModule.containsKey(mod))
                    {
                        out.add(mod);
                    }
                }
            }
            return out;
        }
        catch (Exception e)
        {
            log.warn("AI导航模块选择失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private JSONObject parseJson(String content)
    {
        try
        {
            String c = StringUtils.nvl(content, "").trim();
            int s = c.indexOf('{');
            int e = c.lastIndexOf('}');
            if (s < 0 || e <= s)
            {
                return null;
            }
            return JSON.parseObject(c.substring(s, e + 1));
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    /** 组装前端入口：path 只从权限索引取，杜绝模型幻觉路径 */
    private Map<String, Object> buildAction(AiNavIntent it, Map<Long, Map<String, Object>> index)
    {
        Map<String, Object> menu = index.get(it.getMenuId());
        if (menu == null)
        {
            return null;
        }
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("intentKey", it.getIntentKey());
        a.put("title", StringUtils.isNotEmpty(it.getTitle()) ? it.getTitle() : menu.get("title"));
        a.put("path", menu.get("path"));
        a.put("breadcrumb", menu.get("breadcrumb"));
        a.put("icon", menu.get("icon"));
        a.put("moduleCode", menu.get("moduleCode"));
        a.put("query", parseQuery(it.getNavQuery()));
        return a;
    }

    private Map<String, Object> parseQuery(String json)
    {
        if (StringUtils.isEmpty(json))
        {
            return new HashMap<>();
        }
        try
        {
            return JSON.parseObject(json);
        }
        catch (Exception e)
        {
            return new HashMap<>();
        }
    }

    /** 去重（同路径只留一个）+ 按置信度降序 + 截断到上限 */
    private List<Map<String, Object>> dedup(List<Map<String, Object>> list)
    {
        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        if (list != null)
        {
            for (Map<String, Object> a : list)
            {
                if (a == null)
                {
                    continue;
                }
                String p = String.valueOf(a.get("path"));
                if (m.containsKey(p))
                {
                    continue;
                }
                m.put(p, a);
            }
        }
        List<Map<String, Object>> out = new ArrayList<>(m.values());
        out.sort((x, y) -> Double.compare(confOf(y), confOf(x)));
        if (out.size() > MAX_ACTIONS)
        {
            return new ArrayList<>(out.subList(0, MAX_ACTIONS));
        }
        return out;
    }

    private double confOf(Map<String, Object> a)
    {
        Object v = a == null ? null : a.get("confidence");
        return v instanceof Number ? ((Number) v).doubleValue() : 0.0;
    }

    @Override
    public boolean navEnabled()
    {
        try
        {
            return "true".equalsIgnoreCase(configService.selectConfigByKey("ai.nav.enabled"));
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
