@echo off
title EDOP - Stop MySQL
REM ============================================================
REM Stop MySQL service
REM Path: D:\EDOP\scripts\stop-mysql.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql

echo %INFO% Stopping MySQL...

if not exist "%MYSQL_HOME%\bin\mysqladmin.exe" (
    echo %ERR% mysqladmin not found: %MYSQL_HOME%\bin\mysqladmin.exe
    pause
    exit /b 1
)

REM Method 1: Graceful shutdown via mysqladmin (no password)
cd /d "%MYSQL_HOME%"
bin\mysqladmin.exe -u root shutdown >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% MySQL gracefully stopped
    timeout /t 2 /nobreak >nul
    exit /b 0
)

REM Method 2: With password
set /p "mysql_pwd=Enter root password: "
bin\mysqladmin.exe -u root -p"%mysql_pwd%" shutdown >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% MySQL gracefully stopped
    exit /b 0
)

REM Method 3: Kill by port
set "found=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3306 " ^| findstr LISTENING') do (
    echo %INFO% Terminating PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

if "%found%"=="0" (
    echo %WARN% MySQL not running
) else (
    echo %OK% MySQL stopped
)
