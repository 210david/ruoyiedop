@echo off
chcp 65001 >nul
title EDOP Backend - 8081
cd /d E:\AIcode\EDOP\RuoYi\RuoYi-Vue3

echo ============================================
echo  EDOP Backend starting on port 8081
echo  Jar : ruoyi-admin/target/edop-admin-v13.jar
echo  Log : logs\boot8081.log
echo ============================================
echo.

rem stop old instance on 8081 if any
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":8081 " ^| findstr "LISTENING"') do taskkill /F /PID %%P >nul 2>&1

start "EDOP-Backend-8081" /min java -jar ruoyi-admin\target\edop-admin-v13.jar --server.port=8081
echo Backend is starting in a minimized window (takes about 30-90s).
echo Frontend: http://localhost:81/  (admin / admin123)
echo.
pause
