@echo off
rem ============================================================
rem Run incremental upgrade SQL: 2026-11-14 YHNY sync batch
rem Scope: 1) upgrade_20261112_certificate.sql
rem           - HR cert employee_id nullable (external personnel)
rem        2) cleanup_seed_employee_yhny.sql
rem           - remove seeded demo employees + backup tables +
rem             dynamic business-reference check (information_schema) +
rem             seed-linked user cleanup (no fixed id range) +
rem             employee_no sequence reset (if mk_number_rule exists)
rem Target DB: edop_yhny  (backend port 84, frontend 8084)
rem Date: 2026-11-14
rem Usage: double-click, follow prompts. Uses mysql client only.
rem Put this .bat together with the .sql files in the same folder.
rem NOTE: ASCII-only prompts to avoid codepage issues on server.
rem NOTE: Both scripts are safe to re-run (idempotent / guarded).
rem ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "SQL1=upgrade_20261112_certificate.sql"
set "SQL2=cleanup_seed_employee_yhny.sql"

if not exist "%SQL1%" (
    echo [ERROR] SQL file "%SQL1%" not found. Keep this .bat in the sql folder.
    pause
    exit /b 1
)
if not exist "%SQL2%" (
    echo [ERROR] SQL file "%SQL2%" not found. Keep this .bat in the sql folder.
    pause
    exit /b 1
)

rem ---- backup reminder: take a mysqldump BEFORE continuing ----
echo [WARN] Backup before upgrade is strongly recommended.
echo        Example:
echo        mysqldump -uroot -p --no-tablespaces --default-character-set=utf8mb4 edop_yhny ^> backup_before_20261114.sql
echo.

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
set "DB_NAME=edop_yhny"
set /p DB_NAME=Database name [default edop_yhny]: 
if "%DB_NAME%"=="" set "DB_NAME=edop_yhny"

echo.
echo [INFO] Will run on database [%DB_NAME%]:
echo        1. "%SQL1%"   - cert employee_id nullable (idempotent)
echo        2. "%SQL2%"   - seed employee cleanup (guarded, creates backup tables)
echo [INFO] Step 2 deletes seeded demo employee rows and resets the
echo        employee_no sequence. Review its output carefully.
choice /C YN /M "Press Y to execute"
if errorlevel 2 (
    echo [INFO] Cancelled.
    pause
    exit /b 0
)

echo.
echo [INFO] Step 1/2: running "%SQL1%" ...
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQL1%"
if errorlevel 1 (
    echo [ERROR] Step 1 reported errors. Review output above. Step 2 NOT run.
    pause
    exit /b 1
)
echo [INFO] Step 1 done.

echo.
echo [INFO] Step 2/2: running "%SQL2%" ...
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQL2%"
if errorlevel 1 (
    echo [ERROR] Step 2 reported errors. Review output above.
    pause
    exit /b 1
)
echo [INFO] Step 2 done.

echo.
echo [INFO] Done. Self-check suggestions (run in mysql):
echo        - SHOW COLUMNS FROM biz_hr_certificate LIKE 'employee_id';  (Null=YES)
echo        - SELECT COUNT(*) FROM biz_hr_employee;                     (seed rows removed)
echo        - SELECT MAX(CAST(employee_no AS UNSIGNED)) FROM biz_hr_employee; (sequence reset)
echo.
echo [INFO] Next: deploy new backend jar + frontend dist, then restart backend.
echo.
pause
