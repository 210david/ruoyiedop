@echo off
REM ============================================================
REM  EDOP MCP Gateway - HTTP mode starter (Windows server)
REM  Usage: double-click or run in customer server console
REM
REM  Node lookup order: bundled runtime\node.exe -> PATH node
REM  Never hardcode a developer machine path (absent on customer servers).
REM  /nopause : do not pause at the end (for scheduled tasks / automation)
REM
REM  Profile resolution: EDOP_PROFILE env > the single profiles/*.json
REM  (Auto-detect avoids the "no env var -> fallback standard -> no such
REM   file" trap on branch repos that only carry a customer profile.)
REM  Port: read from the active profile (httpPort), not hardcoded, so the
REM  script can never drift from the gateway's real listening port.
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0.."

set "NOPAUSE=0"
if /i "%~1"=="/nopause" set "NOPAUSE=1"

REM ---- locate node.exe: bundled runtime first, then PATH ----
set "NODE_EXE=%~dp0..\runtime\node.exe"
if not exist "%NODE_EXE%" set "NODE_EXE="
if not defined NODE_EXE (
  where node >nul 2>nul && set "NODE_EXE=node"
)
if not defined NODE_EXE (
  echo [FAIL] node.exe not found: neither runtime\node.exe nor node in PATH.
  echo        Put Node.js 18+ node.exe into the runtime\ directory.
  goto :fail
)

REM ---- resolve profile when EDOP_PROFILE is not set ----
if not defined EDOP_PROFILE (
  set "FOUND_CNT=0"
  set "FOUND_NAME="
  for %%F in ("profiles\*.json") do (
    echo %%~nxF | findstr /i /c:".example.json" >nul || (
      set /a FOUND_CNT+=1
      set "FOUND_NAME=%%~nF"
    )
  )
  if "!FOUND_CNT!"=="1" set "EDOP_PROFILE=!FOUND_NAME!"
  if "!FOUND_CNT!"=="0" (
    echo [FAIL] no profile found in profiles\ (only *.example.json present^)
    echo        copy profiles\standard.example.json profiles\standard.json and fill it in
    goto :fail
  )
  if !FOUND_CNT! GTR 1 (
    echo [FAIL] multiple profiles in profiles\, set EDOP_PROFILE to pick one:
    for %%F in ("profiles\*.json") do (
      echo %%~nxF | findstr /i /c:".example.json" >nul || echo        %%~nF
    )
    goto :fail
  )
)

REM ---- preflight: profile / apiKey / manifest / deps (node-based, UTF-8 safe) ----
set EDOP_GATEWAY_MODE=http
"%NODE_EXE%" "%~dp0preflight.mjs"
if errorlevel 1 (
  echo [FAIL] preflight check failed, gateway NOT started.
  goto :fail
)

REM ---- effective port from profile (falls back to 3000) ----
set "GW_PORT=3000"
for /f "usebackq delims=" %%P in (`""%NODE_EXE%" "%~dp0preflight.mjs" --print-port"`) do set "GW_PORT=%%P"

REM ---- already running? ----
netstat -ano | findstr ":%GW_PORT% " | findstr "LISTENING" >nul 2>nul
if %errorlevel%==0 (
  echo [WARN] Port %GW_PORT% already in LISTENING. Gateway may already be running.
  echo Use stop-http.bat first if you want to restart.
  goto :fail
)

REM ---- start minimized in a separate window so it survives this script ----
start "edop-mcp-gateway" /min "%NODE_EXE%" gateway.mjs

REM ---- verify listening ----
ping -n 4 127.0.0.1 >nul
netstat -ano | findstr ":%GW_PORT% " | findstr "LISTENING" >nul 2>nul
if %errorlevel%==0 (
  echo [OK] edop-mcp-gateway started on port %GW_PORT% ^(profile=%EDOP_PROFILE%^).
  echo      Health: http://127.0.0.1:%GW_PORT%/health
  echo      MCP   : http://SERVER_IP:%GW_PORT%/mcp   ^(replace SERVER_IP^)
  if "%NOPAUSE%"=="1" exit /b 0
  pause
  exit /b 0
) else (
  echo [FAIL] Gateway did not start. Check audit.log and config in profiles\
  goto :fail
)

:fail
if "%NOPAUSE%"=="1" exit /b 1
pause
exit /b 1
