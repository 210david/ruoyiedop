@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT user_id, dept_id, user_name, nick_name, remark FROM sys_user WHERE user_id=1; SELECT dept_id, dept_name FROM sys_dept WHERE dept_name='信息技术部'; SELECT user_id, dept_id, user_name, nick_name FROM sys_user WHERE del_flag='0' ORDER BY dept_id, user_id"
