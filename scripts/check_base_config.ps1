# ============================================================================
# 基础配置表一键巡检（环境迁移 / 清理业务数据 / 换库后必跑）
# 用法：powershell -File scripts\check_base_config.ps1 -DbName RUOYI_CFSP
# 退出码：0=全部通过；1=存在被清空的配置表（需执行基线恢复脚本）
# 换环境：改 -DbName 参数即可；新增配置表时同步维护 $tables 清单
# ============================================================================
param(
    [string]$DbName = 'RUOYI_CFSP',
    [string]$User = 'root',
    [string]$Password = 'ZBrj@2026'
)

$ErrorActionPreference = 'Stop'
# mysql 的 stderr 密码警告会被 PS 当异常，故查询段临时降级为 Continue
$ErrorActionPreference = 'Continue'
$tables = @(
    'mk_number_rule','mk_number_rule_prefix',
    'fin_subject','fin_fiscal_config','fin_period','fin_voucher_rule',
    'fin_cost_method_config','fin_ledger','fin_material_tax',
    'qms_aql_plan','qms_defect_code','qms_material_attr','qms_quality_target',
    'qms_audit_check_template','qms_audit_check_template_item',
    'qms_workshop','qms_gauge','qms_insp_item',
    'mms_code_rule','mms_factory','mms_workshop','mms_line','mms_station',
    'mms_shift','mms_process','mms_route','mms_route_process',
    'mms_bom','mms_bom_detail','mms_resource','mms_team',
    'dms_data_collection_config','dms_equipment_category','dms_equipment',
    'dms_repair_faq','dms_inspection_route','dms_pm_plan','dms_spare_part',
    'biz_hr_shift','biz_hr_workday_calendar','biz_hr_salary_item','biz_hr_employee',
    'mk_customer_tag','mk_customer','mk_material',
    'wms_warehouse','wms_supplier','wms_material',
    'biz_safety_area','pms_supplier_catalog'
)

$fail = @()
foreach ($t in $tables) {
    $out = & cmd /c "mysql -u$User -p$Password -N -B -e `"SELECT COUNT(*) FROM ``$DbName``.``$t``;`"" 2>$null
    $cnt = 0
    [void][int]::TryParse("$out".Trim(), [ref]$cnt)
    $status = if ($cnt -gt 0) { 'OK' } else { 'FAIL-EMPTY' }
    Write-Host ("{0,-35} {1,6}  {2}" -f $t, $cnt, $status)
    if ($cnt -eq 0) { $fail += $t }
}

if ($fail.Count -gt 0) {
    Write-Host ""
    Write-Host "CHECK FAILED: $($fail.Count) base-config table(s) EMPTY (maybe wrongly truncated):" -ForegroundColor Red
    $fail | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host "Restore: run sql/mk_number_rule_baseline.sql then sql/base_config_baseline.sql" -ForegroundColor Yellow
    exit 1
} else {
    Write-Host ""
    Write-Host "CHECK PASSED: all $($tables.Count) base-config tables have data." -ForegroundColor Green
    exit 0
}
