-- 查看盘点相关的全部菜单
SELECT menu_id, menu_name, parent_id, order_num, menu_type, perms, status
FROM sys_menu
WHERE perms LIKE 'wms:stocktake%' OR menu_name LIKE '%盘点%'
ORDER BY menu_id;
