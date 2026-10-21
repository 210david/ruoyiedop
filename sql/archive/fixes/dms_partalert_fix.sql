-- 直接添加 dms:partalert:remove 权限
-- parent_id=3030 为备件管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('预警删除', 3030, 99, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:partalert:remove', '#', 'admin', sysdate(), '备件库存预警删除权限');

-- 分配给admin角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms = 'dms:partalert:remove';
