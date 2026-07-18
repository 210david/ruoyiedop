@echo off
chcp 65001 >nul
title EDOP - 初始化部署目录
REM ============================================================
REM 初始化部署目录结构
REM 路径: D:\EDOP\scripts\init-dirs.bat
REM 说明: 首次部署时运行，创建所有必要的目录
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    初始化部署目录结构
echo ============================================
echo.

echo [INFO] 根目录: %EDOP_HOME%

REM 创建所有目录
set dirs="%EDOP_HOME%" "%JAVA_HOME%" "%REDIS_HOME%" "%REDIS_HOME%\data" "%REDIS_HOME%\logs" "%NGINX_HOME%" "%NGINX_HOME%\logs" "%NGINX_HOME%\conf" "%APP_HOME%" "%APP_HOME%\logs" "%FRONTEND_HOME%" "%UPLOAD_PATH%" "%EDOP_HOME%\service" "%EDOP_HOME%\scripts" "%EDOP_HOME%\config" "%EDOP_HOME%\config\sql"

for %%d in (%dirs%) do (
    if not exist %%d (
        mkdir %%d
        echo [OK] 创建目录: %%d
    ) else (
        echo [SKIP] 目录已存在: %%d
    )
)

echo.
echo ============================================
echo    目录初始化完成！
echo ============================================
echo.
echo  目录结构:
echo   D:\EDOP\
echo   ├── jdk17\           (放入 JDK 17 便携版)
echo   ├── redis\           (放入 Redis)
echo   ├── nginx\           (放入 Nginx)
echo   ├── app\             (放入 ruoyi-admin.jar)
echo   │   └── logs\
echo   ├── frontend\dist\   (放入前端打包文件)
echo   ├── uploadPath\      (文件上传目录)
echo   ├── service\         (WinSW服务配置)
echo   ├── scripts\         (管理脚本)
echo   └── config\          (配置文件模板)
echo.
pause
