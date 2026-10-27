@echo off
title EDOP - Stop Nginx
REM ============================================================
REM Stop Nginx service
REM Path: D:\EDOP\scripts\stop-nginx.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Stopping Nginx...

if not exist "%NGINX_HOME%\nginx.exe" (
    echo %ERR% Nginx not found: %NGINX_HOME%\nginx.exe
    pause
    exit /b 1
)

REM Method 1: Graceful shutdown via nginx -s quit
cd /d "%NGINX_HOME%"
nginx.exe -s quit >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Nginx gracefully stopped
    timeout /t 2 /nobreak >nul
    exit /b 0
)

REM Method 2: Kill by process name
tasklist | findstr "nginx.exe" >nul 2>&1
if %errorlevel% equ 0 (
    taskkill /F /IM nginx.exe >nul 2>&1
    echo %OK% Nginx process terminated
) else (
    echo %WARN% Nginx not running
)
