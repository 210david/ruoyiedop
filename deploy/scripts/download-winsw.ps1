# ============================================================
# 下载 WinSW 并复制为三个服务 exe 文件
# ============================================================
$ErrorActionPreference = "Stop"

$serviceDir = "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\deploy\service"
$winswUrl = "https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-net4-x64.exe"
$tempFile = Join-Path $serviceDir "WinSW-net4-x64.exe"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   下载 WinSW 并配置服务文件" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 确保目录存在
if (-not (Test-Path $serviceDir)) {
    New-Item -ItemType Directory -Path $serviceDir -Force | Out-Null
}

# 下载 WinSW
Write-Host "[1/4] 正在下载 WinSW-net4-x64.exe ..."
Write-Host "      URL: $winswUrl"
try {
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $winswUrl -OutFile $tempFile -UseBasicParsing
    $fileSize = (Get-Item $tempFile).Length
    Write-Host "[OK]  下载成功！文件大小: $([math]::Round($fileSize/1KB, 1)) KB" -ForegroundColor Green
} catch {
    Write-Host "[ERR] 下载失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "请手动下载:" -ForegroundColor Yellow
    Write-Host "  $winswUrl"
    Write-Host "  保存到: $tempFile"
    Write-Host "  然后重新运行此脚本"
    exit 1
}

# 复制为三个服务 exe
Write-Host ""
Write-Host "[2/4] 复制为 edop-backend.exe ..."
Copy-Item -Path $tempFile -Destination (Join-Path $serviceDir "edop-backend.exe") -Force
Write-Host "[OK]  edop-backend.exe 已创建" -ForegroundColor Green

Write-Host "[3/4] 复制为 edop-nginx.exe ..."
Copy-Item -Path $tempFile -Destination (Join-Path $serviceDir "edop-nginx.exe") -Force
Write-Host "[OK]  edop-nginx.exe 已创建" -ForegroundColor Green

Write-Host "[4/4] 复制为 edop-redis.exe ..."
Copy-Item -Path $tempFile -Destination (Join-Path $serviceDir "edop-redis.exe") -Force
Write-Host "[OK]  edop-redis.exe 已创建" -ForegroundColor Green

# 删除原始下载文件
Remove-Item -Path $tempFile -Force
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   WinSW 配置完成！" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "service 目录内容:"
Get-ChildItem $serviceDir | ForEach-Object {
    $size = if ($_.Extension -eq ".exe") { "$([math]::Round($_.Length/1KB, 1)) KB" } else { "$($_.Length) bytes" }
    Write-Host "  $($_.Name)  ($size)"
}
Write-Host ""
Write-Host "现在可以运行 install-services.bat 注册 Windows 服务了" -ForegroundColor Yellow
