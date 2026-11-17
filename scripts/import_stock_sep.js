/**
 * 9月库存明细表导入：以「保留历史 + 按差异调整」方式把 9 月结存同步到 WMS 库存。
 * 用法：
 *   node scripts/import_stock_sep.js --dry   仅试算，不写库
 *   node scripts/import_stock_sep.js         正式写库
 */
const XLSX = require('xlsx');
const mysql = require('mysql2/promise');

const FILE = 'c:\\Users\\Administrator\\.trae-cn\\attachments\\8ed1c6b9-974a-4708-9712-4e319b2d8500_11db5206-9c2a-4f41-b100-d90834e03c04_9月库存明细表.xlsx';
const DRY = process.argv.includes('--dry');
const USER = 'admin';
const BATCH_DATE = '20260930';
const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4', decimalNumbers: true };
let CONN = null;

const SHEETS = [
  { sheet: '成品', cat: '成品' },
  { sheet: '辅料', cat: '辅料' },
  { sheet: '包材', cat: '包材' },
  { sheet: '原料', cat: '原料' },
];

const TYPE_CODE = { '0': '0', '2': '2', '3': '3', '6': '6', '成品': '2', '辅料': '3', '包材': '6', '原料': '0' };
const CAT_TYPE = { '成品': '2', '辅料': '3', '包材': '6', '原料': '0' };

// 名称 → 已有物料编码（人工确认的同义映射）
const ALIAS = {
  '辅料': {
    '生辣子面': 'WL20260052', '辣辣子面': 'WL20260027', '香辣子面': 'WL20260026',
    '米婆婆米酒': 'WL20260016', '玫瑰露酒': 'WL20260110', 'D-异': 'WL20260062',
    '松茸': 'WL20260040', '鸡枞': 'WL20260039', '海天草菇老抽': 'WL20260033',
    '鸡精454g': 'WL20260029', 'K7': 'WL20260107', '葡萄糖粉': 'WL20260106',
    '红皮花生': 'WL20260020', '青麻椒粉': 'WL20260047', '豆腐乳': 'WL20260023',
    '猪肠衣': 'WL20260009', '白酒': 'WL20260015', '牛肝菌': 'WL20260041',
  },
  '成品': {
    '块状火腿': 'WL20260006', '五花腊肉': 'WL20260219', '琵琶腿': 'WL20260210',
    '五目肠': 'WL20260005', '香辣肠': 'WL20260206', '一口乌猪肠': 'WL20260214',
    '原味即食火腿': 'WL20260235', '蜜汁即食火腿': 'WL20260236',
    '松茸菌火腿酱': 'WL20260231', '牛肝菌火腿酱': 'WL20260232',
    '川味香肠': 'WL20260220', '前腿肉2号肉': 'WL20260228', '梅花肉1号肉': 'WL20260227',
  },
  '包材': {
    '真空包装袋': 'WL20260010', '红色礼盒箱': 'WL20260211', '火腿酱（礼盒）': 'WL20260134',
  },
  '原料': {},
};

const norm = (s) => String(s == null ? '' : s)
  .replace(/[\s\u3000]/g, '')
  .replace(/（/g, '(').replace(/）/g, ')')
  .replace(/[，,]/g, '');

const stripPrefix = (s) => String(s).replace(/^大河乌猪/, '');

function parseSheet(wb, sh) {
  const rows = XLSX.utils.sheet_to_json(wb.Sheets[sh.sheet], { header: 1, raw: true, defval: '' });
  const items = [];
  let cur = null;
  for (let i = 7; i < rows.length; i++) {
    const r = rows[i];
    const seq = r[0];
    const name = String(r[1] == null ? '' : r[1]).trim();
    const spec = String(r[2] == null ? '' : r[2]).trim();
    const remark = String(r[7] == null ? '' : r[7]).trim();
    if (!seq && /^合计$/.test(name)) break;
    if (!seq && !name && !spec && !remark) continue;
    const num = (v) => (v === '' || v == null ? 0 : Number(v) || 0);
    const rec = { seq: seq === '' ? null : Number(seq), name, spec, open: num(r[3]), out: num(r[4]), in: num(r[5]), close: num(r[6]), remark };
    if (rec.seq && rec.name) {
      cur = { cat: sh.cat, name: rec.name, rows: [rec] };
      items.push(cur);
    } else if (cur) {
      cur.rows.push(rec);
    }
  }
  return items;
}

