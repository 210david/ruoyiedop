@echo off
title EDOP - Stop Redis
REM ============================================================
REM Stop Redis service
REM Path: D:\EDOP\scripts\stop-redis.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Stopping Redis...

REM Method 1: Graceful shutdown via redis-cli
if exist "%REDIS_HOME%\redis-cli.exe" (
    cd /d "%REDIS_HOME%"
    redis-cli.exe shutdown nosave >nul 2>&1
    if %errorlevel% equ 0 (
        echo %OK% Redis gracefully stopped
        exit /b 0
    )
)

REM Method 2: Kill by port
set "found=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%REDIS_PORT% " ^| findstr LISTENING') do (
    echo %INFO% Terminating PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

if "%found%"=="0" (
    echo %WARN% Redis not running
) else (
    echo %OK% Redis stopped
)
