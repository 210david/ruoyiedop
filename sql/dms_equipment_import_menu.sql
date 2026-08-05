-- =============================================
-- 设备台账管理 - 新增导入按钮权限
-- 适配多套菜单结构（固定ID + 动态ID结构）
-- =============================================

-- 方式1：针对 dms_all.sql 中的固定ID（3020设备台账菜单）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 3026, '设备导入', 3020, 6, '', 'F', '0', '0', 'dms:equipment:import', 'admin', sysdate()
FROM (SELECT 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='dms:equipment:import' AND parent_id=3020);

-- 方式2：针对动态ID结构（按 perms 定位父菜单）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '设备导入', m.menu_id, 6, '', 'F', '0', '0', 'dms:equipment:import', 'admin', sysdate()
FROM sys_menu m
WHERE m.perms = 'dms:equipment:list'
  AND m.menu_type = 'C'
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu sm WHERE sm.perms='dms:equipment:import' AND sm.parent_id=m.menu_id
  );

-- 给管理员角色授权（role_id=1）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, sm.menu_id FROM sys_menu sm
WHERE sm.perms='dms:equipment:import'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu srm WHERE srm.role_id=1 AND srm.menu_id=sm.menu_id
  );
