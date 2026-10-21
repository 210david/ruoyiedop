$body = '{"username":"admin","password":"admin123"}'
$resp = Invoke-RestMethod -Uri 'http://localhost:8081/login' -Method Post -ContentType 'application/json' -Body $body
$token = $resp.token
$headers = @{ Authorization = "Bearer $token" }

# 获取 DD20260819002 订单明细
$orderDetail = Invoke-RestMethod -Uri 'http://localhost:8081/mk/order/25' -Method Get -Headers $headers
Write-Host "=== DD20260819002 订单明细 ==="
foreach ($item in $orderDetail.data.itemList) {
    Write-Host "itemId=$($item.itemId), materialId=$($item.materialId), materialCode=$($item.materialCode), productName=$($item.productName), productSpec=$($item.productSpec), unit=$($item.unit)"
}
