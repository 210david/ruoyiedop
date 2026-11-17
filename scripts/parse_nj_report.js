/**
 * 解析《低温肉制品每批次内检报告.xlsx》—— 每个 sheet 是一份出厂检验报告单
 * 输出 JSON 到 tmp/nj_reports.json，供 PDF 生成脚本使用
 */
const XLSX = require('xlsx')
const fs = require('fs')
const path = require('path')

const SRC = 'C:/Users/Administrator/AppData/Local/Temp/nj.xlsx'
const OUT = path.join(__dirname, '../tmp/nj_reports.json')

function excelDate(v) {
  if (v === null || v === undefined || v === '') return ''
  if (typeof v === 'string' && /^\d{5}(\.\d+)?$/.test(v.trim())) v = Number(v.trim())
  if (typeof v === 'number') {
    const ms = Math.round((v - 25569) * 86400 * 1000)
    const d = new Date(ms)
    const p = n => String(n).padStart(2, '0')
    return `${d.getUTCFullYear()}-${p(d.getUTCMonth() + 1)}-${p(d.getUTCDate())}`
  }
  const s = String(v).trim()
  const m = s.match(/^(\d{4})[-./年](\d{1,2})[-./月](\d{1,2})/)
  if (m) return `${m[1]}-${String(m[2]).padStart(2, '0')}-${String(m[3]).padStart(2, '0')}`
  return s
}

const wb = XLSX.readFile(SRC)
const out = []

for (const sn of wb.SheetNames) {
  const rows = XLSX.utils.sheet_to_json(wb.Sheets[sn], { header: 1, defval: '' })
  if (!rows.length) continue

  // 1. 头部信息：扫描含冒号的标签单元格，取其后第一个非空值
  const head = {}
  for (let i = 0; i < Math.min(8, rows.length); i++) {
    const r = rows[i]
    for (let j = 0; j < r.length; j++) {
      const cell = String(r[j] || '').trim()
      if (!cell) continue
      const m = cell.match(/^([\u4e00-\u9fa5A-Za-z\/]{2,8})[：:]\s*(.*)$/)
      if (m) {
        const label = m[1]
        let val = m[2].trim()
        if (!val) {
          for (let k = j + 1; k < r.length; k++) {
            const nxt = String(r[k] || '').trim()
            if (nxt) { val = nxt; break }
          }
        }
        if (!head[label]) head[label] = dateLike(label) ? excelDate(val) : val
      }
    }
  }
  function dateLike(label) {
    return /日期/.test(label)
  }

  // 2. 检验项目表：定位表头行
  let hIdx = -1
  for (let i = 0; i < rows.length; i++) {
    const line = rows[i].map(c => String(c || '')).join('|')
    if (/检验项目/.test(line) && /判定|结论/.test(line)) { hIdx = i; break }
  }
  const items = []
  if (hIdx >= 0) {
    for (let i = hIdx + 1; i < rows.length; i++) {
      const r = rows[i].map(c => String(c === null || c === undefined ? '' : c).trim())
      const name = r[0]
      if (!name) break
      if (/^(检验结论|结论|备注|检验员|审核)/.test(name)) break
      items.push({ name, unit: r[1] || '/', std: r[2] || '', result: r[3] || '', judge: r[4] || '合格' })
    }
  }

  // 3. 结论行（表后可能还有）
  let conclusion = ''
  for (let i = hIdx + 1; i < rows.length; i++) {
    const line = rows[i].map(c => String(c || '')).join(' ')
    const m = line.match(/(?:检验结论|结论)[：:]\s*(\S.*)$/)
    if (m) { conclusion = m[1].trim(); break }
  }

  out.push({ sheet: sn, head, items, conclusion })
}

fs.mkdirSync(path.dirname(OUT), { recursive: true })
fs.writeFileSync(OUT, JSON.stringify(out, null, 2), 'utf8')

console.log('sheets parsed:', out.length)
for (const r of out) {
  console.log('---', r.sheet, '| 品名:', r.head['品名'], '| 生产:', r.head['生产日期'], '| 检验:', r.head['检验日期'], '| 报告:', r.head['报告日期'], '| 数量:', r.head['数量'], '| 规格:', r.head['包装规格'], '| 项目:', r.items.length, '| 结论:', r.conclusion || '(无)')
}