function inferUnit(cat, name, specs) {
  const spec = specs.join(' ');
  if (cat === '原料') return 'kg';
  if (cat === '包材') {
    if (/标签/.test(name)) return '张';
    if (/膜|封口线/.test(name)) return '卷';
    if (/锯条/.test(name)) return '根';
    return '个';
  }
  if (cat === '辅料' && /胶原蛋白肠衣/.test(name)) return '米';
  // 规格中显式的 “/单位”
  const m = spec.match(/\/\s*([A-Za-z\u4e00-\u9fa5]+)/);
  if (m) {
    const u = m[1];
    const allow = ['袋', '盒', '瓶', '个', '只', '桶', '卷', '根', '张', '套', '包', '条', '支', '块', '双', '件', '箱', 'kg', '公斤', '米'];
    if (allow.includes(u)) return u === '公斤' ? 'kg' : u;
  }
  const g = spec.match(/称重\s*\/\s*([A-Za-z]+)/);
  if (g) return g[1];
  if (/根\s*\*/.test(spec)) return '根';
  if (cat === '辅料') return 'kg';
  if (cat === '成品') return '袋';
  return '个';
}

async function main() {
  const wb = XLSX.readFile(FILE);
  const all = [];
  for (const sh of SHEETS) {
    const items = parseSheet(wb, sh);
    const sum = items.reduce((s, it) => s + it.rows.reduce((a, b) => a + b.close, 0), 0);
    console.log(`[解析] ${sh.sheet}: 品名 ${items.length} 个 / 规格行 ${items.reduce((s, it) => s + it.rows.length, 0)} 行 / 结存合计 ${sum.toFixed(2)}`);
    all.push(...items);
  }

  const conn = await mysql.createConnection(DB);
  CONN = conn;
  if (!DRY) await conn.beginTransaction();
  const q = async (sql, p) => (await conn.query(sql, p))[0];
  const run = async (sql, p) => (await conn.query(sql, p))[0];

  // 1) 物料主数据：统一类型编码，补 包材 字典
  if (!DRY) {
    const dict = await q("select dict_code from sys_dict_data where dict_type='wms_material_type' and dict_value='6'");
    if (dict.length === 0) {
      await run("insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) values (6,'包材','6','wms_material_type','','default','N','0',?,now(),'9月库存导入新增')", [USER]);
      console.log('[字典] 新增 wms_material_type 包材=6');
    }
    await run("update wms_material set material_type='2' where material_type='成品'");
    await run("update wms_material set material_type='3' where material_type='辅料'");
    await run("update wms_material set material_type='6' where material_type='包材'");
    await run("update wms_material set material_type='0' where material_type='原料'");
  }

  // 2) 物料索引
  const mats = await q("select material_id, material_code, material_name, material_type, unit from wms_material where del_flag='0' order by material_id");
  const byCode = new Map(mats.map(m => [m.material_code, m]));
  const byNameCat = new Map();
  for (const m of mats) {
    const code = TYPE_CODE[m.material_type] || m.material_type;
    const keys = [norm(m.material_name), norm(stripPrefix(m.material_name))];
    for (const k of keys) {
      const kk = code + '|' + k;
      if (!byNameCat.has(kk)) byNameCat.set(kk, m);
    }
  }
  let nextCodeNum = Math.max(...mats.map(m => Number(String(m.material_code).slice(-4)) || 0)) + 1;

  // 3) 仓库/库位
  async function ensureNode(code, name, parentId, nodeType) {
    const found = await q('select warehouse_id from wms_warehouse where warehouse_code=? and del_flag=\'0\'', [code]);
    if (found.length) return found[0].warehouse_id;
    if (DRY) { console.log(`[试算] 需新增仓库节点 ${code} ${name}`); return null; }
    const parent = await q('select ancestors from wms_warehouse where warehouse_id=?', [parentId]);
    const anc = (parent[0] && parent[0].ancestors ? parent[0].ancestors : '') + ',' + parentId;
    const r = await run("insert into wms_warehouse (parent_id, ancestors, node_type, warehouse_code, warehouse_name, dept_id, capacity, location_status, status, del_flag, create_by, create_time, remark) values (?,?,?,?,?,106,0,'0','0','0',?,now(),'9月库存导入自动创建')", [parentId, anc, nodeType, code, name, USER]);
    console.log(`[仓库] 新增节点 ${code} ${name} (id=${r.insertId})`);
    return r.insertId;
  }

  const whCheng = await ensureNode('WH02A01', '成品A区', 2, '2');
  const locCheng = await ensureNode('WH02A01L01', '成品A-01库位', whCheng || 2, '3');
  const locMap = {
    '成品': { wh: 2, loc: locCheng },
    '辅料': { wh: 3, loc: 11 },
    '包材': { wh: 3, loc: 11 },
    '原料': { wh: 1, loc: 9 },
  };

  // 4) 物料解析（匹配 / 新建）
  const resolved = new Map(); // material_id -> { mat, cat, target, specs[], items[] }
  const report = { matched: 0, created: 0, createdList: [], ambiguous: [] };

  for (const item of all) {
    const cat = item.cat;
    const typeCode = CAT_TYPE[cat];
    let mat = null;
    const aliasCode = ALIAS[cat] && ALIAS[cat][item.name];
    if (aliasCode && byCode.has(aliasCode)) mat = byCode.get(aliasCode);
    if (!mat) {
      const keys = [norm(item.name), norm(stripPrefix(item.name))];
      for (const k of keys) {
        const hit = byNameCat.get(typeCode + '|' + k);
        if (hit) { mat = hit; break; }
      }
    }
    if (!mat) {
      const specs = [...new Set(item.rows.map(r => r.spec).filter(Boolean))];
      const unit = inferUnit(cat, item.name, specs);
      const code = 'WL2026' + String(nextCodeNum++).padStart(4, '0');
      if (DRY) {
        mat = { material_id: null, material_code: code, material_name: item.name, material_type: typeCode, unit, spec_model: specs.join(' / ').slice(0, 250) };
      } else {
        const r = await run("insert into wms_material (material_code, material_name, material_type, spec_model, unit, safety_stock_min, safety_stock_max, is_batch_manage, is_expiry_manage, expiry_alert_days, status, del_flag, create_by, create_time, remark) values (?,?,?,?,?,10,100,'1','0',30,'0','0',?,now(),'9月库存明细表导入')", [code, item.name, typeCode, specs.join(' / ').slice(0, 250), unit, USER]);
        mat = { material_id: r.insertId, material_code: code, material_name: item.name, material_type: typeCode, unit, spec_model: specs.join(' / ') };
      }
      byCode.set(code, mat);
      byNameCat.set(typeCode + '|' + norm(item.name), mat);
      report.created++;
      report.createdList.push(`${cat} ${code} ${item.name} [${unit}]`);
    } else {
      report.matched++;
    }

    const key = mat.material_id != null ? 'id:' + mat.material_id : 'code:' + mat.material_code;
    if (!resolved.has(key)) resolved.set(key, { mat, cat, target: 0, open: 0, out: 0, in: 0, items: [], specs: new Set() });
    const g = resolved.get(key);
    for (const r of item.rows) {
      g.target += r.close; g.open += r.open; g.out += r.out; g.in += r.in;
      if (r.spec) g.specs.add(r.spec);
      if (r.remark) g.specs.add(r.remark);
    }
    g.items.push(item);
  }

  // 5) 现有库存
  const invRows = await q("select inventory_id, material_id, warehouse_id, location_id, batch_no, qty from wms_inventory where del_flag='0' order by inventory_id");
  const invByMat = new Map();
  for (const r of invRows) {
    if (!invByMat.has(r.material_id)) invByMat.set(r.material_id, []);
    invByMat.get(r.material_id).push(r);
  }

  const stats = { inc: 0, dec: 0, same: 0, invInserted: 0, reducedQty: 0, addedQty: 0 };

  for (const g of resolved.values()) {
    const mat = g.mat;
    const loc = locMap[g.cat];
    const olds = mat.material_id != null ? (invByMat.get(mat.material_id) || []) : [];
    const oldQty = olds.reduce((s, r) => s + Number(r.qty || 0), 0);
    let delta = Number(g.target.toFixed(2)) - Number(oldQty.toFixed(2));
    delta = Number(delta.toFixed(2));
    if (Math.abs(delta) < 0.005) { stats.same++; continue; }

    if (delta > 0) {
      stats.inc++;
      const batchNo = `SEP${BATCH_DATE}-${mat.material_code}`;
      const remark = `9月结存调整：结存 ${g.target}，原库存 ${oldQty}${g.specs.size ? '；规格 ' + [...g.specs].join('、') : ''}`.slice(0, 480);
      if (DRY) { console.log(`[试算][盘盈] ${mat.material_code} ${mat.material_name} +${delta}`); continue; }
      if (mat.material_id == null) throw new Error('new material has no id');
      const r = await run("insert into wms_inventory (material_id, warehouse_id, location_id, batch_no, qty, lock_qty, version, del_flag, create_by, create_time, remark) values (?,?,?,?,?,0,0,'0',?,now(),?)", [mat.material_id, loc.wh, loc.loc, batchNo, delta, USER, remark]);
      await run("insert into wms_inventory_log (material_id, warehouse_id, location_id, batch_no, change_type, change_qty, before_qty, after_qty, ref_order_type, ref_order_no, create_by, create_time, remark) values (?,?,?,?,?,?,?,?,?,?,?,now(),?)", [mat.material_id, loc.wh, loc.loc, batchNo, '4', delta, oldQty, Number((oldQty + delta).toFixed(2)), 'stock', 'SEP2026', USER, '9月库存差异调整（盘盈）']);
      const fb = await run("insert into food_batch (batch_code, ext_batch_code, material_id, material_code, source_type, source_bill_type, produce_date, receive_date, qty, qty_unit, batch_status, expire_status, del_flag, create_by, create_time, remark) values (?,NULL,?,?, '6','stock', NULL, ?, ?, ?, '1','0','0',?,now(),'9月结存导入')", [batchNo, mat.material_id, mat.material_code, `${BATCH_DATE.slice(0,4)}-${BATCH_DATE.slice(4,6)}-${BATCH_DATE.slice(6,8)}`, delta, mat.unit, USER]);
      await run("insert into food_batch_stock (material_id, warehouse_id, location_id, batch_id, qty, qty_locked, version, create_by, create_time) values (?,?,?,?,?,0,0,?,now())", [mat.material_id, loc.wh, loc.loc, fb.insertId, delta, USER]);
      stats.invInserted++;
      stats.addedQty += delta;
    } else {
      stats.dec++;
      let need = -delta;
      let before = oldQty;
      for (const row of olds) {
        if (need <= 0) break;
        const have = Number(row.qty || 0);
        if (have <= 0) continue;
        const cut = Math.min(have, need);
        need = Number((need - cut).toFixed(2));
        const after = Number((have - cut).toFixed(2));
        if (!DRY) {
          await run('update wms_inventory set qty=?, update_by=?, update_time=now() where inventory_id=?', [after, USER, row.inventory_id]);
          await run("insert into wms_inventory_log (material_id, warehouse_id, location_id, batch_no, change_type, change_qty, before_qty, after_qty, ref_order_type, ref_order_no, create_by, create_time, remark) values (?,?,?,?,?,?,?,?,?,?,?,now(),?)", [mat.material_id, row.warehouse_id, row.location_id, row.batch_no || '', '5', -cut, before, Number((before - cut).toFixed(2)), 'stock', 'SEP2026', USER, '9月库存差异调整（盘亏）']);
          if (row.batch_no) {
            await run('update food_batch set qty=GREATEST(0, qty-?), update_by=?, update_time=now() where batch_code=? and del_flag=\'0\'', [cut, USER, row.batch_no]);
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
        stats.reducedQty += cut;
      }
      if (need > 0.004) console.log(`[警告] ${mat.material_code} ${mat.material_name} 现有库存不足，仍差 ${need}`);
      if (DRY) console.log(`[试算][盘亏] ${mat.material_code} ${mat.material_name} ${delta}`);
    }
  }

  console.log('\n========== 导入结果 ==========');
  console.log(`物料：匹配 ${report.matched}，新建 ${report.created}`);
  console.log(`库存：盘盈 ${stats.inc} 项，盘亏 ${stats.dec} 项，无差异 ${stats.same} 项`);
  console.log(`新增库存行 ${stats.invInserted}，新增数量 ${stats.addedQty.toFixed(2)}，减少数量 ${stats.reducedQty.toFixed(2)}`);
  if (report.createdList.length) console.log('\n新建物料清单：\n' + report.createdList.join('\n'));

  if (!DRY) {
    await conn.commit();
    const sums = await q("select m.material_type, count(*) n, sum(i.qty) qty from wms_inventory i join wms_material m on m.material_id=i.material_id where i.del_flag='0' group by m.material_type");
    console.log('\n当前库存按类型：', JSON.stringify(sums));
  }
  await conn.end();
}

main().catch(async e => {
  console.error(e);
  try { if (CONN) await CONN.rollback(); } catch (_) {}
  process.exit(1);
});
