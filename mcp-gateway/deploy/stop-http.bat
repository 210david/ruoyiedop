@echo off
REM ============================================================
REM  EDOP MCP Gateway - stop HTTP mode (Windows server)
REM ============================================================
setlocal enabledelayedexpansion

set FOUND=0
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":3000 " ^| findstr "LISTEN"') do (
  set PID=%%P
  set FOUND=1
)

if !FOUND!==0 (
  echo [INFO] No process listening on port 3000. Nothing to stop.
  pause
  exit /b 0
)

echo [INFO] Stopping gateway PID=!PID! ...
taskkill /F /PID !PID!
timeout /t 1 /nobreak >nul
netstat -ano | findstr ":3000 " | findstr "LISTEN" >nul 2>nul
if %errorlevel%==0 (
  echo [FAIL] Port 3000 still listening. Stop it manually.
) else (
  echo [OK] edop-mcp-gateway stopped.
)
pause
