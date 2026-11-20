-- =============================================================
-- 升级脚本：项目管理 - 编号规则管理菜单（复用 mk 通用页面）
-- 日期：2026-11-13
-- 说明：pjm 模块此前缺少编号规则管理页（其他模块 fin/hr/mk/mms/pdm 均有），
--       导致 PJM 编号规则只能靠 SQL 初始化，生产库漏执行即引发
--       "新增项目保存提示系统繁忙"（generateNumber 抛 ServiceException）。
--       本脚本在项目管理（906202）下挂"编号规则"菜单，复用
--       views/mk/numberRule/index.vue 通用页面（query: module=pjm），
--       权限标识 pjm:numberRule:*（后端 MkNumberRuleController 已放行）。
-- 幂等：可重复执行。
-- =============================================================

-- 1. 菜单（C 类型，挂项目管理下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 906202, 99, 'numberRule', 'mk/numberRule/index', '{"module":"pjm"}', 'PjmNumberRule', 1, 0, 'C', '0', '0', 'pjm:numberRule:list', 'number', 'admin', NOW(), '项目管理编号规则管理（复用 mk 通用页面，module=pjm）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:numberRule:list' AND menu_type = 'C');

SET @pjm_rule_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'pjm:numberRule:list' AND menu_type = 'C' LIMIT 1);

-- 2. 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.menu_name, @pjm_rule_menu, t.ord, '', '', '', NULL, 1, 0, 'F', '0', '0', t.perms, '#', 'admin', NOW(), ''
FROM (
  SELECT '编号规则查询' AS menu_name, 1 AS ord, 'pjm:numberRule:query' AS perms UNION ALL
  SELECT '编号规则新增', 2, 'pjm:numberRule:add' UNION ALL
  SELECT '编号规则修改', 3, 'pjm:numberRule:edit' UNION ALL
  SELECT '编号规则删除', 4, 'pjm:numberRule:remove' UNION ALL
  SELECT '编号规则导出', 5, 'pjm:numberRule:export'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = t.perms AND m.menu_type = 'F');

-- 3. 授权给管理员角色（role_id=1 默认超管免权，此处授给普通管理角色可按需调整）
-- INSERT INTO sys_role_menu (role_id, menu_id)
-- SELECT r.role_id, m.menu_id FROM sys_role r, sys_menu m
-- WHERE m.perms LIKE 'pjm:numberRule%' AND NOT EXISTS
-- (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);
