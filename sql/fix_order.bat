@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "UPDATE sys_menu SET order_num = 8 WHERE menu_id = 4727;"
echo Done: 质量管理 order_num set to 8
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT menu_id, menu_name, order_num, path, menu_type FROM sys_menu WHERE parent_id = 0 AND menu_type = 'M' ORDER BY order_num"
