<#
============================================================
 EDOP YLSP 一键导入（结构 + 7 模块业务数据）
------------------------------------------------------------
 双击同目录的「一键导入数据库.bat」即可，不需要手工执行任何 SQL。

 做的事情：
   1. 自动定位 mysql.exe / mysqldump.exe
   2. 自动备份当前库（backup_<库名>_<时间>.sql）
   3. STEP 1  执行 5 个结构 / 配置脚本
   4. STEP 2  执行 7 个模块业务数据脚本
   5. 逐模块统计导入后的实际表数与行数，与预期值比对
   6. 全程写日志 import_log_<时间>.txt

 全部 SQL 使用 INSERT IGNORE，重复执行安全，不会覆盖已有单据。

 可选参数：
   -Db ruoyi_ylsp     目标库名
   -User root         数据库账号
   -SkipBackup        跳过导入前备份（不建议）
============================================================
#>
param(
    [string]$Db = "ruoyi_ylsp",
    [string]$User = "root",
    [string]$Password = "",
    [switch]$SkipBackup,
    [switch]$NonInteractive
)

$ErrorActionPreference = "Stop"
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

function Wait-Key {
    if (-not $NonInteractive) { Read-Host "按回车退出" }
}

function Line { Write-Host ("-" * 64) -ForegroundColor DarkGray }
function Head([string]$t) {
    Write-Host ""
    Write-Host ("=" * 64) -ForegroundColor DarkCyan
    Write-Host ("  " + $t) -ForegroundColor Cyan
    Write-Host ("=" * 64) -ForegroundColor DarkCyan
}
function Ok([string]$t)   { Write-Host ("  [OK]   " + $t) -ForegroundColor Green }
function Bad([string]$t)  { Write-Host ("  [FAIL] " + $t) -ForegroundColor Red }
function Tip([string]$t)  { Write-Host ("  " + $t) -ForegroundColor Gray }

# ---------------- 定位目录 ----------------
$root = $PSScriptRoot
if (-not $root) { $root = Split-Path -Parent $MyInvocation.MyCommand.Definition }

$sqlDir = Join-Path $root "sql"
if (-not (Test-Path $sqlDir)) { $sqlDir = $root }
$dataDir = Join-Path $sqlDir "data"

$logPath = Join-Path $root ("import_log_" + (Get-Date -Format "yyyyMMdd_HHmmss") + ".txt")
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
function Log([string]$t) {
    try { [System.IO.File]::AppendAllText($logPath, ("[{0}] {1}`r`n" -f (Get-Date -Format "HH:mm:ss"), $t), $utf8NoBom) } catch { }
}

Head "EDOP YLSP 一键导入"
Tip ("数据包目录 : " + $root)
Tip ("SQL 目录   : " + $sqlDir)
Tip ("目标数据库 : " + $Db)
Log "start, root=$root, sqlDir=$sqlDir, db=$Db"

# ---------------- 待执行文件清单 ----------------
$schemaFiles = @(
    "01_upgrade_20261115_flow_module_sync.sql",
    "02_upgrade_20261116_flow_config_data.sql",
    "03_oa_ledger.sql",
    "04_upgrade_20261117_qms_mk_data.sql",
    "05_upgrade_20261117_qms_report_file.sql"
)
$dataFiles = @(
    "01_wms_data.sql",
    "02_dms_data.sql",
    "03_mk_data.sql",
    "04_pms_data.sql",
    "05_safety_data.sql",
    "06_qms_data.sql",
    "07_mms_data.sql"
)
$modules = @(
    @{ Key = "wms";    Label = "仓储管理";     Re = "^wms_";         ExpTables = 15; ExpRows = 3314 },
    @{ Key = "dms";    Label = "设备管理";     Re = "^dms_";         ExpTables = 14; ExpRows = 872  },
    @{ Key = "mk";     Label = "营销管理";     Re = "^mk_";          ExpTables = 45; ExpRows = 1343 },
    @{ Key = "pms";    Label = "采购管理";     Re = "^pms_";         ExpTables = 31; ExpRows = 2058 },
    @{ Key = "safety"; Label = "安全生产管理"; Re = "^biz_safety_";  ExpTables = 33; ExpRows = 747  },
    @{ Key = "qms";    Label = "质量管理";     Re = "^qms_";         ExpTables = 31; ExpRows = 873  },
    @{ Key = "mms";    Label = "生产管控";     Re = "^mms_";         ExpTables = 44; ExpRows = 3547 }
)

