@echo off
rem ============================================================
rem Run incremental upgrade SQL: 2026-11-11 dept leader_id
rem Scope: dept/user fields + BI decision-support + integration
rem        (DingTalk/WeCom attendance) + safety exam
rem Date: 2026-11-10
rem Usage: double-click, follow prompts. Uses mysql client only.
rem Put this .bat together with the .sql in the same folder.
rem NOTE: ASCII-only prompts to avoid codepage issues on server.
rem NOTE: Idempotent. Re-running has no side effect.
rem       Run the merged script ONLY. Do not run any split sql
rem       from the repo for this date.
rem ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "SQLFILE=upgrade_20261111_dept_leader_id.sql"

if not exist "%SQLFILE%" (
    echo [ERROR] SQL file "%SQLFILE%" not found. Keep this .bat in the sql folder.
    pause
    exit /b 1
)

rem ---- backup reminder: take a mysqldump BEFORE continuing ----
echo [WARN] Backup before upgrade is strongly recommended.
echo        Example:
echo        mysqldump -uroot -p --no-tablespaces --default-character-set=utf8mb4 edop_yhny ^> backup_before_20261110.sql
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
echo [INFO] Will run "%SQLFILE%" on database [%DB_NAME%]
echo [INFO] Scope: sys_dept.leader_id + backfill
echo [INFO] Idempotent: re-running has no side effect.
choice /C YN /M "Press Y to execute"
if errorlevel 2 (
    echo [INFO] Cancelled.
    pause
    exit /b 0
)

echo.
echo [INFO] Running SQL ...
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQLFILE%"
if errorlevel 1 (
    echo [ERROR] SQL execution reported errors. Review output above.
    pause
    exit /b 1
)

echo.
echo [INFO] Done. Self-check suggestions (run in mysql):
echo        - dept fields: sys_dept.dept_short_name/dept_code/charge_leader_id/staff_count
echo        - user fields: sys_user.superior_leader_id/charge_leader_id
echo        - bi tables: bi_metric_dict / bi_metric_snapshot / bi_metric_check_log
echo        - integration tables: integration_user_binding / integration_sync_log
echo        - safety exam tables: biz_safety_exam / biz_safety_exam_paper / biz_safety_exam_question
echo        - menus: BI/integration menus present, no duplicates
echo.
echo [INFO] Next: deploy new backend jar + frontend dist, then restart backend.
echo.
pause
