@echo off
rem 后端启动脚本
rem 说明：jar 产物名带版本号（如 edop-admin-v10.jar），动态解析 target 下最新 jar，
rem       避免写死 jar 名导致 "Unable to access jarfile" 报错。
rem       java 解析顺序：本机 JDK17 完整路径 > JAVA_HOME > PATH 中的 java。

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

rem ---- 解析 java：优先本机 JDK17 完整路径，其次 JAVA_HOME，最后 PATH ----
set "JAVA_EXE="
if exist "E:\Java\jdk-17.0.4\bin\java.exe" set "JAVA_EXE=E:\Java\jdk-17.0.4\bin\java.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE (
    for /f "delims=" %%p in ('where java 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%p"
)
if not defined JAVA_EXE (
    echo [ERROR] 未找到可用的 java：本机 JDK17 路径、JAVA_HOME、PATH 均未命中
    pause
    exit /b 1
)
echo [INFO] 使用 java: %JAVA_EXE%

"%JAVA_EXE%" -Xmx512m -jar target\%JARFILE% 1> backend-run.log 2>&1
