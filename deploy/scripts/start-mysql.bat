@echo off
title EDOP - Start MySQL
REM ============================================================
REM Start MySQL service (portable, console mode)
REM Path: D:\EDOP\scripts\start-mysql.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql

echo %INFO% Starting MySQL...

if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
    echo %ERR% MySQL not found: %MYSQL_HOME%\bin\mysqld.exe
    pause
    exit /b 1
)

REM Check if port already in use
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% Port 3306 already in use, MySQL may be running
    pause
    exit /b 0
)

REM Check if initialized
if not exist "%MYSQL_HOME%\data\mysql" (
    echo %ERR% MySQL not initialized!
    echo %INFO% Please run init-mysql.bat first
    pause
    exit /b 1
)

REM Create log directory
if not exist "%MYSQL_HOME%\logs" mkdir "%MYSQL_HOME%\logs"

REM Start MySQL (background)
cd /d "%MYSQL_HOME%"
start "EDOP-MySQL" /min bin\mysqld.exe --defaults-file="%MYSQL_HOME%\my.ini" --console

REM Wait for startup
echo %INFO% Waiting for MySQL to start...
set "wait=0"
:wait_loop
timeout /t 2 /nobreak >nul
set /a wait+=2
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% MySQL started! Port: 3306
    echo %INFO% Startup took ~%wait% seconds
    echo %INFO% Log file: %MYSQL_HOME%\logs\error.log
    exit /b 0
)
if %wait% geq 30 (
    echo %ERR% MySQL startup timeout (30s), check log:
    echo %INFO% %MYSQL_HOME%\logs\error.log
    pause
    exit /b 1
)
echo %INFO% Waiting... %wait% seconds
goto wait_loop
