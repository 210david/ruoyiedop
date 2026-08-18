@echo off
echo === MMS Dict Types ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_type, dict_name FROM sys_dict_type WHERE dict_type LIKE 'mms_%%' ORDER BY dict_type;"
echo.
echo === MMS Dict Data Counts ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_type, COUNT(*) AS cnt FROM sys_dict_data WHERE dict_type LIKE 'mms_%%' GROUP BY dict_type ORDER BY dict_type;"
