@echo off
rem Stop the HTTPS gateway on port 3443
rem /nopause : do not pause at the end (for scheduled tasks / automation)
setlocal enabledelayedexpansion

set "NOPAUSE=0"
if /i "%~1"=="/nopause" set "NOPAUSE=1"

set "PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":3443 " ^| findstr "LISTENING"') do set "PID=%%P"
if not defined PID (
    echo [INFO] no process listening on port 3443. Nothing to stop.
    if "%NOPAUSE%"=="1" exit /b 0
    pause
    exit /b 0
)
echo [INFO] stopping gateway PID=!PID! ...
taskkill /F /PID !PID! >nul 2>&1
timeout /t 1 /nobreak >nul
netstat -ano | findstr ":3443 " | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo [FAIL] port 3443 still listening. Try manual: taskkill /F /PID !PID!
    if "%NOPAUSE%"=="1" exit /b 1
    pause
    exit /b 1
) else (
    echo [OK] gateway stopped.
    if "%NOPAUSE%"=="1" exit /b 0
    pause
    exit /b 0
)
