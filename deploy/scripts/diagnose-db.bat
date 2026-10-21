@echo off
title EDOP - Diagnose Database
REM ============================================================
REM Database diagnosis - all results saved to log file
REM Path: D:\EDOP\scripts\diagnose-db.bat
REM ============================================================

cd /d "%~dp0"

set MYSQL_EXE=D:\EDOP\mysql\bin\mysql.exe
set LOG_FILE=D:\EDOP\diagnose-result.txt

REM Clear log file
echo EDOP Database Diagnosis > "%LOG_FILE%"
echo Generated: %date% %time% >> "%LOG_FILE%"
echo ============================================ >> "%LOG_FILE%"
echo. >> "%LOG_FILE%"

echo ============================================
echo    EDOP - Database Diagnosis
echo ============================================
echo.

REM Check MySQL exists
if not exist "%MYSQL_EXE%" (
    echo [ERROR] MySQL not found: %MYSQL_EXE%
    echo [ERROR] MySQL not found: %MYSQL_EXE% >> "%LOG_FILE%"
    pause
    exit /b 1
)

REM Input password
set /p "mysql_pwd=Enter root password: "
echo.
echo [INFO] Running diagnosis, please wait...
echo.

REM Run queries - all output goes to log file
echo ---------- [1] Admin User ---------- >> "%LOG_FILE%"
echo SELECT user_id, user_name, nick_name, status FROM sys_user WHERE user_name = 'admin'; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [2] Admin Roles ---------- >> "%LOG_FILE%"
echo SELECT r.role_id, r.role_name, r.role_key, r.status FROM sys_role r INNER JOIN sys_user_role ur ON r.role_id = ur.role_id WHERE ur.user_id = 1; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [3] Hidden Menus ---------- >> "%LOG_FILE%"
echo SELECT menu_id, menu_name, visible, status FROM sys_menu WHERE visible = '1' AND menu_type IN ('M','C'); | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [4] Top-level Menus ---------- >> "%LOG_FILE%"
echo SELECT menu_id, menu_name, menu_type, visible, status, order_num FROM sys_menu WHERE parent_id = 0 ORDER BY order_num; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [5] DMS Menus ---------- >> "%LOG_FILE%"
echo SELECT menu_id, menu_name, parent_id, menu_type, visible, perms FROM sys_menu WHERE perms LIKE 'dms%%' ORDER BY parent_id, order_num; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [6] WMS Menus ---------- >> "%LOG_FILE%"
echo SELECT menu_id, menu_name, parent_id, menu_type, visible, perms FROM sys_menu WHERE perms LIKE 'wms%%' ORDER BY parent_id, order_num; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [7] Chinese Encoding Check ---------- >> "%LOG_FILE%"
echo SELECT dept_id, dept_name FROM sys_dept LIMIT 5; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ---------- [8] Data Summary ---------- >> "%LOG_FILE%"
echo SELECT 'sys_user' AS tbl, COUNT(*) AS cnt FROM sys_user UNION ALL SELECT 'sys_role', COUNT(*) FROM sys_role UNION ALL SELECT 'sys_dept', COUNT(*) FROM sys_dept UNION ALL SELECT 'sys_menu', COUNT(*) FROM sys_menu UNION ALL SELECT 'sys_dict_type', COUNT(*) FROM sys_dict_type UNION ALL SELECT 'sys_dict_data', COUNT(*) FROM sys_dict_data UNION ALL SELECT 'sys_config', COUNT(*) FROM sys_config UNION ALL SELECT 'dms_equipment', COUNT(*) FROM dms_equipment UNION ALL SELECT 'dms_work_order', COUNT(*) FROM dms_work_order UNION ALL SELECT 'wms_warehouse', COUNT(*) FROM wms_warehouse UNION ALL SELECT 'wms_material', COUNT(*) FROM wms_material; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue >> "%LOG_FILE%" 2>&1
echo. >> "%LOG_FILE%"

echo ============================================
echo    Diagnosis Complete!
echo ============================================
echo.
echo  Result saved to: %LOG_FILE%
echo.

REM Display result
type "%LOG_FILE%"

echo.
echo ============================================
echo  Full result saved to: %LOG_FILE%"
echo  Please send D:\EDOP\diagnose-result.txt to me
echo ============================================
pause
