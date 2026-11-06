@echo off
REM ============================================================
REM  EDOP MCP Gateway - HTTP mode starter (Windows server)
REM  Usage: double-click or run in customer server console
REM ============================================================
setlocal
cd /d "%~dp0.."

REM ---- locate node.exe (bundled WorkBuddy node or PATH) ----
set NODE_EXE=C:\Users\Laptop\.workbuddy\binaries\node\versions\22.22.2-6\node.exe
if not exist "%NODE_EXE%" (
  where node >nul 2>nul && set NODE_EXE=node
)

REM ---- already running? ----
netstat -ano | findstr ":3000 " | findstr "LISTEN" >nul 2>nul
if %errorlevel%==0 (
  echo [WARN] Port 3000 already in LISTEN. Gateway may already be running.
  echo Use stop-http.bat first if you want to restart.
  pause
  exit /b 1
)

REM ---- start minimized in a separate window so it survives this script ----
set EDOP_GATEWAY_MODE=http
start "edop-mcp-gateway" /min "%NODE_EXE%" gateway.mjs

REM ---- verify listening ----
timeout /t 3 /nobreak >nul
netstat -ano | findstr ":3000 " | findstr "LISTEN" >nul 2>nul
if %errorlevel%==0 (
  echo [OK] edop-mcp-gateway started on port 3000. Health: http://127.0.0.1:3000/health
) else (
  echo [FAIL] Gateway did not start. Check audit.log and config in profiles\
)
pause
