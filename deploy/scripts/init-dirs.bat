@echo off
title EDOP - Init Directories
REM ============================================================
REM Initialize deployment directory structure
REM Path: D:\EDOP\scripts\init-dirs.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    Initialize Deploy Directory Structure
echo ============================================
echo.

echo [INFO] Root: %EDOP_HOME%

REM Create all directories
set dirs="%EDOP_HOME%" "%JAVA_HOME%" "%REDIS_HOME%" "%REDIS_HOME%\data" "%REDIS_HOME%\logs" "%NGINX_HOME%" "%NGINX_HOME%\logs" "%NGINX_HOME%\conf" "%APP_HOME%" "%APP_HOME%\logs" "%FRONTEND_HOME%" "%UPLOAD_PATH%" "%EDOP_HOME%\service" "%EDOP_HOME%\scripts" "%EDOP_HOME%\config"

for %%d in (%dirs%) do (
    if not exist %%d (
        mkdir %%d
        echo [OK] Created: %%~d
    ) else (
        echo [SKIP] Exists: %%~d
    )
)

echo.
echo ============================================
echo    Directory Init Complete!
echo ============================================
echo.
echo  Structure:
echo   D:\EDOP\
echo   +-- jdk17\           (JDK 17 portable)
echo   +-- mysql\           (MySQL portable)
echo   +-- redis\           (Redis)
echo   +-- nginx\           (Nginx)
echo   +-- app\             (ruoyi-admin.jar)
echo   ^|   +-- logs\
echo   +-- frontend\dist\   (frontend build files)
echo   +-- uploadPath\      (file upload directory)
echo   +-- service\         (WinSW service config)
echo   +-- scripts\         (management scripts)
echo   +-- config\          (config templates)
echo.
pause
