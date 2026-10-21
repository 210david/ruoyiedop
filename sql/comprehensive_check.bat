@echo off
chcp 65001 >nul
echo ===== 1. QMS TABLES =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT table_name, table_comment FROM information_schema.tables WHERE table_schema='ry-vue' AND table_name LIKE 'qms_%%' ORDER BY table_name"
echo.
echo ===== 2. QMS DICT TYPES =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_name FROM sys_dict_type WHERE dict_type LIKE 'qms_%%' ORDER BY dict_type"
echo.
echo ===== 3. QMS MENU TREE (M+C only) =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT m1.menu_id, CONCAT(REPEAT('  ', CASE WHEN m1.parent_id=0 THEN 0 WHEN m2.parent_id=0 THEN 1 ELSE 2 END), m1.menu_name) AS tree, m1.menu_type, m1.path, m1.component, m1.route_name, m1.perms FROM sys_menu m1 LEFT JOIN sys_menu m2 ON m1.parent_id = m2.menu_id WHERE (m1.perms LIKE 'qms%%' OR m1.path = 'qms' OR m1.parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=0) OR m1.menu_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=0)) AND m1.menu_type IN ('M','C') ORDER BY m1.parent_id, m1.order_num"
echo.
echo ===== 4. QMS SCHEDULED JOBS =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT job_id, job_name, invoke_target, cron_expression, status FROM sys_job WHERE invoke_target LIKE '%%qms%%'"
echo.
echo ===== 5. TABLE ROW COUNTS =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT 'qms_inspect_std' AS tbl, COUNT(*) AS cnt FROM qms_inspect_std UNION SELECT 'qms_defect_code', COUNT(*) FROM qms_defect_code UNION SELECT 'qms_gauge', COUNT(*) FROM qms_gauge UNION SELECT 'qms_material_attr', COUNT(*) FROM qms_material_attr UNION SELECT 'qms_insp_task', COUNT(*) FROM qms_insp_task UNION SELECT 'qms_insp_item', COUNT(*) FROM qms_insp_item UNION SELECT 'qms_ncr', COUNT(*) FROM qms_ncr UNION SELECT 'qms_ncr_approve_log', COUNT(*) FROM qms_ncr_approve_log UNION SELECT 'qms_workshop', COUNT(*) FROM qms_workshop UNION SELECT 'qms_esig_record', COUNT(*) FROM qms_esig_record"
