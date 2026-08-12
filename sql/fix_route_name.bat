@echo off
chcp 65001 >nul
echo ===== BEFORE =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, path, route_name FROM sys_menu WHERE menu_id = 4544"
echo ===== FIXING =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "UPDATE sys_menu SET route_name = 'QmsDashboard' WHERE menu_id = 4544"
echo ===== AFTER =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, path, route_name FROM sys_menu WHERE menu_id = 4544"
echo ===== CHECK ALL DASHBOARD ROUTE NAMES =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, path, route_name, parent_id FROM sys_menu WHERE path = 'dashboard' AND menu_type = 'C'"
