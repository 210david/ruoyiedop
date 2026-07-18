@echo off
chcp 65001 >nul
title EDOP - 启动后端服务
REM ============================================================
REM 启动后端 Spring Boot 服务
REM 路径: D:\EDOP\scripts\start-backend.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% 正在启动后端服务...

REM 检查 JDK 是否存在
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo %ERR% 未找到 JDK: %JAVA_HOME%\bin\java.exe
    echo %INFO% 请将 JDK 17 解压到 %JAVA_HOME% 目录
    echo %INFO% 或修改 env.bat 中的 JAVA_HOME 路径
    pause
    exit /b 1
)

REM 检查 JAR 包是否存在
if not exist "%APP_HOME%\%JAR_NAME%" (
    echo %ERR% 未找到应用包: %APP_HOME%\%JAR_NAME%
    echo %INFO% 请先执行 build.bat 编译打包，并将 JAR 包复制到 %APP_HOME% 目录
    pause
    exit /b 1
)

REM 创建日志目录
if not exist "%LOG_HOME%" mkdir "%LOG_HOME%"
if not exist "%UPLOAD_PATH%" mkdir "%UPLOAD_PATH%"

REM 检查端口是否已被占用
netstat -ano | findstr ":%BACKEND_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% 端口 %BACKEND_PORT% 已被占用，后端服务可能已在运行
    pause
    exit /b 0
)

REM 启动后端服务（后台运行，使用 javaw）
cd /d "%APP_HOME%"
echo %INFO% JAR 包: %APP_HOME%\%JAR_NAME%
echo %INFO% 端口: %BACKEND_PORT%
echo %INFO% 日志: %LOG_HOME%\ruoyi.log

start "EDOP-Backend" /min cmd /c "%JAVA_HOME%\bin\javaw %JVM_OPTS% -jar %JAR_NAME% > %LOG_HOME%\console.log 2>&1"

REM 等待服务启动
echo %INFO% 等待服务启动中，请稍候...
set "wait=0"
:wait_loop
timeout /t 3 /nobreak >nul
set /a wait+=3
netstat -ano | findstr ":%BACKEND_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% 后端服务启动成功！端口: %BACKEND_PORT%
    echo %INFO% 启动耗时约 %wait% 秒
    echo %INFO% 接口地址: http://localhost:%BACKEND_PORT%
    exit /b 0
)
if %wait% geq 60 (
    echo %ERR% 后端服务启动超时（60秒），请检查日志:
    echo %INFO% 控制台日志: %LOG_HOME%\console.log
    echo %INFO% 应用日志:   %LOG_HOME%\ruoyi.log
    pause
    exit /b 1
)
echo %INFO% 已等待 %wait% 秒...
goto wait_loop
