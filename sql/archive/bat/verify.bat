@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT user_id, dept_id, user_name, nick_name, status FROM sys_user WHERE del_flag='0' ORDER BY user_id"
