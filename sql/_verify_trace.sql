SHOW TABLES LIKE 'qms_lot_genealogy';
SHOW TABLES LIKE 'qms_trace_sn';
SELECT COUNT(*) AS dict_count FROM sys_dict_type WHERE dict_type IN ('qms_genealogy_source','qms_trace_direction');
SELECT menu_id, menu_name, perms FROM sys_menu WHERE perms LIKE 'qms:trace%%' OR perms LIKE 'qms:genealogy%%' ORDER BY menu_id;