Write-Host ""
Tip "检查文件完整性 ..."
$missing = @()
foreach ($f in $schemaFiles) {
    if (-not (Test-Path (Join-Path $sqlDir $f))) { $missing += ("sql\" + $f) }
}
foreach ($f in $dataFiles) {
    if (-not (Test-Path (Join-Path $dataDir $f))) { $missing += ("sql\data\" + $f) }
}
if ($missing.Count -gt 0) {
    Bad "缺少以下文件，请确认解压完整："
    $missing | ForEach-Object { Write-Host ("         " + $_) -ForegroundColor Red }
    Log ("missing files: " + ($missing -join ", "))
    Write-Host ""
    Wait-Key
    exit 1
}
Ok ("结构脚本 5 个、数据脚本 7 个，全部就位")

# ---------------- 定位 mysql ----------------
$mysqlCandidates = @(
    "D:\EDOP\mysql\bin\mysql.exe",
    "D:\mysql\bin\mysql.exe",
    "E:\EDOP\mysql\bin\mysql.exe",
    "$env:ProgramFiles\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "$env:ProgramFiles\MySQL\MySQL Server 5.7\bin\mysql.exe",
    "C:\phpstudy_pro\Extensions\MySQL5.7.26\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe"
)
$mysql = $mysqlCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $mysql) {
    $c = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($c) { $mysql = $c.Source }
}
if (-not $mysql) {
    Bad "没找到 mysql.exe"
    Tip "请把 MySQL 客户端加入 PATH，或把 mysql.exe 放到 D:\EDOP\mysql\bin\ 下再运行"
    Log "mysql.exe not found"
    Write-Host ""
    Wait-Key
    exit 1
}
$mysqlBin = Split-Path -Parent $mysql
$mysqldump = Join-Path $mysqlBin "mysqldump.exe"
if (-not (Test-Path $mysqldump)) { $mysqldump = $null }
Ok ("mysql    : " + $mysql)
if ($mysqldump) { Ok ("mysqldump: " + $mysqldump) } else { Tip "未找到 mysqldump.exe，本次不做自动备份" }

# ---------------- 取密码 ----------------
$pwdFile = Join-Path $root "pwd.txt"
$pwd = $Password
if (-not $pwd -and (Test-Path $pwdFile)) {
    $pwd = (Get-Content -Path $pwdFile -Raw).Trim()
    if ($pwd) { Ok "已读取 pwd.txt 中的密码（以后可直接双击，无需再输）" }
}
if (-not $pwd) {
    if ($NonInteractive) {
        Bad "未提供密码（非交互模式需用 -Password 传入）"
        exit 1
    }
    Write-Host ""
    Write-Host "  请输入 MySQL 密码（账号 $User）：" -ForegroundColor Yellow
    $pwd = Read-Host "  密码"
    if ([string]::IsNullOrEmpty($pwd)) {
        Bad "密码为空，已退出"
        Write-Host ""
        Wait-Key
        exit 1
    }
    $save = Read-Host "  是否保存到 pwd.txt 以便下次一键运行？(y/N)"
    if ($save -match "^(y|Y)") {
        Set-Content -Path $pwdFile -Value $pwd -Encoding ASCII
        Ok "已保存 pwd.txt（注意：明文存放，勿外传此目录）"
    }
}
Log "password provided"

# ---------------- 临时 client 配置文件（避免命令行明文密码与特殊字符问题） ----------------
$tmpCnf = Join-Path $env:TEMP ("edop_my_" + [Guid]::NewGuid().ToString("N") + ".cnf")
$cnfText = "[client]`r`nuser=" + $User + "`r`npassword=" + $pwd + "`r`ndefault-character-set=utf8mb4`r`n"
[System.IO.File]::WriteAllText($tmpCnf, $cnfText, (New-Object System.Text.UTF8Encoding $false))
Log "cnf created"

$script:failFlag = $false

# ---- 原生进程调用：绕过 Start-Process 的环境变量字典冲突 bug，stdin/stdout 全按字节传输，
#      不受 PowerShell 管道编码影响（中文 SQL 不会变问号） ----
function Run-Native([string]$exe, [string[]]$argArray, [string]$stdinFile, [string]$stdoutFile) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $exe
    $quoted = @()
    foreach ($a in $argArray) {
        if ($a -match '[\s]') { $quoted += ('"' + $a + '"') } else { $quoted += $a }
    }
    $psi.Arguments = ($quoted -join ' ')
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true

    $p = [System.Diagnostics.Process]::Start($psi)
    $errTask = $p.StandardError.ReadToEndAsync()

    $fs = $null
    $copyTask = $null
    $outTask = $null
    $outText = ""
    if ($stdoutFile) {
        $fs = [System.IO.File]::Create($stdoutFile)
        $copyTask = $p.StandardOutput.BaseStream.CopyToAsync($fs)
    } else {
        $outTask = $p.StandardOutput.ReadToEndAsync()
    }

    try {
        if ($stdinFile) {
            $inFs = [System.IO.File]::OpenRead($stdinFile)
            $buf = New-Object byte[] 65536
            while ($true) {
                $n = $inFs.Read($buf, 0, $buf.Length)
                if ($n -le 0) { break }
                $p.StandardInput.BaseStream.Write($buf, 0, $n)
            }
            $p.StandardInput.BaseStream.Flush()
            $inFs.Close()
        }
    } catch { }
    try { $p.StandardInput.Close() } catch { }

    $p.WaitForExit()

    if ($stdoutFile) {
        try { $copyTask.Wait() } catch { }
        if ($fs) { try { $fs.Close() } catch { } }
    } else {
        try { $outText = $outTask.Result } catch { }
    }
    $errText = ""
    try { $errText = $errTask.Result } catch { }

    return @{ Code = $p.ExitCode; Out = $outText; Err = $errText }
}

function Invoke-SqlFile([string]$file, [string]$label) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $r = Run-Native -exe $mysql -argArray @("--defaults-extra-file=$tmpCnf", "--database=$Db") -stdinFile $file -stdoutFile ""
    $sw.Stop()
    $errText = $r.Err

    if ($r.Code -ne 0 -or ($errText -and $errText -match "ERROR")) {
        Bad ("$label  (" + [math]::Round($sw.Elapsed.TotalSeconds, 1) + "s)")
        Write-Host $errText -ForegroundColor Red
        Log ("FAIL " + $label + " :: " + ($errText -replace "`r?`n", " "))
        return $false
    }
    Ok ("$label  (" + [math]::Round($sw.Elapsed.TotalSeconds, 1) + "s)")
    Log ("OK " + $label)
    return $true
}

function Invoke-MySql([string]$sqlText, [switch]$NoDb) {
    $inF = [System.IO.Path]::GetTempFileName()
    [System.IO.File]::WriteAllText($inF, $sqlText, (New-Object System.Text.UTF8Encoding $false))
    $a2 = @("--defaults-extra-file=$tmpCnf")
    if (-not $NoDb) { $a2 += "--database=$Db" }
    $a2 += @("-N", "-B")
    $r = Run-Native -exe $mysql -argArray $a2 -stdinFile $inF -stdoutFile ""
    Remove-Item $inF -Force -ErrorAction SilentlyContinue

    $lines = @()
    if ($r.Out) { $lines = @($r.Out -split "`r?`n" | Where-Object { $_ -ne "" }) }
    return @{ Code = $r.Code; Out = $lines; Err = $r.Err }
}

# ---------------- 连接测试 ----------------
Write-Host ""
Tip "测试数据库连接 ..."
$probe = Invoke-MySql -NoDb -sqlText ("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='" + $Db + "';")
if ($probe.Code -ne 0) {
    Bad "连接数据库失败"
    Write-Host $probe.Err -ForegroundColor Red
    Log ("connect failed: " + ($probe.Err -replace "`r?`n", " "))
    Remove-Item $tmpCnf -Force -ErrorAction SilentlyContinue
    Write-Host ""
    Wait-Key
    exit 1
}
if (-not $probe.Out -or $probe.Out.Count -eq 0) {
    Bad ("数据库 " + $Db + " 不存在")
    Tip "请先在服务器上建库（字符集 utf8mb4），或确认库名是否正确"
    Log "database not exists"
    Remove-Item $tmpCnf -Force -ErrorAction SilentlyContinue
    Write-Host ""
    Wait-Key
    exit 1
}
Ok ("已连接 " + $Db)

# ---------------- 备份 ----------------
if (-not $SkipBackup -and $mysqldump) {
    Write-Host ""
    $bk = Join-Path $root ("backup_" + $Db + "_" + (Get-Date -Format "yyyyMMdd_HHmmss") + ".sql")
    Tip ("正在备份当前库 -> " + (Split-Path -Leaf $bk) + "  （库大时需等一会儿）")
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $rb = Run-Native -exe $mysqldump -argArray @("--defaults-extra-file=$tmpCnf", "--default-character-set=utf8mb4", "--single-transaction", "--routines", "--events", "--triggers", $Db) -stdinFile "" -stdoutFile $bk
    $sw.Stop()
    $errText = $rb.Err
    if ($rb.Code -ne 0 -or ($errText -and $errText -match "ERROR")) {
        Bad "备份失败"
        Write-Host $errText -ForegroundColor Red
        Log "backup failed"
        $go = if ($NonInteractive) { "n" } else { Read-Host "  仍要继续导入吗？(y/N)" }
        if ($go -notmatch "^(y|Y)") {
            Remove-Item $tmpCnf -Force -ErrorAction SilentlyContinue
            Wait-Key
            exit 1
        }
    } else {
        $sizeMb = [math]::Round((Get-Item $bk).Length / 1MB, 1)
        Ok ("备份完成  " + (Split-Path -Leaf $bk) + "  (" + $sizeMb + " MB, " + [math]::Round($sw.Elapsed.TotalSeconds, 1) + "s)")
        Log ("backup ok: " + $bk)
    }
}

# ---------------- STEP 1 ----------------
Head "STEP 1 / 2   结构与配置脚本（5 个）"
$i = 0
foreach ($f in $schemaFiles) {
    $i++
    Line
    Write-Host ("  [" + $i + "/5] " + $f) -ForegroundColor White
    $okRun = Invoke-SqlFile -file (Join-Path $sqlDir $f) -label $f
    if (-not $okRun) {
        $script:failFlag = $true
        break
    }
}

# ---------------- STEP 2 ----------------
if (-not $script:failFlag) {
    Head "STEP 2 / 2   7 个模块业务数据"
    $i = 0
    foreach ($f in $dataFiles) {
        $i++
        Line
        Write-Host ("  [" + $i + "/7] " + $f) -ForegroundColor White
        $okRun = Invoke-SqlFile -file (Join-Path $dataDir $f) -label $f
        if (-not $okRun) {
            $script:failFlag = $true
            break
        }
    }
}

# ---------------- 校验行数 ----------------
$verifyOk = $false
if (-not $script:failFlag) {
    Head "导入结果核对"
    Tip "统计各模块实际表数与行数 ..."

    $likeClause = @(
        "table_name LIKE 'wms\\_%'",
        "table_name LIKE 'dms\\_%'",
        "table_name LIKE 'mk\\_%'",
        "table_name LIKE 'pms\\_%'",
        "table_name LIKE 'biz\\_safety\\_%'",
        "table_name LIKE 'qms\\_%'",
        "table_name LIKE 'mms\\_%'"
    ) -join " OR "

    $sqlNames = "SELECT table_name FROM information_schema.tables WHERE table_schema='" + $Db + "' AND table_type='BASE TABLE' AND (" + $likeClause + ") AND table_name NOT REGEXP '_bak[0-9]{8}[a-z]*$' ORDER BY table_name;"
    $r = Invoke-MySql -sqlText $sqlNames
    $tables = @()
    if ($r.Code -eq 0 -and $r.Out) { $tables = @($r.Out | Where-Object { $_ -and $_.Trim() -ne "" } | ForEach-Object { $_.Trim() }) }

    if ($tables.Count -eq 0) {
        Bad "未查到任何业务表，可能库选错了"
        Log "no tables found"
    } else {
        $unionParts = @()
        foreach ($t in $tables) { $unionParts += ("SELECT '" + $t + "' AS t, COUNT(*) AS c FROM ``" + $t + "``") }
        $sqlCount = ($unionParts -join " UNION ALL ") + ";"
        $rc = Invoke-MySql -sqlText $sqlCount
        $countMap = @{}
        if ($rc.Code -eq 0 -and $rc.Out) {
            foreach ($lineRow in $rc.Out) {
                if (-not $lineRow) { continue }
                $parts = $lineRow -split "`t"
                if ($parts.Count -ge 2) { $countMap[$parts[0]] = [int]$parts[1] }
            }
        } else {
            Write-Host $rc.Err -ForegroundColor Red
        }

        Write-Host ""
        Write-Host "  模块              表数      行数      预期行数   状态" -ForegroundColor White
        Line
        $totT = 0; $totR = 0; $allOk = $true
        foreach ($m in $modules) {
            $mine = @($tables | Where-Object { $_ -match $m.Re })
            $rows = 0
            foreach ($t in $mine) { if ($countMap.ContainsKey($t)) { $rows += $countMap[$t] } }
            $totT += $mine.Count
            $totR += $rows
            $st = "OK"
            $color = "Green"
            if ($mine.Count -lt $m.ExpTables -or $rows -lt $m.ExpRows) {
                $st = "偏少，请检查"
                $color = "Yellow"
                $allOk = $false
            }
            Write-Host ("  {0,-16}  {1,4}  {2,9}  {3,10}   {4}" -f $m.Label, $mine.Count, $rows, $m.ExpRows, $st) -ForegroundColor $color
        }
        Line
        Write-Host ("  {0,-16}  {1,4}  {2,9}" -f "合计", $totT, $totR) -ForegroundColor White
        Log ("verify tables=" + $totT + " rows=" + $totR + " allOk=" + $allOk)
        $verifyOk = $allOk
    }
}

# ---------------- 收尾 ----------------
Remove-Item $tmpCnf -Force -ErrorAction SilentlyContinue

Head "完成"
if ($script:failFlag) {
    Write-Host "  导入中断：有脚本执行失败，请查看上方红色报错。" -ForegroundColor Red
    Tip ("完整日志：" + $logPath)
    Write-Host ""
    Wait-Key
    exit 1
}

if ($verifyOk) {
    Write-Host "  数据库导入成功，7 个模块的数据已全部就位。" -ForegroundColor Green
} else {
    Write-Host "  脚本已全部执行完，但行数核对有偏差，请看上表黄字部分。" -ForegroundColor Yellow
}
Write-Host ""
Tip "接下来（如果这次也要换程序）："
Tip "  1. 替换后端 jar   2. 替换前端 dist   3. 拷贝 uploadPath 下的附件   4. 重启服务"
Tip ("本次日志：" + $logPath)
Write-Host ""
Wait-Key
