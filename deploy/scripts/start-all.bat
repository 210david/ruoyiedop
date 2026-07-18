@echo off
chcp 65001 >nul
title EDOP - 一键启动全部服务
REM ============================================================
REM 一键启动: MySQL -> Redis -> 后端 -> Nginx
REM 路径: D:\EDOP\scripts\start-all.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    企业数字化运营平台 - 一键启动
echo ============================================
echo.

REM Step 1: 启动 MySQL
echo [1/4] 启动 MySQL...
call start-mysql.bat
echo.

REM Step 2: 启动 Redis
echo [2/4] 启动 Redis...
call start-redis.bat
echo.

REM Step 3: 启动后端
echo [3/4] 启动后端服务...
call start-backend.bat
echo.

REM Step 4: 启动 Nginx
echo [4/4] 启动 Nginx...
call start-nginx.bat
echo.

echo ============================================
echo    全部服务启动完成！
echo ============================================
echo.
echo  访问地址:     http://localhost:%NGINX_PORT%
echo  后端API:      http://localhost:%BACKEND_PORT%
echo  Druid监控:    http://localhost:%NGINX_PORT%/druid/
echo  Swagger文档:  http://localhost:%NGINX_PORT%/swagger-ui.html
echo.
echo  默认账号: admin / admin123
echo.
echo  MySQL端口:   %MYSQL_PORT%
echo  Redis端口:   %REDIS_PORT%
echo  日志目录:     %LOG_HOME%
echo.
pause
