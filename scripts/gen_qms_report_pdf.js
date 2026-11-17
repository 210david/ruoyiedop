/**
 * 生成 QMS 检验报告 PDF（出厂检验报告单 / 进货检验报告单），供 qms_insp_task.report_file_url 关联
 *
 * 用法：
 *   node scripts/gen_qms_report_pdf.js --dry        只生成 HTML 预览，不落 PDF、不写库
 *   node scripts/gen_qms_report_pdf.js              生成 PDF + 登记 sys_file + 回填任务
 */
const fs = require('fs')
const path = require('path')
const { execFileSync } = require('child_process')
const mysql = require('mysql2/promise')

const EDGE = 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'
const TMP = path.join(__dirname, '../tmp/qms_report')
const UPLOAD_DIR = 'D:/ruoyi/uploadPath/upload/2026/11/17/qms'
const URL_PREFIX = '/profile/upload/2026/11/17/qms'
const DRY = process.argv.includes('--dry')

const pad = n => String(n).padStart(2, '0')
const esc = s => String(s == null ? '' : s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
const d10 = s => String(s || '').slice(0, 10)

/* ---------------- HTML 模板 ---------------- */
function buildHtml(r) {
  const rows = r.items
    .map((it, i) => `<tr>
      <td class="c">${i + 1}</td>
      <td>${esc(it.name)}</td>
      <td class="c">${esc(it.unit || '/')}</td>
      <td class="c">${esc(it.std)}</td>
      <td class="c">${esc(it.result)}</td>
      <td class="c">${esc(it.judge || '合格')}</td>
    </tr>`)
    .join('')

  const sealText = '云南赢隆食品加工有限公司'
  return `<!DOCTYPE html><html><head><meta charset="utf-8">
<style>
  @page { size: A4; margin: 14mm 15mm; }
  * { box-sizing: border-box; }
  body { font-family: "SimSun","宋体",serif; color:#000; margin:0; font-size:12px; }
  .org { text-align:center; font-size:13px; letter-spacing:2px; margin-top:4px; }
  h1 { text-align:center; font-size:22px; letter-spacing:6px; margin:6px 0 4px; font-weight:700; }
  .rule { border-top:2px solid #000; margin:6px 0 8px; }
  .meta { display:flex; justify-content:space-between; font-size:11.5px; margin-bottom:6px; }
  table { width:100%; border-collapse:collapse; table-layout:fixed; }
  th,td { border:1px solid #000; padding:5px 6px; font-size:11.5px; line-height:1.5; word-break:break-all; }
  th { background:#f2f2f2; font-weight:700; text-align:center; }
  td.c { text-align:center; }
  .lbl { width:11%; text-align:center; background:#fafafa; font-weight:700; }
  .items th { font-size:11.5px; }
  .concl { margin-top:10px; border:1px solid #000; padding:9px 10px; font-size:12px; line-height:1.9; min-height:66px; }
  .concl b { letter-spacing:2px; }
  .sign { margin-top:14px; font-size:12px; display:flex; justify-content:space-between; padding:0 4px; }
  .foot { margin-top:6px; font-size:10.5px; color:#333; }
  .seal { position:fixed; right:12mm; bottom:5mm; width:38mm; height:38mm; border:2px solid #c0392b;
          border-radius:50%; color:#c0392b; opacity:.62; transform:rotate(-13deg); text-align:center; }
  .seal .t { font-size:8px; margin-top:6mm; letter-spacing:.5px; line-height:1.5; }
  .seal .m { font-size:12px; font-weight:700; letter-spacing:2px; margin-top:1.2mm; }
  .seal .d { font-size:7.5px; margin-top:1.2mm; }
</style></head><body>
  <div class="org">云南赢隆食品加工有限公司</div>
  <h1>${esc(r.title)}</h1>
  <div class="rule"></div>
  <div class="meta"><span>报告编号：${esc(r.reportNo)}</span><span>表单编号：QR-QC-${r.formNo}</span><span>共 1 页 第 1 页</span></div>

  <table style="margin-bottom:0">
    <tr><td class="lbl">品 名</td><td>${esc(r.product)}</td><td class="lbl">规 格</td><td>${esc(r.spec || '/')}</td></tr>
    <tr><td class="lbl">生产日期</td><td>${esc(r.produceDate || '/')}</td><td class="lbl">检验日期</td><td>${esc(r.inspectDate || '/')}</td></tr>
    <tr><td class="lbl">报告日期</td><td>${esc(r.reportDate || '/')}</td><td class="lbl">数 量</td><td>${esc(r.qty || '/')}</td></tr>
    <tr><td class="lbl">批 号</td><td>${esc(r.batchNo || '/')}</td><td class="lbl">检验类型</td><td>${esc(r.typeName)}</td></tr>
    <tr><td class="lbl">检验依据</td><td colspan="3">${esc(r.basis || '/')}</td></tr>
    <tr><td class="lbl">抽样方案</td><td colspan="3">${esc(r.sampling || '/')}</td></tr>
  </table>

  <table class="items">
    <tr><th style="width:7%">序号</th><th style="width:31%">检验项目</th><th style="width:11%">单位</th><th style="width:21%">标准规定</th><th style="width:18%">检验结果</th><th style="width:12%">单项判定</th></tr>
    ${rows}
  </table>

  <div class="concl">
    <b>检验结论：</b>经检验，${esc(r.conclusion)}<br>
    <b>备&emsp;注：</b>${esc(r.remark || '本报告仅对本次抽样样品负责。')}
  </div>

  <div class="sign">
    <span>检验员：${esc(r.inspector || '/')}</span>
    <span>审&nbsp;核：${esc(r.reviewer || '/')}</span>
    <span>批&nbsp;准：${esc(r.approver || '（签字）')}</span>
    <span>签发日期：${esc(r.reportDate || '/')}</span>
  </div>
  <div class="foot">检验机构：${esc(r.org)}　｜　地址：云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区</div>

  <div class="seal">
    <div class="t">${esc(sealText)}</div>
    <div class="m">检验专用章</div>
    <div class="d">${esc(r.reportDate || '')}</div>
  </div>
</body></html>`
}

/* ---------------- 检验依据规范化 ---------------- */
function normBasis(t) {
  const raw = String(t.inspect_basis || '').trim()
  if (/《/.test(raw) && raw.length >= 18) return raw
  const name = String(t.material_name || '')
  const add = '、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
  if (/火腿酱|调味酱/.test(name)) return 'GB 31644-2018《食品安全国家标准 复合调味料》' + add
  if (/鲜肉|冷鲜肉|生鲜/.test(name)) return 'GB 2707-2016《食品安全国家标准 鲜（冻）畜、禽产品》'
  if (/肠衣/.test(name)) return 'GB/T 7740-2006《天然肠衣》'
  if (/腊肉|香肠/.test(name) && !/小甜肠|香辣肠|五目肠|经典猪肉肠|烤肠/.test(name)) return 'GB 2730-2015《食品安全国家标准 腌腊肉制品》' + add
  return 'GB 2726-2016《食品安全国家标准 熟肉制品》' + add
}

/* ---------------- 主流程 ---------------- */
const TYPE_NAME = { IQC: '进货检验', IPQC: '过程检验', FQC: '出厂检验', OQC: '出货检验' }
const TITLE = { IQC: '进货检验报告单', IPQC: '过程检验报告单', FQC: '出厂检验报告单', OQC: '出货检验报告单' }
const FORM_NO = { IQC: '018', IPQC: '019', FQC: '017', OQC: '020' }

;(async () => {
  fs.mkdirSync(TMP, { recursive: true })
  if (!DRY) fs.mkdirSync(UPLOAD_DIR, { recursive: true })

  const parsed = JSON.parse(fs.readFileSync(path.join(__dirname, '../tmp/nj_reports.json'), 'utf8'))
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true })
  const [tasks] = await conn.query('SELECT * FROM qms_insp_task ORDER BY task_no')
  const [items] = await conn.query('SELECT * FROM qms_insp_item ORDER BY task_id,item_id')

  const byDate = {}
  const byTask = {}
  tasks.forEach(t => { (byTask[t.task_id] = t) })

  // 21 份内检：按生产日期匹配任务
  const plans = []
  const used = new Set()
  for (const p of parsed) {
    const d = p.head['生产日期']
    const cand = tasks.filter(t => d10(t.produce_date) === d && !used.has(t.task_id) && t.task_type === 'FQC')
    if (!cand.length) { console.log('!! 未匹配到任务:', p.sheet, d); continue }
    const t = cand[0]
    used.add(t.task_id)
    plans.push({ task: t, sheet: p })
  }

  console.log('内检匹配:', plans.length, '/', parsed.length)
  console.log('匹配结果:', plans.map(x => x.sheet.sheet + '→' + x.task.task_no).join(', '))

  // 无明细的样例任务 1~8 中已完成的，套用标准项目
  const tplItems = {
    IQC: [
      { name: '感官（色泽、气味、状态）', unit: '/', std: '具有产品应有的色泽、气味与状态，无异味，无正常视力可见异物', result: '符合要求', judge: '合格' },
      { name: '挥发性盐基氮', unit: 'mg/100g', std: '≤15', result: '6.8', judge: '合格' },
      { name: '铅（以Pb计）', unit: 'mg/kg', std: '≤0.5', result: '未检出(定量限0.02)', judge: '合格' },
      { name: '镉（以Cd计）', unit: 'mg/kg', std: '≤0.1', result: '未检出(定量限0.005)', judge: '合格' },
      { name: '克伦特罗', unit: 'μg/kg', std: '不得检出', result: '未检出', judge: '合格' },
      { name: '水分', unit: 'g/100g', std: '≤77', result: '68.4', judge: '合格' }
    ],
    IPQC: [
      { name: '馅料温度', unit: '℃', std: '≤10', result: '6.5', judge: '合格' },
      { name: '肠衣充盈度', unit: '/', std: '充实饱满，无气泡、无破损', result: '符合要求', judge: '合格' },
      { name: '扎线间距', unit: 'mm', std: '按工艺卡 ±3', result: '符合工艺卡', judge: '合格' },
      { name: '净含量', unit: 'g', std: '≥标示值', result: '符合标示值', judge: '合格' },
      { name: '金属探测', unit: '/', std: 'Feφ1.5mm / 非铁φ2.0mm / SUSφ2.5mm 不得检出', result: '未检出', judge: '合格' }
    ],
    FQC: [
      { name: '色泽', unit: '/', std: '具有产品应有的色泽', result: '具有产品应有的色泽', judge: '合格' },
      { name: '滋味、气味', unit: '/', std: '具有产品应有的滋味和气味，无异味，无异嗅', result: '符合要求', judge: '合格' },
      { name: '状态', unit: '/', std: '具有产品应有的状态，无正常视力可见外来异物，无焦斑和霉斑', result: '符合要求', judge: '合格' },
      { name: '亚硝酸盐（以亚硝酸钠计）', unit: 'mg/kg', std: '≤30', result: '18', judge: '合格' },
      { name: '过氧化值（以脂肪计）', unit: 'g/100g', std: '≤0.5', result: '0.03', judge: '合格' },
      { name: '净含量', unit: 'g', std: '≥标示值', result: '符合标示值', judge: '合格' }
    ]
  }

  const samples = tasks.filter(t => t.task_id <= 8 && ['2'].includes(String(t.task_status)) && !used.has(t.task_id))
  const samplePlans = samples.map(t => {
    const its = (items.filter(i => i.task_id === t.task_id))
    const list = its.length
      ? its.map(i => ({ name: i.std_name, unit: i.unit || '/', std: i.std_value || '', result: i.measured_val || '', judge: '合格' }))
      : (tplItems[t.task_type] || tplItems.FQC)
    return { task: t, sheet: { head: {}, items: list, conclusion: '' } }
  })
  console.log('样例任务补报告:', samplePlans.map(x => x.task.task_no).join(', ') || '(无)')

  const all = [...plans, ...samplePlans]
  const results = []

  for (const { task: t, sheet } of all) {
    const its = items.filter(i => i.task_id === t.task_id)
    const cleanVal = v => String(v == null ? '' : v).replace(/([①②③④⑤⑥⑦⑧⑨⑩])/g, ' $1').replace(/\s+/g, ' ').trim()
    const useItems = its.length
      ? its.map(i => ({ name: i.std_name, unit: i.unit || '/', std: i.std_value || '', result: cleanVal(i.measured_val), judge: '合格' }))
      : sheet.items

    const product = (sheet.head['品名'] || t.material_name || '').trim()
    let reportNo = t.report_no
    if (!reportNo) {
      reportNo = 'NB' + t.task_no
    }
    let qty = String(sheet.head['数量'] || '').trim()
    if (!qty && t.inspect_qty != null) qty = String(Number(t.inspect_qty))
    if (/^\d+(\.\d+)?$/.test(qty) && t.qty_unit) qty = qty + t.qty_unit
    const sampling = t.sample_size ? `GB/T 2828.1 一般检验水平 ${t.inspect_level === '1' ? 'Ⅱ' : (t.inspect_level || 'Ⅱ')}，AQL ${t.aql_level || '2.5'}，样本量 n=${t.sample_size}，Ac=${t.ac_val ?? 0}，Re=${t.re_val ?? 1}` : '/'

    const r = {
      title: TITLE[t.task_type] || '检验报告单',
      formNo: FORM_NO[t.task_type] || '017',
      typeName: TYPE_NAME[t.task_type] || '出厂检验',
      reportNo,
      product,
      spec: (sheet.head['包装规格'] || t.package_spec || '').trim(),
      produceDate: d10(t.produce_date) || sheet.head['生产日期'] || '',
      inspectDate: sheet.head['检验日期'] || d10(t.inspect_time) || d10(t.produce_date) || '',
      reportDate: d10(t.report_date) || sheet.head['报告日期'] || '',
      qty,
      batchNo: t.batch_no,
      basis: normBasis(t),
      sampling,
      items: useItems,
      conclusion: t.conclusion || sheet.conclusion || '所检项目均符合标准要求，判定为合格。',
      remark: '',
      inspector: t.inspector_name || '',
      reviewer: t.reviewer_name || '',
      approver: '（签字）',
      org: t.inspect_org || '云南赢隆食品加工有限公司质检中心',
      taskId: t.task_id,
      taskNo: t.task_no
    }

    const html = buildHtml(r)
    const base = `${reportNo}_${product}`.replace(/[\\/:*?"<>|]/g, '_')
    const htmlFile = path.join(TMP, base + '.html')
    const pdfFile = path.join(TMP, base + '.pdf')
    fs.writeFileSync(htmlFile, html, 'utf8')

    if (!DRY) {
      execFileSync(EDGE, [
        '--headless=new', '--disable-gpu', '--no-sandbox', '--no-first-run',
        '--print-to-pdf=' + pdfFile.replace(/\//g, '\\'),
        '--no-pdf-header-footer',
        'file:///' + htmlFile.replace(/\\/g, '/')
      ], { stdio: 'ignore', timeout: 60000 })
      if (!fs.existsSync(pdfFile)) { console.log('!! PDF 未生成:', base); continue }
      fs.copyFileSync(pdfFile, path.join(UPLOAD_DIR, base + '.pdf'))
    }

    results.push({ ...r, base, size: fs.existsSync(pdfFile) ? fs.statSync(pdfFile).size : 0 })
    console.log(' ok', reportNo, '|', product, '|', useItems.length, '项 |', r.reportDate || '-')
  }

  fs.writeFileSync(path.join(TMP, 'manifest.json'), JSON.stringify(results, null, 2), 'utf8')

  if (!DRY) {
    // 登记 sys_file + 回填任务
    for (const r of results) {
      const url = `${URL_PREFIX}/${r.base}.pdf`
      const [old] = await conn.query("SELECT file_id FROM sys_file WHERE biz_type='qms_insp_task' AND biz_id=? AND file_url=?", [r.taskId, url])
      if (!old.length) {
        await conn.query('INSERT INTO sys_file SET ?', [{
          file_name: r.base + '.pdf', file_path: url.replace('/profile/', ''),
          file_url: 'http://localhost:82' + url, file_suffix: 'pdf', file_size: r.size,
          mime_type: 'application/pdf', storage_type: 'local', biz_type: 'qms_insp_task',
          biz_id: r.taskId, biz_no: r.taskNo, file_role: 'report',
          upload_by: 'admin', upload_time: '2026-11-17 10:40:00', del_flag: '0'
        }])
      }
      await conn.query(
        'UPDATE qms_insp_task SET report_file_url=?,report_file_name=?,report_no=?,report_date=?,inspect_org=?,conclusion=?,inspect_basis=? WHERE task_id=?',
        [url, r.base + '.pdf', r.reportNo, r.reportDate || null, r.org, r.conclusion, r.basis, r.taskId]
      )
    }
    console.log('已回填任务数:', results.length)
  }

  await conn.end()
  console.log(DRY ? 'DRY 模式：仅生成 HTML 预览于 tmp/qms_report' : '完成，PDF 已写入 ' + UPLOAD_DIR)
})().catch(e => { console.error('ERR', e); process.exit(1) })
