package com.ruoyi.web.controller.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysRoleOnboardTask;
import com.ruoyi.system.domain.SysUserShortcut;
import com.ruoyi.system.mapper.SysRoleOnboardTaskMapper;
import com.ruoyi.system.mapper.SysUserShortcutMapper;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 首页个性化 Controller：常用功能（自动频次 + 手动固定）与岗位新人清单
 *
 * 设计原则：
 * 1. 常用功能不靠用户配置，先按真实访问频次自动沉淀，用户再手动微调；
 * 2. 新人清单按「岗位角色」取模板，判定靠数据（是否访问过该菜单），不靠用户自己打勾；
 * 3. 两者都是用户级数据，无需 @DataScope 数据权限（规范 3.5 统计/个性化类豁免归属）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/home")
public class HomePersonalController extends BaseController
{
    /** 常用功能默认展示条数 */
    private static final int DEFAULT_SHORTCUT_LIMIT = 8;

    /** 常用功能最大展示条数 */
    private static final int MAX_SHORTCUT_LIMIT = 20;

    /** 手动固定上限，防止配置表被滥用 */
    private static final int MAX_PINNED_SIZE = 20;

    @Autowired
    private SysUserShortcutMapper shortcutMapper;

    @Autowired
    private SysRoleOnboardTaskMapper onboardTaskMapper;

    /** 系统级参数（新手上路全局开关） */
    @Autowired
    private ISysConfigService configService;

    /** 新手上路全局开关的参数键名 */
    private static final String ONBOARD_GLOBAL_KEY = "sys.onboard.enabled";

    /**
     * 查询当前用户常用功能（手动固定优先，其次按访问频次）
     */
    @GetMapping("/shortcuts")
    public AjaxResult shortcuts(@RequestParam(value = "limit", required = false) Integer limit)
    {
        int size = (limit == null || limit <= 0) ? DEFAULT_SHORTCUT_LIMIT : Math.min(limit, MAX_SHORTCUT_LIMIT);
        return AjaxResult.success(shortcutMapper.selectByUserId(SecurityUtils.getUserId(), size));
    }

    /**
     * 记录一次菜单访问（前端路由守卫调用，用于自动沉淀常用功能）
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/shortcuts/hit")
    public AjaxResult hit(@RequestBody Map<String, Object> body)
    {
        String path = asText(body.get("path"));
        if (StringUtils.isEmpty(path) || path.length() > 200)
        {
            return error("菜单路径不合法");
        }
        shortcutMapper.recordVisit(SecurityUtils.getUserId(), path,
                cut(asText(body.get("title")), 100),
                cut(asText(body.get("icon")), 100),
                cut(asText(body.get("module")), 50));
        return success();
    }

    /**
     * 保存用户手动固定的常用功能（全量覆盖手动部分，自动频次部分保留）
     */
    @SuppressWarnings("unchecked")
    @PutMapping("/shortcuts/pinned")
    public AjaxResult savePinned(@RequestBody Map<String, Object> body)
    {
        Object raw = body.get("items");
        List<Map<String, Object>> items = new ArrayList<>();
        if (raw instanceof List)
        {
            for (Object o : (List<Object>) raw)
            {
                if (o instanceof Map)
                {
                    items.add((Map<String, Object>) o);
                }
            }
        }
        if (items.size() > MAX_PINNED_SIZE)
        {
            return error("最多固定 " + MAX_PINNED_SIZE + " 个常用功能");
        }

        Long userId = SecurityUtils.getUserId();
        shortcutMapper.deleteManualByUserId(userId);
        for (Map<String, Object> item : items)
        {
            String path = asText(item.get("path"));
            if (StringUtils.isEmpty(path) || path.length() > 200)
            {
                continue;
            }
            shortcutMapper.insertManual(userId, path,
                    cut(asText(item.get("title")), 100),
                    cut(asText(item.get("icon")), 100),
                    cut(asText(item.get("module")), 50));
        }
        return success();
    }

    /**
     * 查询当前用户的岗位新人清单
     *
     * 返回结构：
     * tasks   任务列表（含 done 实时判定结果）
     * total   总步数
     * done    已完成步数
     * roleName 命中的岗位名称（用户有多个岗位时取第一个有模板的角色）
     */
    @GetMapping("/onboard")
    public AjaxResult onboard()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("tasks", new ArrayList<>());
        result.put("total", 0);
        result.put("done", 0);
        result.put("roleName", "");

