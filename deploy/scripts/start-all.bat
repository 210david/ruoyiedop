@echo off
title EDOP - Start All Services
REM ============================================================
REM Start all: MySQL -> Redis -> Backend -> Nginx
REM Path: D:\EDOP\scripts\start-all.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    EDOP - Start All Services
echo ============================================
echo.

REM Step 1: Start MySQL
echo [1/4] Starting MySQL...
call start-mysql.bat
echo.

REM Step 2: Start Redis
echo [2/4] Starting Redis...
call start-redis.bat
echo.

REM Step 3: Start Backend
echo [3/4] Starting Backend...
call start-backend.bat
echo.

REM Step 4: Start Nginx
echo [4/4] Starting Nginx...
call start-nginx.bat
echo.

echo ============================================
echo    All Services Started!
echo ============================================
echo.
echo  Web URL:      http://localhost:%NGINX_PORT%
echo  Backend API:  http://localhost:%BACKEND_PORT%
echo  Druid:        http://localhost:%NGINX_PORT%/druid/
echo  Swagger:      http://localhost:%NGINX_PORT%/swagger-ui.html
echo.
echo  Login:        admin / admin123
echo.
echo  MySQL Port:   %MYSQL_PORT%
echo  Redis Port:   %REDIS_PORT%
echo  Log Dir:      %LOG_HOME%
echo.
pause
