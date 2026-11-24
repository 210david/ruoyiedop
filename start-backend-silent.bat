@echo off
rem Silent backend launcher (ASCII only, no pause) - used by scheduled task / direct run
rem Use script's own directory so this always starts THIS project's backend
cd /d "%~dp0"
if not exist logs mkdir logs
java -jar ruoyi-admin\target\edop-admin-v10.jar >> logs\backend-start.out 2>&1
