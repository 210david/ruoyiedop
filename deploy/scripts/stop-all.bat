@echo off
title EDOP - Stop All Services
REM ============================================================
REM Stop all: Nginx -> Backend -> Redis -> MySQL
REM Path: D:\EDOP\scripts\stop-all.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    EDOP - Stop All Services
echo ============================================
echo.

REM Step 1: Stop Nginx
echo [1/4] Stopping Nginx...
call stop-nginx.bat
echo.

REM Step 2: Stop Backend
echo [2/4] Stopping Backend...
call stop-backend.bat
echo.

REM Step 3: Stop Redis
echo [3/4] Stopping Redis...
call stop-redis.bat
echo.

REM Step 4: Stop MySQL
echo [4/4] Stopping MySQL...
call stop-mysql.bat
echo.

echo ============================================
echo    All Services Stopped
echo ============================================
echo.
pause
