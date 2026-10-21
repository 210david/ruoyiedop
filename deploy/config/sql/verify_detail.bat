@echo off
echo === mms_workorder_status ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_workorder_status' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_order_type ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_order_type' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_priority ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_priority' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_shift_status ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_shift_status' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_route_status ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_route_status' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_issue_status ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_issue_status' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_status ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_status' ORDER BY dict_sort, dict_value;"
echo.
echo === mms_demand_status ===
mysql -u root -p123456 ry-vue --default-character-set=utf8mb4 -e "SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type='mms_demand_status' ORDER BY dict_sort, dict_value;"
