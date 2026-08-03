@echo off
chcp 65001 >nul 2>&1

echo Step 1: Login...
curl -s -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" > login_resp.json
for /f "delims=" %%i in (login_resp.json) do set LOGIN_RESP=%%i
echo Login response: %LOGIN_RESP%

echo.
echo Step 2: Audit SH20260730008 (receive_id=8)...
curl -s -X PUT "http://localhost:8081/pms/receive/audit/8?status=2&auditOpinion=approved" -H "Authorization: Bearer " -H "Content-Type: application/json"
echo.
