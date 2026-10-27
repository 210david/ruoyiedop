@echo off
title EDOP - Import Database SQL
REM ============================================================
REM Drop and recreate database, then import EDOP_ALL.sql
REM Path: D:\EDOP\scripts\import-sql.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql
set SQL_FILE=%EDOP_HOME%\config\sql\EDOP_ALL.sql
set MYSQL_EXE=%MYSQL_HOME%\bin\mysql.exe

echo ============================================
echo    Import Database SQL
echo ============================================
echo.

REM Check MySQL client
if not exist "%MYSQL_EXE%" (
    echo [ERROR] MySQL client not found: %MYSQL_EXE%
    pause
    exit /b 1
)

REM Check SQL file
if not exist "%SQL_FILE%" (
    echo [ERROR] SQL file not found: %SQL_FILE%
    pause
    exit /b 1
)

REM Check MySQL running
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] MySQL is not running!
    echo [INFO]  Please run start-mysql.bat first
    pause
    exit /b 1
)

echo [INFO] SQL file: %SQL_FILE%
for %%I in ("%SQL_FILE%") do echo [INFO] File size: %%~zI bytes
echo.

REM Input password
set /p "mysql_pwd=Enter root password: "
if "%mysql_pwd%"=="" (
    echo [ERROR] Password cannot be empty
    pause
    exit /b 1
)

echo.

REM Step 1: Drop existing database (use pipe to avoid backtick issues)
echo [1/3] Dropping existing database...
echo DROP DATABASE IF EXISTS `ry-vue`; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%"
if %errorlevel% neq 0 (
    echo [ERROR] Drop database failed!
    echo [INFO]  Possible reasons:
    echo   1. Wrong password
    echo   2. MySQL permission issue
    echo.
    echo Try manually:
    echo   echo DROP DATABASE IF EXISTS `ry-vue`; ^| mysql --default-character-set=utf8mb4 -u root -p"password"
    pause
    exit /b 1
)
echo [OK] Database dropped
echo.

REM Step 2: Import SQL (with UTF-8 charset)
echo [2/3] Importing SQL (may take 1-3 minutes)...
"%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" < "%SQL_FILE%"
if %errorlevel% neq 0 (
    echo [ERROR] Import failed!
    echo [INFO]  Check the error message above
    pause
    exit /b 1
)
echo [OK] Import complete
echo.

REM Step 3: Verify tables
echo [3/3] Verifying tables...
echo SHOW TABLES; | "%MYSQL_EXE%" --default-character-set=utf8mb4 -u root -p"%mysql_pwd%" ry-vue 2>nul | find /C /V "" > "%TEMP%\edop_count.txt"
set /p table_count=<"%TEMP%\edop_count.txt"
del "%TEMP%\edop_count.txt" 2>nul
set /a table_count-=1

echo ============================================
echo    Database Import Success!
echo ============================================
echo.
echo  Database: ry-vue
echo  Tables:   %table_count%
echo.
echo  Next steps:
echo   1. Edit D:\EDOP\app\application-druid.yml (set password)
echo   2. Run start-all.bat to start services
echo.
pause
