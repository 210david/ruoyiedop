-- 入职管理：审批按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '入职审批', 905556, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'hr:entry:audit', '#', 'admin', NOW(), '', NULL, '入职审批按钮'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:entry:audit');
