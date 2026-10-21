@echo off
chcp 65001 >nul
echo === Final cleanup of duplicates ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "DELETE d1 FROM sys_dict_data d1 INNER JOIN sys_dict_data d2 WHERE d1.dict_type = d2.dict_type AND d1.dict_value = d2.dict_value AND d1.dict_code > d2.dict_code AND d1.dict_type LIKE 'mms_%%';"
echo.
echo === Final counts ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, COUNT(*) AS cnt FROM sys_dict_data WHERE dict_type IN ('mms_workorder_status','mms_order_type','mms_priority','mms_shift_status','mms_route_status','mms_issue_status','mms_status','mms_demand_status','mms_demand_type','mms_bom_type','mms_process_type','mms_resource_type','mms_qc_type','mms_qc_result','mms_abnormal_type','mms_abnormal_severity','mms_abnormal_status','mms_report_status','mms_kit_status','mms_mps_status','mms_yes_no') GROUP BY dict_type ORDER BY dict_type;"
echo.
echo === Clearing Redis cache again ===
redis-cli KEYS "sys_dict:mms_*" > %TEMP%\mms_keys.txt
for /f %%k in (%TEMP%\mms_keys.txt) do (
    redis-cli DEL "%%k" > nul
)
del %TEMP%\mms_keys.txt
echo Redis cache cleared.
