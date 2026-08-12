@echo off
chcp 65001 >nul
echo ===== TOP LEVEL (no QMS C-type menus) =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, menu_type FROM sys_menu WHERE parent_id = 0 AND perms LIKE 'qms%%'"
echo ===== QMS TREE (directories and menus only) =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT m1.menu_id, CONCAT(REPEAT('  ', CASE WHEN m1.parent_id=0 THEN 0 WHEN m2.parent_id=0 THEN 1 ELSE 2 END), m1.menu_name) AS tree, m1.menu_type, m1.perms FROM sys_menu m1 LEFT JOIN sys_menu m2 ON m1.parent_id = m2.menu_id WHERE (m1.perms LIKE 'qms%%' OR m1.path = 'qms') AND m1.menu_type IN ('M','C') AND m1.menu_id != 4538 ORDER BY m1.parent_id, m1.order_num"
echo ===== GARBLED MENU CHECK =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT COUNT(*) AS garbled_count FROM sys_menu WHERE menu_id = 4538"
