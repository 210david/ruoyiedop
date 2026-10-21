-- ============================================================
-- 需求管理：新增"需求确认"和"需求转计划"按钮权限
-- 在 sys_menu 中为需求管理(9021)新增两个功能按钮
-- ============================================================

-- 先删除已存在的记录（防止重复执行）
DELETE FROM sys_role_menu WHERE menu_id IN (902106, 902107);
DELETE FROM sys_menu WHERE menu_id IN (902106, 902107);

-- 需求确认按钮（含取消确认）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(902106, '需求确认', 9021, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:confirm', '#', 'admin', sysdate(), '');

-- 需求转计划按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(902107, '需求转计划', 9021, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:demand:toplan', '#', 'admin', sysdate(), '');

-- 授权给管理员角色（role_id=1）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 902106), (1, 902107);
