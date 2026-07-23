@echo off
title EDOP - Import Data (with error details)
REM ============================================================
REM Import dev data with full error output
REM Run AFTER stopping backend service
REM ============================================================

cd /d "%~dp0"

set MYSQL_EXE=D:\EDOP\mysql\bin\mysql.exe
set MYSQL_PWD=zbrj@2026
set DATA_SQL=D:\EDOP\config\sql\EDOP_DATA_UPDATE.sql

echo ============================================
echo    EDOP - Import Dev Data
echo ============================================
echo.

REM Check file exists
if not exist "%DATA_SQL%" (
    echo [ERROR] Data file not found: %DATA_SQL%
    pause
    exit /b 1
)

for %%I in ("%DATA_SQL%") do echo [INFO] File: %%~zI bytes
echo.

REM Check if backend is running (must stop first!)
netstat -ano | findstr ":8004 " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo [WARN] Backend is still running! Stop it first to avoid lock conflicts.
    echo [INFO] Run: D:\EDOP\scripts\stop-backend.bat
    echo.
    set /p "continue=Continue anyway? (y/n): "
    if /i not "%continue%"=="y" exit /b 0
    echo.
)

echo [INFO] Importing data (with error output)...
echo.

REM Import with full error output (no 2>nul)
"%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue < "%DATA_SQL%" 2>&1

if %errorlevel% equ 0 (
    echo.
    echo [OK] Data imported successfully!
) else (
    echo.
    echo [ERROR] Import failed! See error above.
)

echo.
echo ============================================
echo  Verification:
echo ============================================
echo SELECT COUNT(*) AS menu_count FROM sys_menu; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue 2>nul
echo.
echo SELECT 'sys_user' AS tbl, COUNT(*) AS cnt FROM sys_user UNION ALL SELECT 'dms_equipment', COUNT(*) FROM dms_equipment UNION ALL SELECT 'wms_warehouse', COUNT(*) FROM wms_warehouse; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%MYSQL_PWD%" ry-vue 2>nul

echo.
pause
