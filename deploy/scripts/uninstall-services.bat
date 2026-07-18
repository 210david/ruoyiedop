@echo off
chcp 65001 >nul
title EDOP - 卸载Windows服务
REM ============================================================
REM 卸载所有 Windows 服务
REM 路径: D:\EDOP\scripts\uninstall-services.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set SERVICE_DIR=%EDOP_HOME%\service

echo ============================================
echo    卸载 Windows 服务
echo ============================================
echo.

REM ===== 停止并卸载 Nginx 服务 =====
echo [1/4] 卸载 Nginx 服务...
net stop EDOP-Nginx >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-nginx.exe uninstall >nul 2>&1
echo [OK] Nginx 服务已卸载
echo.

REM ===== 停止并卸载后端服务 =====
echo [2/4] 卸载后端服务...
net stop EDOP-Backend >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-backend.exe uninstall >nul 2>&1
echo [OK] 后端服务已卸载
echo.

REM ===== 停止并卸载 Redis 服务 =====
echo [3/4] 卸载 Redis 服务...
net stop EDOP-Redis >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-redis.exe uninstall >nul 2>&1
echo [OK] Redis 服务已卸载
echo.

REM ===== 停止并卸载 MySQL 服务 =====
echo [4/4] 卸载 MySQL 服务...
net stop EDOP-MySQL >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-mysql.exe uninstall >nul 2>&1
echo [OK] MySQL 服务已卸载
echo.

echo ============================================
echo    全部服务已卸载
echo ============================================
echo.
pause
