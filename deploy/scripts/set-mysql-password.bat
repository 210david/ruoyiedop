@echo off
chcp 65001 >nul
title EDOP - 修改MySQL root密码
REM ============================================================
REM 修改 MySQL root 密码（首次初始化后必须运行）
REM 路径: D:\EDOP\scripts\set-mysql-password.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql

echo ============================================
echo    修改 MySQL root 密码
echo ============================================
echo.

if not exist "%MYSQL_HOME%\bin\mysql.exe" (
    echo [ERROR] 未找到 mysql 客户端: %MYSQL_HOME%\bin\mysql.exe
    pause
    exit /b 1
)

REM 检查 MySQL 是否在运行
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] MySQL 未在运行！请先运行 start-mysql.bat
    pause
    exit /b 1
)

cd /d "%MYSQL_HOME%"

REM 读取临时密码
set "temp_pwd="
for /f "tokens=*" %%a in ('findstr "temporary password" "%MYSQL_HOME%\logs\init.log" 2^>nul') do (
    set "temp_pwd=%%a"
)

if not "%temp_pwd%"=="" (
    echo [INFO] 检测到初始化时生成的临时密码
    echo [INFO] %temp_pwd%
    echo.
    set /p "new_pwd=请输入新的 root 密码: "
    if "%new_pwd%"=="" (
        echo [ERROR] 密码不能为空
        pause
        exit /b 1
    )
    echo.
    echo [INFO] 正在修改密码...

    REM 提取临时密码中的实际密码部分
    for /f "tokens=11" %%p in ("%temp_pwd%") do (
        set "actual_pwd=%%p"
    )

    bin\mysql.exe -u root -p"!actual_pwd!" --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '%new_pwd%';" 2>nul
    if !errorlevel! equ 0 (
        echo.
        echo [OK] root 密码修改成功！
        echo.
        echo  新密码: %new_pwd%
        echo.
        echo  请同步修改以下配置文件中的密码:
        echo    D:\EDOP\app\application-druid.yml  (password 字段)
        echo.
    ) else (
        echo [ERROR] 密码修改失败！
        echo [INFO]  请手动执行:
        echo   mysql -u root -p"临时密码" --connect-expired-password
        echo   ALTER USER 'root'@'localhost' IDENTIFIED BY '新密码';
    )
) else (
    echo [INFO] 未找到临时密码记录，请手动输入
    echo.
    set /p "old_pwd=请输入当前 root 密码: "
    set /p "new_pwd=请输入新的 root 密码: "

    bin\mysql.exe -u root -p"%old_pwd%" -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '%new_pwd%';" 2>nul
    if %errorlevel% equ 0 (
        echo.
        echo [OK] root 密码修改成功！
    ) else (
        echo [ERROR] 密码修改失败！请检查密码是否正确
    )
)

echo.
pause
