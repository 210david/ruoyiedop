@echo off
rem Silent backend launcher (ASCII only, no pause) - used by scheduled task / direct run
rem EDOP YLSP project only. Backend port 82 (same as run-backend.bat / stop-backend.bat).
cd /d "%~dp0"

rem Clear env overrides so port 82 takes effect
set "SERVER__PORT="
set "SERVER__HOST="
set "PORT=82"

rem Pick newest executable jar in ruoyi-admin\target (skip .original)
set "JARFILE="
for /f "delims=" %%f in ('dir /b /o-d "%~dp0ruoyi-admin\target\*.jar" 2^>nul ^| findstr /v /i ".original$"') do (
    if not defined JARFILE set "JARFILE=%%f"
)
if not defined JARFILE (
    echo [ERROR] No jar found in %~dp0ruoyi-admin\target. Run: mvn -DskipTests -pl ruoyi-admin -am package
    exit /b 1
)

rem Port check: skip start if already listening
set "PID82="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%PORT% .*LISTENING"') do (
    if not defined PID82 set "PID82=%%p"
)
if defined PID82 (
    echo [INFO] Port %PORT% already in use by PID=%PID82%, backend probably running.
    exit /b 0
)

java -Xms512m -Xmx1024m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8 ^
  -jar "%~dp0ruoyi-admin\target\%JARFILE%" --spring.profiles.active=druid --server.port=%PORT% ^
  >> logs\backend-start.out 2>&1
