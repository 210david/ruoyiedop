$ErrorActionPreference = "Stop"

# 1. 登录获取token
$loginBody = @{ username = "admin"; password = "admin123"; code = ""; uuid = "" } | ConvertTo-Json
try {
    $loginResp = Invoke-RestMethod -Uri "http://localhost:8081/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResp.token
    Write-Host "=== 1. 登录测试 ===" -ForegroundColor Green
    Write-Host "登录成功, token: $($token.Substring(0, 20))..."
} catch {
    Write-Host "登录失败: $_" -ForegroundColor Red
    exit 1
}

$headers = @{ Authorization = "Bearer $token" }

# 2. 测试谱系列表查询
Write-Host "`n=== 2. 谱系列表查询（空表） ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
Write-Host "总数: $($resp.total), 行数: $($resp.rows.Count)"

# 3. 新增谱系记录 - 正常记录（原料→半成品）
Write-Host "`n=== 3. 新增谱系（正常: RM001→SF001） ===" -ForegroundColor Green
$addBody = @{
    parentBatchNo = "RM-20260808-001"
    childBatchNo = "SF-20260808-001"
    workOrderNo = "WO-001"
    opName = "混合"
    materialCode = "MAT-RM-001"
    materialName = "原料A"
    qtyIn = 100
    qtyOut = 98
    lineName = "产线1"
    operator = "张三"
    supplierName = "供应商A"
    sourceType = "feeding"
    traceTime = "2026-08-08 10:00:00"
    remark = "测试投料记录"
} | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $addBody -ContentType "application/json"
Write-Host "新增结果: code=$($resp.code), msg=$($resp.msg)"

# 4. 新增谱系记录 - 断点记录（半成品→空）
Write-Host "`n=== 4. 新增谱系（断点: SF001→空） ===" -ForegroundColor Green
$addBody2 = @{
    parentBatchNo = "SF-20260808-001"
    childBatchNo = ""
    workOrderNo = "WO-002"
    opName = "包装"
    materialCode = "MAT-SF-001"
    materialName = "半成品A"
    qtyIn = 98
    qtyOut = $null
    lineName = "产线2"
    operator = "李四"
    sourceType = "manual"
    traceTime = "2026-08-08 14:00:00"
    breakReason = "包装工序产出批次未登记"
    remark = "测试断点记录"
} | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $addBody2 -ContentType "application/json"
Write-Host "新增结果: code=$($resp.code), msg=$($resp.msg)"

# 5. 新增谱系 - 正常链 SF001→FG001
Write-Host "`n=== 5. 新增谱系（正常: SF001→FG001） ===" -ForegroundColor Green
$addBody3 = @{
    parentBatchNo = "SF-20260808-001"
    childBatchNo = "FG-20260808-001"
    workOrderNo = "WO-003"
    opName = "灌装"
    materialCode = "MAT-FG-001"
    materialName = "成品A"
    qtyIn = 98
    qtyOut = 96
    lineName = "产线3"
    operator = "王五"
    sourceType = "complete"
    traceTime = "2026-08-08 16:00:00"
    remark = "测试完工记录"
} | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $addBody3 -ContentType "application/json"
Write-Host "新增结果: code=$($resp.code), msg=$($resp.msg)"

# 6. 再次查询谱系列表
Write-Host "`n=== 6. 谱系列表查询（3条记录） ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
Write-Host "总数: $($resp.total), 行数: $($resp.rows.Count)"
foreach ($row in $resp.rows) {
    Write-Host "  ID=$($row.id) | 投入=$($row.parentBatchNo) | 产出=$($row.childBatchNo) | 断点=$($row.breakFlag) | 来源=$($row.sourceType)"
}

# 7. 正向追溯：RM-001 → 成品
Write-Host "`n=== 7. 正向追溯（RM-20260808-001 → 成品） ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/forward/RM-20260808-001" -Method Get -Headers $headers
Write-Host "追溯结果: code=$($resp.code)"
$traceData = $resp.data
Write-Host "根节点投入批次: $($traceData.parentBatchNo)"
Write-Host "直接下游数量: $($traceData.children.Count)"
function PrintTree($nodes, $indent) {
    foreach ($n in $nodes) {
        Write-Host "$indent  ├─ 投入=$($n.parentBatchNo) → 产出=$($n.childBatchNo) | 工单=$($n.workOrderNo) | 工序=$($n.opName) | 断点=$($n.breakFlag)"
        if ($n.children -and $n.children.Count -gt 0) {
            PrintTree $n.children "$indent    "
        }
    }
}
PrintTree $traceData.children ""

