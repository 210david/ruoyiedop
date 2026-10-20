-- ============================================================
-- PDM 产品设计模块 第四批：统计分析 + 编码规则 页面菜单
-- 菜单号段：11440~11447（11000 产品设计目录下）
-- ============================================================

-- ----------------------------
-- 1. 菜单
-- ----------------------------
DELETE FROM sys_menu WHERE menu_id IN (11440, 11441, 11442, 11443, 11444, 11445, 11446, 11447);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11440, '统计分析', 11000, 99, 'statistics', 'pdm/statistics/index', '', 'PdmStatistics', 1, 0, 'C', '0', '0', 'pdm:statistics:view', 'chart', 'admin', sysdate(), '产品设计统计分析菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11441, '编码规则', 11000, 100, 'numberRule', 'pdm/numberRule/index', '', 'PdmNumberRule', 1, 0, 'C', '0', '0', 'pdm:numberRule:list', 'number', 'admin', sysdate(), '产品设计编码规则菜单');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11442, '规则查询', 11441, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:numberRule:query', '#', 'admin', sysdate(), ''),
(11443, '规则新增', 11441, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:numberRule:add', '#', 'admin', sysdate(), ''),
(11444, '规则修改', 11441, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:numberRule:edit', '#', 'admin', sysdate(), ''),
(11445, '规则删除', 11441, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:numberRule:remove', '#', 'admin', sysdate(), ''),
(11446, '规则导出', 11441, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:numberRule:export', '#', 'admin', sysdate(), '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11447, '统计导出', 11440, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:statistics:export', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 2. 授权给已拥有产品设计目录的角色
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, m.menu_id
FROM sys_role_menu rm
CROSS JOIN (SELECT 11440 menu_id UNION SELECT 11441 UNION SELECT 11442 UNION SELECT 11443 UNION SELECT 11444 UNION SELECT 11445 UNION SELECT 11446 UNION SELECT 11447) m
WHERE rm.menu_id = 11000
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = m.menu_id);
