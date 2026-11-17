@echo off
chcp 65001 >nul 2>nul
title EDOP YLSP 一键导入
cd /d "%~dp0"

where powershell >nul 2>nul
if errorlevel 1 (
    echo [ERROR] PowerShell not found on this machine.
    pause
    exit /b 1
)

rem  %* 透传参数，例如：一键导入数据库.bat -Db ruoyi_ylsp -SkipBackup
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0import_all.ps1" %*

if errorlevel 1 (
    echo.
    echo [ERROR] import failed, see the import_log_*.txt in this folder.
    pause
)
