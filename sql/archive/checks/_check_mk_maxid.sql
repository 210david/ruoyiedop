-- 查看当前最大的MK菜单ID
SELECT menu_id, menu_name, parent_id, perms FROM sys_menu 
WHERE menu_id BETWEEN 4150 AND 4200 
ORDER BY menu_id;
