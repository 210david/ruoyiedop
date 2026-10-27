@echo off
title EDOP - Build
REM ============================================================
REM Build and package frontend + backend
REM Path: D:\EDOP\scripts\build.bat
REM
REM Prerequisites:
REM 1. JDK 17+ installed and JAVA_HOME configured
REM 2. Maven 3.6+ in PATH
REM 3. Node.js 18+ in PATH
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    EDOP - Build and Package
echo ============================================
echo.

REM ===== Source code path =====
set SOURCE_DIR=E:\AIcode\EDOP\RuoYi\RuoYi-Vue3

if not exist "%SOURCE_DIR%\pom.xml" (
    echo [ERROR] Source not found: %SOURCE_DIR%\pom.xml
    echo [INFO]  Please update SOURCE_DIR in this script
    pause
    exit /b 1
)

REM ===== Step 1: Backend build =====
echo [1/4] Building backend (Maven)...
echo.
cd /d "%SOURCE_DIR%"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Backend build failed!
    pause
    exit /b 1
)
echo.
echo [OK] Backend build success

REM ===== Step 2: Copy JAR =====
echo [2/4] Copying JAR to deploy directory...
if not exist "%APP_HOME%" mkdir "%APP_HOME%"
copy /Y "%SOURCE_DIR%\ruoyi-admin\target\ruoyi-admin.jar" "%APP_HOME%\%JAR_NAME%" >nul
echo [OK] JAR copied to: %APP_HOME%\%JAR_NAME%

REM ===== Step 3: Copy config files =====
echo [3/4] Copying config files...
set CONFIG_DIR=%~dp0..\config
if exist "%CONFIG_DIR%\application.yml" (
    copy /Y "%CONFIG_DIR%\application.yml" "%APP_HOME%\application.yml" >nul
    echo [OK] application.yml copied
)
if exist "%CONFIG_DIR%\application-druid.yml" (
    copy /Y "%CONFIG_DIR%\application-druid.yml" "%APP_HOME%\application-druid.yml" >nul
    echo [OK] application-druid.yml copied
)
if exist "%CONFIG_DIR%\nginx.conf" (
    copy /Y "%CONFIG_DIR%\nginx.conf" "%NGINX_HOME%\conf\nginx.conf" >nul
    echo [OK] nginx.conf copied
)
if exist "%CONFIG_DIR%\redis.windows.conf" (
    copy /Y "%CONFIG_DIR%\redis.windows.conf" "%REDIS_HOME%\redis.windows.conf" >nul
    echo [OK] redis.windows.conf copied
)

REM ===== Step 4: Frontend build =====
echo [4/4] Building frontend (npm)...
cd /d "%SOURCE_DIR%\ruoyi-ui"

if not exist "node_modules" (
    echo [INFO] First run, installing dependencies...
    call npm install
    if %errorlevel% neq 0 (
        echo [ERROR] npm install failed!
        pause
        exit /b 1
    )
)

call npm run build:prod
if %errorlevel% neq 0 (
    echo [ERROR] Frontend build failed!
    pause
    exit /b 1
)
echo [OK] Frontend build success

REM Copy frontend files
if not exist "%FRONTEND_HOME%" mkdir "%FRONTEND_HOME%"
if exist "%FRONTEND_HOME%\dist" rmdir /S /Q "%FRONTEND_HOME%\dist"
xcopy /E /I /Y "%SOURCE_DIR%\ruoyi-ui\dist" "%FRONTEND_HOME%\dist" >nul
echo [OK] Frontend files copied to: %FRONTEND_HOME%\dist

echo.
echo ============================================
echo    Build Complete!
echo ============================================
echo.
echo  JAR:        %APP_HOME%\%JAR_NAME%
echo  Frontend:   %FRONTEND_HOME%\dist
echo  Config:     %APP_HOME%\application.yml
echo              %APP_HOME%\application-druid.yml
echo.
echo  Update DB/Redis password in config files
echo  Then run start-all.bat to start services
echo.
pause
