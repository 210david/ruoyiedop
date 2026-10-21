@echo off
title EDOP - Fix Server Issues
REM ============================================================
REM Fix missing sys_alarm_log table + import dev data
REM Run this on the SERVER
REM Path: D:\EDOP\scripts\fix-server.bat
REM ============================================================

cd /d "%~dp0"

set MYSQL_EXE=D:\EDOP\mysql\bin\mysql.exe
set MYSQL_PWD=zbrj@2026
set FIX_SQL=D:\EDOP\config\sql\FIX_ALARM_LOG.sql
set DATA_SQL=D:\EDOP\config\sql\EDOP_DATA_UPDATE.sql

echo ============================================
echo    EDOP - Server Fix Script
echo ============================================
echo.

REM Step 1: Create missing sys_alarm_log table
echo [1/3] Creating missing sys_alarm_log table...
if exist "%FIX_SQL%" (
    "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue < "%FIX_SQL%" 2>nul
    if %errorlevel% equ 0 (
        echo   [OK] sys_alarm_log table created
    ) else (
        echo   [WARN] Table may already exist or creation failed
    )
) else (
    echo   [SKIP] FIX_ALARM_LOG.sql not found
)
echo.

REM Step 2: Import dev data update
echo [2/3] Importing dev data update...
if exist "%DATA_SQL%" (
    "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue < "%DATA_SQL%" 2>nul
    if %errorlevel% equ 0 (
        echo   [OK] Data imported successfully
    ) else (
        echo   [ERROR] Data import failed!
        echo   [INFO] Try running manually:
        echo     %MYSQL_EXE% --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue < "%DATA_SQL%"
    )
) else (
    echo   [SKIP] EDOP_DATA_UPDATE.sql not found
)
echo.

REM Step 3: Verify
echo [3/3] Verifying...
echo SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = 'ry-vue'; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" 2>nul
echo.
echo SELECT 'sys_menu' AS tbl, COUNT(*) AS cnt FROM sys_menu UNION ALL SELECT 'sys_user', COUNT(*) FROM sys_user UNION ALL SELECT 'dms_equipment', COUNT(*) FROM dms_equipment UNION ALL SELECT 'wms_warehouse', COUNT(*) FROM wms_warehouse; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue 2>nul
echo.

echo ============================================
echo    Fix Complete!
echo ============================================
echo.
echo  Next steps:
echo   1. Restart backend:  D:\EDOP\scripts\stop-backend.bat
echo                        D:\EDOP\scripts\start-backend.bat
echo   2. Clear browser cache (Ctrl+Shift+Delete)
echo   3. Re-open browser and visit http://localhost:8005
echo   4. Login with admin / admin123
echo.
pause
