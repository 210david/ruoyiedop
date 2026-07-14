package com.ruoyi.web.controller.system;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.core.domain.entity.SysDictType;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysMenuMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.system.service.ISysDictTypeService;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 系统配置备份 - 一键导出/导入
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/config")
public class SysConfigBackupController extends BaseController
{
    @Autowired
    private ISysConfigService configService;

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Autowired
    private ISysDictDataService dictDataService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 导出系统配置（字典、参数、菜单、角色、角色菜单权限）
     * GET /system/config/export
     */
    @PreAuthorize("@ss.hasPermi('system:config:export')")
    @Log(title = "系统配置备份", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult exportConfig()
    {
        Map<String, Object> backup = new LinkedHashMap<>();

        // 1. 导出参数配置
        List<SysConfig> configList = configService.selectConfigList(new SysConfig());
        backup.put("sys_config", configList);

        // 2. 导出字典类型
        List<SysDictType> dictTypeList = dictTypeService.selectDictTypeAll();
        backup.put("sys_dict_type", dictTypeList);

        // 3. 导出字典数据
        List<SysDictData> dictDataList = dictDataService.selectDictDataList(new SysDictData());
        backup.put("sys_dict_data", dictDataList);

        // 4. 导出菜单
        List<SysMenu> menuList = menuMapper.selectMenuTreeAll();
        backup.put("sys_menu", menuList);

        // 5. 导出角色
        List<SysRole> roleList = roleService.selectRoleAll();
        backup.put("sys_role", roleList);

        // 6. 导出角色-菜单关联
        List<Map<String, Long>> roleMenuList = new ArrayList<>();
        for (SysRole role : roleList)
        {
            List<Long> menuIds = menuService.selectMenuListByRoleId(role.getRoleId());
            for (Long menuId : menuIds)
            {
                Map<String, Long> rm = new HashMap<>();
                rm.put("roleId", role.getRoleId());
                rm.put("menuId", menuId);
                roleMenuList.add(rm);
            }
        }
        backup.put("sys_role_menu", roleMenuList);

        // 汇总信息
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("backupTime", System.currentTimeMillis());
        result.put("summary", buildSummary(backup));
        result.put("data", backup);

        return success(result);
    }

    /**
     * 导入系统配置（字典、参数、菜单、角色、角色菜单权限）
     * POST /system/config/import
     */
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @Log(title = "系统配置备份", businessType = BusinessType.INSERT)
    @PostMapping("/import")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult importConfig(@RequestBody Map<String, Object> requestBody)
    {
        StringBuilder log = new StringBuilder();
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) requestBody.get("data");
        if (data == null)
        {
            return error("导入数据格式错误：缺少 data 节点");
        }

        // 1. 导入参数配置（追加模式）
        log.append(importConfigItems(data)).append("; ");

        // 2. 导入字典类型（追加模式）
        log.append(importDictTypes(data)).append("; ");

        // 3. 导入字典数据（追加模式）
        log.append(importDictData(data)).append("; ");

        // 4. 导入菜单（追加模式）
        log.append(importMenus(data)).append("; ");

        // 5. 导入角色（追加模式）
        Map<Long, Long> roleIdMapping = importRoles(data);
        log.append("角色导入完成; ");

        // 6. 导入角色-菜单关联（增量更新）
        importRoleMenus(data, roleIdMapping);
        log.append("角色菜单关联导入完成");

        // 刷新缓存
        configService.resetConfigCache();
        dictTypeService.resetDictCache();

        return success("配置导入完成：" + log);
    }

    // ==================== 导入辅助方法 ====================

