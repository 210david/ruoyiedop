-- =============================================
-- HR 人力资源管理模块 - 统计报表菜单
-- 挂载到「人力资源管理 > 综合管理」目录下（parent_id 取综合管理菜单ID）
-- =============================================
USE `ry-vue`;
SET NAMES utf8mb4;

-- 定位「人力资源管理」根目录（与综合管理等业务组平级）
SELECT menu_id INTO @parentId FROM sys_menu WHERE menu_id = 905538 LIMIT 1;

-- 统计报表菜单（C，与综合管理平级）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name)
VALUES ('统计报表', @parentId, 8, 'statistics', 'hr/statistics/index', 1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', sysdate(), 'HR统计报表菜单', 'HrStatistics');
SET @menuId = LAST_INSERT_ID();

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('报表查询', @menuId, 1, '#', NULL, 1, 0, 'F', '0', '0', 'hr:statistics:list', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('报表导出', @menuId, 2, '#', NULL, 1, 0, 'F', '0', '0', 'hr:statistics:export', '#', 'admin', sysdate(), '');

-- 授予管理员角色（角色1）
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, @menuId FROM dual WHERE EXISTS (SELECT 1 FROM sys_role WHERE role_id = 1);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE parent_id = @menuId AND perms IN ('hr:statistics:list', 'hr:statistics:export');
