@echo off
chcp 65001 >nul
title EDOP - 注册Windows服务
REM ============================================================
REM 将 MySQL、Redis、后端、Nginx 注册为 Windows 服务（开机自启）
REM 路径: D:\EDOP\scripts\install-services.bat
REM
REM 前置条件: 需要下载 WinSW-x64.exe
REM 下载地址: https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe
REM 下载后复制四份，分别重命名为:
REM   edop-mysql.exe, edop-backend.exe, edop-nginx.exe, edop-redis.exe
REM 放到 D:\EDOP\service\ 目录下
REM
REM 注意: MySQL 注册服务前必须先运行 init-mysql.bat 完成初始化
REM ============================================================

cd /d "%~dp0"
call env.bat

set SERVICE_DIR=%EDOP_HOME%\service

echo ============================================
echo    注册 Windows 服务（开机自启）
echo ============================================
echo.

REM 检查 WinSW 是否存在
if not exist "%SERVICE_DIR%\edop-backend.exe" (
    echo [ERROR] 未找到 WinSW: %SERVICE_DIR%\edop-backend.exe
    echo.
    echo [INFO] 请按以下步骤操作:
    echo   1. 下载 WinSW: https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe
    echo   2. 复制四份，分别重命名为:
    echo      edop-mysql.exe
    echo      edop-backend.exe
    echo      edop-nginx.exe
    echo      edop-redis.exe
    echo   3. 放到 %SERVICE_DIR% 目录
    echo.
    pause
    exit /b 1
)

REM 检查 MySQL 是否已初始化
if exist "%MYSQL_HOME%\bin\mysqld.exe" (
    if not exist "%MYSQL_HOME%\data\mysql" (
        echo [WARN] MySQL 尚未初始化，跳过 MySQL 服务注册
        echo [INFO]  请先运行 init-mysql.bat 完成初始化后再注册服务
        echo.
        goto :skip_mysql_service
    )
    REM 复制 WinSW exe 为 edop-mysql.exe
    if not exist "%SERVICE_DIR%\edop-mysql.exe" (
        copy /Y "%SERVICE_DIR%\edop-backend.exe" "%SERVICE_DIR%\edop-mysql.exe" >nul
    )
) else (
    echo [WARN] 未找到 MySQL，跳过 MySQL 服务注册
    echo.
    goto :skip_mysql_service
)

REM ===== 注册 MySQL 服务 =====
echo [1/4] 注册 MySQL 服务...
cd /d "%SERVICE_DIR%"
edop-mysql.exe install
if %errorlevel% neq 0 (
    echo [WARN] MySQL 服务注册可能失败，请检查上方输出
) else (
    echo [OK] MySQL 服务注册成功
    net start EDOP-MySQL >nul 2>&1
    echo [OK] MySQL 服务已启动
    timeout /t 3 /nobreak >nul
)
echo.

:skip_mysql_service

REM ===== 注册 Redis 服务 =====
echo [2/4] 注册 Redis 服务...
cd /d "%SERVICE_DIR%"
edop-redis.exe install
if %errorlevel% neq 0 (
    echo [WARN] Redis 服务注册可能失败，请检查上方输出
) else (
    echo [OK] Redis 服务注册成功
    net start EDOP-Redis >nul 2>&1
    echo [OK] Redis 服务已启动
)
echo.

REM ===== 注册后端服务 =====
echo [3/4] 注册后端服务...
edop-backend.exe install
if %errorlevel% neq 0 (
    echo [WARN] 后端服务注册可能失败，请检查上方输出
) else (
    echo [OK] 后端服务注册成功
    REM 等 Redis 完全启动后再启动后端
    timeout /t 3 /nobreak >nul
    net start EDOP-Backend >nul 2>&1
    echo [OK] 后端服务已启动
)
echo.

REM ===== 注册 Nginx 服务 =====
echo [4/4] 注册 Nginx 服务...
edop-nginx.exe install
if %errorlevel% neq 0 (
    echo [WARN] Nginx 服务注册可能失败，请检查上方输出
) else (
    echo [OK] Nginx 服务注册成功
    REM 等后端启动后再启动 Nginx
    timeout /t 5 /nobreak >nul
    net start EDOP-Nginx >nul 2>&1
    echo [OK] Nginx 服务已启动
)
echo.

echo ============================================
echo    服务注册完成！
echo ============================================
echo.
echo  服务名称:
echo    EDOP-MySQL   - MySQL 数据库服务
echo    EDOP-Redis   - Redis 缓存服务
echo    EDOP-Backend - 后端应用服务
echo    EDOP-Nginx   - Nginx 代理服务
echo.
echo  管理方式:
echo    服务管理器: services.msc
echo    命令行启动: net start EDOP-Backend
echo    命令行停止: net stop EDOP-Backend
echo.
echo  服务已设置为开机自启！
echo.
pause
