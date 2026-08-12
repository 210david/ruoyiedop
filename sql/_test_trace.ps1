$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== 1. Login ===" -ForegroundColor Green
$loginBody = @{ username = "admin"; password = "admin123"; code = ""; uuid = "" } | ConvertTo-Json
try {
    $loginResp = Invoke-RestMethod -Uri "http://localhost:8081/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResp.token
    Write-Host "Login OK, token: $($token.Substring(0, 20))..."
} catch {
    Write-Host "Login FAILED: $_" -ForegroundColor Red
    exit 1
}
$headers = @{ Authorization = "Bearer $token" }

Write-Host "`n=== 2. List Genealogy (empty) ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
Write-Host "Total: $($resp.total), Rows: $($resp.rows.Count)"

Write-Host "`n=== 3. Add Genealogy (normal: RM001->SF001) ===" -ForegroundColor Green
$addBody = @{
    parentBatchNo = "RM-20260808-001"; childBatchNo = "SF-20260808-001"; workOrderNo = "WO-001"
    opName = "Mixing"; materialCode = "MAT-RM-001"; materialName = "RawMat-A"
    qtyIn = 100; qtyOut = 98; lineName = "Line-1"; operator = "Zhang"
    supplierName = "Supplier-A"; sourceType = "feeding"; traceTime = "2026-08-08 10:00:00"; remark = "Test feeding"
} | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $addBody -ContentType "application/json"
Write-Host "Result: code=$($resp.code), msg=$($resp.msg)"

Write-Host "`n=== 4. Add Genealogy (breakpoint: SF001->empty) ===" -ForegroundColor Green
$addBody2 = @{
    parentBatchNo = "SF-20260808-001"; childBatchNo = ""; workOrderNo = "WO-002"
    opName = "Packing"; materialCode = "MAT-SF-001"; materialName = "SemiFin-A"
    qtyIn = 98; lineName = "Line-2"; operator = "Li"; sourceType = "manual"
    traceTime = "2026-08-08 14:00:00"; breakReason = "Output batch not registered"; remark = "Test breakpoint"
} | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $addBody2 -ContentType "application/json"
Write-Host "Result: code=$($resp.code), msg=$($resp.msg)"

Write-Host "`n=== 5. Add Genealogy (normal: SF001->FG001) ===" -ForegroundColor Green
$addBody3 = @{
    parentBatchNo = "SF-20260808-001"; childBatchNo = "FG-20260808-001"; workOrderNo = "WO-003"
    opName = "Filling"; materialCode = "MAT-FG-001"; materialName = "FinGoods-A"
    qtyIn = 98; qtyOut = 96; lineName = "Line-3"; operator = "Wang"
    sourceType = "complete"; traceTime = "2026-08-08 16:00:00"; remark = "Test complete"
} | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $addBody3 -ContentType "application/json"
Write-Host "Result: code=$($resp.code), msg=$($resp.msg)"

Write-Host "`n=== 6. List Genealogy (3 records expected) ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
Write-Host "Total: $($resp.total)"
foreach ($row in $resp.rows) {
    $child = if ($row.childBatchNo) { $row.childBatchNo } else { "(null)" }
    Write-Host "  ID=$($row.id) | parent=$($row.parentBatchNo) | child=$child | break=$($row.breakFlag) | src=$($row.sourceType)"
}

Write-Host "`n=== 7. Forward Trace (RM-20260808-001 -> finished goods) ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/forward/RM-20260808-001" -Method Get -Headers $headers
Write-Host "code=$($resp.code), root parentBatch=$($resp.data.parentBatchNo), children=$($resp.data.children.Count)"
function PrintTree($nodes, $depth) {
    foreach ($n in $nodes) {
        $indent = "  " * $depth
        $child = if ($n.childBatchNo) { $n.childBatchNo } else { "(null)" }
        Write-Host "${indent}|- parent=$($n.parentBatchNo) -> child=$child | WO=$($n.workOrderNo) | op=$($n.opName) | break=$($n.breakFlag)"
        if ($n.children -and $n.children.Count -gt 0) { PrintTree $n.children ($depth + 1) }
    }
}
PrintTree $resp.data.children 1

Write-Host "`n=== 8. Backward Trace (FG-20260808-001 -> raw materials) ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/backward/FG-20260808-001" -Method Get -Headers $headers
Write-Host "code=$($resp.code), root childBatch=$($resp.data.childBatchNo), children=$($resp.data.children.Count)"
PrintTree $resp.data.children 1

