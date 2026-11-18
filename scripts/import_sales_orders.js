/**
 * 导入《2026上海滇胤销售清单.xlsx》→ 营销管理：客户 / 合同 / 销售订单 / 发货执行
 * 运行: node scripts/import_sales_orders.js
 */
const XLSX = require('xlsx');
const mysql = require('mysql2/promise');

const FILE = 'c:\\Users\\Administrator\\.trae-cn\\attachments\\461a5911-6f7b-4e5d-a482-58110af4ef74_7c69b278-837b-4a05-b7cd-26fa862819fb_2026上海滇胤销售清单.xlsx';
const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4' };
const USER = 'admin';
const DEPT = 106;

function cell(row, i) { return row && row[i] != null ? String(row[i]).trim() : ''; }
function pad(n, w) { return String(n).padStart(w, '0'); }
function ymd(d) {
  const m = String(d).match(/(\d{4})[\/.\-](\d{1,2})[\/.\-](\d{1,2})/);
  if (m) return `${m[1]}-${pad(m[2], 2)}-${pad(m[3], 2)}`;
  return null;
}

function detectCustomer(raw) {
  if (!raw) return null;
  if (raw.includes('新农垦')) return { name: '新农垦', type: '1', level: '2' };
  if (raw.includes('雷景华')) return { name: '雷景华', type: '3', level: '3', phone: '13901733270' };
  if (raw.includes('滇胤')) return { name: '上海滇胤贸易有限公司', type: '2', level: '5' };
  return null;
}

function extractAddress(raw) {
  const m = raw.match(/地址[：:]\s*(.+)$/);
  return m ? m[1].trim() : '';
}

