@echo off
rem Start EDOP MCP gateway in HTTPS mode (for WorkBuddy OAuth auto-authorization flow)
rem Port 3443, self-signed cert (10 years, localhost / 127.0.0.1)
cd /d "%~dp0.."

set "NODE_EXE=C:\Users\Laptop\.workbuddy\binaries\node\versions\22.22.2-6\node.exe"
if not exist "%NODE_EXE%" set "NODE_EXE=node"

set EDOP_GATEWAY_MODE=http
set EDOP_HTTPS=1
set EDOP_HTTP_PORT=3443

netstat -ano | findstr ":3443 " | findstr "LISTEN" >nul
if %errorlevel%==0 (
    echo [WARN] port 3443 already listening. Run stop-https.bat first if you want a restart.
    pause
    exit /b 0
)

start "edop-mcp-gateway-https" /min "%NODE_EXE%" gateway.mjs
timeout /t 3 /nobreak >nul

netstat -ano | findstr ":3443 " | findstr "LISTEN" >nul
if %errorlevel%==0 (
    echo [OK] HTTPS gateway started on port 3443
    echo      Connector URL: https://127.0.0.1:3443/mcp
) else (
    echo [FAIL] gateway did not start. Check certs\key.pem and certs\cert.pem exist.
)
pause
