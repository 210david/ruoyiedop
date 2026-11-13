-- =============================================================
-- 集成模块菜单脚本（增量）：系统集成目录 + 钉钉考勤集成菜单 + 按钮权限
-- 版本: v1.0  日期: 2026-11-07
-- 幂等：NOT EXISTS 防重，可重复执行
-- =============================================================

-- 1. 顶级目录：系统集成
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063000, '系统集成', 0, 90, 'integration', NULL, '', 1, 0, 'M', '0', '0', '', 'link', 'admin', sysdate(), '第三方集成管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063000);

-- 2. 菜单：钉钉考勤集成
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063010, '钉钉集成', 9063000, 1, 'attendance', 'integration/attendance/index', '', 1, 0, 'C', '0', '0', 'integration:attendance:list', 'date', 'admin', sysdate(), '钉钉第三方集成（考勤同步、后续消息等）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063010);

-- 3. 按钮：集成同步（同步通讯录/打卡同步/人工绑定）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063011, '集成同步', 9063010, 1, '#', '', '', 1, 0, 'F', '0', '0', 'integration:attendance:sync', '#', 'admin', sysdate(), '同步通讯录、打卡同步与人工绑定'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063011);

-- 4. 按钮：绑定查询
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063012, '绑定查询', 9063010, 2, '#', '', '', 1, 0, 'F', '0', '0', 'integration:attendance:query', '#', 'admin', sysdate(), '绑定关系明细查询'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063012);

-- 5. 按钮：集成配置（读写钉钉集成参数，AppSecret 只写不回显）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063020, '集成配置', 9063010, 3, '#', '', '', 1, 0, 'F', '0', '0', 'integration:attendance:config', '#', 'admin', sysdate(), '钉钉集成参数配置（AppKey/AppSecret/开关/匹配策略）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063020);

-- 6. 授权角色 1
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_id IN (9063000, 9063010, 9063011, 9063012, 9063020)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id = 1 AND r.menu_id = m.menu_id);
