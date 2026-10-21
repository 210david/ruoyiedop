@echo off
setlocal enabledelayedexpansion
title EDOP - Change MySQL Password
REM ============================================================
REM Change MySQL root password (run after first init)
REM Path: D:\EDOP\scripts\set-mysql-password.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

set MYSQL_HOME=%EDOP_HOME%\mysql

echo ============================================
echo    Change MySQL Root Password
echo ============================================
echo.

if not exist "%MYSQL_HOME%\bin\mysql.exe" (
    echo [ERROR] mysql client not found: %MYSQL_HOME%\bin\mysql.exe
    pause
    exit /b 1
)

REM Check MySQL running
netstat -ano | findstr ":3306 " | findstr LISTENING >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] MySQL not running! Please run start-mysql.bat first
    pause
    exit /b 1
)

cd /d "%MYSQL_HOME%"

REM Read temp password
set "temp_pwd="
for /f "tokens=*" %%a in ('findstr "temporary password" "%MYSQL_HOME%\logs\init.log" 2^>nul') do (
    set "temp_pwd=%%a"
)

if not "!temp_pwd!"=="" (
    echo [INFO] Found temporary password from init log
    echo [INFO] !temp_pwd!
    echo.

    REM Extract actual password (last token)
    for /f "tokens=11" %%p in ("!temp_pwd!") do (
        set "actual_pwd=%%p"
    )
    echo [INFO] Temp password: !actual_pwd!
    echo.
    set /p "new_pwd=Enter new root password: "
    if "!new_pwd!"=="" (
        echo [ERROR] Password cannot be empty
        pause
        exit /b 1
    )
    echo.
    echo [INFO] Changing password...

    bin\mysql.exe -u root -p"!actual_pwd!" --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '!new_pwd!';" 2>nul
    if !errorlevel! equ 0 (
        echo.
        echo [OK] Root password changed successfully!
        echo.
        echo  New password: !new_pwd!
        echo.
        echo  Please update password in config file:
        echo    D:\EDOP\app\application-druid.yml  (password field)
        echo.
    ) else (
        echo [ERROR] Password change failed!
        echo [INFO]  Try manually:
        echo   mysql -u root -p"temp_pwd" --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_pwd';"
    )
) else (
    echo [INFO] No temp password found, please enter manually
    echo.
    set /p "old_pwd=Enter current root password: "
    set /p "new_pwd=Enter new root password: "

    bin\mysql.exe -u root -p"!old_pwd!" -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '!new_pwd!';" 2>nul
    if !errorlevel! equ 0 (
        echo.
        echo [OK] Root password changed successfully!
    ) else (
        echo [ERROR] Password change failed! Check if current password is correct
    )
)

echo.
pause
