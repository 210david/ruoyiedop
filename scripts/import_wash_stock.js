/**
 * 9月洗涤库存表导入：洗涤及零用品 + 五金配件 → 物料主数据 + 辅料仓库存（按差异调整）
 * 用法：
 *   node scripts/import_wash_stock.js --dry
 *   node scripts/import_wash_stock.js
 */
const XLSX = require('xlsx');
const mysql = require('mysql2/promise');

const FILE = 'c:\\Users\\Administrator\\.trae-cn\\attachments\\f5bb1934-8ed8-422b-9cd3-9ca2a116765c_5370610e-4456-43bd-a4cb-e65bf1b1423f_9月洗涤库存表.xlsx';
const DRY = process.argv.includes('--dry');
const USER = 'admin';
const BATCH_DATE = '20260930';
const WH = 3;      // 辅料包材库
const LOC = 11;    // 辅料包材A-01库位
const TYPE = '3';  // 辅料
const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4', decimalNumbers: true };

// 同义名称映射（避免重复建料）
const ALIAS = {
  '云南白药创可贴': '创可贴',
};

const norm = (s) => String(s == null ? '' : s).replace(/[\s\u3000]/g, '').replace(/（/g, '(').replace(/）/g, ')');
const num = (v) => (v === '' || v == null ? 0 : Number(v) || 0);

function parseSheet(wb, sheet) {
  const rows = XLSX.utils.sheet_to_json(wb.Sheets[sheet], { header: 1, raw: true, defval: '' });
  const items = [];
  for (let i = 4; i < rows.length; i++) {
    const r = rows[i];
    const name = String(r[1] == null ? '' : r[1]).trim();
    if (!name) continue;
    const isClean = sheet === '清洁库存表';
    items.push({
      sheet,
      name,
      spec: String(r[2] == null ? '' : r[2]).trim(),
      unit: String(r[3] == null ? '' : r[3]).trim() || '个',
      inQty: num(isClean ? r[5] : r[4]),
      outQty: num(isClean ? r[6] : r[5]),
      stock: num(isClean ? r[7] : r[6]),
      remark: String((isClean ? r[8] : r[8]) == null ? '' : (isClean ? r[8] : r[8])).trim(),
    });
  }
  return items;
}

