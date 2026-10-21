@echo off
chcp 65001 >nul
echo === Re-inserting mms_abnormal_severity and mms_abnormal_status ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES (1, '一般', '0', 'mms_abnormal_severity', '', 'info', 'Y', '0', 'admin', sysdate(), '一般'), (2, '严重', '1', 'mms_abnormal_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '严重'), (3, '紧急', '2', 'mms_abnormal_severity', '', 'danger', 'N', '0', 'admin', sysdate(), '紧急') ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);"
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES (1, '待响应', '0', 'mms_abnormal_status', '', 'danger', 'Y', '0', 'admin', sysdate(), '待响应'), (2, '处理中', '1', 'mms_abnormal_status', '', 'warning', 'N', '0', 'admin', sysdate(), '处理中'), (3, '已关闭', '2', 'mms_abnormal_status', '', 'success', 'N', '0', 'admin', sysdate(), '已关闭') ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);"
echo.
echo === Verify ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_value, dict_label, HEX(dict_label) as hex_label FROM sys_dict_data WHERE dict_type IN ('mms_abnormal_severity','mms_abnormal_status') ORDER BY dict_type, dict_sort;"
echo.
echo === Clear Redis ===
redis-cli DEL "sys_dict:mms_abnormal_severity" > nul
redis-cli DEL "sys_dict:mms_abnormal_status" > nul
echo Done.
