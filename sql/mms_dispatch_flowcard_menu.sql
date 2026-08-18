-- ============================================================
-- MMS 派工管理 & 流转卡 - 菜单权限补充
-- 说明: 为派工管理和流转卡页面添加菜单和按钮权限
-- 执行时间: 2026-08-16
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、派工管理菜单（菜单类型 C）
-- ============================================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工管理', 5000, 17, 'dispatch', 'mms/dispatch/index', '', '', 1, 0, 'C', '0', '0', 'mms:dispatch:list', 'people', 'admin', sysdate(), '派工管理菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:list');

-- 派工管理按钮权限（菜单类型 F）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工查询', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工新增', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:add', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工修改', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:edit', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工删除', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:remove', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工导出', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工开工', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:start', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:start');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工完工', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 7, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:finish', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:finish');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '派工取消', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:dispatch:list' LIMIT 1) t), 8, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:dispatch:cancel', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:dispatch:cancel');

-- ============================================================
-- 二、流转卡菜单（菜单类型 C）
-- ============================================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡', 5000, 18, 'flowcard', 'mms/flowcard/index', '', '', 1, 0, 'C', '0', '0', 'mms:flowcard:list', 'documentation', 'admin', sysdate(), '流转卡菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:list');

-- 流转卡按钮权限（菜单类型 F）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡查询', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡新增', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:add', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡修改', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:edit', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡删除', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:remove', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡导出', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:export', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:export');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡打印', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:print', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:print');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '流转卡扫码', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE perms = 'mms:flowcard:list' LIMIT 1) t), 7, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:flowcard:scan', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mms:flowcard:scan');

-- ============================================================
-- 三、角色菜单分配（admin 角色 = 1）
-- ============================================================

-- 派工管理菜单及按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN (
  'mms:dispatch:list', 'mms:dispatch:query', 'mms:dispatch:add',
  'mms:dispatch:edit', 'mms:dispatch:remove', 'mms:dispatch:export',
  'mms:dispatch:start', 'mms:dispatch:finish', 'mms:dispatch:cancel'
) ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);

-- 流转卡菜单及按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN (
  'mms:flowcard:list', 'mms:flowcard:query', 'mms:flowcard:add',
  'mms:flowcard:edit', 'mms:flowcard:remove', 'mms:flowcard:export',
  'mms:flowcard:print', 'mms:flowcard:scan'
) ON DUPLICATE KEY UPDATE menu_id = VALUES(menu_id);
