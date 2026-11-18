/**
 * 为已导入的 113 张入库单生成：上架作业（库位/批次）+ 批次 + 库存 + 库存流水
 * 运行: node scripts/gen_inbound_inventory.js
 */
const mysql = require('mysql2/promise');

const DB = { host:'localhost', port:3306, user:'root', password:'ZBrj@2026', database:'ruoyi_ylsp', charset:'utf8mb4' };
const OPERATOR = 'admin';
const TODAY = new Date().toISOString().slice(0, 10);

function fmtDate(v) {
  if (!v) return null;
  if (v instanceof Date) return v.toISOString().slice(0, 10);
  return String(v).slice(0, 10);
}

async function main() {
  const conn = await mysql.createConnection(DB);
  console.log('✓ 数据库已连接');

  // 1. 批次库存表（库存双写依赖）
  await conn.query(`CREATE TABLE IF NOT EXISTS food_batch_stock (
    bs_id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    material_id bigint NOT NULL COMMENT '物料ID',
    warehouse_id bigint NOT NULL COMMENT '仓库ID',
    location_id bigint NOT NULL COMMENT '库位ID',
    batch_id bigint NOT NULL COMMENT '批次ID',
    qty decimal(18,2) NOT NULL DEFAULT 0 COMMENT '批次库存量',
    qty_locked decimal(18,2) NOT NULL DEFAULT 0 COMMENT '锁定/预留量',
    version int NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_by varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    update_by varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (bs_id),
    UNIQUE KEY uk_batch_stock (material_id, warehouse_id, location_id, batch_id),
    KEY idx_batch_stock_batch (batch_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='食品批次库存表'`);
  console.log('✓ food_batch_stock 就绪');

  // 2. 默认库区/库位
  async function ensureLocation(whId, areaCode, areaName, locCode, locName) {
    let [rows] = await conn.query('SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code=?', [areaCode]);
    let areaId;
    if (rows.length) areaId = rows[0].warehouse_id;
    else {
      const [r] = await conn.query(
        `INSERT INTO wms_warehouse (parent_id, ancestors, node_type, warehouse_code, warehouse_name, dept_id, status, del_flag, create_by, create_time, remark)
         VALUES (?, ?, '2', ?, ?, 106, '0', '0', ?, NOW(), '自动初始化库区')`,
        [whId, ',' + whId, areaCode, areaName, OPERATOR]);
      areaId = r.insertId;
      console.log(`  + 库区 ${areaCode} ${areaName} (id=${areaId})`);
    }
    [rows] = await conn.query('SELECT warehouse_id FROM wms_warehouse WHERE warehouse_code=?', [locCode]);
    if (rows.length) return rows[0].warehouse_id;
    const [r2] = await conn.query(
      `INSERT INTO wms_warehouse (parent_id, ancestors, node_type, warehouse_code, warehouse_name, dept_id, status, del_flag, create_by, create_time, remark)
       VALUES (?, ?, '3', ?, ?, 106, '0', '0', ?, NOW(), '自动初始化库位')`,
      [areaId, ',' + whId + ',' + areaId, locCode, locName, OPERATOR]);
    console.log(`  + 库位 ${locCode} ${locName} (id=${r2.insertId})`);
    return r2.insertId;
  }

  const locMap = {};
  locMap[1] = await ensureLocation(1, 'WH01A02', '原料A区', 'WH01A02L01', '原料A-01库位');
  locMap[3] = await ensureLocation(3, 'WH03A01', '辅料包材A区', 'WH03A01L01', '辅料包材A-01库位');

  // 3. 读取待处理入库明细
  const [details] = await conn.query(`
    SELECT d.detail_id, d.order_id, d.material_id, d.batch_no, d.ext_batch_no,
           d.plan_qty, d.received_qty, d.putaway_qty, d.production_date, d.expiry_date, d.unit_price,
           o.order_no, o.warehouse_id, o.inbound_date,
           m.material_code, m.material_name, m.unit, m.shelf_life_days, m.is_expiry_manage
      FROM wms_inbound_order_detail d
      JOIN wms_inbound_order o ON d.order_id = o.order_id
      JOIN wms_material m ON d.material_id = m.material_id
     WHERE d.location_id IS NULL
     ORDER BY o.inbound_date, d.detail_id`);
  console.log(`\n=== 待生成明细: ${details.length} 条 ===`);

  let seq = 0;
  let batchCnt = 0, invRows = 0, logRows = 0, stockRows = 0;
  const invMap = new Map(); // key -> {inv row}

  for (const d of details) {
    const qty = Number(d.putaway_qty || 0) > 0 ? Number(d.putaway_qty) : Number(d.received_qty || 0);
    const whId = d.warehouse_id;
    const locId = locMap[whId];
    if (!locId) { console.log('  SKIP 无默认库位 warehouse=', whId); continue; }

    let batchNo = '';
    let batchId = null;
    const isExpiry = String(d.is_expiry_manage) === '1';

    if (isExpiry) {
      seq++;
      const receiveDate = fmtDate(d.inbound_date) || TODAY;
      const batchCode = 'PC' + receiveDate.replace(/-/g, '') + '-' + String(seq).padStart(5, '0');
      const expireDate = fmtDate(d.expiry_date);
      const expireStatus = !expireDate ? '0' : (expireDate < TODAY ? '2' : '0');
      const [r] = await conn.query(
        `INSERT INTO food_batch
          (batch_code, ext_batch_code, material_id, material_code, source_type, source_bill_type, source_bill_id,
           produce_date, receive_date, shelf_life_days, expire_date, qty, qty_unit, batch_status, expire_status,
           del_flag, create_by, create_time)
         VALUES (?, ?, ?, ?, '1', 'inbound', ?, ?, ?, ?, ?, ?, ?, '1', ?, '0', ?, NOW())`,
        [batchCode, d.batch_no || null, d.material_id, d.material_code, d.order_id,
         fmtDate(d.production_date), receiveDate, d.shelf_life_days, expireDate,
         qty, d.unit, expireStatus, OPERATOR]);
      batchId = r.insertId;
      batchNo = batchCode;
      batchCnt++;

      // 批次库存（与 wms_inventory 双写）
      await conn.query(
        `INSERT INTO food_batch_stock (material_id, warehouse_id, location_id, batch_id, qty, qty_locked, version, create_by, create_time)
         VALUES (?, ?, ?, ?, ?, 0, 0, ?, NOW())`,
        [d.material_id, whId, locId, batchId, qty, OPERATOR]);
      stockRows++;
    }

    // 更新明细：上架库位 + 批次绑定（原始批号转外部批号）
    await conn.query(
      `UPDATE wms_inbound_order_detail
          SET location_id=?, batch_id=?, batch_no=?, ext_batch_no=?
        WHERE detail_id=?`,
      [locId, batchId, isExpiry ? batchNo : (d.batch_no || ''), isExpiry ? (d.batch_no || null) : d.ext_batch_no, d.detail_id]);

    // 库存聚合键
    const key = [d.material_id, whId, locId, batchNo].join('|');
    if (invMap.has(key)) {
      invMap.get(key).qty += qty;
    } else {
      invMap.set(key, {
        materialId: d.material_id, whId, locId, batchNo, qty,
        productionDate: fmtDate(d.production_date), expiryDate: fmtDate(d.expiry_date),
        orderNo: d.order_no, inboundDate: fmtDate(d.inbound_date) || TODAY
      });
    }
  }

  // 4. 写库存 + 流水
  for (const inv of invMap.values()) {
    const [r] = await conn.query(
      `INSERT INTO wms_inventory
        (material_id, warehouse_id, location_id, batch_no, production_date, expiry_date, qty, lock_qty, version, del_flag, create_by, create_time, update_by, update_time)
       VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, '0', ?, ?, ?, ?)`,
      [inv.materialId, inv.whId, inv.locId, inv.batchNo, inv.productionDate, inv.expiryDate, inv.qty, OPERATOR, inv.inboundDate, OPERATOR, inv.inboundDate]);
    invRows++;

    await conn.query(
      `INSERT INTO wms_inventory_log
        (material_id, warehouse_id, location_id, batch_no, change_type, change_qty, before_qty, after_qty, ref_order_type, ref_order_no, create_by, create_time, remark)
       VALUES (?, ?, ?, ?, '0', ?, 0, ?, 'inbound', ?, ?, ?, '采购入库历史数据初始化')`,
      [inv.materialId, inv.whId, inv.locId, inv.batchNo, inv.qty, inv.qty, inv.orderNo, OPERATOR, inv.inboundDate]);
    logRows++;
  }

  // 5. 校验
  const [v] = await conn.query(`
    SELECT 'food_batch' t, COUNT(*) c FROM food_batch
    UNION ALL SELECT 'food_batch_stock', COUNT(*) FROM food_batch_stock
    UNION ALL SELECT 'wms_inventory', COUNT(*) FROM wms_inventory
    UNION ALL SELECT 'wms_inventory_log', COUNT(*) FROM wms_inventory_log
    UNION ALL SELECT 'detail_with_location', COUNT(*) FROM wms_inbound_order_detail WHERE location_id IS NOT NULL`);

  console.log('\n========================================');
  console.log('生成完成:');
  console.log('  内部批次:', batchCnt);
  console.log('  批次库存行:', stockRows);
  console.log('  库存行:', invRows);
  console.log('  库存流水:', logRows);
  console.log('========================================');
  v.forEach(r => console.log(`  ${r.t.padEnd(26)} ${r.c}`));

  const [[sum]] = await conn.query(`SELECT SUM(qty) total_qty, COUNT(DISTINCT material_id) mats, COUNT(DISTINCT warehouse_id) whs FROM wms_inventory`);
  console.log(`\n库存合计数量: ${sum.total_qty}  物料数: ${sum.mats}  仓库数: ${sum.whs}`);

  await conn.end();
}

main().catch(e => { console.error('FATAL:', e); process.exit(1); });
