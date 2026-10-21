@echo off
chcp 65001 >nul
echo === Fixing MMS dict encoding ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "source e:/AIcode/EDOP/RuoYi/RuoYi-Vue3/deploy/config/sql/fix_dict_encoding.sql"
echo EXIT_CODE: %ERRORLEVEL%
echo.
echo === Verifying mms_workorder_status ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_value, dict_label, HEX(dict_label) as hex_label FROM sys_dict_data WHERE dict_type='mms_workorder_status' ORDER BY dict_sort;"
echo.
echo === Verifying mms_order_type ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_value, dict_label, HEX(dict_label) as hex_label FROM sys_dict_data WHERE dict_type='mms_order_type' ORDER BY dict_sort;"
echo.
echo === Verifying mms_priority ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_value, dict_label, HEX(dict_label) as hex_label FROM sys_dict_data WHERE dict_type='mms_priority' ORDER BY dict_sort;"
