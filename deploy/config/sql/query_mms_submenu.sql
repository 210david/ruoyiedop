USE `ry-vue`;
SELECT menu_id, menu_name, parent_id, order_num, path, component, menu_type
FROM sys_menu 
WHERE parent_id IN (5230, 5231, 5232, 5233, 5234, 5235)
ORDER BY parent_id, order_num;
