@echo off
rem Stop the HTTPS gateway on port 3443
setlocal enabledelayedexpansion
set "PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":3443 " ^| findstr "LISTEN"') do set "PID=%%P"
if not defined PID (
    echo [INFO] no process listening on port 3443. Nothing to stop.
    pause
    exit /b 0
)
echo [INFO] stopping gateway PID=!PID! ...
taskkill /F /PID !PID! >nul 2>&1
timeout /t 1 /nobreak >nul
netstat -ano | findstr ":3443 " | findstr "LISTEN" >nul
if %errorlevel%==0 (
    echo [FAIL] port 3443 still listening. Try manual: taskkill /F /PID !PID!
) else (
    echo [OK] gateway stopped.
)
pause
