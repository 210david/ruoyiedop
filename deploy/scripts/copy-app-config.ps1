Copy-Item "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\deploy\config\application.yml" -Destination "D:\EDOP\app\application.yml" -Force
Copy-Item "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\deploy\config\application-druid.yml" -Destination "D:\EDOP\app\application-druid.yml" -Force
Copy-Item "D:\EDOP\config\nginx.conf" -Destination "D:\EDOP\nginx\conf\nginx.conf" -Force
Copy-Item "D:\EDOP\config\redis.windows.conf" -Destination "D:\EDOP\redis\redis.windows.conf" -Force
Copy-Item "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\ry_20260417.sql" -Destination "D:\EDOP\config\sql\ry_20260417.sql" -Force
Copy-Item "e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql\quartz.sql" -Destination "D:\EDOP\config\sql\quartz.sql" -Force
Write-Host "Application configs and SQL scripts copied."
