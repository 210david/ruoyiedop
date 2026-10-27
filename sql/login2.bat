@echo off
chcp 65001 >nul 2>&1

echo Step 1: Login...
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > login_resp.json
type login_resp.json
echo.
