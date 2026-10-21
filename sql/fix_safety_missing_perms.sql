-- =============================================================
-- 修复安全生产模块缺失的菜单权限（回归测试发现）
-- 1. safety:hazard:approve     隐患审批（后端在用但菜单未注册，导致除 admin 外无人可审批）
-- 2. safety:remind:export      到期提醒导出（后端已补导出接口）
-- 3. safety:training:attendee:import 参训人员导入模板
-- 并将以上权限授予安全主管（role_id=111）
-- 执行前请确认菜单不存在，避免重复插入
-- =============================================================

USE `ry-vue`;

-- 0. 清理可能存在的重复权限（按 perms 幂等）
DELETE m FROM sys_menu m
LEFT JOIN sys_menu p ON p.menu_id = m.parent_id
WHERE m.perms IN ('safety:hazard:approve', 'safety:remind:export', 'safety:training:attendee:import');

-- 1. 隐患审批按钮（挂在隐患管理菜单下）
SET @hazardMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:hazard:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('隐患审批', @hazardMenuId, 8, '', NULL, 1, 0, 'F', '0', '0', 'safety:hazard:approve', '#', 'admin', NOW(), '隐患审批按钮（修复缺失）');

-- 2. 到期提醒导出按钮（挂在到期提醒菜单下）
SET @remindMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:remind:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('到期提醒导出', @remindMenuId, 3, '', NULL, 1, 0, 'F', '0', '0', 'safety:remind:export', '#', 'admin', NOW(), '到期提醒导出按钮（修复缺失）');

-- 3. 参训人员导入按钮（挂在参训人员菜单下）
SET @attendeeMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:training:attendee:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('参训人员导入', @attendeeMenuId, 6, '', NULL, 1, 0, 'F', '0', '0', 'safety:training:attendee:import', '#', 'admin', NOW(), '参训人员导入模板按钮（修复缺失）');

-- 4. 授权安全主管（safety_manager, role_id=111）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 111, menu_id FROM sys_menu
WHERE perms IN ('safety:hazard:approve', 'safety:remind:export', 'safety:training:attendee:import');

-- 5. 验证
SELECT m.menu_id, m.menu_name, m.perms, m.parent_id,
       (SELECT COUNT(1) FROM sys_role_menu rm WHERE rm.menu_id = m.menu_id AND rm.role_id = 111) AS granted_to_manager
FROM sys_menu m
WHERE m.perms IN ('safety:hazard:approve', 'safety:remind:export', 'safety:training:attendee:import');
