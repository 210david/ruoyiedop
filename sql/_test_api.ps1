$body = '{"username":"admin","password":"admin123"}'
$resp = Invoke-RestMethod -Uri 'http://localhost:8081/login' -Method Post -ContentType 'application/json' -Body $body
$token = $resp.token
Write-Host "Token: $token"

# Test demand list
$headers = @{ Authorization = "Bearer $token" }
$demandResp = Invoke-RestMethod -Uri 'http://localhost:8081/mms/demand/list?pageNum=1&pageSize=10' -Method Get -Headers $headers
Write-Host "Demand list code: $($demandResp.code)"
Write-Host "Demand rows count: $($demandResp.rows.Count)"
if ($demandResp.rows.Count -gt 0) {
    $row = $demandResp.rows[0]
    Write-Host "First row: demandNo=$($row.demandNo), productId=$($row.productId), productCode=$($row.productCode), status=$($row.status)"
}
