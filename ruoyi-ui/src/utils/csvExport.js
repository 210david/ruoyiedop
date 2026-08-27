/**
 * 前端导出工具：保证导出数据与列表查询口径一致（所见即所得）
 */

/**
 * 分页拉取全部符合条件的数据（用于前端导出）
 * @param {Function} fetchFn 分页查询函数 (params) => Promise<{rows, total}>
 * @param {Object} params 查询参数（pageNum/pageSize 会被覆盖）
 * @returns {Promise<Array>} 全部数据行
 */
export async function fetchAllPages(fetchFn, params) {
  const rows = []
  const page = { ...params, pageNum: 1, pageSize: 500 }
  while (true) {
    const res = await fetchFn(page)
    const cur = res.rows || []
    rows.push(...cur)
    if (!cur.length || rows.length >= (res.total || 0)) break
    page.pageNum++
  }
  return rows
}

/**
 * 下载 CSV 文件（带 BOM 头，Excel 打开中文不乱码）
 * @param {String} filename 文件名（自动补 .csv 后缀）
 * @param {Array<String>} headers 表头
 * @param {Array<Array>} rows 数据行
 */
export function downloadCsv(filename, headers, rows) {
  const csv = [headers, ...rows].map(r => r.map(v => {
    const s = String(v != null ? v : '')
    return /[",\n]/.test(s) ? '"' + s.replace(/"/g, '""') + '"' : s
  }).join(',')).join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename.endsWith('.csv') ? filename : `${filename}.csv`
  document.body.appendChild(link)
  link.click()
  setTimeout(() => { document.body.removeChild(link); URL.revokeObjectURL(link.href) }, 200)
}
