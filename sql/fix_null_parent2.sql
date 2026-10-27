USE `ry-vue`;

-- 先找到双控机制的菜单ID
SELECT menu_id INTO @dualControlId FROM sys_menu WHERE menu_name = '双控机制' AND menu_type = 'M' LIMIT 1;

-- 如果找不到，尝试其他条件
SELECT @dualControlId;

-- 更新检查标准库的 parent_id
UPDATE sys_menu SET parent_id = @dualControlId WHERE menu_name = '检查标准库';

-- 验证
SELECT menu_id, menu_name, parent_id, perms FROM sys_menu WHERE menu_name = '检查标准库';
