Write-Host "========================================"
Write-Host "  Verifying deployment files"
Write-Host "========================================"

$files = @(
    @{ Path = "D:\EDOP\redis\redis-server.exe"; Name = "Redis Server" },
    @{ Path = "D:\EDOP\redis\redis-cli.exe"; Name = "Redis CLI" },
    @{ Path = "D:\EDOP\nginx\nginx.exe"; Name = "Nginx" },
    @{ Path = "D:\EDOP\config\application.yml"; Name = "Backend Config" },
    @{ Path = "D:\EDOP\config\nginx.conf"; Name = "Nginx Config" },
    @{ Path = "D:\EDOP\scripts\env.bat"; Name = "Environment Script" },
    @{ Path = "D:\EDOP\scripts\start-all.bat"; Name = "Start All Script" }
)

$allOK = $true
foreach ($f in $files) {
    if (Test-Path $f.Path) {
        Write-Host "  OK: $($f.Name)" -ForegroundColor Green
    } else {
        Write-Host "  MISSING: $($f.Name) ($($f.Path))" -ForegroundColor Red
        $allOK = $false
    }
}

# Check JDK seprarately
if (Test-Path "D:\EDOP\jdk17\bin\java.exe") {
    Write-Host "  OK: JDK 17" -ForegroundColor Green
} else {
    Write-Host "  JDK 17 not downloaded yet. Check D:\EDOP\downloads for jdk17.zip, then extract manually." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================"
if ($allOK) {
    Write-Host "  All core files ready!" -ForegroundColor Green
} else {
    Write-Host "  Some files missing." -ForegroundColor Red
}
