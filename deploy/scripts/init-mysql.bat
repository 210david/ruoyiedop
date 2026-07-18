@echo off
chcp 65001 >nul
title EDOP - 初始化MySQL便携版
REM ============================================================
REM 初始化 MySQL 便携版（首次使用必须运行一次）
REM 路径: D:\EDOP\scripts\init-mysql.bat
REM
REM 功能:
REM   1. 复制 my.ini 到 MySQL 目录
REM   2. 创建 data 和 logs 目录
REM   3. 初始化数据库（生成临时root密码）
REM   4. 安装为 Windows 服务（可选）
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql
set MYSQL_INI=%EDOP_HOME%\config\my.ini

echo ============================================
echo    初始化 MySQL 便携版
echo ============================================
echo.

REM 检查 MySQL 是否存在
if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
    echo [ERROR] 未找到 MySQL: %MYSQL_HOME%\bin\mysqld.exe
    echo [INFO]  请将 MySQL ZIP 解压到 %MYSQL_HOME% 目录
    echo [INFO]  确保目录结构为: %MYSQL_HOME%\bin\mysqld.exe
    pause
    exit /b 1
)

REM Step 1: 复制配置文件
echo [1/4] 复制配置文件...
if exist "%MYSQL_INI%" (
    copy /Y "%MYSQL_INI%" "%MYSQL_HOME%\my.ini" >nul
    echo [OK] my.ini 已复制到 %MYSQL_HOME%\my.ini
) else (
    echo [WARN] 未找到 %MYSQL_INI%，使用 MySQL 默认配置
)
echo.

REM Step 2: 创建目录
echo [2/4] 创建目录...
if not exist "%MYSQL_HOME%\data" mkdir "%MYSQL_HOME%\data"
if not exist "%MYSQL_HOME%\logs" mkdir "%MYSQL_HOME%\logs"
echo [OK] data 和 logs 目录已创建
echo.

REM Step 3: 检查是否已初始化
if exist "%MYSQL_HOME%\data\mysql" (
    echo [SKIP] MySQL 数据目录已存在，无需再次初始化
    echo.
    echo ============================================
    echo    MySQL 已初始化，无需重复操作！
    echo ============================================
    echo.
    echo  启动 MySQL:  D:\EDOP\scripts\start-mysql.bat
    echo  停止 MySQL:  D:\EDOP\scripts\stop-mysql.bat
    echo  修改密码:    D:\EDOP\scripts\set-mysql-password.bat
    echo.
    pause
    exit /b 0
)

REM Step 4: 初始化数据库
echo [3/4] 初始化数据库（请稍候）...
cd /d "%MYSQL_HOME%"
bin\mysqld.exe --initialize --console > "%MYSQL_HOME%\logs\init.log" 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 初始化失败！请查看日志:
    echo [INFO]  %MYSQL_HOME%\logs\init.log
    pause
    exit /b 1
)

echo [OK] 数据库初始化成功！
echo.

REM 提取临时密码
echo [INFO] 正在读取临时密码...
findstr "temporary password" "%MYSQL_HOME%\logs\init.log" > "%MYSQL_HOME%\logs\temp_password.txt" 2>nul
echo.
echo ============================================
echo    MySQL 初始化完成！
echo ============================================
echo.
echo  临时 root 密码已保存到:
echo    %MYSQL_HOME%\logs\temp_password.txt
echo.
echo  请执行以下步骤:
echo   1. 运行 start-mysql.bat 启动 MySQL
echo   2. 运行 set-mysql-password.bat 修改 root 密码
echo   3. 运行 import-sql.bat 导入平台数据
echo.
pause
