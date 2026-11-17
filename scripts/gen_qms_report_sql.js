/**
 * 生成检验报告附件增量 SQL（幂等）：sys_file 登记 + qms_insp_task 字段回填
 * 输出：sql/upgrade_20261117_qms_report_file.sql
 */
const fs = require('fs')
const path = require('path')
const mysql = require('mysql2/promise')

const OUT = path.join(__dirname, '../sql/upgrade_20261117_qms_report_file.sql')
const q = s => (s === null || s === undefined ? 'NULL' : "'" + String(s).replace(/\\/g, '\\\\').replace(/'/g, "''") + "'")

;(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true })

  const [files] = await conn.query(
    "SELECT * FROM sys_file WHERE biz_type='qms_insp_task' AND del_flag='0' ORDER BY file_id"
  )
  const [tasks] = await conn.query(
    "SELECT * FROM qms_insp_task WHERE report_file_url IS NOT NULL AND report_file_url<>'' ORDER BY task_no"
  )

  const L = []
  L.push('-- ============================================================')
  L.push('-- 20261117 增量 05：质量检验报告附件（内检 27 份 + 第三方外检 23 份）')
  L.push('-- 前置：文件需先按目录放到 uploadPath 下，路径与 file_path 一致')
  L.push('--   2026/11/16/qms/*.pdf  （第三方外检，23 份）')
  L.push('--   2026/11/17/qms/*.pdf  （出厂/进货/过程检验，27 份）')
  L.push('-- 特性：全部语句幂等，可重复执行')
  L.push('-- ============================================================')
  L.push('SET NAMES utf8mb4;')
  L.push('')
  L.push('-- ---------- 1. sys_file 附件登记 ----------')
  for (const f of files) {
    L.push(`INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)`)
    L.push(`SELECT ${q(f.file_name)},${q(f.file_path)},${q(f.file_url)},${q(f.file_suffix)},${f.file_size || 0},${q(f.mime_type)},'local','qms_insp_task',`)
    L.push(`       (SELECT task_id FROM qms_insp_task WHERE task_no=${q(f.biz_no)} LIMIT 1),${q(f.biz_no)},${q(f.file_role || 'attach')},'admin',${q(f.upload_time)},'0'`)
    L.push(`FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url=${q(f.file_url)} AND del_flag='0');`)
  }
  L.push('')
  L.push('-- 修正历史记录中 biz_id 与任务主键不一致的问题')
  L.push("UPDATE sys_file f JOIN qms_insp_task t ON t.task_no COLLATE utf8mb4_general_ci = f.biz_no COLLATE utf8mb4_general_ci")
  L.push("SET f.biz_id = t.task_id WHERE f.biz_type='qms_insp_task' AND (f.biz_id IS NULL OR f.biz_id <> t.task_id);")
  L.push('')
  L.push('-- ---------- 2. 检验任务报告字段回填 ----------')
  L.push('UPDATE qms_insp_task SET')
  L.push('  report_no = CASE task_no')
  L.push(tasks.map(t => `    WHEN ${q(t.task_no)} THEN ${q(t.report_no)}`).join('\n'))
  L.push('  END,')
  L.push('  report_file_url = CASE task_no')
  L.push(tasks.map(t => `    WHEN ${q(t.task_no)} THEN ${q(t.report_file_url)}`).join('\n'))
  L.push('  END,')
  L.push('  report_file_name = CASE task_no')
  L.push(tasks.map(t => `    WHEN ${q(t.task_no)} THEN ${q(t.report_file_name)}`).join('\n'))
  L.push('  END,')
  L.push('  inspect_basis = CASE task_no')
  L.push(tasks.map(t => `    WHEN ${q(t.task_no)} THEN ${q(t.inspect_basis)}`).join('\n'))
  L.push('  END,')
  L.push('  report_date = CASE task_no')
  L.push(tasks.map(t => `    WHEN ${q(t.task_no)} THEN ${t.report_date ? q(t.report_date) : 'report_date'}`).join('\n'))
  L.push('  END')
  L.push(`WHERE task_no IN (${tasks.map(t => q(t.task_no)).join(',')});`)
  L.push('')
  L.push('-- ---------- 3. 第三方外检任务：结论与检验机构 ----------')
  const wj = tasks.filter(t => String(t.report_no || '').startsWith('CTT'))
  for (const t of wj) {
    L.push(`UPDATE qms_insp_task SET inspect_org=${q(t.inspect_org)},conclusion=${q(t.conclusion)},package_spec=${q(t.package_spec)} WHERE task_no=${q(t.task_no)};`)
  }
  L.push('')
  L.push('-- 校验：应有 50 条任务带附件')
  L.push("SELECT COUNT(*) AS total, SUM(report_file_url IS NOT NULL AND report_file_url<>'') AS has_report FROM qms_insp_task;")

  fs.writeFileSync(OUT, L.join('\n') + '\n', 'utf8')
  console.log('written', OUT, fs.statSync(OUT).size, 'bytes; files', files.length, 'tasks', tasks.length, 'wj', wj.length)
  await conn.end()
})().catch(e => { console.error(e); process.exit(1) })
