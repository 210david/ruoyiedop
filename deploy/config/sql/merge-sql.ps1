$sqlRoot = 'e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql'
$output = 'e:\AIcode\EDOP\RuoYi\RuoYi-Vue3\deploy\config\sql\EDOP_ALL.sql'

'-- ================================================================='
'-- 企业数字化运营平台 - 完整数据库初始化 SQL'
'-- 包含: RuoYi 基础表 + Quartz 任务表 + WMS 仓库表 + DMS 设备表'
'-- 生成时间: {0}'
'-- 使用: mysql -u root -p ry-vue < EDOP_ALL.sql'
'-- ================================================================='
''
'SET FOREIGN_KEY_CHECKS = 0;'
'SET NAMES utf8mb4;'
'SET time_zone = "+08:00";'
''
'CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;'
'USE `ry-vue`;'
'' | Out-File -FilePath $output -Encoding UTF8

Get-ChildItem $sqlRoot -Filter '*.sql' | Sort-Object Name | ForEach-Object {
    Get-Content $_.FullName | Out-File -FilePath $output -Encoding UTF8 -Append
    '' | Out-File -FilePath $output -Encoding UTF8 -Append
}

'-- ================================================================='
'SET FOREIGN_KEY_CHECKS = 1;'
'-- 数据库初始化完成'
'-- =================================================================' | Out-File -FilePath $output -Encoding UTF8 -Append

Write-Host ('已合并为单文件: {0}' -f $output) -ForegroundColor Green
