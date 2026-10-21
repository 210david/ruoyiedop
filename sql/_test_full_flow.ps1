$body = '{"username":"admin","password":"admin123"}'
$resp = Invoke-RestMethod -Uri 'http://localhost:8081/login' -Method Post -ContentType 'application/json' -Body $body
$token = $resp.token
$headers = @{ Authorization = "Bearer $token" }

# 1. 查询已审核订单列表
$orderResp = Invoke-RestMethod -Uri 'http://localhost:8081/mk/order/list?pageNum=1&pageSize=10&orderStatus=2' -Method Get -Headers $headers
Write-Host "=== 已审核订单列表 ==="
foreach ($o in $orderResp.rows) {
    Write-Host "Order: $($o.orderNo), Status: $($o.orderStatus), ID: $($o.orderId)"
}

# 2. 获取 DD20260819002 的明细
$orderDetail = Invoke-RestMethod -Uri 'http://localhost:8081/mk/order/25' -Method Get -Headers $headers
Write-Host "`n=== DD20260819002 明细 ==="
foreach ($item in $orderDetail.data.itemList) {
    Write-Host "Item: materialId=$($item.materialId), materialCode=$($item.materialCode), productName=$($item.productName)"
}

# 3. 创建需求 - 从订单明细带出产品信息
$demandBody = @{
    demandType = '0'
    sourceOrderNo = 'DD20260819002'
    productId = $orderDetail.data.itemList[0].materialId
    productCode = $orderDetail.data.itemList[0].materialCode
    productName = $orderDetail.data.itemList[0].productName
    specModel = $orderDetail.data.itemList[0].productSpec
    unit = $orderDetail.data.itemList[0].unit
    demandQty = $orderDetail.data.itemList[0].quantity
    requireDate = '2026-09-15'
    priority = '1'
    customerId = $orderDetail.data.customerId
    customerName = $orderDetail.data.customerName
    status = '0'
} | ConvertTo-Json -Depth 3

$createResp = Invoke-RestMethod -Uri 'http://localhost:8081/mms/demand' -Method Post -ContentType 'application/json' -Body $demandBody -Headers $headers
Write-Host "`n=== 创建需求结果 ==="
Write-Host "Code: $($createResp.code)"
Write-Host "Msg: $($createResp.msg)"
if ($createResp.data) {
    Write-Host "DemandId: $($createResp.data.demandId)"
    Write-Host "DemandNo: $($createResp.data.demandNo)"
    Write-Host "ProductId: $($createResp.data.productId)"
    Write-Host "ProductCode: $($createResp.data.productCode)"
}

# 4. 查询需求列表验证
$demandResp = Invoke-RestMethod -Uri 'http://localhost:8081/mms/demand/list?pageNum=1&pageSize=10' -Method Get -Headers $headers
Write-Host "`n=== 需求列表 ==="
foreach ($d in $demandResp.rows) {
    Write-Host "Demand: $($d.demandNo), ProductId=$($d.productId), ProductCode=$($d.productCode), ProductName=$($d.productName)"
}
