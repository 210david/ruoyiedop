@echo off
chcp 65001 >nul 2>&1

echo === 登录获取Token ===
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json | ConvertFrom-Json).token"') do set TOKEN=%%i

echo === 查询培训计划类型字典 ===
curl -s "http://localhost:8081/system/dict/data/type/safety_training_plan_type" -H "Authorization: Bearer %TOKEN%"
echo.

del e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json 2>nul
