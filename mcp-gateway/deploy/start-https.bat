@echo off
rem Start EDOP MCP gateway in HTTPS mode (for WorkBuddy OAuth auto-authorization flow)
rem Port 3443, self-signed cert (10 years, localhost / 127.0.0.1)
rem Node lookup order: bundled runtime\node.exe -> PATH node (never hardcode a dev machine path)
rem /nopause : do not pause at the end (for scheduled tasks / automation)
cd /d "%~dp0.."

set "NOPAUSE=0"
if /i "%~1"=="/nopause" set "NOPAUSE=1"

set "NODE_EXE=%~dp0..\runtime\node.exe"
if not exist "%NODE_EXE%" set "NODE_EXE="
if not defined NODE_EXE (
  where node >nul 2>nul && set "NODE_EXE=node"
)
if not defined NODE_EXE (
  echo [FAIL] node.exe not found: neither runtime\node.exe nor node in PATH.
  goto :fail
)

set EDOP_GATEWAY_MODE=http
set EDOP_HTTPS=1
set EDOP_HTTP_PORT=3443

rem ---- preflight (profile / apiKey / manifest / deps / certs) ----
"%NODE_EXE%" "%~dp0preflight.mjs"
if errorlevel 1 (
  echo [FAIL] preflight check failed, gateway NOT started.
  goto :fail
)
if not exist "certs\key.pem" (
  echo [FAIL] certs\key.pem not found. HTTPS mode needs certs\key.pem and certs\cert.pem.
  goto :fail
)
if not exist "certs\cert.pem" (
  echo [FAIL] certs\cert.pem not found. HTTPS mode needs certs\key.pem and certs\cert.pem.
  goto :fail
)

netstat -ano | findstr ":3443 " | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo [WARN] port 3443 already listening. Run stop-https.bat first if you want a restart.
    if "%NOPAUSE%"=="1" exit /b 0
    pause
    exit /b 0
)

start "edop-mcp-gateway-https" /min "%NODE_EXE%" gateway.mjs
ping -n 4 127.0.0.1 >nul

netstat -ano | findstr ":3443 " | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo [OK] HTTPS gateway started on port 3443
    echo      Connector URL: https://127.0.0.1:3443/mcp
    if "%NOPAUSE%"=="1" exit /b 0
    pause
    exit /b 0
) else (
    echo [FAIL] gateway did not start. Check certs\key.pem and certs\cert.pem exist.
    goto :fail
)

:fail
if "%NOPAUSE%"=="1" exit /b 1
pause
exit /b 1
