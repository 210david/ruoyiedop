@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ===================================================
rem  RuoYi-Vue 一键部署脚本 (Windows)
rem  功能：编译项目、停止旧进程、部署新版本、启动服务
rem ===================================================

set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..
set ADMIN_DIR=%PROJECT_DIR%\ruoyi-admin
set TARGET_DIR=%ADMIN_DIR%\target
set JAR_FILE=%TARGET_DIR%\ruoyi-admin.jar
set SERVER_PORT=8080
set ACCESS_URL=http://localhost:%SERVER_PORT%
set JAVA_OPTS=-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m

echo.
echo ========================================
echo   RuoYi-Vue 一键部署系统
echo ========================================
echo [时间] %date% %time%
echo [项目路径] %PROJECT_DIR%
echo.

rem ========================================
rem  步骤1：编译项目
rem ========================================
echo --- 步骤1/4：编译后端项目 ---
echo [信息] 执行 mvn clean package -DskipTests ...

cd /d "%PROJECT_DIR%"

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [失败] 未检测到 Maven，请先安装 Maven 并添加到 PATH
    set /a CHECK_FAIL+=1
    pause
    exit /b 1
)

call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [失败] 项目编译失败，请查看上方错误信息
    pause
    exit /b 1
)
echo [成功] 项目编译完成

rem 检查 jar 包是否生成
if not exist "%JAR_FILE%" (
    echo [失败] 未找到编译产物: %JAR_FILE%
    echo       请检查 ruoyi-admin 模块的打包配置
    pause
    exit /b 1
)
echo [信息] 编译产物: %JAR_FILE%
echo.

rem ========================================
rem  步骤2：停止旧进程
rem ========================================
echo --- 步骤2/4：停止旧进程（端口 %SERVER_PORT%）---

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%SERVER_PORT% " ^| findstr "LISTENING"') do (
    echo [信息] 发现占用端口 %SERVER_PORT% 的进程 PID: %%a，正在终止...
    taskkill /pid %%a /f >nul 2>nul
    if !errorlevel! equ 0 (
        echo [成功] 已终止旧进程 PID: %%a
    ) else (
        echo [警告] 无法终止进程 PID: %%a，请手动处理
    )
)

rem 等待端口释放
timeout /t 2 /nobreak >nul
echo [信息] 端口状态检查完成
echo.

rem ========================================
rem  步骤3：部署到运行目录
rem ========================================
echo --- 步骤3/4：部署到运行目录 ---
set DEPLOY_DIR=%PROJECT_DIR%\deploy

if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

echo [信息] 复制 jar 包到部署目录: %DEPLOY_DIR%
copy /y "%JAR_FILE%" "%DEPLOY_DIR%\ruoyi-admin.jar" >nul
if %errorlevel% equ 0 (
    echo [成功] jar 包已复制到部署目录
) else (
    echo [失败] 复制失败
    pause
    exit /b 1
)
echo.

rem ========================================
rem  步骤4：启动服务
rem ========================================
echo --- 步骤4/4：启动服务 ---
echo [信息] JVM 参数: %JAVA_OPTS%
echo [信息] 访问地址: %ACCESS_URL%

cd /d "%DEPLOY_DIR%"
start "RuoYi-Server" /B java %JAVA_OPTS% -jar ruoyi-admin.jar > %DEPLOY_DIR%\server.log 2>&1

echo [信息] 等待服务启动...
timeout /t 8 /nobreak >nul

rem 检查服务是否启动成功
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%SERVER_PORT% " ^| findstr "LISTENING"') do (
    set PID=%%a
    goto :START_SUCCESS
)

:START_FAIL
echo [警告] 服务可能未成功启动，请查看日志: %DEPLOY_DIR%\server.log
goto :END

:START_SUCCESS
echo [成功] 服务已启动！PID: %PID%
echo.
echo ========================================
echo   部署完成！
echo ========================================
echo 访问地址：%ACCESS_URL%
echo 日志文件：%DEPLOY_DIR%\server.log
echo Druid 监控：%ACCESS_URL%/druid
echo Swagger 文档：%ACCESS_URL%/swagger-ui.html
echo.
echo 停止服务：taskkill /pid %PID% /f
echo ========================================

cd /d "%SCRIPT_DIR%"

:END
echo.
pause
endlocal
