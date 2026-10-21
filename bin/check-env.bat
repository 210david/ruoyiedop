@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ===================================================
rem  RuoYi-Vue 系统启动自检脚本 (Windows)
rem  功能：检查Java/MySQL/Redis/端口/配置文件
rem ===================================================

set PASS=√
set FAIL=×
set CHECK_PASS=0
set CHECK_FAIL=0

echo.
echo ========================================
echo   RuoYi-Vue 系统环境自检
echo ========================================
echo [时间] %date% %time%
echo.

rem --- 0. 获取项目根目录 ---
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..
echo [项目路径] %PROJECT_DIR%
echo.

rem ========================================
rem  1. 检查 Java 版本
rem ========================================
echo --- 检查 Java 版本 ---
call :CHECK_JAVA

rem ========================================
rem  2. 检查 MySQL 连接
rem ========================================
echo.
echo --- 检查 MySQL 连接 ---
call :CHECK_MYSQL

rem ========================================
rem  3. 检查 Redis 连接
rem ========================================
echo.
echo --- 检查 Redis 连接 ---
call :CHECK_REDIS

rem ========================================
rem  4. 检查端口占用
rem ========================================
echo.
echo --- 检查端口占用 ---
call :CHECK_PORT 8080
call :CHECK_PORT 80

rem ========================================
rem  5. 检查配置文件
rem ========================================
echo.
echo --- 检查配置文件 ---
call :CHECK_CONFIG

rem ========================================
rem  检查结果汇总
rem ========================================
echo.
echo ========================================
echo   检查结果汇总
echo ========================================
echo 通过项：%CHECK_PASS%    失败项：%CHECK_FAIL%
if %CHECK_FAIL% equ 0 (
    echo 结论：所有检查项通过，系统环境就绪 ^(^>_^<^)
) else (
    echo 结论：存在 %CHECK_FAIL% 项问题，请根据上述提示修复后重试！
)
echo ========================================
echo.

pause
exit /b

rem ==================== 子过程 ====================

:CHECK_JAVA
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [%FAIL%] 未检测到 Java 环境，请安装 JDK 8+ 或 17
    set /a CHECK_FAIL+=1
    exit /b
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%i
set JAVA_VER=!JAVA_VER:"=!

echo [%PASS%] Java 已安装，版本: !JAVA_VER!

rem 检查是否为 JDK 8 或 17+
echo !JAVA_VER! | findstr /r "^1\.8\." >nul
if !errorlevel! equ 0 (
    echo [%PASS%] Java 版本符合要求（JDK 8）
    set /a CHECK_PASS+=1
    exit /b
)

echo !JAVA_VER! | findstr /r "^17\." >nul
if !errorlevel! equ 0 (
    echo [%PASS%] Java 版本符合要求（JDK 17）
    set /a CHECK_PASS+=1
    exit /b
)

rem 检查更高版本（18+）
for /f "tokens=1 delims=." %%i in ("!JAVA_VER!") do set JAVA_MAJOR=%%i
if !JAVA_MAJOR! geq 17 (
    echo [%PASS%] Java 版本符合要求（JDK !JAVA_MAJOR!）
    set /a CHECK_PASS+=1
) else (
    echo [%FAIL%] Java 版本不符合要求（当前: !JAVA_VER!，要求: JDK 8 或 17+）
    set /a CHECK_FAIL+=1
)
exit /b

:CHECK_MYSQL
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo [%FAIL%] 未检测到 mysql 客户端，请确认 MySQL 已安装并添加到 PATH
    set /a CHECK_FAIL+=1
    exit /b
)

mysql -hlocalhost -P3306 -uroot -p123456 -e "SELECT 1" >nul 2>nul
if %errorlevel% equ 0 (
    echo [%PASS%] MySQL 连接正常（localhost:3306）
    set /a CHECK_PASS+=1
) else (
    echo [%FAIL%] MySQL 连接失败，请检查：
    echo        - MySQL 服务是否启动（net start mysql 或 services.msc）
    echo        - 用户名/密码是否正确（root/123456）
    set /a CHECK_FAIL+=1
)
exit /b

:CHECK_REDIS
where redis-cli >nul 2>nul
if %errorlevel% neq 0 (
    echo [!] 未检测到 redis-cli，尝试通过 PowerShell 测试端口...

    powershell -Command "$tcp = New-Object Net.Sockets.TcpClient; try { $tcp.Connect('127.0.0.1', 6379); $tcp.Close(); Write-Host 'OK' } catch { Write-Host 'FAIL' }" > "%TEMP%\redis_check.tmp" 2>nul
    set /p REDIS_CHECK=<"%TEMP%\redis_check.tmp"
    del "%TEMP%\redis_check.tmp" 2>nul

    if "!REDIS_CHECK!"=="OK" (
        echo [%PASS%] Redis 端口 6379 可连接
        set /a CHECK_PASS+=1
    ) else (
        echo [!] Redis 端口 6379 不可达，建议安装 redis-cli 后进行完整检查
        set /a CHECK_FAIL+=1
    )
    exit /b
)

redis-cli -h localhost -p 6379 PING >nul 2>nul
if %errorlevel% equ 0 (
    echo [%PASS%] Redis 连接正常（localhost:6379）
    set /a CHECK_PASS+=1
) else (
    echo [%FAIL%] Redis 连接失败，请检查 Redis 服务是否启动
    set /a CHECK_FAIL+=1
)
exit /b

:CHECK_PORT
set PORT=%1
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul 2>nul
if %errorlevel% equ 0 (
    echo [!] 端口 %PORT% 已被占用：
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
        for /f "tokens=1" %%b in ('tasklist /fi "pid eq %%a" ^| findstr "%%a"') do (
            echo     PID: %%a  进程: %%b
        )
    )
) else (
    echo [%PASS%] 端口 %PORT% 空闲
    set /a CHECK_PASS+=1
)
exit /b

:CHECK_CONFIG
set CFG_FILE=%PROJECT_DIR%\ruoyi-admin\src\main\resources\application.yml
set DRUID_FILE=%PROJECT_DIR%\ruoyi-admin\src\main\resources\application-druid.yml
set ALL_OK=1

if exist "%CFG_FILE%" (
    echo [%PASS%] application.yml 存在
) else (
    echo [%FAIL%] application.yml 不存在
    set ALL_OK=0
    set /a CHECK_FAIL+=1
)

if exist "%DRUID_FILE%" (
    echo [%PASS%] application-druid.yml 存在
) else (
    echo [%FAIL%] application-druid.yml 不存在
    set ALL_OK=0
    set /a CHECK_FAIL+=1
)

if %ALL_OK% equ 1 (
    set /a CHECK_PASS+=1
)
exit /b
