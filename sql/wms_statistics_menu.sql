-- =============================================
-- WMS 统计报表 - 菜单权限SQL
-- 在 WMS仓储管理 目录下新增"统计报表"菜单
-- =============================================

-- 统计报表菜单（C类型，直接挂在WMS仓储管理目录下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2110, '统计报表', 2000, 7, 'statistics', 'wms/statistics/index', '', 'WmsStatistics', 1, 0, 'C', '0', '0', 'wms:statistics:list', 'chart', 'admin', sysdate(), '仓库管理统计报表');

-- 统计报表按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2111, '统计查询', 2110, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:statistics:query', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配统计报表菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2110), (1, 2111);
