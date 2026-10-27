-- 查看WMS模块全部菜单
SELECT menu_id, menu_name, parent_id, order_num, menu_type, perms, status
FROM sys_menu
WHERE menu_id BETWEEN 2000 AND 2999
ORDER BY parent_id, order_num, menu_id;
