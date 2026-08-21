Add-Type -AssemblyName System.IO.Compression.FileSystem
$jarPath = 'e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\ruoyi-admin\target\ruoyi-admin.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
$zip.Entries | Where-Object { $_.Name -like '*MmsDemand*' } | ForEach-Object { Write-Host $_.FullName }
$zip.Dispose()
