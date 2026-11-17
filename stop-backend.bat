@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 停止 EDOP 后端服务

set "PORT=82"
set "PID82="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%PORT% .*LISTENING"') do (
    if not defined PID82 set "PID82=%%p"
)

if not defined PID82 (
    echo [提示] 端口 %PORT% 当前没有进程监听，服务未运行。
    echo.
    pause
    exit /b 0
)

echo [信息] 正在结束占用端口 %PORT% 的进程 PID=!PID82! ...
taskkill /f /pid !PID82!
echo [完成] 已停止。可重新运行 run-backend.bat 启动。
echo.
pause
