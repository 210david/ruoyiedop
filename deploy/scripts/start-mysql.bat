@echo off
chcp 65001 >nul
title EDOP - 启动MySQL服务
REM ============================================================
REM 启动 MySQL 服务（便携版，控制台模式）
REM 路径: D:\EDOP\scripts\start-mysql.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql

echo %INFO% 正在启动 MySQL 服务...

if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
    echo %ERR% 未找到 MySQL: %MYSQL_HOME%\bin\mysqld.exe
    pause
    exit /b 1
)

REM 检查端口是否已占用
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% 端口 3306 已被占用，MySQL 可能已在运行
    pause
    exit /b 0
)

REM 检查是否已初始化
if not exist "%MYSQL_HOME%\data\mysql" (
    echo %ERR% MySQL 尚未初始化！
    echo %INFO% 请先运行 init-mysql.bat 进行初始化
    pause
    exit /b 1
)

REM 创建日志目录
if not exist "%MYSQL_HOME%\logs" mkdir "%MYSQL_HOME%\logs"

REM 启动 MySQL（后台运行）
cd /d "%MYSQL_HOME%"
start "EDOP-MySQL" /min bin\mysqld.exe --defaults-file="%MYSQL_HOME%\my.ini" --console

REM 等待启动
echo %INFO% 等待 MySQL 启动中...
set "wait=0"
:wait_loop
timeout /t 2 /nobreak >nul
set /a wait+=2
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% MySQL 启动成功！端口: 3306
    echo %INFO% 启动耗时约 %wait% 秒
    echo %INFO% 日志文件: %MYSQL_HOME%\logs\error.log
    exit /b 0
)
if %wait% geq 30 (
    echo %ERR% MySQL 启动超时（30秒），请检查日志:
    echo %INFO% %MYSQL_HOME%\logs\error.log
    pause
    exit /b 1
)
echo %INFO% 已等待 %wait% 秒...
goto wait_loop
