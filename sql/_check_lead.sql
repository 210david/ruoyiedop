-- 查看线索相关全部菜单
SELECT menu_id, menu_name, parent_id, order_num, menu_type, perms, status
FROM sys_menu
WHERE perms LIKE 'marketing:lead:%' OR menu_name LIKE '%线索%' OR menu_name LIKE '%公海%'
ORDER BY menu_id;
