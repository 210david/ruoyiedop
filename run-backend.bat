@echo off
chcp 65001 >nul
rem 后端启动脚本
rem 说明：jar 产物名带版本号（如 edop-admin-v10.jar），动态解析 target 下最新 jar，
rem       避免写死 jar 名导致 "Unable to access jarfile" 报错。
rem       系统 PATH 无 java 时，回退使用本机 JDK17。

cd /d E:\AIcode\EDOP\RuoYi\RuoYi-Vue3\ruoyi-admin

rem ---- 动态解析 target 下的 fat jar（排除 .original，取最新一个） ----
set "JARFILE="
for /f "delims=" %%f in ('dir /b /o-d target\*.jar 2^>nul ^| findstr /v /i ".original$"') do (
    if not defined JARFILE set "JARFILE=%%f"
)
if not defined JARFILE (
    echo [ERROR] target 下未找到可执行 jar，请先执行 mvn package 打包
    pause
    exit /b 1
)
echo [INFO] 启动 jar: target\%JARFILE%

rem ---- 解析 java 命令：优先 PATH，其次本机 JDK17 ----
set "JAVA_EXE=java"
where java >nul 2>nul || set "JAVA_EXE=E:\Java\jdk-17.0.4\bin\java.exe"
if not exist "%JAVA_EXE%" if "%JAVA_EXE%"=="E:\Java\jdk-17.0.4\bin\java.exe" (
    echo [ERROR] 未找到可用的 java（PATH 无 java 且默认 JDK17 路径不存在）
    pause
    exit /b 1
)

"%JAVA_EXE%" -Xmx512m -jar target\%JARFILE% 1> backend-run.log 2>&1
