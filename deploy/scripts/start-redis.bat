@echo off
title EDOP - Start Redis
REM ============================================================
REM Start Redis service
REM Path: D:\EDOP\scripts\start-redis.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Starting Redis...

REM Check directory
if not exist "%REDIS_HOME%\redis-server.exe" (
    echo %ERR% Redis not found: %REDIS_HOME%\redis-server.exe
    echo %INFO% Please extract Redis to %REDIS_HOME%
    pause
    exit /b 1
)

REM Create data and log directories
if not exist "%REDIS_HOME%\data" mkdir "%REDIS_HOME%\data"
if not exist "%REDIS_HOME%\logs" mkdir "%REDIS_HOME%\logs"

REM Check if port already in use
netstat -ano | findstr ":%REDIS_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% Port %REDIS_PORT% already in use, Redis may be running
    pause
    exit /b 0
)

REM Start Redis (background)
cd /d "%REDIS_HOME%"
start "EDOP-Redis" /min redis-server.exe redis.windows.conf

REM Wait for startup
timeout /t 2 /nobreak >nul

REM Verify startup
netstat -ano | findstr ":%REDIS_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Redis started! Port: %REDIS_PORT%
) else (
    echo %ERR% Redis startup failed, check log: %REDIS_HOME%\logs\redis.log
    pause
    exit /b 1
)

echo %INFO% Data dir: %REDIS_HOME%\data
echo %INFO% Log file: %REDIS_HOME%\logs\redis.log
