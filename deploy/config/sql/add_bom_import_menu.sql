-- BOM导入按钮权限
-- 在BOM管理菜单下添加导入按钮

-- 检查是否已存在，避免重复插入
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 5016, 'BOM导入', 5010, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mms:bom:import', '#', 'admin', sysdate(), ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 5016);

-- 给超级管理员角色(role_id=1)授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 5016
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 5016);