Write-Host "`n=== 9. Completeness Dashboard ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/completeness" -Method Get -Headers $headers
Write-Host "Linked orders: $($resp.data.linkedOrders)"
Write-Host "Break orders: $($resp.data.breakOrders)"
Write-Host "Total orders: $($resp.data.totalOrders)"
Write-Host "Completeness rate: $($resp.data.completenessRate)%"
Write-Host "Target: $($resp.data.targetRate)%, Gap: $($resp.data.gap)%"

Write-Host "`n=== 10. Break List ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/breakList" -Method Get -Headers $headers
Write-Host "Break count: $($resp.data.Count)"
foreach ($item in $resp.data) {
    Write-Host "  parent=$($item.parentBatchNo) | WO=$($item.workOrderNo) | reason=$($item.breakReason)"
}

Write-Host "`n=== 11. Cycle Detection Test (FG001->RM001 should be blocked) ===" -ForegroundColor Green
$cycleBody = @{ parentBatchNo = "FG-20260808-001"; childBatchNo = "RM-20260808-001"; sourceType = "manual" } | ConvertTo-Json
try {
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $cycleBody -ContentType "application/json"
    Write-Host "Result: code=$($resp.code), msg=$($resp.msg)" -ForegroundColor Yellow
} catch {
    $errJson = $_.ErrorDetails.Message
    if ($errJson) { $errResp = $errJson | ConvertFrom-Json; Write-Host "Expected block: code=$($errResp.code), msg=$($errResp.msg)" -ForegroundColor Cyan }
    else { Write-Host "Blocked (HTTP error): $($_.Exception.Response.StatusCode)" -ForegroundColor Cyan }
}

Write-Host "`n=== 12. Update Genealogy (fix breakpoint: SF001->FG002) ===" -ForegroundColor Green
$listResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list?parentBatchNo=SF-20260808-001" -Method Get -Headers $headers
$breakRow = $listResp.rows | Where-Object { $_.breakFlag -eq 1 } | Select-Object -First 1
if ($breakRow) {
    $updateBody = @{
        id = $breakRow.id; parentBatchNo = "SF-20260808-001"; childBatchNo = "FG-20260808-002"
        workOrderNo = "WO-002"; opName = "Packing"; materialCode = "MAT-FG-002"; materialName = "FinGoods-A-Small"
        qtyIn = 98; qtyOut = 96; lineName = "Line-2"; operator = "Li"; sourceType = "manual"
        traceTime = "2026-08-08 14:00:00"; breakFlag = 0; remark = "Breakpoint fixed"
    } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Put -Headers $headers -Body $updateBody -ContentType "application/json"
    Write-Host "Update result: code=$($resp.code), msg=$($resp.msg)"
    $verifyResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/$($breakRow.id)" -Method Get -Headers $headers
    Write-Host "Verify: child=$($verifyResp.data.childBatchNo), break=$($verifyResp.data.breakFlag)"
} else { Write-Host "No breakpoint record found" -ForegroundColor Red }

Write-Host "`n=== 13. Completeness After Fix ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/completeness" -Method Get -Headers $headers
Write-Host "Linked: $($resp.data.linkedOrders), Break: $($resp.data.breakOrders), Rate: $($resp.data.completenessRate)%"

Write-Host "`n=== 14. Delete Test ===" -ForegroundColor Green
$allResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
if ($allResp.rows.Count -gt 0) {
    $delId = $allResp.rows[0].id
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/$delId" -Method Delete -Headers $headers
    Write-Host "Delete ID=${delId} -> code=$($resp.code), msg=$($resp.msg)"
    $afterResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
    Write-Host "After delete count: $($afterResp.total)"
}

Write-Host "`n=== 15. Forward Trace After Fix (RM-001 -> all downstream) ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/forward/RM-20260808-001" -Method Get -Headers $headers
PrintTree $resp.data.children 1

Write-Host "`n=== 16. Backward Trace After Fix (FG-20260808-002 -> upstream) ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/backward/FG-20260808-002" -Method Get -Headers $headers
PrintTree $resp.data.children 1

Write-Host "`n========== ALL TESTS DONE ==========" -ForegroundColor Green
