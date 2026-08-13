-- 修复检查标准库菜单的 parent_id 为 NULL 的问题
USE `ry-vue`;

-- 首先找到正确的双控机制菜单ID
SET @dualControlMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '双控机制' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0));

-- 更新检查标准库的 parent_id
UPDATE sys_menu SET parent_id = @dualControlMenuId WHERE menu_name = '检查标准库' AND parent_id IS NULL;

-- 验证修复结果
SELECT menu_id, menu_name, parent_id, perms FROM sys_menu WHERE menu_name = '检查标准库';
