-- 合同审批权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 4096, '合同审批', 4090, 6, '', '', '', 1, 0, 'F', '0', '0', 'marketing:contract:approve', '#', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 4096);

-- 将审批权限分配给管理员角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 4096
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 4096);
