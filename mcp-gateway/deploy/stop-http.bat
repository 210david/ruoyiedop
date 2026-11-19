@echo off
REM ============================================================
REM  EDOP MCP Gateway - stop HTTP mode (Windows server)
REM  /nopause : do not pause at the end (for scheduled tasks / automation)
REM ============================================================
setlocal enabledelayedexpansion

set "NOPAUSE=0"
if /i "%~1"=="/nopause" set "NOPAUSE=1"

set FOUND=0
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTENING"') do (
  set PID=%%P
  set FOUND=1
)

if !FOUND!==0 (
  echo [INFO] No process listening on port 3000. Nothing to stop.
  if "%NOPAUSE%"=="1" exit /b 0
  pause
  exit /b 0
)

echo [INFO] Stopping gateway PID=!PID! ...
taskkill /F /PID !PID!
timeout /t 1 /nobreak >nul
netstat -ano | findstr ":3000 " | findstr "LISTENING" >nul 2>nul
if %errorlevel%==0 (
  echo [FAIL] Port 3000 still listening. Stop it manually.
  if "%NOPAUSE%"=="1" exit /b 1
  pause
  exit /b 1
) else (
  echo [OK] edop-mcp-gateway stopped.
  if "%NOPAUSE%"=="1" exit /b 0
  pause
  exit /b 0
)
