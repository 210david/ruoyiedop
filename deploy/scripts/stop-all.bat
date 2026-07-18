@echo off
chcp 65001 >nul
title EDOP - 一键停止全部服务
REM ============================================================
REM 一键停止: Nginx -> 后端 -> Redis -> MySQL
REM 路径: D:\EDOP\scripts\stop-all.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    企业数字化运营平台 - 一键停止
echo ============================================
echo.

REM Step 1: 停止 Nginx
echo [1/4] 停止 Nginx...
call stop-nginx.bat
echo.

REM Step 2: 停止后端
echo [2/4] 停止后端服务...
call stop-backend.bat
echo.

REM Step 3: 停止 Redis
echo [3/4] 停止 Redis...
call stop-redis.bat
echo.

REM Step 4: 停止 MySQL
echo [4/4] 停止 MySQL...
call stop-mysql.bat
echo.

echo ============================================
echo    全部服务已停止
echo ============================================
echo.
pause
