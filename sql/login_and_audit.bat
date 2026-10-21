@echo off
chcp 65001 >nul 2>&1
echo Logging in...
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > login_response.json
type login_response.json
echo.