    /**
     * 导入参数配置
     */
    @SuppressWarnings("unchecked")
    private String importConfigItems(Map<String, Object> data)
    {
        List<Map<String, Object>> configs = (List<Map<String, Object>>) data.get("sys_config");
        if (configs == null || configs.isEmpty())
        {
            return "无参数配置需要导入";
        }

        int inserted = 0, skipped = 0;
        for (Map<String, Object> item : configs)
        {
            try
            {
                String configKey = (String) item.get("configKey");
                String configValue = (String) item.get("configValue");
                String configName = (String) item.get("configName");
                String configType = (String) item.get("configType");

                if (configKey == null)
                {
                    continue;
                }

                // 检查是否已存在
                String existing = configService.selectConfigByKey(configKey);
                if (existing != null && !existing.isEmpty())
                {
                    skipped++;
                    continue;
                }

                SysConfig config = new SysConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                config.setConfigName(configName != null ? configName : configKey);
                config.setConfigType(configType != null ? configType : "Y");
                config.setCreateBy(getUsername());

                configService.insertConfig(config);
                inserted++;
            }
            catch (Exception e)
            {
                logger.warn("导入参数配置失败: {}", e.getMessage());
            }
        }

        return String.format("参数配置：新增 %d 条，跳过 %d 条", inserted, skipped);
    }

    /**
     * 导入字典类型
     */
    @SuppressWarnings("unchecked")
    private String importDictTypes(Map<String, Object> data)
    {
        List<Map<String, Object>> types = (List<Map<String, Object>>) data.get("sys_dict_type");
        if (types == null || types.isEmpty())
        {
            return "无字典类型需要导入";
        }

        int inserted = 0, skipped = 0;
        for (Map<String, Object> item : types)
        {
            try
            {
                String dictType = (String) item.get("dictType");
                if (dictType == null)
                {
                    continue;
                }

                // 检查是否已存在
                SysDictType existing = dictTypeService.selectDictTypeByType(dictType);
                if (existing != null)
                {
                    skipped++;
                    continue;
                }

                SysDictType type = new SysDictType();
                type.setDictName((String) item.get("dictName"));
                type.setDictType(dictType);
                type.setStatus((String) item.get("status"));
                type.setCreateBy(getUsername());
                type.setRemark((String) item.get("remark"));

                dictTypeService.insertDictType(type);
                inserted++;
            }
            catch (Exception e)
            {
                logger.warn("导入字典类型失败: {}", e.getMessage());
            }
        }

        return String.format("字典类型：新增 %d 条，跳过 %d 条", inserted, skipped);
    }

    /**
     * 导入字典数据
     */
    @SuppressWarnings("unchecked")
    private String importDictData(Map<String, Object> data)
    {
        List<Map<String, Object>> dicts = (List<Map<String, Object>>) data.get("sys_dict_data");
        if (dicts == null || dicts.isEmpty())
        {
            return "无字典数据需要导入";
        }

        int inserted = 0, skipped = 0;
        for (Map<String, Object> item : dicts)
        {
            try
            {
                String dictType = (String) item.get("dictType");
                String dictValue = (String) item.get("dictValue");

                if (dictType == null || dictValue == null)
                {
                    continue;
                }

                // 检查是否已存在（同一类型下同值）
                List<SysDictData> existingList = dictDataService.selectDictDataByType(dictType);
                boolean exists = existingList.stream()
                        .anyMatch(d -> dictValue.equals(d.getDictValue()));
                if (exists)
                {
                    skipped++;
                    continue;
                }

                SysDictData dictData = new SysDictData();
                dictData.setDictType(dictType);
                dictData.setDictValue(dictValue);
                dictData.setDictLabel((String) item.get("dictLabel"));
                dictData.setDictSort(Long.valueOf(getIntValue(item, "dictSort", 0)));
                dictData.setCssClass((String) item.get("cssClass"));
                dictData.setListClass((String) item.get("listClass"));
                dictData.setIsDefault((String) item.get("isDefault"));
                dictData.setStatus((String) item.get("status"));
                dictData.setCreateBy(getUsername());
                dictData.setRemark((String) item.get("remark"));

                dictDataService.insertDictData(dictData);
                inserted++;
            }
            catch (Exception e)
            {
                logger.warn("导入字典数据失败: {}", e.getMessage());
            }
        }

        return String.format("字典数据：新增 %d 条，跳过 %d 条", inserted, skipped);
    }

