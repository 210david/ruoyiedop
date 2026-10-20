-- 1. 角色分类字典：新增「产品设计」
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 10, '产品设计', 'pdm', 'sys_role_category', '', 'primary', 'N', '0', 'admin', NOW(), '产品设计模块角色分类'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='sys_role_category' AND dict_value='pdm');

-- 2. 三个产品设计角色
INSERT INTO sys_role (role_name, role_key, role_sort, role_category, module_key, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT t.n, t.k, t.s, 'pdm', 'pdm', t.d, 1, 1, '0', '0', 'admin', NOW(), t.r FROM (
  SELECT '产品设计主管' n, 'pdm_manager' k, 100 s, '1' d, '产品设计模块全部管理权限' r UNION ALL
  SELECT '设计工程师', 'pdm_engineer', 101, '5', '产品设计业务操作权限（数据范围限本人）' UNION ALL
  SELECT '设计资料查看', 'pdm_viewer', 102, '1', '产品设计只读查看权限'
) t WHERE NOT EXISTS (SELECT 1 FROM sys_role sr WHERE sr.role_key = t.k);

-- 3. 主管/工程师：全套 PDM 菜单授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r, sys_menu m
WHERE r.role_key IN ('pdm_manager','pdm_engineer') AND r.del_flag='0'
  AND (m.menu_id = 11000 OR m.perms LIKE 'pdm:%' OR (m.menu_type='C' AND m.component LIKE 'pdm/%'))
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

-- 4. 查看角色：目录 + 页面 + 查询类按钮（不含增删改/导出/流程动作）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r, sys_menu m
WHERE r.role_key = 'pdm_viewer' AND r.del_flag='0'
  AND (m.menu_id = 11000
    OR (m.menu_type='C' AND m.component LIKE 'pdm/%')
    OR m.perms LIKE 'pdm:%:query'
    OR m.perms = 'pdm:statistics:view'
    OR m.perms = 'pdm:scheme:formula')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id=r.role_id AND rm.menu_id=m.menu_id);

-- 复核
SELECT r.role_key, r.role_name, r.role_category, r.data_scope, COUNT(rm.menu_id) granted
FROM sys_role r LEFT JOIN sys_role_menu rm ON r.role_id=rm.role_id
WHERE r.role_key LIKE 'pdm_%' GROUP BY r.role_key, r.role_name, r.role_category, r.data_scope;
SELECT dict_label, dict_value, dict_sort FROM sys_dict_data WHERE dict_type='sys_role_category' ORDER BY dict_sort;
