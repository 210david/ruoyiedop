@echo off
chcp 65001 >nul
title EDOP - 停止Redis服务
REM ============================================================
REM 停止 Redis 服务
REM 路径: D:\EDOP\scripts\stop-redis.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在停止 Redis 服务...

REM 方式1: 通过 redis-cli 优雅关闭
if exist "%REDIS_HOME%\redis-cli.exe" (
    cd /d "%REDIS_HOME%"
    redis-cli.exe shutdown nosave >nul 2>&1
    if %errorlevel% equ 0 (
        echo %OK% Redis 已通过命令优雅关闭
        exit /b 0
    )
)

REM 方式2: 通过端口查找进程并终止
set "found=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%REDIS_PORT% " ^| findstr LISTENING') do (
    echo %INFO% 正在终止进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

if "%found%"=="0" (
    echo %WARN% Redis 未在运行
) else (
    echo %OK% Redis 服务已停止
)
