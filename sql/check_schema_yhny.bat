@echo off
rem ============================================================
rem Read-only schema check: edop_yhny vs current code
rem Script: check_schema_yhny.sql v2 (2026-11-14, INSERT-only expectation
rem         list + Section C split, fixes ERROR 1137 and false positives)
rem         (TEMP table only, no data change)
rem Output: A) missing tables (informational)  B) missing columns
rem         C) summary counts
rem Date: 2026-11-14
rem Usage: double-click, follow prompts. Uses mysql client only.
rem Put this .bat together with the .sql in the same folder.
rem NOTE: ASCII-only prompts to avoid codepage issues on server.
rem NOTE: 100% read-only. Safe to run anytime, even while backend
rem       is running. Re-running has no side effect.
rem ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "SQLFILE=check_schema_yhny.sql"

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
set "DB_NAME=edop_yhny"
set /p DB_NAME=Database name [default edop_yhny]: 
if "%DB_NAME%"=="" set "DB_NAME=edop_yhny"

echo.
echo [INFO] Will CHECK schema of database [%DB_NAME%] (read-only, no changes).
choice /C YN /M "Press Y to run check"
if errorlevel 2 (
    echo [INFO] Cancelled.
    pause
    exit /b 0
)

echo.
echo [INFO] Section A: missing tables (informational) ...
echo [INFO] Section B: missing columns (KEY issues) ...
echo [INFO] Section C: summary ...
echo.
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 --table %DB_NAME% < "%SQLFILE%"
if errorlevel 1 (
    echo [ERROR] Check reported errors. Review output above.
    pause
    exit /b 1
)

echo.
echo [INFO] How to read the result:
echo        - Section B rows = columns the code expects but the DB lacks.
echo          These MUST be fixed before deploying the new backend jar,
echo          otherwise the related pages will report SQL errors.
echo        - Section A rows = whole tables absent from the DB. Tables of
echo          disabled modules are normal; tables of enabled modules need
echo          to be created.
echo        - Section C missing_columns = 0 means schema matches code.
echo.
echo [INFO] This check is read-only. You can re-run it anytime.
echo.
pause
