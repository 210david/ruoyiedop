-- DMS 备件模块删除权限菜单补充
-- 为备件入库、备件出库、库存流水、备件预警、备件台账添加删除权限
-- 使用子查询自动查找父菜单ID，避免硬编码

-- 备件出入库记录 - 删除权限（用于入库、出库、流水三个模块共用）
-- 权限 dms:partstock:remove
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '记录删除', parent_id, 99, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:partstock:remove', '#', 'admin', sysdate(), '备件出入库记录删除权限（入库/出库/流水共用）'
FROM sys_menu WHERE perms = 'dms:partstock:export' LIMIT 1;

-- 备件预警 - 删除权限
-- 权限 dms:partalert:remove
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '预警删除', parent_id, 99, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:partalert:remove', '#', 'admin', sysdate(), '备件库存预警删除权限'
FROM sys_menu WHERE perms = 'dms:partalert:export' LIMIT 1;

-- 为超级管理员角色（role_id=1）分配新增权限
-- 超级管理员本身已有 *:*:* 权限，这里仅为保险起见
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN ('dms:partstock:remove', 'dms:partalert:remove')
ON DUPLICATE KEY UPDATE menu_id = menu_id;
