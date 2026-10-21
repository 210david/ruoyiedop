-- =====================================================
-- DMS 设备大屏菜单 - 增量SQL脚本
-- 将"设备大屏"添加到 设备管理 > 报表看板 菜单组下
-- =====================================================

-- 1. 插入"设备大屏"菜单（放在报表看板 3006 下，紧跟"数据看板"3090 之后）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3091, '设备大屏', 3006, 2, 'screen', 'dms/equipment/screen', '', 'DmsEquipmentScreen', 1, 1, 'C', '0', '0', 'dms:equipment:query', 'monitor', 'admin', sysdate(), '设备全生命周期监控大屏');

-- 2. 给 admin 角色（role_id=1）分配设备大屏菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 3091);

-- 3. 给所有已拥有"报表看板(3006)"菜单权限的角色，也分配"设备大屏"菜单权限
--    （确保非admin角色也能看到设备大屏）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT role_id, 3091
FROM sys_role_menu
WHERE menu_id = 3006
  AND role_id NOT IN (SELECT role_id FROM sys_role_menu WHERE menu_id = 3091);
