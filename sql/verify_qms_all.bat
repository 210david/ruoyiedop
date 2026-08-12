@echo off
chcp 65001 >nul
echo ===== QRTZ TABLES =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SHOW TABLES LIKE 'qrtz%%'"
echo ===== SYS_JOB (RuoYi quartz) =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT job_id, job_name, job_group, invoke_target, status FROM sys_job WHERE invoke_target LIKE '%%qms%%' OR job_name LIKE '%%质量%%' OR job_name LIKE '%%QMS%%' OR job_name LIKE '%%IQC%%'"
echo ===== GARBLED DICT CHECK =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_name, HEX(dict_name) AS hex_name FROM sys_dict_type WHERE dict_type IN ('qms_esig_biz_type','qms_workshop_node_type')"
echo ===== DICT DATA FOR GARBLED TYPES =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_label, dict_value FROM sys_dict_data WHERE dict_type IN ('qms_esig_biz_type','qms_workshop_node_type') ORDER BY dict_type, dict_sort"
