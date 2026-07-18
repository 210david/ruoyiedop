@echo off
chcp 65001 >nul
title EDOP - 停止Nginx服务
REM ============================================================
REM 停止 Nginx 服务
REM 路径: D:\EDOP\scripts\stop-nginx.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在停止 Nginx 服务...

if not exist "%NGINX_HOME%\nginx.exe" (
    echo %ERR% 未找到 Nginx: %NGINX_HOME%\nginx.exe
    pause
    exit /b 1
)

REM 方式1: 通过 nginx -s quit 优雅关闭
cd /d "%NGINX_HOME%"
nginx.exe -s quit >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Nginx 已优雅关闭
    timeout /t 2 /nobreak >nul
    exit /b 0
)

REM 方式2: 通过进程名终止
tasklist | findstr "nginx.exe" >nul 2>&1
if %errorlevel% equ 0 (
    taskkill /F /IM nginx.exe >nul 2>&1
    echo %OK% Nginx 进程已强制终止
) else (
    echo %WARN% Nginx 未在运行
)
