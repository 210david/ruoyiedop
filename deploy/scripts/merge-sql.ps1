# ============================================================
# EDOP - Merge SQL Files into EDOP_ALL.sql
# Generates a single SQL file for server deployment
# ============================================================

param(
    [string]$OutputDir = "$PSScriptRoot\..\config\sql"
)

$ErrorActionPreference = "Stop"

# Source SQL directory
$SourceDir = "E:\AIcode\EDOP\RuoYi\RuoYi-Vue3\sql"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   EDOP - Merge SQL Files" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check source directory
if (-not (Test-Path $SourceDir)) {
    Write-Host "[ERROR] Source SQL directory not found: $SourceDir" -ForegroundColor Red
    exit 1
}

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

$OutputFile = Join-Path $OutputDir "EDOP_ALL.sql"

# SQL files to merge (order matters!)
$SqlFiles = @(
    "ry_20260417.sql",
    "quartz.sql",
    "performance_indexes.sql",
    "role_templates.sql",
    "menu_hide_tech.sql",
    "sys_data_change_log.sql",
    "sys_undo_log.sql",
    "logininfor_upgrade.sql",
    "wms_all.sql",
    "wms_statistics_menu.sql",
    "wms_stock_alert_menu.sql",
    "dms_all.sql",
    "mk_all.sql",
    "mk_supplement.sql",
    "mk_number_rule.sql",
    "mk_contract_approve_log.sql",
    "openapi.sql",
    "dms_wms_number_rules.sql",
    "apidoc_menu.sql",
    "openapidoc_menu.sql",
    "add_contract_approve_menu.sql"
)

Write-Host "[INFO] Source: $SourceDir"
Write-Host "[INFO] Output: $OutputFile"
Write-Host ""

# Start building merged SQL
$sb = [System.Text.StringBuilder]::new()

# Header
[void]$sb.AppendLine("-- ============================================================")
[void]$sb.AppendLine("-- EDOP - Merged SQL (Auto-generated, do not edit manually)")
[void]$sb.AppendLine("-- Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
[void]$sb.AppendLine("-- ============================================================")
[void]$sb.AppendLine("")

# Database creation
[void]$sb.AppendLine("CREATE DATABASE IF NOT EXISTS ``ry-vue`` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;")
[void]$sb.AppendLine("USE ``ry-vue``;")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("SET NAMES utf8mb4;")
[void]$sb.AppendLine("SET FOREIGN_KEY_CHECKS = 0;")
[void]$sb.AppendLine("")

$totalFiles = $SqlFiles.Count
$current = 0

foreach ($sqlFile in $SqlFiles) {
    $current++
    $filePath = Join-Path $SourceDir $sqlFile

    if (-not (Test-Path $filePath)) {
        Write-Host "[WARN] File not found, skipping: $sqlFile" -ForegroundColor Yellow
        continue
    }

    $fileSize = (Get-Item $filePath).Length
    Write-Host "[$current/$totalFiles] Merging: $sqlFile ($fileSize bytes)" -ForegroundColor Green

    # Separator
    [void]$sb.AppendLine("")
    [void]$sb.AppendLine("-- ============================================================")
    [void]$sb.AppendLine("-- File: $sqlFile")
    [void]$sb.AppendLine("-- ============================================================")
    [void]$sb.AppendLine("")

    # Read file content (UTF-8)
    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
    [void]$sb.AppendLine($content)
    [void]$sb.AppendLine("")
}

# Footer
[void]$sb.AppendLine("")
[void]$sb.AppendLine("-- ============================================================")
[void]$sb.AppendLine("-- End of merged SQL")
[void]$sb.AppendLine("-- ============================================================")
[void]$sb.AppendLine("SET FOREIGN_KEY_CHECKS = 1;")

# Write output file (UTF-8 without BOM)
$outputContent = $sb.ToString()
[System.IO.File]::WriteAllText($OutputFile, $outputContent, (New-Object System.Text.UTF8Encoding($false)))

$outputSize = (Get-Item $OutputFile).Length
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Merge Complete!" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Output:  $OutputFile"
Write-Host "  Size:    $outputSize bytes"
Write-Host "  Files:   $totalFiles merged"
Write-Host ""
