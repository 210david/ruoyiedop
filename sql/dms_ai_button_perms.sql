-- =============================================
-- 设备AI分析按钮权限（AI设备健康总览 / 备件消耗预测）
-- 按钮级权限控制：无权限角色不显示按钮，后端接口同步校验，防止随意点击消耗大模型token
-- 说明：后端 DmsPredictController @PreAuthorize 已改为 dms:ai:healthOverview / dms:ai:sparepartForecast
--      备件预测原借用 wms:inventory:list，本次收敛为专用权限点
-- =============================================

-- 一、AI设备健康总览按钮（父菜单：设备台账，按 perms 动态定位）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT 'AI设备健康总览', m.menu_id, 100, '', 'F', '0', '0', 'dms:ai:healthOverview', 'admin', sysdate()
FROM sys_menu m
WHERE m.perms = 'dms:equipment:list'
  AND m.menu_type = 'C'
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu sm WHERE sm.perms='dms:ai:healthOverview' AND sm.parent_id=m.menu_id
  );

-- 二、备件消耗预测按钮（父菜单：设备台账）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '备件消耗预测', m.menu_id, 101, '', 'F', '0', '0', 'dms:ai:sparepartForecast', 'admin', sysdate()
FROM sys_menu m
WHERE m.perms = 'dms:equipment:list'
  AND m.menu_type = 'C'
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu sm WHERE sm.perms='dms:ai:sparepartForecast' AND sm.parent_id=m.menu_id
  );

-- 三、给已有"设备台账"菜单权限的角色同步授权（保持权限继承关系）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT srm.role_id, sm.menu_id
FROM sys_menu sm
JOIN sys_role_menu srm ON srm.menu_id = sm.parent_id
WHERE sm.perms IN ('dms:ai:healthOverview', 'dms:ai:sparepartForecast')
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu x WHERE x.role_id = srm.role_id AND x.menu_id = sm.menu_id
  );

-- 四、验证
SELECT menu_id, menu_name, parent_id, perms FROM sys_menu
WHERE perms IN ('dms:ai:healthOverview', 'dms:ai:sparepartForecast');
