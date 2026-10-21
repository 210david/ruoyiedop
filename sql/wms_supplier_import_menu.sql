-- =============================================
-- 供应商信息管理 - 新增导入按钮权限
-- 适配多套菜单结构（WMS原生 + PMS采购入口 + 新菜单结构）
-- =============================================

-- 方式1：针对 wms_all.sql 中的固定ID（2020供应商菜单）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商导入', 2020, 6, '', 'F', '0', '0', 'wms:supplier:import', 'admin', sysdate()
FROM (SELECT 1) AS t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='wms:supplier:import' AND parent_id=2020);

-- 方式2：针对动态ID结构（按 perms 定位父菜单）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '供应商导入', m.menu_id, 6, '', 'F', '0', '0', 'wms:supplier:import', 'admin', sysdate()
FROM sys_menu m
WHERE m.perms = 'wms:supplier:list'
  AND m.menu_type = 'C'
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu sm WHERE sm.perms='wms:supplier:import' AND sm.parent_id=m.menu_id
  );

-- 给管理员角色授权（role_id=1）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, sm.menu_id FROM sys_menu sm
WHERE sm.perms='wms:supplier:import'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu srm WHERE srm.role_id=1 AND srm.menu_id=sm.menu_id
  );
