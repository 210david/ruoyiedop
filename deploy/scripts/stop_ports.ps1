$ports = @(8081, 81)
foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($conn in $conns) {
            if ($conn.State -eq 'Listen') {
                Write-Host "Stopping process $($conn.OwningProcess) on port $port"
                Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue
            }
        }
    }
}
Write-Host "Done"
