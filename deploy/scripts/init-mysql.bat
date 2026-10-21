@echo off
title EDOP - Init MySQL
REM ============================================================
REM Initialize MySQL portable (run once on first use)
REM Path: D:\EDOP\scripts\init-mysql.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql
set MYSQL_INI=%EDOP_HOME%\config\my.ini

echo ============================================
echo    Initialize MySQL Portable
echo ============================================
echo.

REM Check MySQL
if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
    echo [ERROR] MySQL not found: %MYSQL_HOME%\bin\mysqld.exe
    echo [INFO]  Please extract MySQL ZIP to %MYSQL_HOME%
    echo [INFO]  Expected: %MYSQL_HOME%\bin\mysqld.exe
    pause
    exit /b 1
)

REM Step 1: Copy config
echo [1/4] Copying config...
if exist "%MYSQL_INI%" (
    copy /Y "%MYSQL_INI%" "%MYSQL_HOME%\my.ini" >nul
    echo [OK] my.ini copied to %MYSQL_HOME%\my.ini
) else (
    echo [WARN] %MYSQL_INI% not found, using MySQL defaults
)
echo.

REM Step 2: Create directories
echo [2/4] Creating directories...
if not exist "%MYSQL_HOME%\data" mkdir "%MYSQL_HOME%\data"
if not exist "%MYSQL_HOME%\logs" mkdir "%MYSQL_HOME%\logs"
echo [OK] data and logs directories created
echo.

REM Step 3: Check if already initialized
if exist "%MYSQL_HOME%\data\mysql" (
    echo [SKIP] MySQL data directory exists, no need to reinitialize
    echo.
    echo ============================================
    echo    MySQL Already Initialized!
    echo ============================================
    echo.
    echo  Start MySQL:     D:\EDOP\scripts\start-mysql.bat
    echo  Stop MySQL:      D:\EDOP\scripts\stop-mysql.bat
    echo  Change password: D:\EDOP\scripts\set-mysql-password.bat
    echo.
    pause
    exit /b 0
)

REM Step 4: Initialize database
echo [3/4] Initializing database (please wait)...
cd /d "%MYSQL_HOME%"
bin\mysqld.exe --initialize --console > "%MYSQL_HOME%\logs\init.log" 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Initialization failed! Check log:
    echo [INFO]  %MYSQL_HOME%\logs\init.log
    pause
    exit /b 1
)

echo [OK] Database initialized successfully!
echo.

REM Extract temp password
echo [INFO] Reading temporary password...
findstr "temporary password" "%MYSQL_HOME%\logs\init.log" > "%MYSQL_HOME%\logs\temp_password.txt" 2>nul
echo.
echo ============================================
echo    MySQL Initialization Complete!
echo ============================================
echo.
echo  Temporary root password saved to:
echo    %MYSQL_HOME%\logs\temp_password.txt
echo.
echo  Next steps:
echo   1. Run start-mysql.bat to start MySQL
echo   2. Run set-mysql-password.bat to change root password
echo   3. Run import-sql.bat to import platform data
echo.
pause
