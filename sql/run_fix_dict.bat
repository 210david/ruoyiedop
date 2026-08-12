@echo off
chcp 65001 >nul
echo === Fixing QMS dict and jobs ===
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue < "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\fix_qms_dict_and_jobs.sql"
if %errorlevel% neq 0 (
    echo FAILED
    exit /b 1
)
echo SUCCESS
echo ===== VERIFY DICT =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_name FROM sys_dict_type WHERE dict_type IN ('qms_esig_biz_type','qms_workshop_node_type')"
echo ===== VERIFY DICT DATA =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT dict_type, dict_label, dict_value FROM sys_dict_data WHERE dict_type IN ('qms_esig_biz_type','qms_workshop_node_type') ORDER BY dict_type, dict_sort"
echo ===== VERIFY JOBS =====
mysql -u root -p123456 --default-character-set=utf8mb4 ry-vue -e "SELECT job_id, job_name, job_group, invoke_target, status FROM sys_job WHERE invoke_target LIKE '%%qms%%'"
