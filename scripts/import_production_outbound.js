/**
 * 9月「原料用途台账」→ 生产管控（工单 / 领料 / 完工入库）
 * 9月 4 类库存台账「出库数量」→ 出库管理（出库单 + 明细）
 *
 * 用法：
 *   node scripts/import_production_outbound.js --dry   试算，不写库
 *   node scripts/import_production_outbound.js         写库
 */
const XLSX = require('xlsx');
const mysql = require('mysql2/promise');

const FILE = 'c:\\Users\\Administrator\\.trae-cn\\attachments\\8ed1c6b9-974a-4708-9712-4e319b2d8500_11db5206-9c2a-4f41-b100-d90834e03c04_9月库存明细表.xlsx';
const DRY = process.argv.includes('--dry');
const USER = 'admin';
const DEPT = 104;
const OPERATOR = '宋秋琼';
const MARK_PROD = '9月原料用途台账导入';
const MARK_OUT = '9月出库台账导入';

const DB = { host: 'localhost', port: 3306, user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', charset: 'utf8mb4', decimalNumbers: true };

/* ---------------- 名称映射 ---------------- */
// 原料名（台账写法） → 物料名称（物料主数据）
const RAW_ALIAS = {
  '3号肉': '3号肉（里脊肉）',
  '4号肉': '4号肉（后腿肉）',
  '梅花肉': '梅花肉（1号肉）',
  '五花肉': '五花肉原料',
  '23年火腿': '三期2023年火腿',
  '25年火腿': '2025/11月火腿',
  '五花腊肉原料': '五花腊肉块',
  '鲜肉肋排': '大肋排',
};
// 成品名 → 物料编码
const PRODUCT_CODE = {
  '乌猪小甜肠': 'WL20260248',
  '黑松露小甜肠': 'WL20260276',
  '黑松露香肠': 'WL20260270',
  '玫瑰小甜肠': 'WL20260278',
  '大河乌猪前腿肉': 'WL20260228',
  '香辣肠': 'WL20260206',
  '烤肠': 'WL20260264',
  '大河乌猪大排': 'WL20260253',
  '大河乌猪梅花肉': 'WL20260227',
  '块状火腿': 'WL20260006',
  '琵琶腿': 'WL20260210',
  '广味': 'WL20260003',
  '广味香肠': 'WL20260003',
  '川味': 'WL20260220',
  '川味香肠': 'WL20260220',
  '五花腊肉块': 'WL20260219',
  '块状五花腊肉': 'WL20260219',
  '大河乌猪火腿片': 'WL20260226',
  '火腿片': 'WL20260226',
  '蜂蜜小甜肠': 'WL20260274',
  '金钱腿': 'WL20260241',
  '腊猪脚': 'WL20260240',
  '五花培根片': 'WL20260205',
  '大河乌猪肋排': 'WL20260251',
  '大河乌猪五花肉': 'WL20260225',
  '梅花肉': 'WL20260227',
  '五花肉': 'WL20260225',
  '大排': 'WL20260253',
};
const PROD_KEYS = Object.keys(PRODUCT_CODE).sort((a, b) => b.length - a.length);

// 库存台账 名称→编码（沿用 import_stock_sep.js 的人工确认映射）
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
  .replace(/[\s\u3000]/g, '').replace(/（/g, '(').replace(/）/g, ')').replace(/[，,]/g, '');
const stripPrefix = (s) => String(s).replace(/^大河乌猪/, '');
const num = (s) => { const m = String(s).match(/-?\d+(?:\.\d+)?/); return m ? Number(m[0]) : 0; };
const r2 = (n) => Math.round(n * 100) / 100;

/* ---------------- 日期 ---------------- */
const excelToDate = (serial) => {
  const ms = Date.UTC(1899, 11, 30) + Number(serial) * 86400000;
  return new Date(ms);
};
const pad = (n, l = 2) => String(n).padStart(l, '0');
const ymd = (d) => `${d.getUTCFullYear()}${pad(d.getUTCMonth() + 1)}${pad(d.getUTCDate())}`;
const ymdDash = (d) => `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}`;
const dt = (d) => `${ymdDash(d)} 09:00:00`;

/* ---------------- 解析原料用途台账 ---------------- */
function parseUsage(wb) {
  const rows = XLSX.utils.sheet_to_json(wb.Sheets['原料用途台账'], { header: 1, raw: true, defval: '' });
  const recs = [];
  let cur = null;
  let prevName = '';
  for (let i = 5; i < rows.length; i++) {
    const r = rows[i];
    const seq = String(r[0] == null ? '' : r[0]).trim();
    const name = String(r[1] == null ? '' : r[1]).trim();
    if (!name) { prevName = ''; continue; }
    const isNew = seq !== '' || prevName === '';
    if (isNew) {
      cur = {
        seq: seq || null,
        rawText: name,
        qtyText: String(r[2] || '').trim(),
        user: String(r[3] || '').trim(),
        serial: Number(r[4]) || 0,
        place: String(r[5] || '').trim(),
        usage: String(r[6] || '').trim(),
        prodText: String(r[7] || '').trim(),
        prodQtyText: String(r[8] || '').trim(),
        remark: String(r[9] || '').trim(),
      };
      recs.push(cur);
    }
    prevName = name;
  }
  return recs;
}

// 名称 / 数量 多行拆分配对
function splitPairs(rawText, qtyText) {
  const names = rawText.split(/\n+/).map(s => s.trim()).filter(Boolean);
  const qtys = qtyText.split(/\n+/).map(s => s.trim()).filter(Boolean)
    .filter(s => !/碎料/.test(s) && !/^废料/.test(s)); // 碎料/废料不是投料
  const pairs = [];
  const residue = [];
  const skipIdx = names.findIndex(n => /剩余原料/.test(n));
  if (skipIdx >= 0 && names.length === qtys.length) {
    residue.push(`${names[skipIdx]}${qtys[skipIdx]}`);
    names.splice(skipIdx, 1); qtys.splice(skipIdx, 1);
  } else if (skipIdx >= 0) {
    residue.push(names[skipIdx]);
    names.splice(skipIdx, 1);
  }
  for (let i = 0; i < names.length; i++) {
    pairs.push({ name: names[i], qty: num(qtys[i]), rawQty: qtys[i] || '' });
  }
  if (qtys.length > names.length) residue.push('未配数量:' + qtys.slice(names.length).join('/'));
  return { pairs, residue };
}

// 产出明细行 → [{productKey, qty, unit}]
function parseOutputs(prodText, prodQtyText) {
  const products = prodText.split(/\n+/).map(s => s.trim()).filter(Boolean);
  const lines = prodQtyText.split(/\n+/).map(s => s.trim()).filter(Boolean);
  const out = [];
  const residue = [];
  let idx = 0;
  for (const line of lines) {
    let key = null;
    for (const k of PROD_KEYS) { if (line.startsWith(k)) { key = k; break; } }
    let rest = line;
    if (key) rest = line.slice(key.length);
    else { key = products[idx] || products[0] || null; idx++; }
    if (!key) { residue.push(line); continue; }
    const ms = [...rest.matchAll(/(\d+(?:\.\d+)?)\s*(袋|块|只|根|瓶|盒|kg|KG|公斤|包|条|卷|张)/g)];
    if (!ms.length) { residue.push(line); continue; }
    const last = ms[ms.length - 1];
    out.push({ productKey: key, qty: Number(last[1]), unit: last[2] === '公斤' ? 'kg' : last[2] });
  }
  return { out, residue };
}

/* ---------------- 主流程 ---------------- */
(async () => {
  const wb = XLSX.readFile(FILE);
  const conn = await mysql.createConnection(DB);
  const q = async (s, p) => (await conn.query(s, p))[0];
  const run = async (s, p) => (await conn.query(s, p))[0];

  // 幂等保护
  const dupProd = await q("select count(*) n from mms_work_order where remark=?", [MARK_PROD]);
  const dupOut = await q("select count(*) n from wms_outbound_order where remark=?", [MARK_OUT]);
  if (dupProd[0].n > 0 || dupOut[0].n > 0) {
    console.log(`[跳过] 已存在导入数据：工单 ${dupProd[0].n} 条 / 出库单 ${dupOut[0].n} 条`);
    await conn.end();
    return;
  }

  const mats = await q("select material_id, material_code, material_name, material_type, unit, spec_model from wms_material where del_flag='0'");
  const byCode = new Map(mats.map(m => [m.material_code, m]));
  const byNameType = new Map();
  for (const m of mats) {
    const k = m.material_type + '|' + norm(m.material_name);
    const old = byNameType.get(k);
    if (!old || m.material_id > old.material_id) byNameType.set(k, m); // 同名取最新
    const k2 = m.material_type + '|' + norm(stripPrefix(m.material_name));
    if (!byNameType.has(k2)) byNameType.set(k2, m);
  }
  const findRaw = (name) => {
    const alias = RAW_ALIAS[name] || name;
    return byNameType.get('0|' + norm(alias)) || byNameType.get('0|' + norm(stripPrefix(alias))) || null;
  };

  /* ===== 一、生产管控 ===== */
  const recs = parseUsage(wb);
  console.log(`[解析] 原料用途台账 记录 ${recs.length} 条`);

  const seqMap = new Map(); // 单号序列
  const nextNo = (prefix, dateStr, len = 3) => {
    const k = prefix + dateStr;
    const v = (seqMap.get(k) || 0) + 1;
    seqMap.set(k, v);
    return prefix + dateStr + pad(v, len);
  };

  let woCount = 0, issueCount = 0, detailCount = 0, finishCount = 0, totalIssueQty = 0, totalFinishQty = 0;
  const unmatched = [];
  const plan = [];

  for (const rec of recs) {
    const d = excelToDate(rec.serial);
    const dStr = ymd(d);
    const time = dt(d);
    const { pairs, residue } = splitPairs(rec.rawText, rec.qtyText);
    const { out, residue: outRes } = parseOutputs(rec.prodText, rec.prodQtyText);

    const rawLines = [];
    for (const p of pairs) {
      const m = findRaw(p.name);
      if (!m) { unmatched.push(`原料:${p.name}`); continue; }
      rawLines.push({ mat: m, qty: p.qty, rawQty: p.rawQty });
    }
    // 同一原料合并
    const rawAgg = new Map();
    for (const l of rawLines) {
      const k = l.mat.material_id;
      if (!rawAgg.has(k)) rawAgg.set(k, { ...l, qty: 0 });
      rawAgg.get(k).qty += l.qty;
    }

    const outs = [];
    for (const o of out) {
      const code = PRODUCT_CODE[o.productKey];
      const pm = code && byCode.get(code);
      if (!pm) { unmatched.push(`成品:${o.productKey}`); continue; }
      const ex = outs.find(x => x.mat.material_id === pm.material_id && x.unit === o.unit);
      if (ex) ex.qty += o.qty;
      else outs.push({ mat: pm, qty: o.qty, unit: o.unit });
    }
    if (!outs.length) { unmatched.push(`整条无产出:序号${rec.seq}`); continue; }

    const remark = [
      MARK_PROD,
      `出库人:${rec.user || OPERATOR}`,
      `使用地点:${rec.place}`,
      `用途:${rec.usage}`,
      residue.length ? `说明:${residue.join(';')}` : '',
      outRes.length ? `产出说明:${outRes.join(';')}` : '',
      rec.remark ? `台账备注:${rec.remark.replace(/\n/g, ' ')}` : '',
    ].filter(Boolean).join(' | ').slice(0, 500);

    plan.push({ rec, d, dStr, time, raws: [...rawAgg.values()], outs, remark });
  }

  // 写库
  if (!DRY) await conn.beginTransaction();
  try {
    for (const p of plan) {
      let firstWoId = null, firstWoNo = null;
      for (const o of p.outs) {
        const woNo = nextNo('WO', p.dStr);
        const qty = r2(o.qty);
        const woRemark = `${MARK_PROD} 序号${p.rec.seq || '-'} 用途:${p.rec.usage} 使用地点:${p.rec.place}${p.rec.remark ? ' 台账备注:' + p.rec.remark.replace(/\n/g, ' ') : ''}`.slice(0, 500);
        let woId;
        if (DRY) woId = -woCount;
        else {
          const r = await run(
            `insert into mms_work_order
             (work_order_no, order_type, source_type, product_id, product_code, product_name, spec_model, unit,
              plan_qty, plan_start, plan_finish, actual_start, actual_finish, finished_qty, qualified_qty, defect_qty,
              priority, status, audit_by, audit_time, audit_opinion, release_by, release_time,
              del_flag, create_by, create_time, remark, dept_id)
             values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,'1','finished',?,?,?,?,?,'0',?,?,?,?)`,
            [woNo, 'normal', '3', o.mat.material_id, o.mat.material_code, o.mat.material_name,
              (o.mat.spec_model || '').slice(0, 100), o.mat.unit || o.unit, qty,
              p.time, p.time, p.time, p.time, qty, qty,
              USER, p.time, '9月历史数据导入', USER, p.time, USER, p.time, woRemark, DEPT]);
          woId = r.insertId;
        }
        woCount++;
        totalFinishQty += qty;
        if (!firstWoId) { firstWoId = woId; firstWoNo = woNo; }

        // 完工入库
        const rkNo = nextNo('RK', p.dStr);
        if (!DRY) {
          await run(
            `insert into mms_finish_receipt
             (finish_no, work_order_id, work_order_no, product_id, product_code, product_name, spec_model, unit,
              plan_qty, finish_qty, qualified_qty, finish_time, finish_by, is_on_time, status, inbound_time, inbound_by,
              del_flag, create_by, create_time, remark, dept_id)
             values (?,?,?,?,?,?,?,?,?,?,?,?,?,'1','1',?,?,'0',?,?,?,?)`,
            [rkNo, woId, woNo, o.mat.material_id, o.mat.material_code, o.mat.material_name,
              (o.mat.spec_model || '').slice(0, 100), o.mat.unit || o.unit, qty, qty, qty,
              p.time, OPERATOR, p.time, USER, USER, p.time,
              `${MARK_PROD}（成品库存已按9月结存导入，不再重复入库）`, DEPT]);
        }
        finishCount++;
      }

      // 领料单（每条使用记录一张，挂第一个工单）
      const issueNo = nextNo('FL', p.dStr);
      const issueQty = r2(p.raws.reduce((s, x) => s + x.qty, 0));
      const first = p.raws[0];
      if (first) {
        if (!DRY) {
          const r = await run(
            `insert into mms_issue
             (issue_no, work_order_id, work_order_no, material_id, material_code, material_name, spec_model, unit,
              issue_qty, issue_time, issue_by, status, del_flag, create_by, create_time, remark, dept_id)
             values (?,?,?,?,?,?,?,?,?,?,?,'1','0',?,?,?,?)`,
            [issueNo, firstWoId, firstWoNo, first.mat.material_id, first.mat.material_code, first.mat.material_name,
              (first.mat.spec_model || '').slice(0, 100), first.mat.unit, issueQty, p.time, p.rec.user || OPERATOR,
              USER, p.time, `${p.remark} | 工单:${p.outs.length > 1 ? '多条共线' : firstWoNo}`, DEPT]);
          let seq = 1;
          for (const l of p.raws) {
            await run(
              `insert into mms_issue_detail
               (issue_id, seq, material_id, material_code, material_name, spec_model, unit,
                usage_qty, suggested_qty, issue_qty, del_flag, create_by, create_time, remark)
               values (?,?,?,?,?,?,?,?,?,?,'0',?,?,?)`,
              [r.insertId, seq++, l.mat.material_id, l.mat.material_code, l.mat.material_name,
                (l.mat.spec_model || '').slice(0, 200), l.mat.unit, l.qty, l.qty, l.qty,
                USER, p.time, l.rawQty ? `台账数量:${l.rawQty}` : null]);
            detailCount++;
          }
        } else {
          detailCount += p.raws.length;
        }
        issueCount++;
        totalIssueQty += issueQty;
      }
    }

    /* ===== 二、出库单 ===== */
    const SHEETS = [
      { sheet: '成品', type: '2', wh: 2, loc: null, orderType: '0' },
      { sheet: '辅料', type: '3', wh: 3, loc: 11, orderType: '1' },
      { sheet: '包材', type: '6', wh: 3, loc: 11, orderType: '1' },
      { sheet: '原料', type: '0', wh: 1, loc: 9, orderType: '1' },
    ];
    const locRow = await q("select warehouse_id from wms_warehouse where warehouse_code='WH02A01L01' limit 1");
    const locCheng = locRow[0] ? locRow[0].warehouse_id : null;

    const outPlan = [];
    for (const sh of SHEETS) {
      const rows = XLSX.utils.sheet_to_json(wb.Sheets[sh.sheet], { header: 1, raw: true, defval: '' });
      let cur = null;
      const agg = new Map();
      for (let i = 7; i < rows.length; i++) {
        const r = rows[i];
        const seq = r[0], name = String(r[1] == null ? '' : r[1]).trim();
        const spec = String(r[2] == null ? '' : r[2]).trim();
        if (!seq && /^合计$/.test(name)) break;
        if (seq && name) cur = { name, specs: [] };
        if (!cur) continue;
        if (spec) cur.specs.push(spec);
        let out = Number(r[4]) || 0;
        if (!out) continue;
        if (!name && !cur.name) continue;
        let m = null;
        const aliasCode = ALIAS[sh.sheet] && ALIAS[sh.sheet][name || cur.name];
        if (aliasCode && byCode.has(aliasCode)) m = byCode.get(aliasCode);
        if (!m) m = byNameType.get(sh.type + '|' + norm(name || cur.name)) || byNameType.get(sh.type + '|' + norm(stripPrefix(name || cur.name)));
        if (!m) { unmatched.push(`出库物料(${sh.sheet}):${name || cur.name}`); continue; }
        const k = m.material_id;
        if (agg.has(k)) agg.get(k).qty += out;
        else agg.set(k, { mat: m, qty: out });
      }
      outPlan.push({ sheet: sh.sheet, ...sh, loc: sh.loc || locCheng, items: [...agg.values()] });
    }

    let outCount = 0;
    const outTotals = {};
    for (const o of outPlan) {
      const total = r2(o.items.reduce((s, x) => s + x.qty, 0));
      outTotals[o.sheet] = { rows: o.items.length, qty: total };
      if (!o.items.length) continue;
      const outNo = nextNo('OUT', '20260930', 2);
      if (!DRY) {
        const r = await run(
          `insert into wms_outbound_order
           (order_no, order_type, warehouse_id, status, outbound_date, complete_date, total_qty,
            del_flag, create_by, create_time, remark, dept_id)
           values (?,?,?,'3','2026-09-30 09:00:00','2026-09-30 17:00:00',?,'0',?,now(),?,?)`,
          [outNo, o.orderType, o.wh, total, USER, `${MARK_OUT} ${o.sheet}仓库台账出库数量汇总（库存已按9月结存导入，不重复扣减）`, DEPT]);
        for (const it of o.items) {
          await run(
            `insert into wms_outbound_order_detail
             (order_id, material_id, plan_qty, actual_qty, location_id, pick_qty, del_flag, create_by, create_time, remark)
             values (?,?,?,?,?,?,'0',?,now(),?)`,
            [r.insertId, it.mat.material_id, r2(it.qty), r2(it.qty), o.loc, r2(it.qty), USER, MARK_OUT]);
        }
      }
      outCount++;
    }

    if (!DRY) await conn.commit();

    console.log('\n===== 生产管控 =====');
    console.log(`工单 ${woCount} 张 / 完工入库 ${finishCount} 张 / 领料单 ${issueCount} 张 / 领料明细 ${detailCount} 行`);
    console.log(`领料数量合计 ${r2(totalIssueQty)} kg / 完工数量合计 ${r2(totalFinishQty)}`);
    console.log('\n===== 出库单 =====');
    for (const [k, v] of Object.entries(outTotals)) console.log(`${k}: 明细 ${v.rows} 行 / 出库数量 ${v.qty}`);
    console.log(`出库单 ${outCount} 张`);
    if (unmatched.length) {
      console.log('\n[未匹配] ' + unmatched.length);
      console.log([...new Set(unmatched)].join('\n'));
    }
    console.log(DRY ? '\n[试算模式] 未写库' : '\n[已写库] 事务提交完成');
  } catch (e) {
    if (!DRY) await conn.rollback();
    console.error('[失败]', e.message);
    throw e;
  } finally {
    await conn.end();
  }
})().catch(e => { console.error(e); process.exit(1); });