    /**
     * 导入菜单
     */
    @SuppressWarnings("unchecked")
    private String importMenus(Map<String, Object> data)
    {
        List<Map<String, Object>> menus = (List<Map<String, Object>>) data.get("sys_menu");
        if (menus == null || menus.isEmpty())
        {
            return "无菜单需要导入";
        }

        int inserted = 0, skipped = 0;
        for (Map<String, Object> item : menus)
        {
            try
            {
                String menuName = (String) item.get("menuName");
                if (menuName == null)
                {
                    continue;
                }

                // 检查是否已存在（按名称+父ID）
                SysMenu checkMenu = new SysMenu();
                checkMenu.setMenuName(menuName);
                checkMenu.setParentId(getLongValue(item, "parentId", 0L));
                if (!menuService.checkMenuNameUnique(checkMenu))
                {
                    skipped++;
                    continue;
                }

                SysMenu menu = new SysMenu();
                menu.setMenuName(menuName);
                menu.setParentId(getLongValue(item, "parentId", 0L));
                menu.setOrderNum(getIntValue(item, "orderNum", 0));
                menu.setPath((String) item.get("path"));
                menu.setComponent((String) item.get("component"));
                menu.setQuery((String) item.get("query"));
                menu.setRouteName((String) item.get("routeName"));
                menu.setIsFrame((String) item.get("isFrame"));
                menu.setIsCache((String) item.get("isCache"));
                menu.setMenuType((String) item.get("menuType"));
                menu.setVisible((String) item.get("visible"));
                menu.setStatus((String) item.get("status"));
                menu.setPerms((String) item.get("perms"));
                menu.setIcon((String) item.get("icon"));
                menu.setCreateBy(getUsername());
                menu.setRemark((String) item.get("remark"));

                menuService.insertMenu(menu);
                inserted++;
            }
            catch (Exception e)
            {
                logger.warn("导入菜单失败: {}", e.getMessage());
            }
        }

        return String.format("菜单：新增 %d 条，跳过 %d 条", inserted, skipped);
    }

    /**
     * 导入角色（返回旧ID→新ID映射）
     */
    @SuppressWarnings("unchecked")
    private Map<Long, Long> importRoles(Map<String, Object> data)
    {
        Map<Long, Long> idMapping = new HashMap<>();
        List<Map<String, Object>> roles = (List<Map<String, Object>>) data.get("sys_role");
        if (roles == null || roles.isEmpty())
        {
            return idMapping;
        }

        for (Map<String, Object> item : roles)
        {
            try
            {
                String roleKey = (String) item.get("roleKey");
                String roleName = (String) item.get("roleName");

                if (roleName == null || roleKey == null)
                {
                    continue;
                }

                SysRole checkRole = new SysRole();
                checkRole.setRoleName(roleName);
                checkRole.setRoleKey(roleKey);

                // 检查是否已存在
                if (!roleService.checkRoleNameUnique(checkRole) || !roleService.checkRoleKeyUnique(checkRole))
                {
                    // 已存在，查找并记录映射
                    List<SysRole> existingRoles = roleMapper.selectRoleList(new SysRole());
                    for (SysRole r : existingRoles)
                    {
                        if (roleKey.equals(r.getRoleKey()))
                        {
                            Long oldId = getLongValue(item, "roleId", null);
                            if (oldId != null)
                            {
                                idMapping.put(oldId, r.getRoleId());
                            }
                            break;
                        }
                    }
                    continue;
                }

                SysRole role = new SysRole();
                role.setRoleName(roleName);
                role.setRoleKey(roleKey);
                role.setRoleSort(getIntValue(item, "roleSort", 0));
                role.setDataScope((String) item.get("dataScope"));
                role.setMenuCheckStrictly(getBooleanValue(item, "menuCheckStrictly", false));
                role.setDeptCheckStrictly(getBooleanValue(item, "deptCheckStrictly", false));
                role.setStatus((String) item.get("status"));
                role.setCreateBy(getUsername());
                role.setRemark((String) item.get("remark"));

                roleService.insertRole(role);

                // 记录映射：原始ID → 新ID
                Long oldId = getLongValue(item, "roleId", null);
                if (oldId != null)
                {
                    idMapping.put(oldId, role.getRoleId());
                }
            }
            catch (Exception e)
            {
                logger.warn("导入角色失败: {}", e.getMessage());
            }
        }

        return idMapping;
    }

