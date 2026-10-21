@echo off
chcp 65001 >nul 2>&1
set TOKEN=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImxvZ2luX3VzZXJfa2V5IjoiNDA0OGIwMTctNmQzOC00YzNkLTk5MjUtNzAyODIwZDAyNzVjIn0.rhuycb5EiiGPpOMcvO1_MT4isBZfxusRHN7cdxIW7A3wz1K_PYNaaaDvltjG-OHMWO7OWScEaIb8X1gwTN7_Mg

echo Auditing SH20260730008 (receive_id=8)...
curl -s -X PUT "http://localhost:8081/pms/receive/audit/8?status=2&auditOpinion=approved" -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json"
echo.
