-- ============================================================
-- 金蝶云星辰对接 菜单 + 权限 + 管理员授权
-- 幂等：重复执行不会产生重复菜单
-- ============================================================

-- 1. 一级菜单：金蝶对接（顶部导航）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063109, '金蝶云星辰集成', 9063000, 3, 'kingdee', 'kingdee/index', '', '', 1, 0, 'C', '0', '0', 'kingdee:config:list', 'edit',
       'admin', NOW(), '金蝶云星辰对接（基础数据同步）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063109);

-- 历史环境修正: 旧结构(目录9063108+子菜单9063109)迁移为单层菜单
UPDATE sys_menu SET menu_name = '金蝶云星辰集成', parent_id = 9063000, path = 'kingdee', component = 'kingdee/index', menu_type = 'C', perms = 'kingdee:config:list', icon = 'edit', order_num = 3
WHERE menu_id = 9063109 AND (parent_id <> 9063000 OR menu_type <> 'C');
DELETE FROM sys_menu WHERE menu_id = 9063108;

-- 2. 二级菜单：金蝶配置（页面）
-- 菜单本体已在顶层段以 C 型直建(9063109), 此段仅做历史结构幂等修正
UPDATE sys_menu SET path = 'kingdee', component = 'kingdee/index', menu_type = 'C', parent_id = 9063000
WHERE menu_id = 9063109 AND (path <> 'kingdee' OR component <> 'kingdee/index' OR parent_id <> 9063000);

-- 3. 按钮权限：保存修改
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶配置修改', m.menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'kingdee:config:edit', '#',
       'admin', NOW(), '保存配置 + 测试连通性'
FROM sys_menu m
WHERE m.perms = 'kingdee:config:list'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:config:edit');

-- 4. 按钮权限：客户同步
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '客户同步', m.menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'kingdee:sync:customer', '#',
       'admin', NOW(), '手动触发客户全量同步'
FROM sys_menu m
WHERE m.perms = 'kingdee:config:list'
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:customer');

-- 5. 授权给管理员角色 (role_id = 1)
--    先删除可能存在的旧授权，再重新插入
DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu
    WHERE perms IN (
      'kingdee:config:list', 'kingdee:config:edit', 'kingdee:sync:customer'
    ) OR (menu_id = 9063109)
  ) tmp
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN ('kingdee:config:list', 'kingdee:config:edit', 'kingdee:sync:customer')
   OR (menu_id = 9063109);
