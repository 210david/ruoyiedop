@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT user_id, dept_id, user_name, nick_name, remark FROM sys_user WHERE user_id=1"
