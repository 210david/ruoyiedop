-- ============================================================
-- 危化品管理精简：移除冗余模块菜单
-- 1. 供应商管理（复用采购管理模块的WMS供应商）
-- 2. 重大危险源（复用风险管控-风险点管理）
-- 3. 合规检查（复用风险管控-排查任务）
-- ============================================================

-- 删除供应商管理菜单及按钮权限
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:supplier:%'
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:supplier:%';

-- 删除重大危险源菜单及按钮权限
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:majorHazard:%'
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:majorHazard:%';

-- 删除合规检查菜单及按钮权限
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE perms LIKE 'safety:complianceCheck:%'
);
DELETE FROM sys_menu WHERE perms LIKE 'safety:complianceCheck:%';

-- 删除冗余的编号规则
DELETE FROM mk_number_rule WHERE rule_code IN ('safety_material_supplier', 'safety_major_hazard', 'safety_compliance_check');

-- 删除冗余的字典数据
DELETE FROM sys_dict_data WHERE dict_type IN ('safety_supplier_type', 'safety_hazard_level', 'safety_compliance_type', 'safety_check_result', 'safety_rectify_status');
DELETE FROM sys_dict_type WHERE dict_type IN ('safety_supplier_type', 'safety_hazard_level', 'safety_compliance_type', 'safety_check_result', 'safety_rectify_status');

-- 验证
SELECT '清理完成' AS result;
SELECT menu_name, perms FROM sys_menu WHERE perms LIKE 'safety:material%' OR perms LIKE 'safety:materialRecord%' ORDER BY perms;
