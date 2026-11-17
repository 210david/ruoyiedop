/**
 * 9月洗涤库存表：本期入库 / 本期出库 → 汇总入库单、出库单（仅业务单据，不重复动库存）
 * 用法：
 *   node scripts/import_wash_movement.js --dry
 *   node scripts/import_wash_movement.js
 */
const XLSX = require('xlsx');
const mysql = require('mysql2/promise');

const FILE = 'c:\\Users\\Administrator\\.trae-cn\\attachments\\f5bb1934-8ed8-422b-9cd3-9ca2a116765c_5370610e-4456-43bd-a4cb-e65bf1b1423f_9月洗涤库存表.xlsx';
const DRY = process.argv.includes('--dry');
const USER = 'admin';
const WH = 3;
const LOC = 11;
const DAY = '2026-09-30';
const MARK = '9月洗涤库存表汇总';

const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4', decimalNumbers: true };
const norm = (s) => String(s == null ? '' : s).replace(/[\s\u3000]/g, '').replace(/（/g, '(').replace(/）/g, ')');
const ALIAS = { '云南白药创可贴': '创可贴' };
const num = (v) => (v === '' || v == null ? 0 : Number(v) || 0);
const pad = (n, l = 2) => String(n).padStart(l, '0');

function parseSheet(wb, sheet) {
  const rows = XLSX.utils.sheet_to_json(wb.Sheets[sheet], { header: 1, raw: true, defval: '' });
  const items = [];
  for (let i = 4; i < rows.length; i++) {
    const r = rows[i];
    const name = String(r[1] == null ? '' : r[1]).trim();
    if (!name) continue;
    const isClean = sheet === '清洁库存表';
    items.push({
      name,
      spec: String(r[2] == null ? '' : r[2]).trim(),
      unit: String(r[3] == null ? '' : r[3]).trim() || '个',
      inQty: num(isClean ? r[5] : r[4]),
      outQty: num(isClean ? r[6] : r[5]),
    });
  }
  return items;
}

(async () => {
  const wb = XLSX.readFile(FILE);
  const groups = [
    { sheet: '清洁库存表', label: '洗涤及零用品' },
    { sheet: '配件库存表', label: '五金配件工具' },
  ];
  const conn = await mysql.createConnection(DB);
  const q = async (s, p) => (await conn.query(s, p))[0];
  const run = async (s, p) => (await conn.query(s, p))[0];

  const dup = await q("select count(*) n from wms_inbound_order where remark like ?", [MARK + '%']);
  const dup2 = await q("select count(*) n from wms_outbound_order where remark like ?", [MARK + '%']);
  if (dup[0].n + dup2[0].n > 0) {
    console.log(`[跳过] 已存在汇总单据：入库 ${dup[0].n} / 出库 ${dup2[0].n}`);
    await conn.end();
    return;
  }

  const mats = await q("select material_id, material_code, material_name, unit, material_type from wms_material where del_flag='0'");
  const byName = new Map();
  for (const m of mats) {
    const k = norm(m.material_name);
    const old = byName.get(k);
    if (!old || (old.material_type !== '3' && m.material_type === '3')) byName.set(k, m);
  }

  const maxIn = await q("select order_no from wms_inbound_order where order_no like 'RK20260930%' order by order_no desc limit 1");
  const maxOut = await q("select order_no from wms_outbound_order where order_no like 'OUT20260930%' order by order_no desc limit 1");
  let inSeq = maxIn[0] ? Number(maxIn[0].order_no.slice(-2)) : 0;
  let outSeq = maxOut[0] ? Number(maxOut[0].order_no.slice(-2)) : 0;

  if (!DRY) await conn.beginTransaction();
  try {
    const result = [];
    for (const g of groups) {
      const items = parseSheet(wb, g.sheet);
      const ins = [];
      const outs = [];
      for (const it of items) {
        const m = byName.get(norm(ALIAS[it.name] || it.name));
        if (!m) { console.log(`[未匹配] ${it.name}`); continue; }
        if (it.inQty > 0) ins.push({ mat: m, qty: it.inQty });
        if (it.outQty > 0) outs.push({ mat: m, qty: it.outQty });
      }
      const inTotal = Number(ins.reduce((s, x) => s + x.qty, 0).toFixed(2));
      const outTotal = Number(outs.reduce((s, x) => s + x.qty, 0).toFixed(2));

      if (ins.length) {
        inSeq++;
        const orderNo = `RK20260930${pad(inSeq)}`;
        if (!DRY) {
          const r = await run(
            `insert into wms_inbound_order
             (order_no, order_type, supplier_id, warehouse_id, status, inbound_date, complete_date, total_qty, total_amount,
              del_flag, create_by, create_time, remark, dept_id)
             values (?, '0', null, ?, '3', ?, ?, ?, 0, '0', ?, now(), ?, 100)`,
            [orderNo, WH, `${DAY} 09:00:00`, `${DAY} 17:00:00`, inTotal, USER, `${MARK}（${g.label}）本期入库汇总，来源：9月洗涤库存表`]);
          for (const x of ins) {
            await run(
              `insert into wms_inbound_order_detail
               (order_id, material_id, plan_qty, received_qty, putaway_qty, location_id, unit_price, del_flag, create_by, create_time, remark)
               values (?,?,?,?,?,?,0,'0',?,now(),?)`,
              [r.insertId, x.mat.material_id, x.qty, x.qty, x.qty, LOC, USER, MARK]);
          }
        }
        result.push(`入库单 ${orderNo}：${ins.length} 行 / ${inTotal}（${g.label}）`);
      }

      if (outs.length) {
        outSeq++;
        const orderNo = `OUT20260930${pad(outSeq)}`;
        if (!DRY) {
          const r = await run(
            `insert into wms_outbound_order
             (order_no, order_type, warehouse_id, status, outbound_date, complete_date, total_qty,
              del_flag, create_by, create_time, remark, dept_id)
             values (?, '1', ?, '3', ?, ?, ?, '0', ?, now(), ?, 104)`,
            [orderNo, WH, `${DAY} 09:00:00`, `${DAY} 17:00:00`, outTotal, USER, `${MARK}（${g.label}）本期出库汇总，来源：9月洗涤库存表`]);
          for (const x of outs) {
            await run(
              `insert into wms_outbound_order_detail
               (order_id, material_id, plan_qty, actual_qty, location_id, pick_qty, del_flag, create_by, create_time, remark)
               values (?,?,?,?,?,?,'0',?,now(),?)`,
              [r.insertId, x.mat.material_id, x.qty, x.qty, LOC, x.qty, USER, MARK]);
          }
        }
        result.push(`出库单 ${orderNo}：${outs.length} 行 / ${outTotal}（${g.label}）`);
      }
    }

    if (!DRY) await conn.commit();
    console.log('\n========== 汇总单据 ==========');
    result.forEach(r => console.log(r));
    console.log(DRY ? '\n[试算模式] 未写库' : '\n[已写库] 事务提交完成');
  } catch (e) {
    if (!DRY) await conn.rollback();
    console.error('[失败]', e.message);
    throw e;
  } finally {
    await conn.end();
  }
})().catch(e => { console.error(e); process.exit(1); });
