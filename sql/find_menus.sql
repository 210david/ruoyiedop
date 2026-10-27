USE `ry-vue`;
SELECT menu_id, menu_name, parent_id, menu_type, perms FROM sys_menu WHERE menu_name LIKE '%双控%' OR menu_name LIKE '%安全生产%';
