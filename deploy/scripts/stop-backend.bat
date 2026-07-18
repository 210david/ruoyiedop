@echo off
chcp 65001 >nul
title EDOP - 停止后端服务
REM ============================================================
REM 停止后端 Spring Boot 服务
REM 路径: D:\EDOP\scripts\stop-backend.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在停止后端服务...

REM 通过端口查找并终止进程
set "found=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr LISTENING') do (
    echo %INFO% 正在终止进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

REM 同时查找 javaw 运行的 jar 进程（备用方式）
for /f "tokens=1" %%a in ('jps -l ^| findstr %JAR_NAME%') do (
    echo %INFO% 正在终止 Java进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

if "%found%"=="0" (
    echo %WARN% 后端服务未在运行
) else (
    echo %OK% 后端服务已停止
)