    /**
     * 导入角色-菜单关联（追加模式）
     */
    @SuppressWarnings("unchecked")
    private void importRoleMenus(Map<String, Object> data, Map<Long, Long> roleIdMapping)
    {
        List<Map<String, Object>> roleMenus = (List<Map<String, Object>>) data.get("sys_role_menu");
        if (roleMenus == null || roleMenus.isEmpty())
        {
            return;
        }

        // 构建反向映射：新角色ID → 是否已存在
        Set<Long> processedRoleIds = new HashSet<>();

        for (Map<String, Object> item : roleMenus)
        {
            try
            {
                Long oldRoleId = getLongValue(item, "roleId", null);
                Long menuId = getLongValue(item, "menuId", null);

                if (oldRoleId == null || menuId == null)
                {
                    continue;
                }

                // 映射到新的角色ID
                Long newRoleId = roleIdMapping.get(oldRoleId);
                if (newRoleId == null)
                {
                    // 如果角色ID不在映射中（可能是已存在的角色），使用原ID
                    newRoleId = oldRoleId;
                }

                // 每个角色只处理一次（追加其菜单权限）
                if (!processedRoleIds.contains(newRoleId))
                {
                    // 获取角色已有的菜单ID列表
                    List<Long> existingMenuIds = menuService.selectMenuListByRoleId(newRoleId);

                    // 合并新的菜单权限
                    Set<Long> mergedIds = new LinkedHashSet<>(existingMenuIds);

                    // 从导入数据中收集该角色的所有菜单
                    for (Map<String, Object> rmItem : roleMenus)
                    {
                        Long rmOldRoleId = getLongValue(rmItem, "roleId", null);
                        Long rmNewRoleId = roleIdMapping.getOrDefault(rmOldRoleId, rmOldRoleId);
                        if (newRoleId.equals(rmNewRoleId))
                        {
                            Long rmMenuId = getLongValue(rmItem, "menuId", null);
                            if (rmMenuId != null)
                            {
                                mergedIds.add(rmMenuId);
                            }
                        }
                    }

                    // 更新角色菜单权限
                    SysRole role = new SysRole();
                    role.setRoleId(newRoleId);
                    role.setMenuIds(mergedIds.toArray(new Long[0]));
                    roleService.updateRole(role);

                    processedRoleIds.add(newRoleId);
                }
            }
            catch (Exception e)
            {
                logger.warn("导入角色菜单关联失败: {}", e.getMessage());
            }
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 构建汇总信息
     */
    @SuppressWarnings("rawtypes")
    private Map<String, Integer> buildSummary(Map<String, Object> backup)
    {
        Map<String, Integer> summary = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : backup.entrySet())
        {
            if (entry.getValue() instanceof List)
            {
                summary.put(entry.getKey(), ((List) entry.getValue()).size());
            }
        }
        return summary;
    }

    /**
     * 安全获取整数值
     */
    private int getIntValue(Map<String, Object> map, String key, int defaultValue)
    {
        Object value = map.get(key);
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(value.toString());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    /**
     * 安全获取长整数值
     */
    private Long getLongValue(Map<String, Object> map, String key, Long defaultValue)
    {
        Object value = map.get(key);
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.parseLong(value.toString());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    /**
     * 安全获取布尔值
     */
    private boolean getBooleanValue(Map<String, Object> map, String key, boolean defaultValue)
    {
        Object value = map.get(key);
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(value.toString()) || "1".equals(value.toString());
    }
}
