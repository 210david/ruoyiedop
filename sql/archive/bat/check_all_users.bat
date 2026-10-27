@echo off
chcp 65001 >nul
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT u.user_id, u.dept_id, d.dept_name, u.user_name, u.nick_name, u.remark, GROUP_CONCAT(r.role_name) AS roles FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.dept_id LEFT JOIN sys_user_role ur ON u.user_id=ur.user_id LEFT JOIN sys_role r ON ur.role_id=r.role_id WHERE u.del_flag='0' GROUP BY u.user_id ORDER BY u.dept_id, u.user_id"
