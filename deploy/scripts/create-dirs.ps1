$dirs = @(
    'D:\EDOP', 'D:\EDOP\downloads', 'D:\EDOP\jdk17',
    'D:\EDOP\redis', 'D:\EDOP\redis\data', 'D:\EDOP\redis\logs',
    'D:\EDOP\nginx', 'D:\EDOP\nginx\logs', 'D:\EDOP\nginx\conf',
    'D:\EDOP\app', 'D:\EDOP\app\logs',
    'D:\EDOP\frontend', 'D:\EDOP\frontend\dist',
    'D:\EDOP\uploadPath', 'D:\EDOP\service', 'D:\EDOP\scripts', 'D:\EDOP\config'
)
foreach ($d in $dirs) {
    New-Item -ItemType Directory -Force -Path $d | Out-Null
    Write-Host "Created: $d"
}
Write-Host "All directories created successfully."
