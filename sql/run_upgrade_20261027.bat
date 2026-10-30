@echo off
rem ============================================================
rem Run incremental upgrade SQL: warehouse code fixes (2026-10-27)
rem Usage: double-click, follow prompts. Uses mysql client only.
rem Put this .bat together with the .sql in the same folder.
rem NOTE: ASCII-only prompts to avoid codepage issues on server.
rem ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "SQLFILE=upgrade_20261027_warehouse_code.sql"

if not exist "%SQLFILE%" (
    echo [ERROR] SQL file "%SQLFILE%" not found. Keep this .bat in the sql folder.
    pause
    exit /b 1
)

rem ---- find mysql.exe: PATH first, then known install paths ----
set "MYSQL_EXE="
where mysql >nul 2>nul && set "MYSQL_EXE=mysql"
if not defined MYSQL_EXE for %%p in (
    "D:\EDOP\mysql\bin\mysql.exe"
    "D:\EDOP\middleware\mysql\bin\mysql.exe"
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    "D:\mysql\bin\mysql.exe"
) do if exist "%%~p" set "MYSQL_EXE=%%~p"
if not defined MYSQL_EXE (
    echo [ERROR] mysql.exe not found.
    echo         Add the MySQL bin folder to PATH, or edit this script
    echo         and write the full path into the search list above.
    pause
    exit /b 1
)
echo [INFO] mysql: "%MYSQL_EXE%"

rem ---- input connection params (password typed interactively, never stored) ----
set "DB_USER=root"
set /p DB_USER=MySQL username [default root]: 
if "%DB_USER%"=="" set "DB_USER=root"
set "DB_PASS="
set /p DB_PASS=MySQL password: 
set "DB_NAME=edop_cfsp"
set /p DB_NAME=Database name [default edop_cfsp]: 
if "%DB_NAME%"=="" set "DB_NAME=edop_cfsp"

echo.
echo [INFO] Will run "%SQLFILE%" on database [%DB_NAME%]
echo [INFO] Action: fix warehouse number rule (WH/3-digit) and align current_seq
choice /C YN /M "Press Y to execute"
if errorlevel 2 (
    echo [INFO] Cancelled.
    pause
    exit /b 0
)

echo.
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% %DB_NAME% --default-character-set=utf8mb4 < "%SQLFILE%"
if errorlevel 1 (
    echo.
    echo [ERROR] Execution failed. Check username / password / database name.
    pause
    exit /b 1
)

echo.
echo [INFO] Done. Verification below (next_code should continue existing codes):
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% %DB_NAME% --default-character-set=utf8mb4 -e "SELECT rule_code, prefix, seq_length, seq_start, step, current_seq, CONCAT(prefix, LPAD(current_seq + step, seq_length, '0')) AS next_code FROM mk_number_rule WHERE rule_code='wms_warehouse';"

echo.
echo [INFO] Next steps: stop old backend, replace backend\edop-cfsp-v10.jar, overwrite frontend\dist.
pause
