$ErrorActionPreference = "Stop"

Write-Host "========================================"
Write-Host "  Downloading environment components"
Write-Host "========================================"

# ===== 1. Redis =====
Write-Host "[1/3] Downloading Redis 5.0.14.1..."
$redisUrl = "https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip"
$redisZip = "D:\EDOP\downloads\redis.zip"
try {
    Invoke-WebRequest -Uri $redisUrl -OutFile $redisZip -UseBasicParsing
    Write-Host "  Redis downloaded, extracting..."
    Expand-Archive -Path $redisZip -DestinationPath "D:\EDOP\redis" -Force
    Write-Host "  Redis extracted to D:\EDOP\redis"
} catch {
    Write-Host "  Redis download failed: $_"
}

# ===== 2. Nginx =====
Write-Host "[2/3] Downloading Nginx 1.24.0..."
$nginxUrl = "http://nginx.org/download/nginx-1.24.0.zip"
$nginxZip = "D:\EDOP\downloads\nginx.zip"
try {
    Invoke-WebRequest -Uri $nginxUrl -OutFile $nginxZip -UseBasicParsing
    Write-Host "  Nginx downloaded, extracting..."
    Expand-Archive -Path $nginxZip -DestinationPath "D:\EDOP\downloads\nginx-temp" -Force
    $nginxSubDir = Get-ChildItem "D:\EDOP\downloads\nginx-temp" | Select-Object -First 1
    Copy-Item -Path "$($nginxSubDir.FullName)\*" -Destination "D:\EDOP\nginx" -Recurse -Force
    Remove-Item "D:\EDOP\downloads\nginx-temp" -Recurse -Force
    Write-Host "  Nginx extracted to D:\EDOP\nginx"
} catch {
    Write-Host "  Nginx download failed: $_"
}

# ===== 3. JDK 17 =====
Write-Host "[3/3] Downloading JDK 17 (Eclipse Temurin)..."
Write-Host "  Large file (~170MB), please wait..."
$jdkUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
$jdkZip = "D:\EDOP\downloads\jdk17.zip"
try {
    $wc = New-Object System.Net.WebClient
    $wc.DownloadFile($jdkUrl, $jdkZip)
    Write-Host "  JDK downloaded, extracting..."
    Expand-Archive -Path $jdkZip -DestinationPath "D:\EDOP\downloads\jdk-temp" -Force
    $jdkSubDir = Get-ChildItem "D:\EDOP\downloads\jdk-temp" | Select-Object -First 1
    Copy-Item -Path "$($jdkSubDir.FullName)\*" -Destination "D:\EDOP\jdk17" -Recurse -Force
    Remove-Item "D:\EDOP\downloads\jdk-temp" -Recurse -Force
    Write-Host "  JDK extracted to D:\EDOP\jdk17"
} catch {
    Write-Host "  JDK download failed: $_"
}

# ===== Verify =====
Write-Host "========================================"
Write-Host "  Verifying files..."
Write-Host "========================================"
$files = @(
    "D:\EDOP\redis\redis-server.exe",
    "D:\EDOP\redis\redis-cli.exe",
    "D:\EDOP\nginx\nginx.exe",
    "D:\EDOP\jdk17\bin\java.exe",
    "D:\EDOP\jdk17\bin\javaw.exe"
)
foreach ($f in $files) {
    if (Test-Path $f) {
        Write-Host "  [OK] $f"
    } else {
        Write-Host "  [MISSING] $f"
    }
}
Write-Host "========================================"
Write-Host "  All done."
