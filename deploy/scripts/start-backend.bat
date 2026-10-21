@echo off
title EDOP - Start Backend
REM ============================================================
REM Start Backend Spring Boot service
REM Path: D:\EDOP\scripts\start-backend.bat
REM ============================================================

cd /d "%~dp0"
call env.bat

echo %INFO% Starting Backend service...

REM Check JDK
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo %ERR% JDK not found: %JAVA_HOME%\bin\java.exe
    echo %INFO% Please extract JDK 17 to %JAVA_HOME%
    echo %INFO% Or edit JAVA_HOME in env.bat
    pause
    exit /b 1
)

REM Check JAR
if not exist "%APP_HOME%\%JAR_NAME%" (
    echo %ERR% JAR not found: %APP_HOME%\%JAR_NAME%
    echo %INFO% Please run build.bat first and copy JAR to %APP_HOME%
    pause
    exit /b 1
)

REM Create directories
if not exist "%LOG_HOME%" mkdir "%LOG_HOME%"
if not exist "%UPLOAD_PATH%" mkdir "%UPLOAD_PATH%"

REM Check if port already in use
netstat -ano | findstr ":%BACKEND_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %WARN% Port %BACKEND_PORT% already in use, backend may be running
    pause
    exit /b 0
)

REM Start backend (background, using javaw)
cd /d "%APP_HOME%"
echo %INFO% JAR:    %APP_HOME%\%JAR_NAME%
echo %INFO% Port:   %BACKEND_PORT%
echo %INFO% Log:    %LOG_HOME%\ruoyi.log

start "EDOP-Backend" /min cmd /c "%JAVA_HOME%\bin\javaw %JVM_OPTS% -jar %JAR_NAME% > %LOG_HOME%\console.log 2>&1"

REM Wait for startup
echo %INFO% Waiting for service to start...
set "wait=0"
:wait_loop
timeout /t 3 /nobreak >nul
set /a wait+=3
netstat -ano | findstr ":%BACKEND_PORT% " | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo %OK% Backend started! Port: %BACKEND_PORT%
    echo %INFO% Startup took ~%wait% seconds
    echo %INFO% API URL: http://localhost:%BACKEND_PORT%
    exit /b 0
)
if %wait% geq 60 (
    echo %ERR% Backend startup timeout (60s), check logs:
    echo %INFO% Console log: %LOG_HOME%\console.log
    echo %INFO% App log:     %LOG_HOME%\ruoyi.log
    pause
    exit /b 1
)
echo %INFO% Waiting... %wait% seconds
goto wait_loop
