@echo off
chcp 65001 >nul 2>&1

echo === 1. 登录 ===
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json
type e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json
echo.
echo.

echo === 2. 用jq或sed提取Token ===
:: 使用PowerShell提取token
for /f "delims=" %%i in ('powershell -Command "(Get-Content e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json | ConvertFrom-Json).token"') do set TOKEN=%%i
echo Token: %TOKEN%
echo.

echo === 3. 新增课程 ===
curl -s -X POST http://localhost:8081/safety/training/course -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "{\"courseName\":\"测试课程-自动编码验证\",\"courseType\":\"1\",\"courseForm\":\"1\",\"hours\":4.5,\"instructor\":\"张老师\",\"status\":\"0\"}"
echo.
echo.

echo === 4. 查询课程列表 ===
curl -s "http://localhost:8081/safety/training/course/list?pageNum=1&pageSize=5" -H "Authorization: Bearer %TOKEN%"
echo.

del e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\login_resp.json 2>nul
