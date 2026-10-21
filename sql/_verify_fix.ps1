$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== Verifying Training Record Add Fix ==="
Write-Host ""

# Wait for DevTools to restart
Write-Host "Waiting 10 seconds for DevTools to restart..."
Start-Sleep -Seconds 10

# 1. Login
$loginBody = @{username="admin"; password="admin123"} | ConvertTo-Json
try {
    $loginResp = Invoke-RestMethod -Uri "http://localhost:8081/login" -Method Post -ContentType "application/json" -Body $loginBody
    $token = $loginResp.token
    Write-Host "Token obtained"
} catch {
    Write-Host "Login failed: $_"
    Write-Host "Application may be restarting. Waiting 15 more seconds..."
    Start-Sleep -Seconds 15
    $loginResp = Invoke-RestMethod -Uri "http://localhost:8081/login" -Method Post -ContentType "application/json" -Body $loginBody
    $token = $loginResp.token
    Write-Host "Token obtained on retry"
}

# 2. Test with yyyy-MM-dd format (the original failing case)
Write-Host ""
Write-Host "Test: Adding training record with date format yyyy-MM-dd"
$body = @{
    courseName = "fix-test"
    courseType = "1"
    trainingDate = "2026-08-11"
    trainingLocation = "test-location"
    hours = 2.00
    trainer = "test-trainer"
} | ConvertTo-Json

try {
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/safety/training/record" -Method Post -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $body
    Write-Host "Response: $($resp | ConvertTo-Json)"
} catch {
    $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
    $respBody = $reader.ReadToEnd()
    Write-Host "Failed: $respBody"
}

Write-Host ""
Write-Host "=== Done ==="
