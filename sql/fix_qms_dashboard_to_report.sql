-- =============================================
-- QMS 质量看板移到报表中心下面
-- 将报表中心从页面(C)改为目录(M)，质量看板移为其子菜单
-- =============================================

-- 1. 将"报表中心"(4813)从C改为M(目录)，清空component
UPDATE sys_menu SET menu_type = 'M', component = NULL WHERE menu_id = 4813;

-- 2. 在报表中心下新建"统计报表"页面，承载原报表功能（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '统计报表', 4813, 1, 'list', 'qms/report/index', '', 'QmsReportList', 1, 0, 'C', '0', '0', 'qms:report:list', 'documentation', 'admin', sysdate(), '质量统计报表'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '统计报表' AND parent_id = 4813);

-- 3. 将"质量看板"(4544)移到报表中心(4813)下，order_num=2
UPDATE sys_menu SET parent_id = 4813, order_num = 2 WHERE menu_id = 4544;

-- 4. 给admin角色和普通角色分配新菜单权限（幂等）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_name = '统计报表' AND parent_id = 4813
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE menu_name = '统计报表' AND parent_id = 4813
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- 5. 验证结果
SELECT menu_id, menu_name, parent_id, order_num, path, component, route_name, menu_type, perms
FROM sys_menu WHERE menu_id = 4813 OR parent_id = 4813 ORDER BY order_num;
