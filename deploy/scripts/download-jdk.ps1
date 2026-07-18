$url = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.12_7.zip'
$outfile = 'D:\EDOP\downloads\jdk17.zip'
Write-Host 'Downloading JDK 17.0.12 (~170MB)...' -ForegroundColor Yellow
Write-Host 'From: $url' -ForegroundColor DarkGray
$wc = New-Object System.Net.WebClient
$wc.DownloadFile($url, $outfile)
Write-Host 'Download complete.' -ForegroundColor Green
Write-Host 'Checking file size...' -ForegroundColor Yellow
$fileSize = (Get-Item $outfile).Length / (1MB)
Write-Host 'File size: $fileSize MB' -ForegroundColor DarkGray
Write-Host 'Extracting...' -ForegroundColor Yellow
Expand-Archive -Path $outfile -DestinationPath 'D:\EDOP\downloads\jdk-temp' -Force
$jdkSubDir = Get-ChildItem 'D:\EDOP\downloads\jdk-temp' | Select-Object -First 1
Write-Host 'Extracted to temporary: $($jdkSubDir.FullName)' -ForegroundColor DarkGray
Copy-Item -Path "$($jdkSubDir.FullName)\*" -Destination 'D:\EDOP\jdk17' -Recurse -Force
Remove-Item 'D:\EDOP\downloads\jdk-temp' -Recurse -Force
Write-Host 'JDK extracted to D:\EDOP\jdk17' -ForegroundColor Green
$javaExe = 'D:\EDOP\jdk17\bin\java.exe'
if (Test-Path $javaExe) {
    $javaVersion = & $javaExe -version 2>&1 | Select-Object -First 1
    Write-Host 'Java version: $javaVersion' -ForegroundColor Green
} else {
    Write-Host 'Warning: java.exe not found in D:\EDOP\jdk17\bin' -ForegroundColor Red
}
