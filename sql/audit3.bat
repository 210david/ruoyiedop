@echo off
chcp 65001 >nul 2>&1
set TOKEN=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImxvZ2luX3VzZXJfa2V5IjoiZDY4MTY1MmEtMDk2Yy00YmM0LWIxODUtYzdlMGEwYzQ3MDJlIn0.OvVzXbcYg8oF4qrEAaOdPVpcYOzbXNcvXWx5hSZSzYF2d2EWfstB-MdYAKCcFumVRZPbRJUTXam4Y0gkZiya6g

echo Auditing SH20260730008 (receive_id=8)...
curl -s -X PUT "http://localhost:8081/pms/receive/audit/8?status=2&auditOpinion=approved" -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json"
echo.
