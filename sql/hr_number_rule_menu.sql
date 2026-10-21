-- 人力资源模块：编码规则菜单（挂载在 人力资源管理 下，component 指向 hr/numberRule/index）
-- 幂等：按 perms 已存在则跳过
SET @hr_root := (SELECT menu_id FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编码规则', @hr_root, 99, 'numberRule', 'hr/numberRule/index', '', '', 1, 0, 'C', '0', '0', 'hr:numberRule:list', 'number', 'admin', NOW(), '人力资源编码规则管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:numberRule:list' AND menu_type = 'C');

SET @rule_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'hr:numberRule:list' AND menu_type = 'C' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.menu_name, @rule_menu, t.order_num, '#', '', '', '', 1, 0, 'F', '0', '0', t.perms, '#', 'admin', NOW(), '人力资源编码规则按钮'
FROM (
  SELECT '编码规则查询' menu_name, 1 order_num, 'hr:numberRule:query' perms
  UNION ALL SELECT '编码规则新增', 2, 'hr:numberRule:add'
  UNION ALL SELECT '编码规则修改', 3, 'hr:numberRule:edit'
  UNION ALL SELECT '编码规则删除', 4, 'hr:numberRule:remove'
  UNION ALL SELECT '编码规则导出', 5, 'hr:numberRule:export'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = t.perms AND m.menu_type = 'F');

-- 授权：所有已拥有 人力资源管理 根菜单的非超管角色，同步授予编码规则菜单及按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, m.menu_id
FROM sys_role_menu rm
JOIN sys_menu m ON m.parent_id = @rule_menu OR m.menu_id = @rule_menu
WHERE rm.menu_id = @hr_root
  AND rm.role_id <> 1
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = m.menu_id);
