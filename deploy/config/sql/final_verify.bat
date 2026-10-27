@echo off
chcp 65001 >nul
echo ====================================
echo  MMS Dictionary Final Verification
echo ====================================
echo.
echo === All MMS dict types and data counts ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT t.dict_type, t.dict_name, COUNT(d.dict_value) AS data_count FROM sys_dict_type t LEFT JOIN sys_dict_data d ON t.dict_type = d.dict_type WHERE t.dict_type LIKE 'mms_%%' GROUP BY t.dict_type, t.dict_name ORDER BY t.dict_type;" 2>nul
echo.
echo === Check for any ?? corrupted labels ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_value, dict_label FROM sys_dict_data WHERE dict_type LIKE 'mms_%%' AND dict_label REGEXP '^[?]+$';" 2>nul
echo.
echo === All MMS dict data (key dictionaries) ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type IN ('mms_workorder_status','mms_order_type','mms_priority','mms_shift_status','mms_route_status','mms_issue_status','mms_status','mms_demand_status','mms_demand_type','mms_bom_type','mms_process_type','mms_resource_type','mms_qc_type','mms_qc_result','mms_abnormal_type','mms_abnormal_severity','mms_abnormal_status','mms_report_status','mms_kit_status','mms_mps_status','mms_yes_no') ORDER BY dict_type, dict_sort;" 2>nul
