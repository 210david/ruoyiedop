@echo off
title EDOP - Start Nginx
REM ============================================================
REM Start Nginx service
REM Path: D:\EDOP\scripts\start-nginx.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Starting Nginx...

REM Check directory
if not exist "%NGINX_HOME%\nginx.exe" (
    echo %ERR% Nginx not found: %NGINX_HOME%\nginx.exe
    echo %INFO% Please extract Nginx to %NGINX_HOME%
    pause
    exit /b 1
)

REM Check frontend files
if not exist "%FRONTEND_HOME%\dist\index.html" (
    echo %WARN% Frontend not found: %FRONTEND_HOME%\dist\index.html
    echo %INFO% Please run build.bat first and copy dist to %FRONTEND_HOME%
    echo %INFO% Nginx will still start, but frontend pages unavailable
)

REM Check if port already in use
netstat -ano | findstr ":%NGINX_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% Port %NGINX_PORT% already in use, Nginx may be running
    echo %INFO% To reload config, run reload-nginx.bat
    pause
    exit /b 0
)

REM Create log directory
if not exist "%NGINX_HOME%\logs" mkdir "%NGINX_HOME%\logs"

REM Start Nginx
cd /d "%NGINX_HOME%"
start "EDOP-Nginx" /min nginx.exe

REM Wait for startup
timeout /t 2 /nobreak >nul

REM Verify startup
netstat -ano | findstr ":%NGINX_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Nginx started! Port: %NGINX_PORT%
    echo %INFO% URL: http://localhost:%NGINX_PORT%
) else (
    echo %ERR% Nginx startup failed, check:
    echo %INFO% Config: %NGINX_HOME%\conf\nginx.conf
    echo %INFO% Error log: %NGINX_HOME%\logs\error.log
    pause
    exit /b 1
)
