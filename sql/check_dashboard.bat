@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, parent_id, path, component, menu_type, perms FROM sys_menu WHERE menu_id = 4544"
