/**
 * 导出 7 个业务模块的数据（INSERT IGNORE，幂等）到交付包
 *   仓储管理 wms_ → 设备管理 dms_ → 营销管理 mk_ → 采购管理 pms_
 *   → 安全生产管理 biz_safety_ → 质量管理 qms_ → 生产管控 mms_
 *
 * 用法：node scripts/export_module_data.js [输出目录]
 * 默认输出到 D:/EDOP/patch_20261117/sql/data
 */
const fs = require('fs')
const path = require('path')
const { execFileSync } = require('child_process')
const mysql = require('mysql2/promise')

const OUT_DIR = process.argv[2] || 'D:/EDOP/patch_20261117/sql/data'
const DUMP = 'D:/EDOP/mysql/bin/mysqldump.exe'
const DB = { host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp' }

const MODULES = [
  { seq: '01', key: 'wms', label: '仓储管理', like: "table_name LIKE 'wms\\_%'" },
  { seq: '02', key: 'dms', label: '设备管理', like: "table_name LIKE 'dms\\_%'" },
  { seq: '03', key: 'mk', label: '营销管理', like: "table_name LIKE 'mk\\_%'" },
  { seq: '04', key: 'pms', label: '采购管理', like: "table_name LIKE 'pms\\_%'" },
  { seq: '05', key: 'safety', label: '安全生产管理', like: "table_name LIKE 'biz\\_safety\\_%'" },
  { seq: '06', key: 'qms', label: '质量管理', like: "table_name LIKE 'qms\\_%'" },
  { seq: '07', key: 'mms', label: '生产管控', like: "table_name LIKE 'mms\\_%'" }
]

const pad = n => String(n).padStart(6, ' ')

;(async () => {
  fs.mkdirSync(OUT_DIR, { recursive: true })
  const conn = await mysql.createConnection({ ...DB, dateStrings: true })
  const summary = []

  for (const m of MODULES) {
    const [tbls] = await conn.query(
      `SELECT table_name AS t, table_rows AS r FROM information_schema.tables
        WHERE table_schema=? AND table_type='BASE TABLE' AND ${m.like}
          AND table_name NOT REGEXP '_bak[0-9]{8}' AND table_name NOT LIKE '%\\\\_old'
        ORDER BY table_name`, [DB.database]
    )
    if (!tbls.length) { console.log(`skip ${m.label}（无表）`); continue }

    // 精确行数
    let rows = 0
    const detail = []
    for (const t of tbls) {
      const [[c]] = await conn.query(`SELECT COUNT(*) n FROM \`${t.t}\``)
      rows += Number(c.n)
      detail.push(`${pad(c.n)}  ${t.t}`)
    }

    const names = tbls.map(t => t.t)
    const args = [
      `-u${DB.user}`, `-p${DB.password}`,
      '--no-create-info', '--insert-ignore', '--complete-insert',
      '--skip-add-locks', '--skip-comments', '--skip-disable-keys',
      '--set-gtid-purged=OFF', '--default-character-set=utf8mb4',
      '--no-tablespaces', '--net-buffer-length=1048576',
      '--single-transaction',
      DB.database, ...names
    ]
    const body = execFileSync(DUMP, args, { maxBuffer: 512 * 1024 * 1024, encoding: 'utf8', stderr: 'pipe' })
      .split('\n')
      .filter(l => !/^\/\*!|^-- /i.test(l) || l.startsWith('-- '))
      .filter(l => !/^\/\*!40/.test(l))
      .join('\n')

    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const head = [
      '-- ============================================================',
      `-- 模块数据：${m.label}（${m.key}_，${names.length} 张表，${rows} 行）`,
      `-- 生成：${now}   源库：${DB.database}`,
      '-- 特性：INSERT IGNORE —— 可重复执行，不删除/覆盖服务器已有同主键行',
      '-- ============================================================',
      'SET NAMES utf8mb4;',
      'SET FOREIGN_KEY_CHECKS=0;',
      '',
      '-- 表清单（行数  表名）',
      ...detail.map(d => `-- ${d}`),
      ''
    ].join('\n')

    const file = path.join(OUT_DIR, `${m.seq}_${m.key}_data.sql`)
    fs.writeFileSync(file, head + '\n' + body + '\nSET FOREIGN_KEY_CHECKS=1;\n', 'utf8')
    const size = fs.statSync(file).size
    summary.push({ seq: m.seq, label: m.label, key: m.key, tables: names.length, rows, size })
    console.log(`${m.seq} ${m.label.padEnd(8)} 表 ${String(names.length).padStart(2)}  行 ${String(rows).padStart(6)}  ${(size / 1024 / 1024).toFixed(2)} MB  -> ${path.basename(file)}`)
  }

  await conn.end()
  const total = summary.reduce((a, b) => a + b.size, 0)
  console.log(`\n合计 ${summary.length} 个模块，${summary.reduce((a, b) => a + b.tables, 0)} 张表，${summary.reduce((a, b) => a + b.rows, 0)} 行，${(total / 1024 / 1024).toFixed(2)} MB`)
  fs.writeFileSync(path.join(OUT_DIR, '_summary.json'), JSON.stringify(summary, null, 2), 'utf8')
})()
