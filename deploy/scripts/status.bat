@echo off
chcp 65001 >nul
title EDOP - 查看服务状态
REM ============================================================
REM 查看所有服务运行状态
REM 路径: D:\EDOP\scripts\status.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    企业数字化运营平台 - 服务状态
echo ============================================
echo.

REM MySQL 状态
echo [MySQL] 端口 %MYSQL_PORT%
netstat -ano | findstr ":%MYSQL_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   状态: 运行中
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%MYSQL_PORT% " ^| findstr LISTENING') do (
        echo   PID:  %%a
    )
) else (
    echo   状态: 未运行
)
echo.

REM Redis 状态
echo [Redis] 端口 %REDIS_PORT%
netstat -ano | findstr ":%REDIS_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   状态: 运行中
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%REDIS_PORT% " ^| findstr LISTENING') do (
        echo   PID:  %%a
    )
) else (
    echo   状态: 未运行
)
echo.

REM 后端状态
echo [后端] 端口 %BACKEND_PORT%
netstat -ano | findstr ":%BACKEND_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   状态: 运行中
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr LISTENING') do (
        echo   PID:  %%a
    )
) else (
    echo   状态: 未运行
)
echo.

REM Nginx 状态
echo [Nginx] 端口 %NGINX_PORT%
netstat -ano | findstr ":%NGINX_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   状态: 运行中
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%NGINX_PORT% " ^| findstr LISTENING') do (
        echo   PID:  %%a
    )
) else (
    echo   状态: 未运行
)
echo.

REM Windows 服务状态
echo [Windows 服务]
sc query EDOP-Backend >nul 2>&1
if %errorlevel% equ 0 (
    sc query EDOP-Backend | findstr "STATE"
) else (
    echo   EDOP-Backend: 未注册为服务
)
sc query EDOP-Nginx >nul 2>&1
if %errorlevel% equ 0 (
    sc query EDOP-Nginx | findstr "STATE"
) else (
    echo   EDOP-Nginx: 未注册为服务
)
sc query EDOP-Redis >nul 2>&1
if %errorlevel% equ 0 (
    sc query EDOP-Redis | findstr "STATE"
) else (
    echo   EDOP-Redis: 未注册为服务
)
sc query EDOP-MySQL >nul 2>&1
if %errorlevel% equ 0 (
    sc query EDOP-MySQL | findstr "STATE"
) else (
    echo   EDOP-MySQL: 未注册为服务
)
echo.
echo ============================================
pause
