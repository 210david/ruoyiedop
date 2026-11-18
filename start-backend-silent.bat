@echo off
rem Silent backend launcher (ASCII only, no pause) - used by scheduled task / direct run
cd /d E:\AIcode\EDOP\RuoYi\RuoYi-Vue3
java -jar ruoyi-admin\target\edop-admin-v13.jar --server.port=8081 >> logs\backend-start.out 2>&1