(async () => {
  const wb = XLSX.readFile(FILE);
  const clean = parseSheet(wb, '清洁库存表');
  const parts = parseSheet(wb, '配件库存表');
  const all = [...clean, ...parts];
  console.log(`[解析] 清洁用品 ${clean.length} 项 / 配件 ${parts.length} 项`);

  const conn = await mysql.createConnection(DB);
  const q = async (s, p) => (await conn.query(s, p))[0];
  const run = async (s, p) => (await conn.query(s, p))[0];

  const mats = await q("select material_id, material_code, material_name, material_type, unit, spec_model from wms_material where del_flag='0'");
  const byName = new Map(); // norm(name) -> 择优物料
  for (const m of mats) {
    const k = norm(m.material_name);
    const old = byName.get(k);
    if (!old) byName.set(k, m);
    else if (old.material_type !== TYPE && m.material_type === TYPE) byName.set(k, m);
  }
  let nextNum = Math.max(...mats.map(m => Number(String(m.material_code).slice(-4)) || 0)) + 1;
  const newCode = () => 'WL2026' + String(nextNum++).padStart(4, '0');

  const invRows = await q("select inventory_id, material_id, warehouse_id, location_id, batch_no, qty from wms_inventory where del_flag='0' order by inventory_id");
  const invByMat = new Map();
  for (const r of invRows) {
    if (!invByMat.has(r.material_id)) invByMat.set(r.material_id, []);
    invByMat.get(r.material_id).push(r);
  }

  const stats = { matched: 0, created: 0, inc: 0, dec: 0, same: 0, addedQty: 0, reducedQty: 0, relocated: 0, unitFixed: [] };
  const createdList = [];

  if (!DRY) await conn.beginTransaction();
  try {
    for (const it of all) {
      let mat = byName.get(norm(ALIAS[it.name] || it.name)) || null;
      if (mat) {
        stats.matched++;
        if (mat.unit !== it.unit) {
          if (!DRY) await run('update wms_material set unit=?, update_by=?, update_time=now() where material_id=?', [it.unit, USER, mat.material_id]);
          stats.unitFixed.push(`${mat.material_name} ${mat.unit}→${it.unit}`);
          mat.unit = it.unit;
        }
        if (!mat.spec_model && it.spec) {
          if (!DRY) await run('update wms_material set spec_model=?, update_by=?, update_time=now() where material_id=?', [it.spec, USER, mat.material_id]);
          mat.spec_model = it.spec;
        }
      } else {
        const code = newCode();
        if (DRY) {
          mat = { material_id: null, material_code: code, material_name: it.name, material_type: TYPE, unit: it.unit, spec_model: it.spec };
        } else {
          const r = await run(
            "insert into wms_material (material_code, material_name, material_type, spec_model, unit, safety_stock_min, safety_stock_max, is_batch_manage, is_expiry_manage, expiry_alert_days, status, del_flag, create_by, create_time, remark) values (?,?,?,?,?,0,0,'0','0',0,'0','0',?,now(),'9月洗涤库存表导入')",
            [code, it.name, TYPE, it.spec || null, it.unit, USER]);
          mat = { material_id: r.insertId, material_code: code, material_name: it.name, material_type: TYPE, unit: it.unit, spec_model: it.spec };
        }
        byName.set(norm(it.name), mat);
        stats.created++;
        createdList.push(`${code} ${it.name} [${it.unit}]${it.spec ? ' ' + it.spec : ''}`);
      }

      const olds = mat.material_id != null ? (invByMat.get(mat.material_id) || []) : [];
      // 洗涤/零用品统一归到辅料包材库（原先从原料台账导入的会落在原料仓）
      for (const row of olds) {
        if (row.warehouse_id === WH && row.location_id === LOC) continue;
        stats.relocated++;
        if (DRY) continue;
        await run('update wms_inventory set warehouse_id=?, location_id=?, update_by=?, update_time=now() where inventory_id=?', [WH, LOC, USER, row.inventory_id]);
        if (row.batch_no) {
          await run('update food_batch_stock set warehouse_id=?, location_id=?, update_by=?, update_time=now() where batch_id=(select batch_id from food_batch where batch_code=? and del_flag=\'0\' limit 1)',
            [WH, LOC, USER, row.batch_no]);
        }
        row.warehouse_id = WH;
        row.location_id = LOC;
      }
      const oldQty = Number(olds.reduce((s, r) => s + Number(r.qty || 0), 0).toFixed(2));
      const delta = Number((it.stock - oldQty).toFixed(2));
      if (Math.abs(delta) < 0.005) { stats.same++; continue; }

      if (delta > 0) {
        stats.inc++;
        stats.addedQty = Number((stats.addedQty + delta).toFixed(2));
        if (DRY) { console.log(`[试算][盘盈] ${mat.material_name} ${oldQty}→${it.stock} (+${delta}${it.unit})`); continue; }
        const batchNo = `SEP${BATCH_DATE}-${mat.material_code}`;
        await run("insert into wms_inventory (material_id, warehouse_id, location_id, batch_no, qty, lock_qty, version, del_flag, create_by, create_time, remark) values (?,?,?,?,?,0,0,'0',?,now(),?)",
          [mat.material_id, WH, LOC, batchNo, delta, USER, `9月洗涤库存表：实时库存 ${it.stock}，原库存 ${oldQty}${it.spec ? '；规格 ' + it.spec : ''}`.slice(0, 480)]);
        await run("insert into wms_inventory_log (material_id, warehouse_id, location_id, batch_no, change_type, change_qty, before_qty, after_qty, ref_order_type, ref_order_no, create_by, create_time, remark) values (?,?,?,?,?,?,?,?,?,?,?,now(),?)",
          [mat.material_id, WH, LOC, batchNo, '4', delta, oldQty, Number((oldQty + delta).toFixed(2)), 'stock', 'SEP2026', USER, '9月洗涤库存差异调整（盘盈）']);
        const fb = await run("insert into food_batch (batch_code, ext_batch_code, material_id, material_code, source_type, source_bill_type, produce_date, receive_date, qty, qty_unit, batch_status, expire_status, del_flag, create_by, create_time, remark) values (?,NULL,?,?, '6','stock', NULL, '2026-09-30', ?, ?, '1','0','0',?,now(),'9月结存导入')",
          [batchNo, mat.material_id, mat.material_code, delta, mat.unit, USER]);
        await run("insert into food_batch_stock (material_id, warehouse_id, location_id, batch_id, qty, qty_locked, version, create_by, create_time) values (?,?,?,?,?,0,0,?,now())",
          [mat.material_id, WH, LOC, fb.insertId, delta, USER]);
      } else {
        stats.dec++;
        let need = Number((-delta).toFixed(2));
        stats.reducedQty = Number((stats.reducedQty + need).toFixed(2));
        if (DRY) { console.log(`[试算][盘亏] ${mat.material_name} ${oldQty}→${it.stock} (-${need}${it.unit})`); continue; }
        let before = oldQty;
        for (const row of olds) {
          if (need <= 0) break;
          const have = Number(row.qty || 0);
          if (have <= 0) continue;
          const cut = Math.min(have, need);
          need = Number((need - cut).toFixed(2));
          const after = Number((have - cut).toFixed(2));
          await run('update wms_inventory set qty=?, update_by=?, update_time=now() where inventory_id=?', [after, USER, row.inventory_id]);
          await run("insert into wms_inventory_log (material_id, warehouse_id, location_id, batch_no, change_type, change_qty, before_qty, after_qty, ref_order_type, ref_order_no, create_by, create_time, remark) values (?,?,?,?,?,?,?,?,?,?,?,now(),?)",
            [mat.material_id, row.warehouse_id, row.location_id, row.batch_no || '', '5', -cut, before, Number((before - cut).toFixed(2)), 'stock', 'SEP2026', USER, '9月洗涤库存差异调整（盘亏）']);
          if (row.batch_no) {
            await run("update food_batch set qty=GREATEST(0, qty-?), update_by=?, update_time=now() where batch_code=? and del_flag='0'", [cut, USER, row.batch_no]);
            const fb = await q("select batch_id from food_batch where batch_code=? and del_flag='0' limit 1", [row.batch_no]);
            if (fb.length) {
              const st = await q('select bs_id, qty from food_batch_stock where batch_id=? order by bs_id', [fb[0].batch_id]);
              let left = cut;
              for (const s of st) {
                if (left <= 0) break;
                const c = Math.min(Number(s.qty), left);
                left = Number((left - c).toFixed(2));
                await run('update food_batch_stock set qty=?, update_by=?, update_time=now() where bs_id=?', [Number((Number(s.qty) - c).toFixed(2)), USER, s.bs_id]);
              }
            }
          }
          before = Number((before - cut).toFixed(2));
        }
        if (need > 0.004) console.log(`[警告] ${mat.material_name} 库存不足，仍差 ${need}`);
      }
    }

    if (!DRY) await conn.commit();
    console.log('\n========== 导入结果 ==========');
    console.log(`物料：匹配 ${stats.matched}，新建 ${stats.created}`);
    console.log(`库存：盘盈 ${stats.inc} 项（+${stats.addedQty}），盘亏 ${stats.dec} 项（-${stats.reducedQty}），无差异 ${stats.same} 项`);
    console.log(`仓库归位：${stats.relocated} 行移入辅料包材库`);
    if (stats.unitFixed.length) console.log('单位修正：\n' + stats.unitFixed.join('\n'));
    if (createdList.length) console.log('新建物料：\n' + createdList.join('\n'));
    console.log(DRY ? '\n[试算模式] 未写库' : '\n[已写库] 事务提交完成');
  } catch (e) {
    if (!DRY) await conn.rollback();
    console.error('[失败]', e.message);
    throw e;
  } finally {
    await conn.end();
  }
})().catch(e => { console.error(e); process.exit(1); });
