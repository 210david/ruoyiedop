@echo off
chcp 65001 >nul
title EDOP - Download WinSW
REM ============================================================
REM Download WinSW and create three service exe files
REM ============================================================

set SERVICE_DIR=%~dp0..\service

echo ============================================
echo    Download WinSW and Setup Service Files
echo ============================================
echo.

REM Use PowerShell to download
powershell -ExecutionPolicy Bypass -NoProfile -Command "$ProgressPreference='SilentlyContinue'; $url='https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-net4-x64.exe'; $out='%SERVICE_DIR%\WinSW-net4-x64.exe'; Write-Host '[1/5] Downloading WinSW...'; Invoke-WebRequest -Uri $url -OutFile $out -UseBasicParsing; Write-Host '[OK] Download complete'; $sz=(Get-Item $out).Length; Write-Host ('File size: ' + [math]::Round($sz/1KB,1) + ' KB')"

if not exist "%SERVICE_DIR%\WinSW-net4-x64.exe" (
    echo.
    echo [ERROR] Download failed!
    echo Please download manually from:
    echo   https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-net4-x64.exe
    echo Save to: %SERVICE_DIR%\WinSW-net4-x64.exe
    echo Then run this script again.
    pause
    exit /b 1
)

echo.
echo [2/5] Copying to edop-backend.exe ...
copy /Y "%SERVICE_DIR%\WinSW-net4-x64.exe" "%SERVICE_DIR%\edop-backend.exe" >nul
echo [OK] edop-backend.exe created

echo [3/5] Copying to edop-nginx.exe ...
copy /Y "%SERVICE_DIR%\WinSW-net4-x64.exe" "%SERVICE_DIR%\edop-nginx.exe" >nul
echo [OK] edop-nginx.exe created

echo [4/5] Copying to edop-redis.exe ...
copy /Y "%SERVICE_DIR%\WinSW-net4-x64.exe" "%SERVICE_DIR%\edop-redis.exe" >nul
echo [OK] edop-redis.exe created

echo [5/5] Cleaning up ...
del /Q "%SERVICE_DIR%\WinSW-net4-x64.exe" >nul 2>&1
echo [OK] Cleanup done

echo.
echo ============================================
echo    WinSW Setup Complete!
echo ============================================
echo.
echo  service directory contents:
dir /B "%SERVICE_DIR%"
echo.
echo  Now you can run install-services.bat to register Windows services.
echo.
pause
