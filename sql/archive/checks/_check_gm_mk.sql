-- 查看总经理(104)已有的MK模块权限
SELECT rm.menu_id, m.menu_name, m.perms, m.menu_type
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 104 AND m.menu_id BETWEEN 4000 AND 4199
ORDER BY m.menu_id;
