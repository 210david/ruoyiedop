@echo off
rem ============================================================
rem EDOP YLSP - DB incremental upgrade, 2026-11-24 batch
rem
rem Target DB : edop_ylsp  (backend port 82, jar edop-ylsp-v10.jar)
rem Script    : upgrade_20261124_all.sql  (single entry, all-in-one)
rem Steps     : backup -> apply -> self check (embedded in SQL tail)
rem
rem This SQL is the CUMULATIVE upgrade since the 2026-11-14 baseline
rem (34 sections + 39 self check items). No prerequisite batch is
rem needed: any older state can run this single file directly.
rem
rem Idempotent: safe to re-run, second run inserts 0 rows.
rem No DROP of business tables.
rem Usage: double-click, follow prompts. mysql client only.
rem Put this .bat together with upgrade_20261124_all.sql in the
rem same folder.
rem NOTE: ASCII-only prompts to avoid codepage issues on server.
rem NOTE: RESTART backend after upgrade (new jar + Flowable cache).
rem ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "SQLFILE=upgrade_20261124_all.sql"
set "BAKFILE=backup_before_20261124.sql"
set "LOGFILE=upgrade_20261124_run.log"

rem ---- check SQL file ----
if not exist "%SQLFILE%" (
    echo [ERROR] SQL file "%SQLFILE%" not found.
    echo         Keep this .bat in the same folder as the .sql file.
    echo.
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
    echo.
    pause
    exit /b 1
)

rem ---- find mysqldump.exe for the safety backup ----
set "DUMP_EXE="
where mysqldump >nul 2>nul && set "DUMP_EXE=mysqldump"
if not defined DUMP_EXE for %%p in (
    "D:\EDOP\mysql\bin\mysqldump.exe"
    "D:\EDOP\middleware\mysql\bin\mysqldump.exe"
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqldump.exe"
    "D:\mysql\bin\mysqldump.exe"
) do if exist "%%~p" set "DUMP_EXE=%%~p"

echo ============================================================
echo   EDOP YLSP  -  DB upgrade  2026-11-24
echo ============================================================
echo   mysql  : "%MYSQL_EXE%"
if defined DUMP_EXE (
    echo   dump   : "%DUMP_EXE%"
) else (
    echo   dump   : NOT FOUND - automatic backup will be skipped
)
echo   script : %SQLFILE%   (34 sections, cumulative, idempotent)
echo   target : edop_ylsp
echo ============================================================
echo.
echo   Steps this run:
echo     1) full backup  -^> %BAKFILE%
echo     2) apply        -^> %SQLFILE%  (idempotent)
echo     3) self check   -^> 39 read-only items (embedded in SQL tail)
echo.
echo   RESTART backend after this upgrade.
echo.

rem ---- input connection params (password typed, never stored) ----
set "DB_USER=root"
set /p DB_USER=MySQL username [default root]:
if "%DB_USER%"=="" set "DB_USER=root"

set "DB_PASS="
set /p DB_PASS=MySQL password:
if "%DB_PASS%"=="" (
    echo [ERROR] Password required. Nothing was changed.
    echo.
    pause
    exit /b 1
)

set "DB_NAME=edop_ylsp"

echo.
choice /C YN /M "Press Y to continue"
if errorlevel 2 (
    echo [INFO] Cancelled. Nothing was changed.
    echo.
    pause
    exit /b 0
)

rem ---------------- STEP 0: backup ----------------
if not defined DUMP_EXE goto skip_backup
echo.
echo [STEP 0/3] Backing up %DB_NAME% ...
"%DUMP_EXE%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 --single-transaction --routines --triggers --no-tablespaces %DB_NAME% > "%BAKFILE%"
if !errorlevel! neq 0 (
    echo [ERROR] Backup FAILED. Upgrade aborted, database NOT changed.
    echo         Check mysqldump, disk space and the password.
    echo.
    pause
    exit /b 1
)
for %%f in ("%BAKFILE%") do echo [OK] Backup done: %%~nxf  (%%~zf bytes)
goto run_upgrade

:skip_backup
echo.
echo [WARN] mysqldump not found, automatic backup skipped.
set /p GOON=Already backed up manually? (Y/N):
if /i not "%GOON%"=="Y" (
    echo [ABORT] Cancelled by user. Nothing was changed.
    echo.
    pause
    exit /b 1
)

rem ---------------- STEP 1: upgrade ----------------
:run_upgrade
echo.
echo [STEP 1/3] Applying %SQLFILE% ...
"%MYSQL_EXE%" -u%DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 %DB_NAME% < "%SQLFILE%" > "%LOGFILE%" 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] SQL execution reported errors.
    echo         Last lines of %LOGFILE%:
    echo ------------------------------------------------------------
    powershell -NoProfile -Command "if (Test-Path '%LOGFILE%') { Get-Content -Path '%LOGFILE%' -Tail 30 }"
    echo ------------------------------------------------------------
    echo The script is idempotent: fix the cause, then re-run this bat.
    echo.
    pause
    exit /b 1
)
echo [OK] Applied. Log: %LOGFILE%

rem ---------------- STEP 2: self check ----------------
echo.
echo [STEP 2/3] Self check results (embedded in SQL tail):
echo ------------------------------------------------------------
echo   PASS count :
powershell -NoProfile -Command "(Select-String -Path '%LOGFILE%' -Pattern 'PASS$').Count"
echo   FAIL count :
powershell -NoProfile -Command "(Select-String -Path '%LOGFILE%' -Pattern 'FAIL$').Count"
echo ------------------------------------------------------------
echo   FAIL detail (empty means all good):
powershell -NoProfile -Command "Get-Content '%LOGFILE%' | Select-String -Pattern 'FAIL$'"
echo ------------------------------------------------------------
powershell -NoProfile -Command "if (Select-String -Path '%LOGFILE%' -Pattern 'FAIL$' -Quiet) { exit 1 } else { exit 0 }"
if !errorlevel! neq 0 (
    echo [WARN] Some check items FAIL. Send %LOGFILE% back for review.
) else (
    echo [OK] All check items PASS.
)

echo.
echo [STEP 3/3] Done. Next steps:
echo    1) stop backend      : bin\stop.bat
echo    2) jar already new   : backend\edop-ylsp-v10.jar
echo    3) start backend     : bin\start.bat
echo       RESTART IS REQUIRED. New tables/columns and menu cache.
echo    4) frontend          : frontend\dist already new
echo    5) browser Ctrl+F5, verify:
echo       - WMS  : warehouse tree shows personal warehouse WH06
echo         (zone 01 + location 01), inbound type has personal item,
echo         selecting it locks target warehouse
echo       - WMS  : outbound type personal item locks warehouse too
echo       - MK   : shipment from an order only pulls goods with
echo         remaining qty greater than 0, fully shipped goods are
echo         skipped with a clear hint
echo       - Flow : leave process still works, test process gone
echo ============================================================
echo.
pause
