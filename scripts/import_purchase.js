/**
 * 2026年采购查验台账 → EDOP ERP 数据导入脚本
 * 
 * 数据流向:
 *   Excel Sheet → WMS供应商 + WMS物料 → PMS采购订单+明细 → PMS收货+明细 → WMS入库单+明细
 * 
 * 运行: node scripts/import_purchase.js
 */

const XLSX = require('xlsx');
const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

const EXCEL = process.env.EXCEL_FILE || 
  'c:\\Users\\Administrator\\.trae-cn\\attachments\\de322f53-3d51-419b-a424-e852566fea2c_cb731e4d-6294-4d3d-9419-6ab8c197f652_2026采购查验台账.xlsx';

const DB = { host:'localhost', port:3306, user:'root', password:'ZBrj@2026', database:'ruoyi_ylsp', charset:'utf8mb4' };

// ============ 工具函数 ============
function excelDate(n) {
  if (typeof n !== 'number' || isNaN(n)) return null;
  const ms = Math.round((n - 25569) * 86400 * 1000);
  const d = new Date(ms);
  if (d.getFullYear() < 2000 || d.getFullYear() > 2100) return null;
  return d.toISOString().slice(0,10);
}
function safe(v) { return v === undefined || v === null ? '' : String(v).trim(); }
function num(v) { const n = Number(v); return isNaN(n) ? 0 : n; }
function esc(s) { return String(s || '').replace(/\\/g,'\\\\').replace(/'/g,"\\'"); }
function sqlVal(v) {
  if (v === null || v === undefined || v === '') return 'NULL';
  if (typeof v === 'number') return v;
  return `'${esc(v)}'`;
}
function monthsToDays(s) {
  if (!s) return null;
  const m = String(s).match(/(\d+)\s*个月/);
  if (m) return parseInt(m[1]) * 30;
  const y = String(s).match(/(\d+)\s*年/);
  if (y) return parseInt(y[1]) * 365;
  const d = String(s).match(/(\d+)\s*天/);
  if (d) return parseInt(d[1]);
  return null;
}
function addDays(dateStr, days) {
  if (!dateStr || !days) return null;
  const d = new Date(dateStr);
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0,10);
}

// ============ 读取 Excel ============
const wb = XLSX.readFile(EXCEL);
console.log('Excel sheets:', wb.SheetNames.join(', '));

// Sheet 配置: material_type, warehouse_id
const SHEET_CFG = {
  '原料':           { matType:'0', warehouse:1, hasExpiry:true },
  '火腿猪肉原料':   { matType:'0', warehouse:1, hasExpiry:true },
  '辅料':           { matType:'3', warehouse:3, hasExpiry:true },
  '包材·配件':      { matType:'0', warehouse:3, hasExpiry:false }, // 归入原材料，可后续加字典
};

// 统一的数据结构 { rows: [{...}], cfg }
const allRows = []; // 扁平数据行，带来源 sheet

for (const sheetName of wb.SheetNames) {
  if (sheetName === '报价') continue;
  const cfg = SHEET_CFG[sheetName];
  if (!cfg) { console.log('跳过:', sheetName); continue; }
  
  const rows = XLSX.utils.sheet_to_json(wb.Sheets[sheetName], { header:1, defval:'' });
  // 找表头行（包含"品名"的行）
  const headerIdx = rows.findIndex(r => r.some(c => String(c).includes('品名')));
  if (headerIdx < 0) continue;
  const headers = rows[headerIdx].map(h => safe(h));
  const dataRows = rows.slice(headerIdx + 1).filter(r => r.some(c => safe(c) !== ''));
  
  for (const r of dataRows) {
    const obj = {};
    headers.forEach((h, i) => obj[h] = r[i]);
    obj.__sheet = sheetName;
    obj.__cfg = cfg;
    allRows.push(obj);
  }
}

console.log('=== 读取到数据行:', allRows.length, '===\n');

// ============ 数据清洗 ============
// 统一字段名映射
const cleanRows = allRows.map(r => {
  const guarantee = safe(r['保质期限']);
  const days = monthsToDays(guarantee);
  const extBatchRaw = safe(r['产品批号']);
  const dateStr = excelDate(r['查验入库日期']);
  
  return {
    sheet: r.__sheet,
    cfg: r.__cfg,
    date: dateStr,
    name: safe(r['品名']),
    unit: safe(r['单位']),
    spec: safe(r['规格']),
    qty: num(r['数量']),
    price: num(r['单价']),
    amount: num(r['金额']),
    buyer: safe(r['采购人']) || safe(r['采购']),
    supplier: safe(r['供货商']) || safe(r['供应商']),
    batchNo: extBatchRaw,
    guaranteeMonths: guarantee,
    shelfLifeDays: days,
    expiryDate: dateStr && days ? addDays(dateStr, days) : null,
    licenseOk: safe(r['是否有营业执照']) === '是',
    permitOk: safe(r['是否有生产或经营许可证']) === '是',
    reportOk: safe(r['是否有         产品检验报告']) || safe(r['产品检验报告']),
    contact: safe(r['供货商联系方式']),
    receiver: safe(r['查验入库']) || safe(r['收货人']),
  };
}).filter(r => r.name && r.supplier); // 必须有品名和供货商

console.log('清洗后有效行:', cleanRows.length);
console.log('日期范围:', cleanRows.reduce((a,b)=>!a.date||b.date&&b.date<a.date?b:a,{}).date, 
  '~', cleanRows.reduce((a,b)=>!a.date||b.date&&b.date>a.date?b:a,{}).date);

// ============ 去重收集 ============
const supplierMap = new Map(); // name -> supplier
const materialMap = new Map(); // sheet+name+spec -> material

cleanRows.forEach(r => {
  if (!supplierMap.has(r.supplier)) {
    supplierMap.set(r.supplier, { name:r.supplier, contact:r.contact, licenseOk:r.licenseOk, permitOk:r.permitOk });
  } else {
    const s = supplierMap.get(r.supplier);
    if (!s.contact && r.contact) s.contact = r.contact;
    if (!s.licenseOk) s.licenseOk = r.licenseOk;
    if (!s.permitOk) s.permitOk = r.permitOk;
  }
  const mk = r.sheet + '|' + r.name + '|' + r.spec;
  if (!materialMap.has(mk)) {
    materialMap.set(mk, { sheet:r.sheet, name:r.name, spec:r.spec, unit:r.unit, 
      shelfLifeDays:r.shelfLifeDays, hasExpiry:r.cfg.hasExpiry });
  }
});

console.log('\n=== 唯一供应商:', supplierMap.size, '===');
Array.from(supplierMap.keys()).forEach(n => console.log('  -', n));

console.log('\n=== 唯一物料:', materialMap.size, '===');
const groupedBySheet = {};
Array.from(materialMap.values()).forEach(m => {
  groupedBySheet[m.sheet] = groupedBySheet[m.sheet] || [];
  groupedBySheet[m.sheet].push(m.name + (m.spec?' ('+m.spec+')':''));
});
Object.entries(groupedBySheet).forEach(([s,names]) => {
  console.log('  ['+s+']', names.length, '项:', names.slice(0,5).join(', '), names.length>5?'...':'');
});

// ============ 连库执行 ============
async function run() {
  const conn = await mysql.createConnection(DB);
  console.log('\n✓ 数据库连接成功\n');
  
  // 1. 查现有最大ID和编码
  const [rows] = await conn.query(`
    SELECT 'supplier' as t, COALESCE(MAX(supplier_id),0) as mx, COALESCE(MAX(CAST(SUBSTRING(supplier_code,4,8) AS SIGNED)),20260005) as cx FROM wms_supplier
    UNION ALL SELECT 'material', COALESCE(MAX(material_id),0), COALESCE(MAX(CAST(SUBSTRING(material_code,3,8) AS SIGNED)),20260010) FROM wms_material
    UNION ALL SELECT 'po', COALESCE(MAX(order_id),0), COALESCE(MAX(CAST(SUBSTRING(order_no,7,10) AS SIGNED)),0) FROM pms_purchase_order
    UNION ALL SELECT 'receive', COALESCE(MAX(receive_id),0), COALESCE(MAX(CAST(SUBSTRING(receive_no,9,10) AS SIGNED)),0) FROM pms_receive
    UNION ALL SELECT 'inbound', COALESCE(MAX(order_id),0), COALESCE(MAX(CAST(SUBSTRING(order_no,9,10) AS SIGNED)),0) FROM wms_inbound_order
  `);
  const ids = {}; rows.forEach(r => ids[r.t] = r.mx);
  const codes = {}; rows.forEach(r => codes[r.t] = r.cx);
  console.log('现有最大ID:', JSON.stringify(ids));
  console.log('现有最大编码序号:', JSON.stringify(codes));

  // ========== STEP 1: 供应商 ==========
  const supplierArr = Array.from(supplierMap.values());
  const supplierIdMap = new Map(); // name -> new_id
  let supCodeSeq = (codes.supplier || 20260005) + 1;
  let nextSupId = (ids.supplier || 5) + 1;
  
  console.log('\n=== STEP 1: 导入', supplierArr.length, '家供应商 ===');
  for (const s of supplierArr) {
    const code = 'GYS' + supCodeSeq++;
    const status = (s.licenseOk && s.permitOk) ? '1' : '0'; // 已审核 or 未审核
    const type = '0'; // 原材料供应商
    const sql = `INSERT INTO wms_supplier 
      (supplier_id, supplier_code, supplier_name, contact_phone, supplier_type, qualification_status, status, del_flag, create_by, create_time)
      VALUES (${nextSupId}, ${sqlVal(code)}, ${sqlVal(s.name)}, ${sqlVal(s.contact)}, ${sqlVal(type)}, ${sqlVal(status)}, '0', '0', 'admin', NOW())`;
    await conn.query(sql);
    supplierIdMap.set(s.name, nextSupId);
    console.log(`  + ${code} ${s.name}`);
    nextSupId++;
  }
  console.log('✓ 供应商导入完成');

  // ========== STEP 2: 物料 ==========
  const materialArr = Array.from(materialMap.values());
  const materialIdMap = new Map(); // sheet|name|spec -> new_id
  let matCodeSeq = (codes.material || 20260010) + 1;
  let nextMatId = (ids.material || 10) + 1;
  
  console.log('\n=== STEP 2: 导入', materialArr.length, '个物料 ===');
  for (const m of materialArr) {
    const code = 'WL' + matCodeSeq++;
    const type = SHEET_CFG[m.sheet].matType;
    const shelfDays = m.shelfLifeDays || (m.hasExpiry ? 365 : null);
    const expiry = shelfDays ? '1' : '0';
    const batch = '1'; // 全部批次管理
    const unit = m.unit || 'kg';
    const sql = `INSERT INTO wms_material 
      (material_id, material_code, material_name, material_type, spec_model, unit, 
       shelf_life_days, is_expiry_manage, is_batch_manage, status, del_flag, create_by, create_time)
      VALUES (${nextMatId}, ${sqlVal(code)}, ${sqlVal(m.name)}, ${sqlVal(type)}, ${sqlVal(m.spec)}, ${sqlVal(unit)},
       ${shelfDays || 'NULL'}, ${sqlVal(expiry)}, ${sqlVal(batch)}, '0', '0', 'admin', NOW())`;
    await conn.query(sql);
    materialIdMap.set(m.sheet + '|' + m.name + '|' + m.spec, nextMatId);
    if (matCodeSeq % 20 === 0 || matCodeSeq === materialArr.length + 11) {
      console.log(`  ... ${matCodeSeq - 1}/${materialArr.length}`);
    }
    nextMatId++;
  }
  console.log('✓ 物料导入完成:', nextMatId - 1 - 10, '个');

  // ========== STEP 3: 按日期+供应商 分组 ==========
  // 同一日期+同一供应商+同一sheet = 一张采购订单
  const groups = new Map(); // key: date|supplier|sheet -> [rows]
  for (const r of cleanRows) {
    const k = (r.date || 'UNKNOWN') + '|' + r.supplier + '|' + r.sheet;
    if (!groups.has(k)) groups.set(k, []);
    groups.get(k).push(r);
  }
  console.log('\n=== STEP 3: 生成', groups.size, '张采购订单 (按日期+供应商+sheet分组) ===');

  let poCodeSeq = (codes.po || 0) + 1;
  let nextPoId = (ids.po || 0) + 1;
  let nextDetailId = 1;
  let nextReceiveId = (ids.receive || 0) + 1;
  let nextReceiveDetailId = 1;
  let nextInboundId = (ids.inbound || 0) + 1;
  let nextInboundDetailId = 1;
  let recvCodeSeq = (codes.receive || 0) + 1;
  let inboundCodeSeq = (codes.inbound || 0) + 1;

  let totalPO = 0, totalRecv = 0, totalInbound = 0;

  for (const [key, rows] of groups) {
    const [date, supplier, sheet] = key.split('|');
    const supId = supplierIdMap.get(supplier);
    if (!supId) { console.log('  SKIP (no supplier):', key); continue; }

    // 汇总 PO 数据
    let totalQty = 0, totalAmount = 0;
    const poDetails = [];
    for (const r of rows) {
      const mk = r.sheet + '|' + r.name + '|' + r.spec;
      const matId = materialIdMap.get(mk);
      if (!matId) { console.log('  SKIP (no material):', mk); continue; }
      totalQty += r.qty;
      totalAmount += r.amount;
      poDetails.push({ r, matId });
    }
    if (poDetails.length === 0) continue;

    // === 采购订单 ===
    const poNo = 'PO' + String(poCodeSeq++).padStart(10,'0');
    const warehouseId = SHEET_CFG[sheet].warehouse;
    const buyerName = rows[0].buyer || '宋春静';
    const orderDate = date !== 'UNKNOWN' ? date : '2026-01-01';
    
    await conn.query(`INSERT INTO pms_purchase_order 
      (order_id, order_no, supplier_id, supplier_name, warehouse_id, status,
       order_date, expect_delivery_date, actual_delivery_date,
       total_qty, total_amount, buyer_name, dept_id, del_flag, create_by, create_time)
      VALUES (${nextPoId}, ${sqlVal(poNo)}, ${supId}, ${sqlVal(supplier)}, ${warehouseId}, '5',
       ${sqlVal(orderDate)}, ${sqlVal(orderDate)}, ${sqlVal(orderDate)},
       ${totalQty.toFixed(2)}, ${totalAmount.toFixed(2)}, ${sqlVal(buyerName)}, 103, '0', 'admin', NOW())`);
    const poId = nextPoId++;
    totalPO++;

    // === 先创建收货单和入库单（主表），拿到 ID ===
    let recvTotalQty = 0, recvTotalAmount = 0, qualifiedSum = 0;
    const recvNo = 'RV' + String(recvCodeSeq++).padStart(10,'0');
    const inboundNo = 'RK' + String(inboundCodeSeq++).padStart(10,'0');
    const receiver = rows[0].receiver || '张姣';

    // 入库单（状态=已完成）
    await conn.query(`INSERT INTO wms_inbound_order 
      (order_id, order_no, order_type, supplier_id, warehouse_id, status,
       inbound_date, complete_date, total_qty, total_amount, del_flag, create_by, create_time)
      VALUES (${nextInboundId}, ${sqlVal(inboundNo)}, '0', ${supId}, ${warehouseId}, '3',
       ${sqlVal(orderDate)}, ${sqlVal(orderDate)}, 0, 0, '0', 'admin', NOW())`);
    const inboundId = nextInboundId++;
    totalInbound++;

    // 收货单（状态=已入库）
    await conn.query(`INSERT INTO pms_receive 
      (receive_id, receive_no, order_id, order_no, supplier_id, supplier_name, warehouse_id, status,
       receive_date, total_qty, qualified_qty, unqualified_qty,
       inspector_name, inbound_order_id, del_flag, dept_id, create_by, create_time)
      VALUES (${nextReceiveId}, ${sqlVal(recvNo)}, ${poId}, ${sqlVal(poNo)}, ${supId}, ${sqlVal(supplier)}, ${warehouseId}, '3',
       ${sqlVal(orderDate)}, 0, 0, 0,
       ${sqlVal(receiver)}, ${inboundId}, '0', 103, 'admin', NOW())`);
    const receiveId = nextReceiveId++;
    totalRecv++;

    // === 明细全部创建 ===
    for (const { r, matId } of poDetails) {
      // PO detail
      await conn.query(`INSERT INTO pms_purchase_order_detail 
        (detail_id, order_id, material_id, material_code, material_name, spec_model, unit, qty, unit_price, amount, received_qty, del_flag, create_by, create_time)
        VALUES (${nextDetailId}, ${poId}, ${matId}, 
         (SELECT material_code FROM wms_material WHERE material_id=${matId}),
         ${sqlVal(r.name)}, ${sqlVal(r.spec)}, ${sqlVal(r.unit)},
         ${r.qty.toFixed(2)}, ${r.price.toFixed(2)}, ${r.amount.toFixed(2)}, ${r.qty.toFixed(2)},
         '0', 'admin', NOW())`);
      const poDetailId = nextDetailId++;

      // 收货明细（receive_id 已创建）
      await conn.query(`INSERT INTO pms_receive_detail 
        (detail_id, receive_id, order_detail_id, material_id, material_code, material_name, spec_model, unit,
         receive_qty, qualified_qty, unqualified_qty, unit_price, amount,
         batch_no, production_date, expiry_date, del_flag, create_by, create_time)
        VALUES (${nextReceiveDetailId}, ${receiveId}, ${poDetailId}, ${matId},
         (SELECT material_code FROM wms_material WHERE material_id=${matId}),
         ${sqlVal(r.name)}, ${sqlVal(r.spec)}, ${sqlVal(r.unit)},
         ${r.qty.toFixed(2)}, ${r.qty.toFixed(2)}, 0, ${r.price.toFixed(2)}, ${r.amount.toFixed(2)},
         ${sqlVal(r.batchNo)}, NULL, ${sqlVal(r.expiryDate)},
         '0', 'admin', NOW())`);
      nextReceiveDetailId++;

      // 入库明细（order_id 已创建）
      await conn.query(`INSERT INTO wms_inbound_order_detail 
        (detail_id, order_id, material_id, plan_qty, received_qty, putaway_qty,
         batch_no, production_date, expiry_date, unit_price, del_flag, create_by, create_time)
        VALUES (${nextInboundDetailId}, ${inboundId}, ${matId},
         ${r.qty.toFixed(2)}, ${r.qty.toFixed(2)}, ${r.qty.toFixed(2)},
         ${sqlVal(r.batchNo)}, NULL, ${sqlVal(r.expiryDate)},
         ${r.price.toFixed(2)}, '0', 'admin', NOW())`);
      nextInboundDetailId++;

      recvTotalQty += r.qty;
      recvTotalAmount += r.amount;
      qualifiedSum += r.qty;
    }

    // 回填收货单和入库单的汇总数量
    await conn.query(`UPDATE pms_receive SET total_qty=${recvTotalQty.toFixed(2)}, qualified_qty=${qualifiedSum.toFixed(2)} WHERE receive_id=${receiveId}`);
    await conn.query(`UPDATE wms_inbound_order SET total_qty=${recvTotalQty.toFixed(2)}, total_amount=${recvTotalAmount.toFixed(2)} WHERE order_id=${inboundId}`);

    if (totalPO % 25 === 0) console.log(`  ... PO ${totalPO}, RV ${totalRecv}, RK ${totalInbound}`);
  }

  console.log('\n========================================');
  console.log('导入完成!');
  console.log('  供应商:', supplierArr.length, '(新增)');
  console.log('  物料:', materialArr.length, '(新增)');
  console.log('  采购订单:', totalPO);
  console.log('  收货单:', totalRecv);
  console.log('  入库单:', totalInbound);
  console.log('========================================');

  // 最终验证
  const [v] = await conn.query(`
    SELECT 'wms_supplier' as t, COUNT(*) as c FROM wms_supplier
    UNION ALL SELECT 'wms_material', COUNT(*) FROM wms_material
    UNION ALL SELECT 'pms_purchase_order', COUNT(*) FROM pms_purchase_order
    UNION ALL SELECT 'pms_purchase_order_detail', COUNT(*) FROM pms_purchase_order_detail
    UNION ALL SELECT 'pms_receive', COUNT(*) FROM pms_receive
    UNION ALL SELECT 'pms_receive_detail', COUNT(*) FROM pms_receive_detail
    UNION ALL SELECT 'wms_inbound_order', COUNT(*) FROM wms_inbound_order
    UNION ALL SELECT 'wms_inbound_order_detail', COUNT(*) FROM wms_inbound_order_detail
  `);
  console.log('\n=== 数据库各表记录数 ===');
  v.forEach(r => console.log(`  ${r.t.padEnd(30)} ${r.c}`));

  await conn.end();
}

run().catch(err => { console.error('FATAL:', err); process.exit(1); });