# 8. 反向追溯：FG-001 → 原料
Write-Host "`n=== 8. 反向追溯（FG-20260808-001 → 原料） ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/backward/FG-20260808-001" -Method Get -Headers $headers
Write-Host "追溯结果: code=$($resp.code)"
$traceData = $resp.data
Write-Host "根节点产出批次: $($traceData.childBatchNo)"
Write-Host "直接上游数量: $($traceData.children.Count)"
PrintTree $traceData.children ""

# 9. 完整度看板
Write-Host "`n=== 9. 谱系完整度统计 ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/completeness" -Method Get -Headers $headers
Write-Host "已关联工单: $($resp.data.linkedOrders)"
Write-Host "断点工单: $($resp.data.breakOrders)"
Write-Host "总工单: $($resp.data.totalOrders)"
Write-Host "完整率: $($resp.data.completenessRate)%"
Write-Host "目标: $($resp.data.targetRate)%"
Write-Host "差距: $($resp.data.gap)%"

# 10. 断点清单
Write-Host "`n=== 10. 断点清单 ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/breakList" -Method Get -Headers $headers
Write-Host "断点数量: $($resp.data.Count)"
foreach ($item in $resp.data) {
    Write-Host "  投入=$($item.parentBatchNo) | 工单=$($item.workOrderNo) | 原因=$($item.breakReason)"
}

# 11. 环检测测试
Write-Host "`n=== 11. 环检测测试（FG001→RM001 会成环） ===" -ForegroundColor Green
$cycleBody = @{
    parentBatchNo = "FG-20260808-001"
    childBatchNo = "RM-20260808-001"
    sourceType = "manual"
} | ConvertTo-Json
try {
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Post -Headers $headers -Body $cycleBody -ContentType "application/json"
    Write-Host "结果: code=$($resp.code), msg=$($resp.msg)" -ForegroundColor Yellow
} catch {
    $errResp = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "预期拦截: code=$($errResp.code), msg=$($errResp.msg)" -ForegroundColor Cyan
}

# 12. 修改谱系（断点补录产出批次）
Write-Host "`n=== 12. 修改谱系（断点补录: SF001→FG002） ===" -ForegroundColor Green
# 先查询断点记录
$listResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list?parentBatchNo=SF-20260808-001&breakFlag=1" -Method Get -Headers $headers
if ($listResp.rows.Count -gt 0) {
    $updateId = $listResp.rows[0].id
    $updateBody = @{
        id = $updateId
        parentBatchNo = "SF-20260808-001"
        childBatchNo = "FG-20260808-002"
        workOrderNo = "WO-002"
        opName = "包装"
        materialCode = "MAT-FG-002"
        materialName = "成品A-小包装"
        qtyIn = 98
        qtyOut = 96
        lineName = "产线2"
        operator = "李四"
        sourceType = "manual"
        traceTime = "2026-08-08 14:00:00"
        breakFlag = 0
        remark = "断点补录完成"
    } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy" -Method Put -Headers $headers -Body $updateBody -ContentType "application/json"
    Write-Host "修改结果: code=$($resp.code), msg=$($resp.msg)"

    # 验证修改后的记录
    $verifyResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/$updateId" -Method Get -Headers $headers
    Write-Host "验证: 产出批次=$($verifyResp.data.childBatchNo), 断点标记=$($verifyResp.data.breakFlag)"
} else {
    Write-Host "未找到断点记录" -ForegroundColor Red
}

# 13. 再次查看完整度（补录后应改善）
Write-Host "`n=== 13. 补录后完整度统计 ===" -ForegroundColor Green
$resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/completeness" -Method Get -Headers $headers
Write-Host "已关联工单: $($resp.data.linkedOrders)"
Write-Host "断点工单: $($resp.data.breakOrders)"
Write-Host "完整率: $($resp.data.completenessRate)%"

# 14. 删除测试
Write-Host "`n=== 14. 删除谱系记录 ===" -ForegroundColor Green
$allResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
if ($allResp.rows.Count -gt 0) {
    $delId = $allResp.rows[0].id
    $resp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/$delId" -Method Delete -Headers $headers
    Write-Host "删除ID=$delId 结果: code=$($resp.code), msg=$($resp.msg)"
    # 验证删除后数量
    $afterResp = Invoke-RestMethod -Uri "http://localhost:8081/qms/trace/genealogy/list" -Method Get -Headers $headers
    Write-Host "删除后剩余记录数: $($afterResp.total)"
}

Write-Host "`n========== 测试完成 ==========" -ForegroundColor Green
