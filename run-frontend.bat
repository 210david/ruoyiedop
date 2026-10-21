@echo off
rem Start frontend Vite dev server (port 8085). Requires Node.js 18.19+ (Node 20 LTS recommended).
cd /d "%~dp0ruoyi-ui"

set "NPM_CMD="
where npm >nul 2>nul && set "NPM_CMD=npm"
if not defined NPM_CMD for %%p in (
    "E:\DevTools\node\npm.cmd"
    "C:\Program Files\nodejs\npm.cmd"
) do if exist "%%~p" set "NPM_CMD=%%~p"
if not defined NPM_CMD (
    echo [ERROR] npm not found. Install Node.js 18+ first.
    pause
    exit /b 1
)
echo [INFO] npm: %NPM_CMD%

call %NPM_CMD% run dev 1> vite-run.log 2>&1
