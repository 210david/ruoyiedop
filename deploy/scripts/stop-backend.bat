@echo off
title EDOP - Stop Backend
REM ============================================================
REM Stop Backend Spring Boot service
REM Path: D:\EDOP\scripts\stop-backend.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Stopping Backend...

REM Find and kill by port
set "found=0"
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% " ^| findstr LISTENING') do (
    echo %INFO% Terminating PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

REM Also find javaw running jar (backup method)
for /f "tokens=1" %%a in ('jps -l ^| findstr %JAR_NAME%') do (
    echo %INFO% Terminating Java PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    set "found=1"
)

if "%found%"=="0" (
    echo %WARN% Backend not running
) else (
    echo %OK% Backend stopped
)
