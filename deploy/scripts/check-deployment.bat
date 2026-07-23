@echo off
title EDOP - Deployment Check

cd /d "%~dp0"

setlocal enabledelayedexpansion

echo ============================================================
echo   EDOP - Deployment Status Check
echo ============================================================
echo.

REM ===== Check directory structure =====
echo [Directories]
set dirs_checked=0
set dirs_ok=0

for %%d in (
    "D:\EDOP"
    "D:\EDOP\jdk17\bin"
    "D:\EDOP\mysql\bin"
    "D:\EDOP\redis"
    "D:\EDOP\nginx\logs"
    "D:\EDOP\app"
    "D:\EDOP\frontend\dist"
    "D:\EDOP\uploadPath"
    "D:\EDOP\scripts"
    "D:\EDOP\config"
    "D:\EDOP\service"
) do (
    set /a dirs_checked+=1
    if exist %%d (
        set /a dirs_ok+=1
        echo   [OK]    %%~d
    ) else (
        echo   [MISS]  %%~d
    )
)
echo     !dirs_ok!/!dirs_checked! directories exist
echo.

REM ===== Check core files =====
echo [Core Files]
set files_checked=0
set files_ok=0

for %%f in (
    "D:\EDOP\jdk17\bin\java.exe"
    "D:\EDOP\jdk17\bin\javaw.exe"
    "D:\EDOP\mysql\bin\mysqld.exe"
    "D:\EDOP\mysql\bin\mysql.exe"
    "D:\EDOP\redis\redis-server.exe"
    "D:\EDOP\redis\redis-cli.exe"
    "D:\EDOP\nginx\nginx.exe"
    "D:\EDOP\config\application.yml"
    "D:\EDOP\config\my.ini"
    "D:\EDOP\config\nginx.conf"
    "D:\EDOP\config\sql\EDOP_ALL.sql"
    "D:\EDOP\scripts\env.bat"
    "D:\EDOP\scripts\init-mysql.bat"
    "D:\EDOP\scripts\start-all.bat"
    "D:\EDOP\scripts\import-sql.bat"
) do (
    set /a files_checked+=1
    if exist %%f (
        set /a files_ok+=1
        echo   [OK]    %%~f
    ) else (
        echo   [MISS]  %%~f
    )
)
echo     !files_ok!/!files_checked! files exist
echo.

REM ===== Check runtime files =====
echo [Runtime Files (after build.bat)]
set runtime_checked=0
set runtime_ok=0

for %%f in (
    "D:\EDOP\app\ruoyi-admin.jar"
    "D:\EDOP\app\application.yml"
    "D:\EDOP\app\application-druid.yml"
    "D:\EDOP\frontend\dist\index.html"
) do (
    set /a runtime_checked+=1
    if exist %%f (
        set /a runtime_ok+=1
        echo   [OK]    %%~f
    ) else (
        echo   [MISS]  %%~f
    )
)
if !runtime_checked! gtr 0 (
    echo     !runtime_ok!/!runtime_checked! files exist
)
echo.

REM ===== Check ports =====
echo [Port Status]

netstat -ano | findstr ":8005 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  Nginx  port 8005
) else (
    echo   [FREE]  Nginx  port 8005
)

netstat -ano | findstr ":8004 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  Backend port 8004
) else (
    echo   [FREE]  Backend port 8004
)

netstat -ano | findstr ":6379 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  Redis  port 6379
) else (
    echo   [FREE]  Redis  port 6379
)

netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  MySQL  port 3306
) else (
    echo   [FREE]  MySQL  port 3306
)
echo.

REM ===== Summary =====
echo ============================================================
echo   Summary
echo ============================================================

if !dirs_ok! equ !dirs_checked! (
    if !files_ok! equ !files_checked! (
        echo   [OK] Core ready, just need build.bat to compile
    ) else (
        echo   [WARN] Some core files missing, please check
    )
) else (
    echo   [ERROR] Directory structure incomplete
)

echo.
echo   Usage:
if !dirs_ok! equ !dirs_checked! (
    echo       1. Run D:\EDOP\scripts\init-mysql.bat to init MySQL
    echo       2. Run D:\EDOP\scripts\start-mysql.bat to start MySQL
    echo       3. Run D:\EDOP\scripts\set-mysql-password.bat to change root password
    echo       4. Run D:\EDOP\scripts\import-sql.bat to import database
    echo       5. Edit D:\EDOP\app\application-druid.yml for DB password
    echo       6. Run D:\EDOP\scripts\build.bat to build frontend/backend
    echo       7. Run D:\EDOP\scripts\start-all.bat to start all services
) else (
    echo       1. Run D:\EDOP\scripts\init-dirs.bat
    echo       2. Download and extract JDK/MySQL/Redis/Nginx
    echo       3. Copy config files to corresponding directories
)
echo.
echo ============================================================
echo.
pause
