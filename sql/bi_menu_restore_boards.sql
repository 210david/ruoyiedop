-- =============================================
-- 决策支持菜单重组增量：恢复原模块看板入口（两边都保留——看板类补充）
-- 背景：bi_menu_restructure.sql 将各模块看板挪入决策支持>看板；bi_menu_restore_entries.sql
--       仅恢复了 17 张统计报表。本脚本补齐 11 个看板类页面的原模块入口
--       （同 component/perms/icon，route_name 加 Legacy 前缀防路由冲突），
--       并取消隐藏采购>统计分析目录。
-- 幂等：先查后插/先查后更，可重复执行
-- =============================================

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备统计分析', 3006, 1, 'overview', 'dms/dashboard/index', 'LegacyDmsDashboard',
    1, 0, 'C', '0', '0', 'dms:dashboard:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyDmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备大屏', 3006, 2, 'screen', 'dms/equipment/screen', 'LegacyDmsEquipmentScreen',
    1, 0, 'C', '0', '0', 'dms:equipment:query', 'monitor', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyDmsEquipmentScreen' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '商机看板', 4003, 2, 'kanban', 'mk/opportunity/kanban', 'LegacyMkOpportunityKanban',
    1, 0, 'C', '0', '0', 'marketing:opportunity:list', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkOpportunityKanban' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '营销统计分析', 4006, 1, 'dashboard', 'mk/dashboard/index', 'LegacyMkDashboard',
    1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '推广统计分析', 4006, 2, 'promotion', 'mk/promotionReport/index', 'LegacyMkPromotionReport',
    1, 0, 'C', '0', '0', 'marketing:promotionReport:view', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkPromotionReport' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购统计分析', 4460, 1, 'dashboard', 'pms/dashboard/index', 'LegacyPmsDashboard',
    1, 0, 'C', '0', '0', 'pms:dashboard:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyPmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量管理驾驶舱', 4813, 2, 'dashboard', 'qms/dashboard/index', 'LegacyQmsDashboard',
    1, 0, 'C', '0', '0', 'qms:dashboard:view', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyQmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产驾驶舱', 5000, 10, 'dashboard', 'mms/dashboard/index', 'LegacyMmsDashboard',
    1, 0, 'C', '0', '0', 'mms:dashboard:list', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '安全生产驾驶舱', 4638, 8, 'dashboard', 'safety/dashboard/index', 'LegacySafetyDashboard',
    1, 0, 'C', '0', '0', 'safety:dashboard:list', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacySafetyDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '谱系完整度', 4833, 3, 'dashboard', 'qms/trace/dashboard', 'LegacyQmsTraceDashboard',
    1, 0, 'C', '0', '0', 'qms:trace:dashboard', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyQmsTraceDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '投标数据分析', 905886, 8, 'dashboard', 'bid/dashboard/index', 'LegacyBidDashboard',
    1, 0, 'C', '0', '0', 'bid:dashboard:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyBidDashboard' AND menu_type='C');

-- =============================================
-- 取消隐藏采购>统计分析目录（visible='1' -> '0'，幂等）
-- =============================================
UPDATE sys_menu SET visible='0' WHERE menu_id=4460 AND visible='1';

-- =============================================
-- admin 授权（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_type='C' AND m.parent_id IN (3006,4003,4006,4460,4813,4638,4833,5000,905886)
  AND m.route_name LIKE 'Legacy%'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);

-- =============================================
-- 普通角色授权迁移（按 perms 匹配挪动看板：角色原持有该看板权限 => 补授 Legacy 菜单）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, lm.menu_id
FROM sys_role_menu rm
-- 4070/4844 已于 2026-11-08 物理删除，保留 id 仅作历史兼容（JOIN 自然无匹配）
JOIN sys_menu om ON rm.menu_id = om.menu_id
  AND om.menu_id IN (3090,3091,4070,4120,4461,4544,4583,4726,4844,5001,905894)
JOIN sys_menu lm ON lm.route_name = CONCAT('Legacy', om.route_name) AND lm.menu_type='C'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id=rm.role_id AND x.menu_id=lm.menu_id);

-- =============================================
-- 取消隐藏原模块目录（有 Legacy 子菜单的目录保持可见，保证执行顺序无关）
-- =============================================
UPDATE sys_menu p LEFT JOIN sys_menu c ON c.parent_id=p.menu_id AND c.visible='0' AND c.status='0'
SET p.visible='0'
WHERE p.menu_id IN (3006,4006,4460,4813,905998) AND c.menu_id IS NOT NULL;