        Long userId = SecurityUtils.getUserId();
        // 双层开关：系统级关闭后，一切用户设置都不再展示
        result.put("globalEnabled", isGlobalOnboardEnabled());
        result.put("userEnabled", isUserOnboardEnabled(userId));

        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<SysRole> roles = loginUser.getUser() == null ? null : loginUser.getUser().getRoles();
        if (roles == null || roles.isEmpty())
        {
            return AjaxResult.success(result);
        }

        List<Long> roleIds = new ArrayList<>();
        for (SysRole role : roles)
        {
            if (role.getRoleId() != null)
            {
                roleIds.add(role.getRoleId());
            }
        }
        if (roleIds.isEmpty())
        {
            return AjaxResult.success(result);
        }

        List<SysRoleOnboardTask> tasks = onboardTaskMapper.selectByRoleIds(roleIds);
        if (tasks == null || tasks.isEmpty())
        {
            return AjaxResult.success(result);
        }

        Set<String> visited = new HashSet<>(shortcutMapper.selectVisitedPaths(userId));
        Set<Long> manualDone = new HashSet<>(onboardTaskMapper.selectManualDoneTaskIds(userId));

        int doneCount = 0;
        for (SysRoleOnboardTask task : tasks)
        {
            boolean isDone;
            if ("MANUAL".equalsIgnoreCase(task.getCheckType()))
            {
                isDone = manualDone.contains(task.getId());
            }
            else
            {
                String checkPath = StringUtils.isNotEmpty(task.getCheckParam()) ? task.getCheckParam() : task.getMenuPath();
                isDone = StringUtils.isNotEmpty(checkPath) && visited.contains(checkPath);
            }
            task.setDone(isDone);
            if (isDone)
            {
                doneCount++;
            }
        }

        result.put("tasks", tasks);
        result.put("total", tasks.size());
        result.put("done", doneCount);
        result.put("roleName", tasks.get(0).getRoleName() == null ? "" : tasks.get(0).getRoleName());
        return AjaxResult.success(result);
    }

    /**
     * 手工确认某个任务完成（仅 check_type = MANUAL 的任务使用）
     */
    @PostMapping("/onboard/{taskId}/done")
    public AjaxResult onboardDone(@PathVariable("taskId") Long taskId)
    {
        if (taskId == null || taskId <= 0)
        {
            return error("任务ID不合法");
        }
        onboardTaskMapper.insertProgress(SecurityUtils.getUserId(), taskId);
        return success();
    }

    /**
     * 保存当前用户的新手引导开关
     *
     * 请求体：{ "enabled": true } 开启 / { "enabled": false } 关闭
     * 注意：系统级开关关闭时，用户即便设为开启也不会展示。
     */
    @PutMapping("/onboard/setting")
    public AjaxResult saveOnboardSetting(@RequestBody Map<String, Object> body)
    {
        Object raw = body.get("enabled");
        boolean enabled = true;
        if (raw instanceof Boolean)
        {
            enabled = (Boolean) raw;
        }
        else if (raw != null)
        {
            enabled = !"false".equalsIgnoreCase(String.valueOf(raw).trim());
        }
        onboardTaskMapper.saveUserEnabled(SecurityUtils.getUserId(), enabled ? "0" : "1");
        return success();
    }

    /**
     * 系统级开关：管理员在「系统管理 → 参数设置」维护 sys.onboard.enabled
     * 未配置时默认开启，避免部署后功能默认消失。
     */
    private boolean isGlobalOnboardEnabled()
    {
        String value = null;
        try
        {
            value = configService.selectConfigByKey(ONBOARD_GLOBAL_KEY);
        }
        catch (Exception e)
        {
            value = null;
        }
        return !"false".equalsIgnoreCase(value == null ? "" : value.trim());
    }

    /** 用户级开关：无记录视为开启 */
    private boolean isUserOnboardEnabled(Long userId)
    {
        String value = onboardTaskMapper.selectUserEnabled(userId);
        return !"1".equals(value);
    }

    /** 安全取字符串 */
    private String asText(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }

    /** 截断字符串 */
    private String cut(String value, int maxLength)
    {
        if (value == null)
        {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
