@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue < "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\fix_all_route_names.sql"
echo Done
echo ===== VERIFY ALL QMS ROUTE NAMES =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, path, route_name, menu_type FROM sys_menu WHERE perms LIKE 'qms%%' AND menu_type = 'C' ORDER BY menu_id"
echo ===== CHECK CONFLICTS =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT route_name, COUNT(*) AS cnt, GROUP_CONCAT(menu_name) AS menus FROM sys_menu WHERE menu_type = 'C' AND (route_name IS NOT NULL AND route_name != '') GROUP BY route_name HAVING cnt > 1"
echo ===== CHECK EMPTY ROUTE_NAMES WITH C TYPE =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, path, parent_id FROM sys_menu WHERE menu_type = 'C' AND (route_name IS NULL OR route_name = '') AND path IN (SELECT path FROM sys_menu WHERE menu_type = 'C' GROUP BY path HAVING COUNT(*) > 1) ORDER BY path"
