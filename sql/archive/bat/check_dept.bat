@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dept_id, parent_id, dept_name, status, del_flag FROM sys_dept ORDER BY parent_id, order_num"
