/**
 * 导入《低温肉制品每批次内检报告.xlsx》→ QMS 成品检验任务 + 检验项目
 * 运行: node scripts/import_inspection_reports.js
 */
const XLSX = require('xlsx');
const mysql = require('mysql2/promise');

const FILE = 'c:\\Users\\Administrator\\.trae-cn\\attachments\\a893951e-3ac2-455c-bd70-36a7d29387d3_cc85ce7a-2c70-4483-95f2-6258f39a291b_低温肉制品每批次内检报告.xlsx';
const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4' };
const OPERATOR = 'admin';

// 品名归一化：Excel 中存在品牌前缀不一致，统一到标准品名
const PRODUCT_MAP = {
  '鲜丰旺乌猪经典猪肉肠': '乌猪经典猪肉肠',
  '乌猪经典猪肉肠': '乌猪经典猪肉肠',
  '大河乌猪五目肠': '大河乌猪五目肠',
  '五花培根': '五花培根',
  '大河乌猪香辣肠': '大河乌猪香辣肠',
  '五花培根碎': '五花培根碎'
};

function serialToDate(v) {
  if (v == null || v === '') return null;
  if (typeof v === 'number') {
    const d = new Date(Date.UTC(1899, 11, 30) + Math.round(v) * 86400000);
    return d.toISOString().slice(0, 10);
  }
  const s = String(v).trim().replace(/[.\/]/g, '-');
  const m = s.match(/(\d{4})-(\d{1,2})-(\d{1,2})/);
  if (m) return `${m[1]}-${String(m[2]).padStart(2, '0')}-${String(m[3]).padStart(2, '0')}`;
  return null;
}

function parseQty(v) {
  const s = String(v == null ? '' : v).trim();
  const m = s.match(/([\d.]+)\s*(\D*)/);
  if (!m) return { qty: null, unit: '' };
  return { qty: Number(m[1]), unit: (m[2] || '').trim() };
}

function cell(row, i) { return row && row[i] != null ? String(row[i]).trim() : ''; }

async function colExists(conn, table, col) {
  const [r] = await conn.query(
    'SELECT COUNT(*) c FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? AND column_name=?',
    [table, col]);
  return r[0].c > 0;
}

async function addCol(conn, table, col, ddl) {
  if (await colExists(conn, table, col)) return;
  await conn.query(`ALTER TABLE ${table} ADD COLUMN ${col} ${ddl}`);
  console.log(`  + ${table}.${col}`);
}

