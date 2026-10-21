-- WMS 删除权限菜单补充
-- 为库存查询、库存流水、库存预警添加删除权限
-- 作者: admin

-- 库存查询 - 删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2123, '库存删除', 2080, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:inventory:remove', '#', 'admin', sysdate(), '');

-- 库存流水 - 删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2124, '流水删除', 2081, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:log:remove', '#', 'admin', sysdate(), '');

-- 库存预警 - 删除权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2125, '预警删除', 2120, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stockAlert:remove', '#', 'admin', sysdate(), '');

-- 为超级管理员角色（role_id=1）分配权限（保险起见，超级管理员本身已有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2123);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2124);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2125);
