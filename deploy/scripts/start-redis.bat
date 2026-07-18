@echo off
chcp 65001 >nul
title EDOP - 启动Redis服务
REM ============================================================
REM 启动 Redis 服务
REM 路径: D:\EDOP\scripts\start-redis.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在启动 Redis 服务...

REM 检查目录是否存在
if not exist "%REDIS_HOME%\redis-server.exe" (
    echo %ERR% 未找到 Redis: %REDIS_HOME%\redis-server.exe
    echo %INFO% 请将 Redis 解压到 %REDIS_HOME% 目录
    pause
    exit /b 1
)

REM 创建数据目录和日志目录
if not exist "%REDIS_HOME%\data" mkdir "%REDIS_HOME%\data"
if not exist "%REDIS_HOME%\logs" mkdir "%REDIS_HOME%\logs"

REM 检查端口是否已被占用
netstat -ano | findstr ":%REDIS_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% 端口 %REDIS_PORT% 已被占用，Redis 可能已在运行
    pause
    exit /b 0
)

REM 启动 Redis（后台运行）
cd /d "%REDIS_HOME%"
start "EDOP-Redis" /min redis-server.exe redis.windows.conf

REM 等待启动
timeout /t 2 /nobreak >nul

REM 验证是否启动成功
netstat -ano | findstr ":%REDIS_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Redis 启动成功！端口: %REDIS_PORT%
) else (
    echo %ERR% Redis 启动失败，请检查日志: %REDIS_HOME%\logs\redis.log
    pause
    exit /b 1
)

echo %INFO% 数据目录: %REDIS_HOME%\data
echo %INFO% 日志文件: %REDIS_HOME%\logs\redis.log
