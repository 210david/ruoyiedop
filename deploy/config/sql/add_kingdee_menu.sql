-- ============================================================
-- 金蝶云星辰对接 菜单 + 权限 + 管理员授权
-- 幂等：重复执行不会产生重复菜单
-- ============================================================

-- 1. 一级菜单：金蝶对接（顶部导航）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶对接', 0, 20, 'kingdee', NULL, '', '', 1, 0, 'M', '0', '0', '', 'server',
       'admin', NOW(), '金蝶云星辰对接（基础数据同步）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '金蝶对接' AND parent_id = 0);

-- 2. 二级菜单：金蝶配置（页面）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶配置', m.menu_id, 1, 'config', 'kingdee/index', '', '', 1, 0, 'C', '0', '0', 'kingdee:config:list', 'edit',
       'admin', NOW(), ''
FROM sys_menu m
WHERE m.menu_name = '金蝶对接' AND m.parent_id = 0
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:config:list');

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
    ) OR (menu_name = '金蝶对接' AND parent_id = 0)
  ) tmp
);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN ('kingdee:config:list', 'kingdee:config:edit', 'kingdee:sync:customer')
   OR (menu_name = '金蝶对接' AND parent_id = 0);
