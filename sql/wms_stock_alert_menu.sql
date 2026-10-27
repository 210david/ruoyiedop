-- =============================================
-- 库存预警菜单及权限
-- 菜单ID：2120（页面）、2121（查询权限）、2122（导出权限）
-- 父菜单：2004（库存管理）
-- =============================================

-- 删除已存在的记录（防止重复执行）
DELETE FROM sys_role_menu WHERE menu_id IN (2120, 2121, 2122);
DELETE FROM sys_menu WHERE menu_id IN (2120, 2121, 2122);

-- 库存预警菜单（页面）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2120, '库存预警', 2004, 3, 'alert', 'wms/stockAlert/index', '', 'WmsStockAlert', 1, 0, 'C', '0', '0', 'wms:stockAlert:list', 'bug', 'admin', sysdate(), '库存预警管理（库存不足/积压/临期/过期）');

-- 库存预警功能权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2121, '预警查询', 2120, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:query', '#', 'admin', sysdate(), ''),
(2122, '预警导出', 2120, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:export', '#', 'admin', sysdate(), '');

-- 给admin角色（role_id=1）分配权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2120), (1, 2121), (1, 2122);
