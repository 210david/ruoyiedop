@echo off
chcp 65001 >nul
title 企业数字化运营平台 - 部署状态检查

cd /d "%~dp0"

setlocal enabledelayedexpansion

echo ============================================================
echo   企业数字化运营平台 - 部署状态检查
echo ============================================================
echo.

REM ===== 检查目录结构 =====
echo [目录结构]
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
        echo   [OK]    %%d
    ) else (
        echo   [MISS]  %%d
    )
)
echo     !dirs_ok!/!dirs_checked! 目录存在
echo.

REM ===== 检查核心文件 =====
echo [核心文件]
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
        echo   [OK]    %%f
    ) else (
        echo   [MISS]  %%f
    )
)
echo     !files_ok!/!files_checked! 文件存在
echo.

REM ===== 检查运行时文件 =====
echo [运行时文件 (build.bat 编译后生成)]
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
        echo   [OK]    %%f
    ) else (
        echo   [MISS]  %%f
    )
)
if !runtime_checked! gtr 0 (
    echo     !runtime_ok!/!runtime_checked! 文件存在
)
echo.

REM ===== 检查端口 =====
echo [端口占用]

netstat -ano | findstr ":8005 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  Nginx 端口 8005
) else (
    echo   [FREE]  Nginx 端口 8005
)

netstat -ano | findstr ":8004 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  后端端口 8004
) else (
    echo   [FREE]  后端端口 8004
)

netstat -ano | findstr ":6379 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  Redis 端口 6379
) else (
    echo   [FREE]  Redis 端口 6379
)

netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if !errorlevel! equ 0 (
    echo   [USED]  MySQL 端口 3306
) else (
    echo   [FREE]  MySQL 端口 3306
)
echo.

REM ===== 汇总 =====
echo ============================================================
echo   汇总
echo ============================================================

if !dirs_ok! equ !dirs_checked! (
    if !files_ok! equ !files_checked! (
        echo   ✅ 主程序已就绪，只等 build.bat 编译前后端即可部署
    ) else (
        echo   ⚠️  部分核心文件缺失，请检查
    )
) else (
    echo   ❌ 目录结构不完整
)

echo.
echo   📋 使用方法:
if !dirs_ok! equ !dirs_checked! (
    echo       1. 运行 D:\EDOP\scripts\init-mysql.bat 初始化MySQL
    echo       2. 运行 D:\EDOP\scripts\start-mysql.bat 启动MySQL
    echo       3. 运行 D:\EDOP\scripts\set-mysql-password.bat 修改root密码
    echo       4. 运行 D:\EDOP\scripts\import-sql.bat 导入数据库
    echo       5. 修改 D:\EDOP\app\application-druid.yml 数据库密码
    echo       6. 运行 D:\EDOP\scripts\build.bat 编译前后端
    echo       7. 运行 D:\EDOP\scripts\start-all.bat 启动全部服务
) else (
    echo       1. 运行 D:\EDOP\scripts\init-dirs.bat
    echo       2. 参考 DOWNLOAD.md 下载并解压 JDK/MySQL/Redis/Nginx
    echo       3. 复制配置文件到对应目录
)
echo.
echo ============================================================
echo.
pause
