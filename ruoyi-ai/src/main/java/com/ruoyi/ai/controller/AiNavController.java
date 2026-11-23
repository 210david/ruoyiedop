package com.ruoyi.ai.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.ai.domain.AiNavIntent;
import com.ruoyi.ai.mapper.AiNavIntentMapper;
import com.ruoyi.ai.service.IAiNavService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysMenuService;

/**
 * AI 菜单直达：意图管理与自查
 *
 * 用途：
 * 1. /myMenus      排查权限过滤是否生效（开发/实施自查）
 * 2. /menuOptions  管理界面下拉：当前用户可见的「可跳转菜单」清单
 * 3. CRUD          维护「问题→菜单」意图及别名
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/nav")
public class AiNavController extends BaseController
{
    @Autowired
    private AiNavIntentMapper navMapper;

    @Autowired
    private IAiNavService navService;

    @Autowired
    private ISysMenuService menuService;

    /**
     * 当前登录人可被推荐的菜单索引（自查用）
     * 返回 {menuId: {path,title,breadcrumb,icon,moduleCode}}
     */
    @PreAuthorize("@ss.hasPermi('ai:nlquery:query')")
    @GetMapping("/myMenus")
    public AjaxResult myMenus()
    {
        return AjaxResult.success(navService.currentUserMenuIndex());
    }

    /**
     * 管理界面「目标菜单」下拉选项：按当前登录人权限裁剪的叶子菜单
     * 只返回 C 类型且配置了组件的菜单（与导航能力一致）
     */
    @PreAuthorize("@ss.hasPermi('ai:nav:query')")
    @GetMapping("/menuOptions")
    public AjaxResult menuOptions()
    {
        Map<Long, Map<String, Object>> index = navService.currentUserMenuIndex();
        List<Map<String, Object>> options = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> e : index.entrySet())
        {
            Map<String, Object> o = new HashMap<>();
            o.put("menuId", e.getKey());
            o.put("path", e.getValue().get("path"));
            o.put("title", e.getValue().get("breadcrumb"));
            options.add(o);
        }
        return AjaxResult.success(options);
    }

    /**
     * 意图列表（管理界面）
     *
     * @param keyword 按 title/aliases/intentKey 模糊过滤
     */
    @PreAuthorize("@ss.hasPermi('ai:nav:list')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String keyword)
    {
        List<AiNavIntent> all = navMapper.selectAll();
        if (StringUtils.isEmpty(keyword))
        {
            return AjaxResult.success(all);
        }
        String kw = keyword.trim().toLowerCase();
        List<AiNavIntent> filtered = new ArrayList<>();
        for (AiNavIntent it : all)
        {
            String hay = (StringUtils.nvl(it.getIntentKey(), "") + " "
                    + StringUtils.nvl(it.getTitle(), "") + " "
                    + StringUtils.nvl(it.getAliases(), "")).toLowerCase();
            if (hay.contains(kw))
            {
                filtered.add(it);
            }
        }
        return AjaxResult.success(filtered);
    }

    /**
     * 意图详情（含解析后的真实路径，便于核对配置是否正确）
     */
    @PreAuthorize("@ss.hasPermi('ai:nav:query')")
    @GetMapping("/{intentId}")
    public AjaxResult getInfo(@PathVariable Long intentId)
    {
        AiNavIntent it = navMapper.selectById(intentId);
        if (it == null)
        {
            return AjaxResult.error("意图不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("intent", it);
        // 顺带给出该菜单对当前登录人的真实路径，配置错误一眼可见
        Map<String, Object> menu = navService.currentUserMenuIndex().get(it.getMenuId());
        data.put("resolvedMenu", menu);
        return AjaxResult.success(data);
    }

    @PreAuthorize("@ss.hasPermi('ai:nav:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AiNavIntent intent)
    {
        String err = validate(intent, null);
        if (err != null)
        {
            return AjaxResult.error(err);
        }
        intent.setCreateBy(getUsername());
        if (StringUtils.isEmpty(intent.getStatus()))
        {
            intent.setStatus("0");
        }
        if (intent.getSortNo() == null)
        {
            intent.setSortNo(10);
        }
        try
        {
            return toAjax(navMapper.insert(intent));
        }
        catch (Exception e)
        {
            // 唯一键冲突（intent_key 重复）给出可读提示
            return AjaxResult.error("保存失败，意图标识可能已存在：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('ai:nav:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody AiNavIntent intent)
    {
        if (intent.getIntentId() == null)
        {
            return AjaxResult.error("缺少意图ID");
        }
        String err = validate(intent, intent.getIntentId());
        if (err != null)
        {
            return AjaxResult.error(err);
        }
        intent.setUpdateBy(getUsername());
        try
        {
            return toAjax(navMapper.update(intent));
        }
        catch (Exception e)
        {
            return AjaxResult.error("保存失败，意图标识可能已存在：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('ai:nav:remove')")
    @DeleteMapping("/{intentId}")
    public AjaxResult remove(@PathVariable Long intentId)
    {
        return toAjax(navMapper.deleteById(intentId));
    }

    /**
     * 表单校验：菜单必须存在且是 C 类型可跳转菜单
     * 防止管理员把按钮/目录/外链配成跳转目标
     */
    private String validate(AiNavIntent intent, Long selfId)
    {
        if (intent == null)
        {
            return "参数不能为空";
        }
        if (StringUtils.isEmpty(intent.getIntentKey()))
        {
            return "意图标识不能为空";
        }
        if (intent.getMenuId() == null)
        {
            return "请选择目标菜单";
        }
        SysMenu menu = menuService.selectMenuById(intent.getMenuId());
        if (menu == null)
        {
            return "目标菜单不存在（menu_id=" + intent.getMenuId() + "）";
        }
        if (!"C".equals(menu.getMenuType()))
        {
            return "只能选择「菜单」类型的目标（当前为" + menu.getMenuType() + "，目录/按钮不可跳转）";
        }
        if (StringUtils.isEmpty(menu.getComponent()))
        {
            return "该菜单未配置组件路径，无法作为跳转目标";
        }
        if (StringUtils.isEmpty(intent.getTitle()))
        {
            return "展示名称不能为空";
        }
        // 同名意图标识排重（编辑时排除自身）
        List<AiNavIntent> same = navMapper.selectByMenuId(intent.getMenuId());
        for (AiNavIntent other : same)
        {
            if (intent.getIntentKey().equals(other.getIntentKey())
                    && (selfId == null || !selfId.equals(other.getIntentId())))
            {
                return "该菜单下已存在相同意图标识：" + other.getIntentKey();
            }
        }
        return null;
    }
}
