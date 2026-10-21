SELECT COUNT(*) AS hr_table_count FROM information_schema.tables WHERE table_schema='ry-vue' AND table_name LIKE 'biz_hr_%';
SELECT COUNT(*) AS hr_menu_count FROM sys_menu WHERE perms LIKE 'hr:%';
SELECT COUNT(*) AS hr_dict_count FROM sys_dict_type WHERE dict_type LIKE 'hr_%';
SELECT menu_name, perms FROM sys_menu WHERE perms LIKE 'hr:%list' ORDER BY order_num;
