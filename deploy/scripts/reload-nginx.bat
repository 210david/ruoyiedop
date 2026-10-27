@echo off
title EDOP - Reload Nginx
REM ============================================================
REM Reload Nginx config (without stopping service)
REM Path: D:\EDOP\scripts\reload-nginx.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Reloading Nginx config...

cd /d "%NGINX_HOME%"

REM Validate config first
nginx.exe -t >nul 2>&1
if %errorlevel% neq 0 (
    echo %ERR% Nginx config syntax error!
    echo %INFO% Details:
    nginx.exe -t
    pause
    exit /b 1
)

REM Reload config
nginx.exe -s reload >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Nginx config reloaded
) else (
    echo %ERR% Nginx reload failed, check if Nginx is running
    pause
    exit /b 1
)
