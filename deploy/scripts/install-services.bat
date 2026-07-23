@echo off
title EDOP - Install Windows Services
REM ============================================================
REM Register MySQL, Redis, Backend, Nginx as Windows services
REM Path: D:\EDOP\scripts\install-services.bat
REM
REM Prerequisite: WinSW-x64.exe required
REM Download: https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe
REM Copy 4 copies and rename to:
REM   edop-mysql.exe, edop-backend.exe, edop-nginx.exe, edop-redis.exe
REM Place in D:\EDOP\service\
REM
REM Note: MySQL must be initialized (init-mysql.bat) before registering service
REM ============================================================

cd /d "%~dp0"
call env.bat

set SERVICE_DIR=%EDOP_HOME%\service

echo ============================================
echo    Install Windows Services (Auto-start)
echo ============================================
echo.

REM Check WinSW
if not exist "%SERVICE_DIR%\edop-backend.exe" (
    echo [ERROR] WinSW not found: %SERVICE_DIR%\edop-backend.exe
    echo.
    echo [INFO] Steps:
    echo   1. Download WinSW: https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe
    echo   2. Copy 4 copies, rename to:
    echo      edop-mysql.exe
    echo      edop-backend.exe
    echo      edop-nginx.exe
    echo      edop-redis.exe
    echo   3. Place in %SERVICE_DIR%
    echo.
    pause
    exit /b 1
)

REM Check MySQL initialized
if exist "%MYSQL_HOME%\bin\mysqld.exe" (
    if not exist "%MYSQL_HOME%\data\mysql" (
        echo [WARN] MySQL not initialized, skipping MySQL service
        echo [INFO]  Run init-mysql.bat first, then register service
        echo.
        goto :skip_mysql_service
    )
    if not exist "%SERVICE_DIR%\edop-mysql.exe" (
        copy /Y "%SERVICE_DIR%\edop-backend.exe" "%SERVICE_DIR%\edop-mysql.exe" >nul
    )
) else (
    echo [WARN] MySQL not found, skipping MySQL service
    echo.
    goto :skip_mysql_service
)

REM ===== Register MySQL service =====
echo [1/4] Registering MySQL service...
cd /d "%SERVICE_DIR%"
edop-mysql.exe install
if %errorlevel% neq 0 (
    echo [WARN] MySQL service registration may have failed, check output above
) else (
    echo [OK] MySQL service registered
    net start EDOP-MySQL >nul 2>&1
    echo [OK] MySQL service started
    timeout /t 3 /nobreak >nul
)
echo.

:skip_mysql_service

REM ===== Register Redis service =====
echo [2/4] Registering Redis service...
cd /d "%SERVICE_DIR%"
edop-redis.exe install
if %errorlevel% neq 0 (
    echo [WARN] Redis service registration may have failed, check output above
) else (
    echo [OK] Redis service registered
    net start EDOP-Redis >nul 2>&1
    echo [OK] Redis service started
)
echo.

REM ===== Register Backend service =====
echo [3/4] Registering Backend service...
edop-backend.exe install
if %errorlevel% neq 0 (
    echo [WARN] Backend service registration may have failed, check output above
) else (
    echo [OK] Backend service registered
    REM Wait for Redis to fully start before starting backend
    timeout /t 3 /nobreak >nul
    net start EDOP-Backend >nul 2>&1
    echo [OK] Backend service started
)
echo.

REM ===== Register Nginx service =====
echo [4/4] Registering Nginx service...
edop-nginx.exe install
if %errorlevel% neq 0 (
    echo [WARN] Nginx service registration may have failed, check output above
) else (
    echo [OK] Nginx service registered
    REM Wait for backend to start before starting Nginx
    timeout /t 5 /nobreak >nul
    net start EDOP-Nginx >nul 2>&1
    echo [OK] Nginx service started
)
echo.

echo ============================================
echo    Service Registration Complete!
echo ============================================
echo.
echo  Service names:
echo    EDOP-MySQL   - MySQL database service
echo    EDOP-Redis   - Redis cache service
echo    EDOP-Backend - Backend application service
echo    EDOP-Nginx   - Nginx proxy service
echo.
echo  Management:
echo    Service manager: services.msc
echo    CLI start: net start EDOP-Backend
echo    CLI stop:  net stop EDOP-Backend
echo.
echo  Services set to auto-start on boot!
echo.
pause
