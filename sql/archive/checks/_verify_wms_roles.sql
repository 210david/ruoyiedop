SELECT r.role_id, r.role_name, r.role_key, r.data_scope, r.status, COUNT(rm.menu_id) AS wms_menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id AND m.menu_id BETWEEN 2000 AND 2999
WHERE r.role_id IN (400,401,402,403,404)
GROUP BY r.role_id
ORDER BY r.role_id;
