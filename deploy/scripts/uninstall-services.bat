@echo off
title EDOP - Uninstall Windows Services
REM ============================================================
REM Uninstall all Windows services
REM Path: D:\EDOP\scripts\uninstall-services.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set SERVICE_DIR=%EDOP_HOME%\service

echo ============================================
echo    Uninstall Windows Services
echo ============================================
echo.

REM ===== Stop and uninstall Nginx service =====
echo [1/4] Uninstalling Nginx service...
net stop EDOP-Nginx >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-nginx.exe uninstall >nul 2>&1
echo [OK] Nginx service uninstalled
echo.

REM ===== Stop and uninstall Backend service =====
echo [2/4] Uninstalling Backend service...
net stop EDOP-Backend >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-backend.exe uninstall >nul 2>&1
echo [OK] Backend service uninstalled
echo.

REM ===== Stop and uninstall Redis service =====
echo [3/4] Uninstalling Redis service...
net stop EDOP-Redis >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-redis.exe uninstall >nul 2>&1
echo [OK] Redis service uninstalled
echo.

REM ===== Stop and uninstall MySQL service =====
echo [4/4] Uninstalling MySQL service...
net stop EDOP-MySQL >nul 2>&1
cd /d "%SERVICE_DIR%"
edop-mysql.exe uninstall >nul 2>&1
echo [OK] MySQL service uninstalled
echo.

echo ============================================
echo    All Services Uninstalled
echo ============================================
echo.
pause
