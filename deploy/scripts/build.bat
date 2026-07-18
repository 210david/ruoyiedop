@echo off
chcp 65001 >nul
title EDOP - 编译打包
REM ============================================================
REM 编译打包前后端项目
REM 路径: D:\EDOP\scripts\build.bat
REM 
REM 使用前请确保:
REM 1. 已安装 JDK 17+ 并配置 JAVA_HOME
REM 2. 已安装 Maven 3.6+ 并配置到 PATH
REM 3. 已安装 Node.js 18+ 并配置到 PATH
REM ============================================================

cd /d "%~dp0"
call env.bat

echo ============================================
echo    企业数字化运营平台 - 编译打包
echo ============================================
echo.

REM ===== 配置源代码路径 =====
REM 请修改为实际的源代码路径
set SOURCE_DIR=E:\AIcode\EDOP\RuoYi\RuoYi-Vue3

if not exist "%SOURCE_DIR%\pom.xml" (
    echo [ERROR] 未找到源代码: %SOURCE_DIR%\pom.xml
    echo [INFO]  请修改此脚本中的 SOURCE_DIR 变量
    pause
    exit /b 1
)

REM ===== Step 1: 后端编译打包 =====
echo [1/4] 开始编译后端 (Maven)...
echo.
cd /d "%SOURCE_DIR%"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] 后端编译失败！
    pause
    exit /b 1
)
echo.
echo [OK] 后端编译成功

REM ===== Step 2: 复制 JAR 包 =====
echo [2/4] 复制 JAR 包到部署目录...
if not exist "%APP_HOME%" mkdir "%APP_HOME%"
copy /Y "%SOURCE_DIR%\ruoyi-admin\target\ruoyi-admin.jar" "%APP_HOME%\%JAR_NAME%" >nul
echo [OK] JAR 包已复制到: %APP_HOME%\%JAR_NAME%

REM ===== Step 3: 复制配置文件 =====
echo [3/4] 复制配置文件...
set CONFIG_DIR=%~dp0..\config
if exist "%CONFIG_DIR%\application.yml" (
    copy /Y "%CONFIG_DIR%\application.yml" "%APP_HOME%\application.yml" >nul
    echo [OK] application.yml 已复制
)
if exist "%CONFIG_DIR%\application-druid.yml" (
    copy /Y "%CONFIG_DIR%\application-druid.yml" "%APP_HOME%\application-druid.yml" >nul
    echo [OK] application-druid.yml 已复制
)
if exist "%CONFIG_DIR%\nginx.conf" (
    copy /Y "%CONFIG_DIR%\nginx.conf" "%NGINX_HOME%\conf\nginx.conf" >nul
    echo [OK] nginx.conf 已复制
)
if exist "%CONFIG_DIR%\redis.windows.conf" (
    copy /Y "%CONFIG_DIR%\redis.windows.conf" "%REDIS_HOME%\redis.windows.conf" >nul
    echo [OK] redis.windows.conf 已复制
)

REM ===== Step 4: 前端编译打包 =====
echo [4/4] 开始编译前端 (npm)...
cd /d "%SOURCE_DIR%\ruoyi-ui"

if not exist "node_modules" (
    echo [INFO] 首次运行，正在安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo [ERROR] npm install 失败！
        pause
        exit /b 1
    )
)

call npm run build:prod
if %errorlevel% neq 0 (
    echo [ERROR] 前端编译失败！
    pause
    exit /b 1
)
echo [OK] 前端编译成功

REM 复制前端文件
if not exist "%FRONTEND_HOME%" mkdir "%FRONTEND_HOME%"
if exist "%FRONTEND_HOME%\dist" rmdir /S /Q "%FRONTEND_HOME%\dist"
xcopy /E /I /Y "%SOURCE_DIR%\ruoyi-ui\dist" "%FRONTEND_HOME%\dist" >nul
echo [OK] 前端文件已复制到: %FRONTEND_HOME%\dist

echo.
echo ============================================
echo    编译打包全部完成！
echo ============================================
echo.
echo  JAR 包:    %APP_HOME%\%JAR_NAME%
echo  前端文件:  %FRONTEND_HOME%\dist
echo  配置文件:  %APP_HOME%\application.yml
echo             %APP_HOME%\application-druid.yml
echo.
echo  请修改配置文件中的数据库和Redis连接信息
echo  然后运行 start-all.bat 启动服务
echo.
pause
