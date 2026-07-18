@echo off
chcp 65001 >nul
title EDOP - 启动Nginx服务
REM ============================================================
REM 启动 Nginx 服务
REM 路径: D:\EDOP\scripts\start-nginx.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在启动 Nginx 服务...

REM 检查目录是否存在
if not exist "%NGINX_HOME%\nginx.exe" (
    echo %ERR% 未找到 Nginx: %NGINX_HOME%\nginx.exe
    echo %INFO% 请将 Nginx 解压到 %NGINX% 目录
    pause
    exit /b 1
)

REM 检查前端文件是否存在
if not exist "%FRONTEND_HOME%\dist\index.html" (
    echo %WARN% 未找到前端文件: %FRONTEND_HOME%\dist\index.html
    echo %INFO% 请先执行 build.bat 编译前端，并将 dist 目录复制到 %FRONTEND_HOME% 目录
    echo %INFO% Nginx 仍将启动，但前端页面不可用
)

REM 检查端口是否已被占用
netstat -ano | findstr ":%NGINX_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% 端口 %NGINX_PORT% 已被占用，Nginx 可能已在运行
    echo %INFO% 如需重新加载配置请运行 reload-nginx.bat
    pause
    exit /b 0
)

REM 创建日志目录
if not exist "%NGINX_HOME%\logs" mkdir "%NGINX_HOME%\logs"

REM 启动 Nginx
cd /d "%NGINX_HOME%"
start "EDOP-Nginx" /min nginx.exe

REM 等待启动
timeout /t 2 /nobreak >nul

REM 验证是否启动成功
netstat -ano | findstr ":%NGINX_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Nginx 启动成功！端口: %NGINX_PORT%
    echo %INFO% 访问地址: http://localhost:%NGINX_PORT%
) else (
    echo %ERR% Nginx 启动失败，请检查:
    echo %INFO% 配置文件: %NGINX_HOME%\conf\nginx.conf
    echo %INFO% 错误日志: %NGINX_HOME%\logs\error.log
    pause
    exit /b 1
)
