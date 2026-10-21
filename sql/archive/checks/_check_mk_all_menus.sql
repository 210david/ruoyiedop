-- MK模块全部菜单和按钮权限
SELECT menu_id, menu_name, parent_id, order_num, menu_type, perms, status, visible
FROM sys_menu
WHERE menu_id BETWEEN 4000 AND 4199
ORDER BY parent_id, order_num, menu_id;
