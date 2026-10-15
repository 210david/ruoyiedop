@echo off
rem Start backend Spring Boot (fat jar under target). Requires JDK 17.
rem Auto find jar: newest *.jar in target (exclude .original)
cd /d "%~dp0ruoyi-admin"

set "JARFILE="
for /f "delims=" %%f in ('dir /b /o-d target\*.jar 2^>nul ^| findstr /v /i ".original$"') do (
    if not defined JARFILE set "JARFILE=%%f"
)
if not defined JARFILE (
    echo [ERROR] No runnable jar found in target. Run: mvn package -pl ruoyi-admin -am
    pause
    exit /b 1
)
echo [INFO] jar: target\%JARFILE%

rem ---- find java: .\jdk > JAVA_HOME > common JDK17 paths > PATH ----
set "JAVA_EXE="
if exist "%~dp0jdk\bin\java.exe" set "JAVA_EXE=%~dp0jdk\bin\java.exe"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE for %%p in (
    "E:\Java\jdk-17.0.4"
    "C:\Program Files\Java\jdk-17"
    "C:\Program Files\Eclipse Adoptium\jdk-17"
    "C:\Program Files\Java\jdk-17.0.4"
) do if exist "%%~p\bin\java.exe" set "JAVA_EXE=%%~p\bin\java.exe"
if not defined JAVA_EXE (
    for /f "delims=" %%p in ('where java 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%p"
)
if not defined JAVA_EXE (
    echo [ERROR] Java not found. Install JDK 17 and set JAVA_HOME.
    pause
    exit /b 1
)
echo [INFO] java: %JAVA_EXE%

"%JAVA_EXE%" -Xmx512m -jar target\%JARFILE% --server.port=8082 1> backend-run.log 2>&1
