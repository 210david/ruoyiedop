@echo off
title EDOP - Verify Database
cd /d "%~dp0"
call env.bat

set MYSQL_EXE=%MYSQL_HOME%\bin\mysql.exe
set /p "mysql_pwd=Enter root password: "

echo.
echo ============================================
echo    Database Verification
echo ============================================
echo.

echo [1] Table count:
echo SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = 'ry-vue'; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" 2>nul

echo.
echo [2] Key tables check:
echo SELECT 'sys_menu' AS tbl, COUNT(*) AS cnt FROM sys_menu UNION ALL SELECT 'sys_user', COUNT(*) FROM sys_user UNION ALL SELECT 'sys_role', COUNT(*) FROM sys_role UNION ALL SELECT 'wms_warehouse', COUNT(*) FROM wms_warehouse UNION ALL SELECT 'dms_equipment', COUNT(*) FROM dms_equipment UNION ALL SELECT 'dms_work_order', COUNT(*) FROM dms_work_order UNION ALL SELECT 'dms_pm_plan', COUNT(*) FROM dms_pm_plan; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue 2>nul

echo.
echo [3] All tables:
echo SHOW TABLES; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue 2>nul

echo.
echo ============================================
pause
