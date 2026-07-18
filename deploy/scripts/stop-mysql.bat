@echo off
chcp 65001 >nul
title EDOP - 停止MySQL服务
REM ============================================================
REM 停止 MySQL 服务
REM 路径: D:\EDOP\scripts\stop-mysql.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql

echo %INFO% 正在停止 MySQL 服务...

if not exist "%MYSQL_HOME%\bin\mysqladmin.exe" (
    echo %ERR% 未找到 mysqladmin: %MYSQL_HOME%\bin\mysqladmin.exe
    pause
    exit /b 1
)

REM 方式1: 通过 mysqladmin 优雅关闭
cd /d "%MYSQL_HOME%"
bin\mysqladmin.exe -u root shutdown >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% MySQL 已优雅关闭
    timeout /t 2 /nobreak >nul
    exit /b 0
)

REM 方式2: 如果有密码
set /p "mysql_pwd=请输入 root 密码: "
bin\mysqladmin.exe -u root -p"%mysql_pwd%" shutdown >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% MySQL 已优雅关闭
    exit /b 0
)

REM 方式3: 通过端口查找进程并终止
set "found=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3306 " ^| findstr LISTENING') do (
    echo %INFO% 正在终止进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

if "%found%"=="0" (
    echo %WARN% MySQL 未在运行
) else (
    echo %OK% MySQL 服务已停止
)