async function main() {
  const conn = await mysql.createConnection(DB);
  console.log('=== 1. 补充数据库字段 ===');
  await addCol(conn, 'qms_insp_task', 'produce_date', "date DEFAULT NULL COMMENT '生产日期'");
  await addCol(conn, 'qms_insp_task', 'report_date', "date DEFAULT NULL COMMENT '报告日期'");
  await addCol(conn, 'qms_insp_task', 'qty_unit', "varchar(32) DEFAULT NULL COMMENT '数量单位'");
  await addCol(conn, 'qms_insp_task', 'package_spec', "varchar(64) DEFAULT NULL COMMENT '包装规格'");
  await addCol(conn, 'qms_insp_task', 'inspect_basis', "varchar(500) DEFAULT NULL COMMENT '检验依据'");
  await addCol(conn, 'qms_insp_task', 'conclusion', "varchar(500) DEFAULT NULL COMMENT '检验结论'");
  await addCol(conn, 'qms_insp_task', 'inspect_org', "varchar(255) DEFAULT NULL COMMENT '检验机构'");
  await addCol(conn, 'qms_insp_task', 'reviewer_name', "varchar(64) DEFAULT NULL COMMENT '复核人'");
  await addCol(conn, 'qms_insp_item', 'unit', "varchar(32) DEFAULT NULL COMMENT '单位'");
  await addCol(conn, 'qms_insp_item', 'std_value', "varchar(255) DEFAULT NULL COMMENT '标准规定'");
  await addCol(conn, 'qms_insp_item', 'inspect_method', "varchar(128) DEFAULT NULL COMMENT '检验方法'");

  // 成品物料补齐
  console.log('\n=== 2. 成品物料 ===');
  const materialIdMap = {};
  const [maxRow] = await conn.query("SELECT MAX(CAST(RIGHT(material_code,4) AS UNSIGNED)) mx FROM wms_material WHERE material_code LIKE 'WL2026%'");
  let seqCode = Number(maxRow[0].mx || 0);
  for (const name of [...new Set(Object.values(PRODUCT_MAP))]) {
    let [rows] = await conn.query('SELECT material_id FROM wms_material WHERE material_name=? AND del_flag=\'0\' LIMIT 1', [name]);
    if (rows.length) { materialIdMap[name] = rows[0].material_id; console.log(`  = ${name} (id=${rows[0].material_id})`); continue; }
    seqCode++;
    const code = 'WL2026' + String(seqCode).padStart(4, '0');
    const [r] = await conn.query(
      `INSERT INTO wms_material (material_code, material_name, material_type, unit, is_batch_manage, is_expiry_manage, shelf_life_days, status, del_flag, create_by, create_time, remark)
       VALUES (?, ?, '成品', '袋', '1', '1', 90, '0', '0', ?, NOW(), '内检报告导入自动创建')`,
      [code, name, OPERATOR]);
    materialIdMap[name] = r.insertId;
    console.log(`  + ${name} (${code})`);
  }

  console.log('\n=== 3. 解析报告并入库 ===');
  const wb = XLSX.readFile(FILE);
  const [deptRow] = await conn.query('SELECT dept_id FROM qms_insp_task WHERE dept_id IS NOT NULL LIMIT 1');
  const deptId = deptRow.length ? deptRow[0].dept_id : 106;

  let taskCnt = 0, itemCnt = 0;
  for (const sheetName of wb.SheetNames) {
    const rows = XLSX.utils.sheet_to_json(wb.Sheets[sheetName], { header: 1, defval: '' });
    if (rows.length < 6) continue;
    const headText = cell(rows[0], 0).replace(/\s+/g, ' ');
    const org = cell(rows[0], 0).split(/\r?\n/)[0].trim();
    if (!/检验报告/.test(headText)) { console.log(`  SKIP ${sheetName}（非检验报告格式）`); continue; }

    // 头部按标签定位
    let productName = '', inspectDate = null, produceDate = null, reportDate = null, qtyRaw = '', pkgSpec = '';
    for (let i = 0; i < Math.min(6, rows.length); i++) {
      const row = rows[i];
      for (let c = 0; c < row.length; c++) {
        const label = cell(row, c);
        if (label === '品名：') productName = cell(row, c + 1);
        if (label === '检验日期：') inspectDate = serialToDate(row[c + 1]);
        if (label === '生产日期：') produceDate = serialToDate(row[c + 1]);
        if (label === '报告日期：') reportDate = serialToDate(row[c + 1]);
        if (label === '数量：') qtyRaw = cell(row, c + 1);
        if (label === '包装规格：') pkgSpec = cell(row, c + 1);
      }
    }
    if (!productName) { console.log(`  SKIP ${sheetName}（无品名）`); continue; }

    const canonical = PRODUCT_MAP[productName] || productName;
    const materialId = materialIdMap[canonical] || (findInMap(materialIdMap, productName));

    // 表头行
    let headerIdx = -1;
    for (let i = 0; i < rows.length; i++) if (cell(rows[i], 0) === '检验项目') { headerIdx = i; break; }
    if (headerIdx < 0) { console.log(`  SKIP ${sheetName}（无检验项目表头）`); continue; }
    const header = rows[headerIdx];
    const idx = { name: 0, method: -1, std: -1, result: -1, unit: -1, judge: -1 };
    header.forEach((h, i) => {
      const t = String(h).trim();
      if (t === '检验方法') idx.method = i;
      else if (t === '标准规定') idx.std = i;
      else if (t === '检验结果') idx.result = i;
      else if (t === '单位') idx.unit = i;
      else if (t === '判定') idx.judge = i;
    });

    const items = [];
    let basis = '', conclusion = '', analyst = '', reviewer = '';
    for (let i = headerIdx + 1; i < rows.length; i++) {
      const row = rows[i];
      const first = cell(row, 0);
      // 尾部签署行：化验员/复核人（可能分散在多个单元格）
      const rowText = row.map(c => String(c == null ? '' : c)).join(' ');
      if (/化验员|复核|检验员|审核/.test(rowText)) {
        const ma = rowText.match(/化验员[：:]\s*([^\s　]+)/);
        const mr = rowText.match(/复核[人]?[：:]\s*([^\s　]+)/);
        if (ma) analyst = ma[1];
        if (mr) reviewer = mr[1];
        continue;
      }
      if (first.startsWith('检验依据')) { basis = first.replace(/^检验依据[：:]/, '').trim(); continue; }
      if (first.startsWith('结论')) { conclusion = first.replace(/^结论[：:]/, '').trim(); continue; }
      const name = cell(row, idx.name);
      if (!name) continue;
      items.push({
        name,
        method: idx.method >= 0 ? cell(row, idx.method) : '',
        std: idx.std >= 0 ? cell(row, idx.std) : '',
        result: idx.result >= 0 ? cell(row, idx.result) : '',
        unit: idx.unit >= 0 ? cell(row, idx.unit) : '',
        judge: idx.judge >= 0 ? cell(row, idx.judge) : ''
      });
    }

    const qty = parseQty(qtyRaw);
    const day = produceDate || inspectDate || reportDate || '2026-01-01';
    const compact = day.replace(/-/g, '');
    // 批次号：CP + 日期 + 序号字母
    let batchNo = 'CP' + compact + 'A';
    const [dup] = await conn.query('SELECT COUNT(*) c FROM qms_insp_task WHERE batch_no LIKE ?', ['CP' + compact + '%']);
    if (dup[0].c > 0) batchNo = 'CP' + compact + String.fromCharCode(66 + dup[0].c);
    // 任务号
    const [seqRow] = await conn.query("SELECT COUNT(*) c FROM qms_insp_task WHERE task_no LIKE ?", ['FQC' + compact + '%']);
    const taskNo = 'FQC' + compact + String(seqRow[0].c + 1).padStart(3, '0');

    const allPass = items.every(it => !it.judge || it.judge.includes('合格')) && !items.some(it => it.judge.includes('不合格'));

    const [r] = await conn.query(
      `INSERT INTO qms_insp_task
        (task_no, task_type, source_type, source_no, material_id, material_code, material_name,
         batch_no, inspect_qty, task_status, inspect_result, inspector_name, reviewer_name, inspect_time, judge_time,
         status, del_flag, create_by, create_time, dept_id,
         produce_date, report_date, qty_unit, package_spec, inspect_basis, conclusion, inspect_org, remark)
       VALUES (?, 'final', 'batch', ?, ?, ?, ?, ?, ?, 'inspected', ?, ?, ?, ?, ?, '0', '0', ?, NOW(), ?,
         ?, ?, ?, ?, ?, ?, ?, ?)`,
      [taskNo, batchNo, materialId, null, canonical, batchNo, qty.qty, allPass ? 'pass' : 'fail',
       analyst || null, reviewer || null,
       (inspectDate || day) + ' 09:00:00', (reportDate || inspectDate || day) + ' 17:00:00',
       OPERATOR, deptId, produceDate, reportDate, qty.unit, pkgSpec, basis || null, conclusion || null, org,
       `来源：${sheetName}｜品名原文：${productName}`]);
    taskCnt++;

    // 物料编码回填
    const [mrow] = await conn.query('SELECT material_code FROM wms_material WHERE material_id=?', [materialId]);
    if (mrow.length) await conn.query('UPDATE qms_insp_task SET material_code=? WHERE task_id=?', [mrow[0].material_code, r.insertId]);

    for (const it of items) {
      const pass = !it.judge || it.judge.includes('合格');
      await conn.query(
        `INSERT INTO qms_insp_item (task_id, std_name, sample_seq, measured_val, item_result, unit, std_value, inspect_method, create_by, create_time)
         VALUES (?, ?, 1, ?, ?, ?, ?, ?, ?, NOW())`,
        [r.insertId, it.name, it.result || it.judge, pass ? 'pass' : 'fail', it.unit, it.std, it.method || null, OPERATOR]);
      itemCnt++;
    }
    console.log(`  ✓ ${sheetName.padEnd(12)} → ${taskNo} ${canonical} 批次=${batchNo} 数量=${qty.qty}${qty.unit} 项目=${items.length} ${allPass ? '合格' : '不合格'}`);
  }

  const [v] = await conn.query(`
    SELECT COUNT(*) tasks FROM qms_insp_task WHERE task_type='final' AND create_by='admin'
    UNION ALL SELECT COUNT(*) FROM qms_insp_item i JOIN qms_insp_task t ON i.task_id=t.task_id WHERE t.create_by='admin' AND t.task_type='final'`);
  console.log(`\n=== 完成: 任务 ${taskCnt} 个，检验项目 ${itemCnt} 条 ===`);
  v.forEach(x => console.log(' ', JSON.stringify(x)));
  await conn.end();
}

function findInMap(map, key) {
  for (const k of Object.keys(map)) if (key && key.includes(k)) return map[k];
  return null;
}

main().catch(e => { console.error('FATAL:', e); process.exit(1); });
