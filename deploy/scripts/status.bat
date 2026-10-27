@echo off
title EDOP - Service Status
REM ============================================================
REM Show status of all services
REM Path: D:\EDOP\scripts\status.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    EDOP - Service Status
echo ============================================
echo.

REM MySQL status
echo [MySQL] Port %MYSQL_PORT%
netstat -ano | findstr ":%MYSQL_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   Status: RUNNING
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%MYSQL_PORT% " ^| findstr LISTENING') do (
        echo   PID:    %%a
    )
) else (
    echo   Status: STOPPED
)
echo.

REM Redis status
echo [Redis] Port %REDIS_PORT%
netstat -ano | findstr ":%REDIS_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   Status: RUNNING
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%REDIS_PORT% " ^| findstr LISTENING') do (
        echo   PID:    %%a
    )
) else (
    echo   Status: STOPPED
)
echo.

REM Backend status
echo [Backend] Port %BACKEND_PORT%
netstat -ano | findstr ":%BACKEND_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   Status: RUNNING
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr LISTENING') do (
        echo   PID:    %%a
    )
) else (
    echo   Status: STOPPED
)
echo.

REM Nginx status
echo [Nginx] Port %NGINX_PORT%
netstat -ano | findstr ":%NGINX_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo   Status: RUNNING
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%NGINX_PORT% " ^| findstr LISTENING') do (
        echo   PID:    %%a
    )
) else (
    echo   Status: STOPPED
)
echo.

echo ============================================
pause
