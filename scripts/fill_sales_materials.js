/**
 * 补齐销售订单明细对应的成品/包材物料，并回填 material_id/material_code
 */
const mysql = require('mysql2/promise');
const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4' };

// 名称归一化（销售简称 → 系统标准品名）
const CANON = {
  '五目肠': '大河乌猪五目肠',
  '香辣肠': '大河乌猪香辣肠',
  '五花腊肉': '大河乌猪五花腊肉',
  '精修琵琶腿': '大河乌猪精修琵琶腿'
};
const PACKAGING = new Set(['红色礼盒', '四款卡盒', '绿色鲜肉标签', '空火腿酱礼盒配珍珠棉']);
const SKIP = new Set(['快递费']);

(async () => {
  const conn = await mysql.createConnection(DB);
  const [rows] = await conn.query(`
    SELECT product_name, unit, SUM(quantity) qty, COUNT(*) c
    FROM mk_order_item WHERE material_id IS NULL GROUP BY product_name, unit`);

  const byName = new Map();
  for (const r of rows) {
    if (SKIP.has(r.product_name)) continue;
    const name = CANON[r.product_name] || r.product_name;
    if (!byName.has(name)) byName.set(name, { unit: r.unit || '袋', qty: Number(r.qty) });
    else byName.get(name).qty += Number(r.qty);
  }

  const [maxRow] = await conn.query("SELECT MAX(CAST(RIGHT(material_code,4) AS UNSIGNED)) mx FROM wms_material WHERE material_code LIKE 'WL2026%'");
  let seq = Number(maxRow[0].mx || 0);

  const idMap = new Map();
  for (const [name, info] of byName) {
    const [ex] = await conn.query("SELECT material_id, material_code, unit FROM wms_material WHERE material_name=? AND del_flag='0' LIMIT 1", [name]);
    if (ex.length) { idMap.set(name, ex[0]); continue; }
    seq++;
    const code = 'WL2026' + String(seq).padStart(4, '0');
    const isPack = PACKAGING.has(name);
    const [r] = await conn.query(
      `INSERT INTO wms_material (material_code, material_name, material_type, unit, is_batch_manage, is_expiry_manage,
        shelf_life_days, status, del_flag, create_by, create_time, remark)
       VALUES (?, ?, ?, ?, ?, ?, ?, '0', '0', 'admin', NOW(), '销售清单导入自动创建')`,
      [code, name, isPack ? '包材' : '成品', info.unit, isPack ? '0' : '1', isPack ? '0' : '1', isPack ? null : 90]);
    idMap.set(name, { material_id: r.insertId, material_code: code, unit: info.unit });
    console.log(`  + ${name} (${code}, ${isPack ? '包材' : '成品'})`);
  }

  // 回填订单明细
  let updated = 0;
  for (const [name, m] of idMap) {
    const [r] = await conn.query(
      `UPDATE mk_order_item i JOIN mk_order o ON i.order_id=o.order_id
       SET i.material_id=?, i.material_code=?
       WHERE i.material_id IS NULL AND (i.product_name=? OR i.product_name=?)`,
      [m.material_id, m.material_code, name, Object.keys(CANON).find(k => CANON[k] === name) || name]);
    updated += r.affectedRows;
  }

  // 回填发货明细
  await conn.query(`
    UPDATE mk_shipment_detail d JOIN mk_order_item i ON d.order_item_id=i.item_id
    SET d.material_id=i.material_id, d.material_code=i.material_code
    WHERE d.material_id IS NULL AND i.material_id IS NOT NULL`);

  const [[stat]] = await conn.query(`
    SELECT COUNT(*) total, SUM(material_id IS NULL) unmatched,
           SUM(product_name='快递费') fee_rows
    FROM mk_order_item`);
  console.log(`回填订单明细 ${updated} 行`);
  console.log(stat);
  await conn.end();
})().catch(e => { console.error('FATAL:', e.message); process.exit(1); });
