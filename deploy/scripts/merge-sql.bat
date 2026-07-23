@echo off
title EDOP - Merge SQL Files
REM ============================================================
REM Merge all SQL into EDOP_ALL.sql (calls PowerShell version)
REM Path: D:\EDOP\scripts\merge-sql.bat
REM
REM NOTE: This wrapper calls merge-sql.ps1 which handles UTF-8
REM encoding correctly. The old type-based merge caused garbled
REM Chinese characters on Chinese Windows.
REM ============================================================

cd /d "%~dp0"

echo ============================================
echo    EDOP - Merge SQL Files
echo ============================================
echo.
echo Running merge-sql.ps1 (PowerShell, UTF-8 safe)...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0merge-sql.ps1"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Merge failed!
    pause
    exit /b 1
)

echo.
pause
