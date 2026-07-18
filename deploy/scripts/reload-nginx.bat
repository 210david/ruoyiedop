@echo off
chcp 65001 >nul
title EDOP - 重新加载Nginx配置
REM ============================================================
REM 重新加载 Nginx 配置（不停止服务）
REM 路径: D:\EDOP\scripts\reload-nginx.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在重新加载 Nginx 配置...

cd /d "%NGINX_HOME%"

REM 先验证配置文件语法
nginx.exe -t >nul 2>&1
if %errorlevel% neq 0 (
    echo %ERR% Nginx 配置文件语法错误！
    echo %INFO% 正在显示详细错误信息:
    nginx.exe -t
    pause
    exit /b 1
)

REM 重新加载配置
nginx.exe -s reload >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Nginx 配置已重新加载
) else (
    echo %ERR% Nginx 配置重新加载失败，请检查 Nginx 是否在运行
    pause
    exit /b 1
)
