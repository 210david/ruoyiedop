-- =============================================
-- 安全生产管理 - 人员台账 菜单更名为 特种人员
-- 执行时间: 2026-08-09
-- =============================================

-- 1. 更新菜单名称：人员台账 → 特种人员
UPDATE sys_menu SET menu_name = '特种人员', remark = '特种人员菜单'
WHERE menu_name = '人员台账' AND perms = 'safety:worker:list';

-- 2. 更新按钮权限名称：人员查询/新增/修改/删除 → 特种人员查询/新增/修改/删除
UPDATE sys_menu SET menu_name = '特种人员查询' WHERE menu_name = '人员查询' AND perms = 'safety:worker:query';
UPDATE sys_menu SET menu_name = '特种人员新增' WHERE menu_name = '人员新增' AND perms = 'safety:worker:add';
UPDATE sys_menu SET menu_name = '特种人员修改' WHERE menu_name = '人员修改' AND perms = 'safety:worker:edit';
UPDATE sys_menu SET menu_name = '特种人员删除' WHERE menu_name = '人员删除' AND perms = 'safety:worker:remove';

-- 3. 删除多余的特种人员工号自动编码规则（工号为手动输入，无需自动生成）
DELETE FROM mk_number_rule WHERE rule_code = 'safety_worker';
