@echo off
chcp 65001 >nul 2>&1

echo === 1. 登录 ===
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json | ConvertFrom-Json).token"') do set TOKEN=%%i
echo Token: %TOKEN%
echo.

echo === 2. 新增培训计划 ===
curl -s -X POST http://localhost:8081/safety/training/plan -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"planName\":\"测试计划-状态流转验证\",\"planType\":\"1\",\"planYear\":\"2026\",\"targetPersons\":\"全员\",\"totalHours\":8.0,\"planStatus\":\"0\",\"status\":\"0\"}"
echo.
echo.

echo === 3. 查询培训计划列表 ===
curl -s "http://localhost:8081/safety/training/plan/list?pageNum=1&pageSize=3" -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo === 4. 开始执行（状态0→1） ===
curl -s -X PUT http://localhost:8081/safety/training/plan/start/1 -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo === 5. 完成培训计划（状态1→2） ===
curl -s -X PUT http://localhost:8081/safety/training/plan/complete/1 -H "Authorization: Bearer %TOKEN%"
echo.
echo.

echo === 6. 再次查询列表验证状态 ===
curl -s "http://localhost:8081/safety/training/plan/list?pageNum=1&pageSize=3" -H "Authorization: Bearer %TOKEN%"
echo.

del e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json 2>nul
