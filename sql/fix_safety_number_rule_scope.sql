-- =============================================================
-- 安全生产模块遗留问题修复（第二轮）
-- 1. 安全编码规则菜单复用营销模块页面（按钮校验 marketing:numberRule:*），
--    直接为安全主管授予实际生效的营销编号规则权限，使页面按钮可用
-- 2. 安全主管 data_scope 由 2(自定义,未配置部门导致可见空集) 调整为 1(全部数据)，
--    配合隐患/排查任务列表新接入的 @DataScope 数据范围过滤
-- =============================================================

USE `ry-vue`;

-- 1. 授权安全主管（role_id=111）营销编号规则权限（页面按钮实际校验的 perms）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 111, menu_id FROM sys_menu WHERE perms LIKE 'marketing:numberRule:%';

-- 2. 安全主管数据范围：全部数据
UPDATE sys_role SET data_scope = '1' WHERE role_id = 111 AND data_scope = '2';

-- 3. 验证
SELECT '=== marketing numberRule perms granted to 111 ===' AS info;
SELECT m.menu_id, m.menu_name, m.perms FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 111 AND m.perms LIKE 'marketing:numberRule%';

SELECT '=== role data_scope ===' AS info;
SELECT role_id, role_name, data_scope FROM sys_role WHERE role_id IN (110, 111);