async function main() {
  const conn = await mysql.createConnection(DB);

  // 幂等清理：仅清理本脚本生成的数据（HT2026/XS2026/FH2026 前缀）
  await conn.query("DELETE FROM mk_shipment_detail WHERE shipment_id IN (SELECT shipment_id FROM (SELECT shipment_id FROM mk_shipment WHERE shipment_no LIKE 'FH2026%') x)");
  await conn.query("DELETE FROM mk_shipment WHERE shipment_no LIKE 'FH2026%'");
  await conn.query("DELETE FROM mk_order_item WHERE order_id IN (SELECT order_id FROM (SELECT order_id FROM mk_order WHERE order_no LIKE 'XS2026%') x)");
  await conn.query("DELETE FROM mk_order WHERE order_no LIKE 'XS2026%'");
  await conn.query("DELETE FROM mk_contract WHERE contract_no LIKE 'HT2026%'");

  // ---------- 解析 Excel ----------
  const wb = XLSX.readFile(FILE);
  const orders = [];
  for (const sheet of wb.SheetNames) {
    const rows = XLSX.utils.sheet_to_json(wb.Sheets[sheet], { header: 1, defval: '' });
    let custRaw = '', dateStr = '', operator = '';
    for (let i = 0; i < Math.min(6, rows.length); i++) {
      for (let c = 0; c < (rows[i] || []).length; c++) {
        const t = cell(rows[i], c);
        if (!custRaw && t.startsWith('客户名称')) custRaw = t.replace(/客户名称[：:]/, '').trim();
        if (!dateStr && t.includes('日期')) dateStr = t.replace(/^.*日期[：:]/, '').trim();
        if (t.startsWith('开单人')) {
          const m = t.match(/开单人[：:]\s*([^\s　]+)/);
          if (m) operator = m[1];
        }
      }
    }
    if (!dateStr) {
      const m = sheet.match(/^(\d{1,2})\.(\d{1,2})/);
      if (m) dateStr = `2026/${m[1]}/${m[2]}`;
    }
    const date = dateStr ? ymd(dateStr) : null;
    let hi = -1;
    for (let i = 0; i < rows.length; i++) if (cell(rows[i], 0) === '序号') { hi = i; break; }
    if (hi < 0 || !date) continue;
    const lines = [];
    for (let i = hi + 1; i < rows.length; i++) {
      const r = rows[i];
      const no = cell(r, 0);
      if (!no || isNaN(Number(no))) continue;
      const name = cell(r, 1);
      if (!name) continue;
      lines.push({
        lineNo: Number(no), name, unit: cell(r, 2), spec: cell(r, 3),
        qty: Number(r[4]) || 0, price: Number(r[5]) || 0, amount: Number(r[6]) || 0, remark: cell(r, 7)
      });
    }
    if (!lines.length) continue;
    const c = detectCustomer(custRaw) || { name: custRaw.split(/\s{2,}/)[0], type: '1', level: '3' };
    orders.push({
      sheet, date, customer: c, custRaw, address: extractAddress(custRaw),
      operator, lines, amount: lines.reduce((a, b) => a + b.amount, 0),
      qty: lines.reduce((a, b) => a + b.qty, 0)
    });
  }
  console.log(`解析：${orders.length} 张销售单，${orders.reduce((a, b) => a + b.lines.length, 0)} 行明细，金额 ${orders.reduce((a, b) => a + b.amount, 0).toFixed(2)}`);

  // ---------- 客户 ----------
  const custGroups = new Map();
  for (const o of orders) {
    const key = o.customer.name;
    if (!custGroups.has(key)) custGroups.set(key, { info: o.customer, address: o.address, dates: [], amount: 0 });
    const g = custGroups.get(key);
    g.dates.push(o.date);
    g.amount += o.amount;
    if (!g.address && o.address) g.address = o.address;
  }
  const [maxCust] = await conn.query("SELECT MAX(CAST(RIGHT(customer_no,4) AS UNSIGNED)) mx FROM mk_customer WHERE customer_no LIKE 'KH%'");
  let custSeq = Number(maxCust[0].mx || 0);
  const custIds = {};
  for (const [name, g] of custGroups) {
    const [exist] = await conn.query('SELECT customer_id FROM mk_customer WHERE customer_name=? AND del_flag=\'0\' LIMIT 1', [name]);
    if (exist.length) { custIds[name] = exist[0].customer_id; console.log(`  客户已存在 ${name}`); continue; }
    custSeq++;
    const no = 'KH2026' + pad(custSeq, 6);
    const firstDate = g.dates.sort()[0];
    const [r] = await conn.query(
      `INSERT INTO mk_customer (customer_no, customer_name, customer_type, customer_level, customer_status, customer_source,
        industry, province, city, address, contact_phone, first_contact_date, cooperation_date, total_amount,
        credit_limit, credit_days, credit_status, dept_id, del_flag, create_by, create_time, remark)
       VALUES (?, ?, ?, ?, '2', '4', '5', '上海市', '上海市', ?, ?, ?, ?, ?, ?, ?, '0', ?, '0', ?, NOW(), ?)`,
      [no, name, g.info.type, g.info.level, g.address || null, g.info.phone || null, firstDate, firstDate,
       g.amount, g.info.type === '2' ? 500000 : 0, g.info.type === '2' ? 30 : 0, DEPT, USER, '销售清单导入']);
    custIds[name] = r.insertId;
    console.log(`  + 客户 ${name} ${no}`);
  }

  // ---------- 单价/产品映射 ----------
  const [mats] = await conn.query("SELECT material_id, material_code, material_name FROM wms_material WHERE del_flag='0'");
  const matMap = new Map(mats.map(m => [m.material_name, m]));

  // ---------- 合同：每个客户 1 份年度合同 ----------
  const [maxContract] = await conn.query("SELECT MAX(CAST(RIGHT(contract_no,4) AS UNSIGNED)) mx FROM mk_contract WHERE contract_no LIKE 'HT%'");
  let contractSeq = Number(maxContract[0].mx || 0);
  const contractIds = {};
  for (const [name, g] of custGroups) {
    contractSeq++;
    const no = 'HT2026' + pad(contractSeq, 4);
    const signDate = g.dates.sort()[0];
    const [r] = await conn.query(
      `INSERT INTO mk_contract (contract_no, contract_name, contract_type, customer_id, contract_amount, sign_date,
        effective_date, expire_date, payment_method, contract_status, dept_id, del_flag, create_by, create_time, remark)
       VALUES (?, ?, '0', ?, ?, ?, '2026-01-01', '2026-12-31', '0', '2', ?, '0', ?, NOW(), ?)`,
      [no, `2026年度销售合同（${name}）`, custIds[name], g.amount, signDate, DEPT, USER, '销售清单汇总生成']);
    contractIds[name] = r.insertId;
    console.log(`  + 合同 ${no} ${name} ¥${g.amount.toFixed(2)}`);
  }

  // ---------- 订单 + 发货 ----------
  const daySeq = new Map();
  let orderCnt = 0, itemCnt = 0, shipCnt = 0, shipItemCnt = 0;
  for (const o of orders) {
    const d = o.date.replace(/-/g, '');
    const seq = (daySeq.get(d) || 0) + 1;
    daySeq.set(d, seq);
    const orderNo = 'XS' + d + pad(seq, 3);
    const shipNo = 'FH' + d + pad(seq, 3);
    const trackNo = 'DPK' + d + pad(seq, 4);
    const [or] = await conn.query(
      `INSERT INTO mk_order (order_no, contract_id, customer_id, order_amount, shipped_qty, order_status,
        logistics_company, tracking_no, deliver_time, finish_time, confirm_time, receive_person,
        audit_by, audit_time, audit_opinion, dept_id, del_flag, create_by, create_time, settle_type, remark)
       VALUES (?, ?, ?, ?, ?, '4', '德邦快递', ?, ?, ?, ?, ?, ?, ?, '审核通过', ?, '0', ?, NOW(), ?, ?)`,
      [orderNo, contractIds[o.customer.name], custIds[o.customer.name], o.amount, o.qty,
       trackNo, o.date + ' 09:00:00', o.date + ' 18:00:00', o.date + ' 20:00:00', '收货部',
       USER, o.date + ' 08:30:00', DEPT, USER, o.customer.type === '2' ? '1' : '0',
       `来源工作表：${o.sheet}｜客户原文：${o.custRaw}｜开单人：${o.operator}`]);
    orderCnt++;

    const itemIds = [];
    for (const l of o.lines) {
      const m = matMap.get(l.name);
      const [ir] = await conn.query(
        `INSERT INTO mk_order_item (order_id, line_no, material_id, material_code, product_name, product_spec,
          unit, quantity, shipped_qty, unit_price, subtotal, create_time, remark)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)`,
        [or.insertId, l.lineNo, m ? m.material_id : null, m ? m.material_code : null, l.name,
         l.spec || null, l.unit || null, l.qty, l.qty, l.price, l.amount, l.remark || null]);
      itemIds.push({ id: ir.insertId, line: l, mat: m });
      itemCnt++;
    }

    const [sr] = await conn.query(
      `INSERT INTO mk_shipment (shipment_no, order_id, order_no, contract_id, contract_no, customer_id, customer_name,
        status, shipment_date, logistics_company, tracking_no, deliver_time, total_qty, total_amount,
        receiver_name, receiver_phone, receiver_address, shipper_name, audit_by, audit_time, audit_opinion,
        del_flag, create_by, create_time, remark)
       VALUES (?, ?, ?, ?, (SELECT contract_no FROM mk_contract WHERE contract_id=?), ?, ?, '2', ?, '德邦快递', ?, ?,
        ?, ?, ?, ?, ?, ?, ?, ?, '审核通过', '0', ?, NOW(), ?)`,
      [shipNo, or.insertId, orderNo, contractIds[o.customer.name], contractIds[o.customer.name],
       custIds[o.customer.name], o.customer.name, o.date, trackNo, o.date + ' 10:00:00',
       o.qty, o.amount, '收货部', o.customer.phone || null, o.address || null, o.operator || '吴亭亭',
       USER, o.date + ' 18:00:00', USER, `对应销售单 ${o.sheet}`]);
    shipCnt++;

    for (const it of itemIds) {
      await conn.query(
        `INSERT INTO mk_shipment_detail (shipment_id, order_item_id, product_name, product_spec, material_id, material_code,
          material_name, spec_model, unit, order_qty, ship_qty, unit_price, amount, del_flag, create_by, create_time)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '0', ?, NOW())`,
        [sr.insertId, it.id, it.line.name, it.line.spec || null, it.mat ? it.mat.material_id : null,
         it.mat ? it.mat.material_code : null, it.line.name, it.line.spec || null, it.line.unit || null,
         it.line.qty, it.line.qty, it.line.price, it.line.amount, USER]);
      shipItemCnt++;
    }
  }

  // 客户累计金额
  for (const [name, g] of custGroups) {
    await conn.query('UPDATE mk_customer SET total_amount=? WHERE customer_id=?', [g.amount, custIds[name]]);
  }

  const [sum] = await conn.query(`
    SELECT (SELECT COUNT(*) FROM mk_contract WHERE contract_no LIKE 'HT2026%') contracts,
           (SELECT COUNT(*) FROM mk_order WHERE order_no LIKE 'XS2026%') orders,
           (SELECT COUNT(*) FROM mk_order_item i JOIN mk_order o ON i.order_id=o.order_id WHERE o.order_no LIKE 'XS2026%') order_items,
           (SELECT COUNT(*) FROM mk_shipment WHERE shipment_no LIKE 'FH2026%') shipments,
           (SELECT COUNT(*) FROM mk_shipment_detail d JOIN mk_shipment s ON d.shipment_id=s.shipment_id WHERE s.shipment_no LIKE 'FH2026%') ship_details,
           (SELECT SUM(order_amount) FROM mk_order WHERE order_no LIKE 'XS2026%') amount`);
  console.log('\n=== 导入完成 ===');
  console.log(sum[0]);
  console.log(`本次：订单 ${orderCnt}，明细 ${itemCnt}，发货单 ${shipCnt}，发货明细 ${shipItemCnt}`);
  await conn.end();
}

main().catch(e => { console.error('FATAL:', e.message); process.exit(1); });
