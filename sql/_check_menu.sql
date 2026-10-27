SELECT '=== role_menu for team ===' AS info;
SELECT rm.role_id, m.menu_id, m.menu_name, m.perms, m.menu_type
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE m.perms LIKE 'mms:team%' AND rm.role_id = 1;
