USE `ry-vue`;
SELECT menu_id, menu_name, parent_id, order_num, path, component, menu_type
FROM sys_menu 
WHERE parent_id = 5000 OR menu_id = 5000
ORDER BY parent_id, order_num;
