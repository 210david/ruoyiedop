@echo off
chcp 65001 >nul
echo ========================================
echo   QMS SQL 执行情况 - 最终验证报告
echo ========================================
echo.
echo [1] QMS 业务表 (11张)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema='ry-vue' AND table_name LIKE 'qms_%%'"
echo.
echo [2] QMS 字典类型 (13个)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT COUNT(*) AS dict_type_count FROM sys_dict_type WHERE dict_type LIKE 'qms_%%'"
echo.
echo [3] QMS 字典数据
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT COUNT(*) AS dict_data_count FROM sys_dict_data WHERE dict_type LIKE 'qms_%%'"
echo.
echo [4] QMS 菜单 (M=目录 C=菜单 F=按钮)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_type, COUNT(*) AS cnt FROM sys_menu WHERE perms LIKE 'qms%%' OR path='qms' OR parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=0) OR menu_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=0) GROUP BY menu_type ORDER BY menu_type"
echo.
echo [5] 顶级QMS菜单 (应只有1个M类型)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, menu_type FROM sys_menu WHERE parent_id=0 AND (perms LIKE 'qms%%' OR path='qms')"
echo.
echo [6] 定时任务 (应只有2个)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT job_id, job_name, invoke_target, status FROM sys_job WHERE invoke_target LIKE '%%qms%%'"
echo.
echo [7] 乱码检查 (应0条)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT COUNT(*) AS garbled_count FROM sys_dict_type WHERE dict_type LIKE 'qms_%%' AND (dict_name LIKE '%%??%%' OR HEX(dict_name) LIKE '%%3F%%')"
echo.
echo [8] QMS 菜单树 (目录+菜单)
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT CONCAT(REPEAT('  ', CASE WHEN m1.parent_id=0 THEN 0 WHEN m2.parent_id=0 THEN 1 ELSE 2 END), m1.menu_name) AS tree, m1.menu_type FROM sys_menu m1 LEFT JOIN sys_menu m2 ON m1.parent_id = m2.menu_id WHERE (m1.perms LIKE 'qms%%' OR m1.path = 'qms' OR m1.parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=0) OR m1.menu_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=0)) AND m1.menu_type IN ('M','C') ORDER BY m1.parent_id, m1.order_num"
