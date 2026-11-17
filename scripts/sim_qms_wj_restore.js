/**
 * 恢复第三方外检报告（23 份）的真实元数据与附件关联
 * 数据源：《第三方外检报告导入核对表.xlsx》sheet「1-报告清单」+ sys_file 已登记的文件
 */
const XLSX = require('xlsx')
const mysql = require('mysql2/promise')

const SRC = 'C:/Users/Administrator/AppData/Local/Temp/wj.xlsx'

const pad = n => String(n).padStart(2, '0')
const d10 = v => {
  if (!v) return null
  if (typeof v === 'number') {
    const d = new Date(Math.round((v - 25569) * 86400 * 1000))
    return `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}`
  }
  const m = String(v).trim().match(/^(\d{4})[-./年](\d{1,2})[-./月](\d{1,2})/)
  return m ? `${m[1]}-${pad(m[2])}-${pad(m[3])}` : null
}

;(async () => {
  const wb = XLSX.readFile(SRC)
  const rows = XLSX.utils.sheet_to_json(wb.Sheets['1-报告清单'], { header: 1, defval: '' })
  const list = rows.slice(3).filter(r => r[1])
  console.log('报告清单行数:', list.length)

  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true })

  let ok = 0, miss = 0
  for (const r of list) {
    const [idx, taskNo, reportNo, product, matCode, spec, , produceDate, reportDate, org, itemCnt, fileCnt, conclusion] = r
    const [t] = await conn.query('SELECT task_id,task_no FROM qms_insp_task WHERE task_no=?', [taskNo])
    if (!t.length) { console.log('!! 任务不存在:', taskNo); miss++; continue }
    const taskId = t[0].task_id

    // 从 sys_file 取该任务已登记的附件（按 file_id 排序，pdf 在前）
    const [files] = await conn.query(
      "SELECT file_id,file_name,file_url FROM sys_file WHERE biz_type='qms_insp_task' AND (biz_id=? OR biz_no=?) AND del_flag='0' ORDER BY (file_suffix='pdf') DESC, file_id",
      [taskId, taskNo]
    )
    let urls = []
    let names = []
    for (const f of files) {
      const i = String(f.file_url).indexOf('/profile/')
      const rel = i >= 0 ? String(f.file_url).slice(i) : String(f.file_url)
      urls.push(rel)
      names.push(f.file_name)
    }

    await conn.query(
      'UPDATE qms_insp_task SET report_no=?,report_date=?,inspect_org=?,conclusion=?,report_file_url=?,report_file_name=?,package_spec=?,inspect_basis=? WHERE task_id=?',
      [reportNo, d10(reportDate), org, conclusion, urls.join(',') || null, names.join(',') || null,
       spec || null, '第三方检测机构检验', taskId]
    )
    ok++
    console.log(` ok #${idx} ${taskNo} → ${reportNo} | 附件 ${files.length} | 报告日期 ${d10(reportDate)}`)
    // 同步 sys_file.biz_id（历史上 biz_id 与当前 task_id 不一致）
    if (files.length) {
      await conn.query("UPDATE sys_file SET biz_id=? WHERE biz_type='qms_insp_task' AND biz_no=?", [taskId, taskNo])
    }
  }

  console.log(`完成：更新 ${ok} 条，缺失 ${miss} 条`)
  const [chk] = await conn.query("SELECT COUNT(*) c, SUM(report_file_url LIKE '/profile/upload/%') uploaded, SUM(report_file_url LIKE '/profile/qms/%') fake FROM qms_insp_task WHERE report_no LIKE 'CTT%'")
  console.log('外检任务核对:', chk[0])
  await conn.end()
})().catch(e => { console.error('ERR', e); process.exit(1) })
