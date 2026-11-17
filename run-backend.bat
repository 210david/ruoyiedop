@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title EDOP 后端服务（端口 82）

rem ============================================================
rem  EDOP 后端启动脚本
rem  要点：
rem   1) 强制加载 druid profile（application-druid.yml 里是数据源配置，
rem      缺了它必然报 druidProperties / Could not resolve placeholder）
rem   2) 清掉 SERVER__PORT / SERVER__HOST 环境变量，否则端口会被改掉
rem   3) 端口已占用时直接提示，不再重复启动导致报错
rem ============================================================

set "SERVER__PORT="
set "SERVER__HOST="
set "SPRING_PROFILES_ACTIVE=druid"
set "PORT=82"

cd /d "%~dp0"
set "APPDIR=%~dp0ruoyi-admin"
set "LOGFILE=%APPDIR%\backend-run.log"

rem ---------- 1. 查找 jar ----------
set "JARFILE="
for /f "delims=" %%f in ('dir /b /o-d "%APPDIR%\target\*.jar" 2^>nul ^| findstr /v /i ".original$"') do (
    if not defined JARFILE set "JARFILE=%%f"
)
if not defined JARFILE (
    echo [错误] 未找到可执行 jar，请先在项目根目录打包：
    echo        mvn -DskipTests -pl ruoyi-admin -am package
    echo.
    pause
    exit /b 1
)

rem ---------- 2. 端口占用检查 ----------
set "PID82="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%PORT% .*LISTENING"') do (
    if not defined PID82 set "PID82=%%p"
)
if defined PID82 (
    echo [提示] 端口 %PORT% 已被进程 PID=!PID82! 占用，服务应该已经在运行。
    echo        前端地址： http://localhost:8080
    echo        如需重启： 先运行 stop-backend.bat，再运行本脚本
    echo.
    pause
    exit /b 0
)

rem ---------- 3. 查找 java ----------
set "JAVA_EXE="
if exist "%~dp0jdk\bin\java.exe" set "JAVA_EXE=%~dp0jdk\bin\java.exe"
if not defined JAVA_EXE if exist "D:\jdk\jdk-17.0.0.1\bin\java.exe" set "JAVA_EXE=D:\jdk\jdk-17.0.0.1\bin\java.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for %%p in (
    "C:\Program Files\Java\jdk-17"
    "E:\Java\jdk-17.0.4"
) do if exist "%%~p\bin\java.exe" set "JAVA_EXE=%%~p\bin\java.exe"
if not defined JAVA_EXE (
    for /f "delims=" %%p in ('where java 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%p"
)
if not defined JAVA_EXE (
    echo [错误] 没有找到 Java，请安装 JDK 17 并设置 JAVA_HOME
    pause
    exit /b 1
)

echo ============================================================
echo   jar  : %APPDIR%\target\%JARFILE%
echo   java : %JAVA_EXE%
echo   端口 : %PORT%    profile: druid
echo   日志 : %LOGFILE%
echo ============================================================
echo   启动中，约 20~30 秒后可用，请勿关闭本窗口
echo.

rem ---------- 4. 启动 ----------
"%JAVA_EXE%" -Xms512m -Xmx1024m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8 ^
  -jar "%APPDIR%\target\%JARFILE%" --spring.profiles.active=druid --server.port=%PORT% ^
  1>"%LOGFILE%" 2>&1

echo.
echo [错误] 后端进程已退出。最后 30 行日志（完整日志见上面路径）：
echo ------------------------------------------------------------
powershell -NoProfile -Command "Get-Content -LiteralPath '%LOGFILE%' -Tail 30 -Encoding UTF8"
echo ------------------------------------------------------------
echo 常见原因：
echo   1) 数据库没启动或账号密码不对（看 Caused by: Access denied / Communications link failure）
echo   2) Redis 没启动
echo   3) 端口被其他程序占用
pause
