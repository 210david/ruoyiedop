/**
 * 仓储作业模拟：库位补齐 / 盘点任务(快照+明细+审批日志) / 移库单 / 拣货波次
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const ymd = d => fmt(d).slice(0, 10).replace(/-/g, '');
const rnd = (a, b) => a + Math.random() * (b - a);
const ri = (a, b) => Math.floor(rnd(a, b + 1));

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  for (const t of ['wms_stock_take', 'wms_stock_take_detail', 'wms_stock_take_snapshot', 'wms_stock_take_approve_log', 'wms_move_order', 'wms_wave']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ---------- 1. 补齐库位（同区第二个库位，供移库使用） ----------
  const newLocs = [
    ['WH01A02L02', '原料A-02库位', 1, 1, 8],
    ['WH02A01L02', '成品A-02库位', 1, 1, 12],
    ['WH03A01L02', '辅料包材A-02库位', 1, 1, 10]
  ];
  const locMap = new Map(); // code -> id
  const existLoc = await q(`SELECT warehouse_id, warehouse_code FROM wms_warehouse WHERE node_type='3'`);
  existLoc.forEach(l => locMap.set(l.warehouse_code, l.warehouse_id));
  for (const [code, name, dept, type, parent] of newLocs) {
    if (locMap.has(code)) continue;
    const p = await q(`SELECT warehouse_code, ancestors FROM wms_warehouse WHERE warehouse_id=?`, [parent]);
    const ins = await exec(`INSERT INTO wms_warehouse(parent_id,ancestors,node_type,warehouse_code,warehouse_name,dept_id,area_type,location_type,capacity,location_status,status,del_flag,create_by,create_time,remark)
      VALUES(?,?, '3',?,?,?, '0','0',0.00,'0','0','0','system',NOW(),'补建库位')`,
      [parent, (p[0] ? p[0].ancestors : '') + ',' + parent, code, name, dept]);
    locMap.set(code, ins.insertId);
  }
  console.log('库位档案:', [...locMap.entries()].map(e => e[0] + '#' + e[1]).join(', '));

  // ---------- 2. 盘点任务 ----------
  const whs = [
    { id: 3, name: '辅料包材库', type: '2', status: '1', date: '2026-11-10', diff: true, n: 24 },
    { id: 1, name: '冷冻原料库', type: '1', status: '2', date: '2026-11-13', diff: true, n: 18 },
    { id: 2, name: '冷藏成品库', type: '0', status: '3', date: '2026-11-06', diff: false, n: 26 }
  ];
  let tCnt = 0, dCnt = 0, snCnt = 0, lgCnt = 0;
  const reasons = ['收发记录漏记', '称量误差', '包装破损报废未登记', '串批混淆', '在途未及时入账'];
  for (const [i, w] of whs.entries()) {
    const plan = new Date(w.date + 'T08:30:00');
    const takeNo = 'PD' + ymd(plan) + pad(i + 1);
    const begin = new Date(plan.getTime() + 30 * 60000);
    const end = new Date(plan.getTime() + 7 * 3600000);
    const ins = await exec(`INSERT INTO wms_stock_take(take_no,warehouse_id,take_type,sample_ratio,cycle_no,status,plan_date,start_time,end_time,approve_by,approve_time,approve_opinion,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?,100)`,
      [takeNo, w.id, w.type, w.type === '1' ? 30 : null, w.type === '2' ? i + 1 : null, w.status, w.date,
        w.status === '0' ? null : fmt(begin), w.status === '3' ? fmt(end) : null,
        w.status === '3' ? '仓储主管' : null, w.status === '3' ? fmt(new Date(end.getTime() + 3600000)) : null,
        w.status === '3' ? '账实相符，准予结案' : '', fmt(new Date(plan.getTime() - 86400000)),
        w.type === '0' ? '月末全盘' : (w.type === '1' ? '重点物料抽盘' : '循环盘点第' + (i + 1) + '轮'), 100]);

    const rows = await q(`SELECT * FROM wms_inventory WHERE warehouse_id=? AND del_flag='0' ORDER BY qty DESC LIMIT ?`, [w.id, w.n]);
    for (const r of rows) {
      const book = Number(r.qty);
      let actual = book, reason = null;
      if (w.diff && Math.random() < 0.28) {
        const delta = Number((book * rnd(0.01, 0.05) * (Math.random() < 0.5 ? -1 : 1)).toFixed(2));
        actual = Number((book + delta).toFixed(2));
        if (actual < 0) actual = 0;
        reason = reasons[ri(0, reasons.length - 1)];
      }
      const diff = Number((actual - book).toFixed(2));
      await exec(`INSERT INTO wms_stock_take_detail(take_id,material_id,location_id,batch_no,book_qty,actual_qty,diff_qty,diff_reason,status,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?, '0','system',?,?)`,
        [ins.insertId, r.material_id, r.location_id, r.batch_no || null, book, actual, diff, reason,
          w.status === '3' ? '1' : '0', fmt(begin), diff === 0 ? '账实相符' : '已记录差异']);
      await exec(`INSERT INTO wms_stock_take_snapshot(take_id,material_id,location_id,batch_no,snapshot_qty,snapshot_time)
        VALUES(?,?,?,?,?,?)`, [ins.insertId, r.material_id, r.location_id, r.batch_no || null, book, fmt(begin)]);
      dCnt++; snCnt++;
    }
    if (w.status === '2' || w.status === '3') {
      await exec(`INSERT INTO wms_stock_take_approve_log(take_id,approve_by,approve_time,approve_action,approve_opinion)
        VALUES(?,?,?, 'submit', '盘点完成，提交审批')`, [ins.insertId, '仓储主管', fmt(new Date(end.getTime() + 1800000))]);
      lgCnt++;
    }
    if (w.status === '3') {
      await exec(`INSERT INTO wms_stock_take_approve_log(take_id,approve_by,approve_time,approve_action,approve_opinion)
        VALUES(?,?,?, 'approve', '账实相符，准予结案')`, [ins.insertId, '仓储经理', fmt(new Date(end.getTime() + 3600000))]);
      lgCnt++;
    }
    tCnt++;
  }
  console.log(`盘点任务 ${tCnt} 个 / 明细 ${dCnt} 行 / 快照 ${snCnt} 行 / 审批日志 ${lgCnt} 条`);

  // ---------- 3. 移库单 ----------
  const pool = await q(`SELECT i.*, m.material_name, m.unit FROM wms_inventory i LEFT JOIN wms_material m ON m.material_id=i.material_id
    WHERE i.del_flag='0' AND i.qty>0 ORDER BY i.qty DESC LIMIT 60`);
  let mCnt = 0;
  const movePlans = [
    { fromWh: 1, toCode: 'WH01A02L02', date: '2026-11-11', status: '2' },
    { fromWh: 2, toCode: 'WH02A01L02', date: '2026-11-12', status: '2' },
    { fromWh: 3, toCode: 'WH03A01L02', date: '2026-11-14', status: '1' }
  ];
  // 已存在的同区库位
  const locs = await q(`SELECT warehouse_id, warehouse_code FROM wms_warehouse WHERE node_type='3'`);
  const codeToId = new Map(locs.map(l => [l.warehouse_code, l.warehouse_id]));
  for (const p of movePlans) {
    const items = pool.filter(x => x.warehouse_id === p.fromWh).slice(0, 4);
    for (const [i, it] of items.entries()) {
      const d = new Date(p.date + 'T10:00:00');
      const no = 'YK' + ymd(d) + pad(i + 1);
      const mv = Number((Number(it.qty) * 0.2).toFixed(2));
      await exec(`INSERT INTO wms_move_order(move_no,warehouse_id,material_id,batch_no,from_location_id,to_location_id,move_qty,status,approve_by,approve_time,approve_opinion,complete_time,del_flag,create_by,create_time,remark,dept_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?,100)`,
        [no, p.fromWh, it.material_id, it.batch_no || null, it.location_id, codeToId.get(p.toCode) || it.location_id, mv, p.status,
          p.status === '0' ? null : '仓储主管', p.status === '0' ? null : fmt(new Date(d.getTime() + 3600000)),
          p.status === '2' ? '同意移库' : '审核通过，待执行', p.status === '2' ? fmt(new Date(d.getTime() + 7200000)) : null,
          fmt(d), '库内移位/整理库容']);
      mCnt++;
    }
  }
  console.log(`移库单 ${mCnt} 张`);

  // ---------- 4. 拣货波次 ----------
  const outs = await q(`SELECT order_id, order_no, order_type, warehouse_id, outbound_date, create_time, status FROM wms_outbound_order WHERE del_flag='0' ORDER BY order_id`);
  const dets = await q(`SELECT order_id, SUM(plan_qty) q FROM wms_outbound_order_detail GROUP BY order_id`);
  const qtyMap = new Map(dets.map(d => [d.order_id, Number(d.q) || 0]));
  // 按出库类型 + 日期分组
  const groups = new Map();
  for (const o of outs) {
    const k = o.order_type + '|' + fmt(new Date(o.create_time)).slice(0, 10) + '|' + o.warehouse_id;
    if (!groups.has(k)) groups.set(k, []);
    groups.get(k).push(o);
  }
  let wvCnt = 0;
  const keys = [...groups.keys()].sort().slice(-8);
  for (const [i, k] of keys.entries()) {
    const [type, day, wh] = k.split('|');
    const list = groups.get(k);
    if (!list.length) continue;
    const d = new Date(day + 'T09:00:00');
    const no = 'BC' + ymd(d) + pad(i + 1);
    const totalQty = list.reduce((a, o) => a + (qtyMap.get(o.order_id) || 0), 0);
    const typeName = { '0': '销售', '1': '领料', '2': '调拨', '3': '退货' }[type] || '出库';
    await exec(`INSERT INTO wms_wave(wave_no,wave_name,warehouse_id,status,total_qty,order_count,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?, '0','system',?,?,100)`,
      [no, day + ' ' + typeName + '出库波次', Number(wh), '2', totalQty, list.length, fmt(d), '按出库单自动归集']);
    wvCnt++;
  }
  console.log(`拣货波次 ${wvCnt} 个`);

  console.log(await q(`SELECT (SELECT COUNT(*) FROM wms_stock_take) 盘点,(SELECT COUNT(*) FROM wms_stock_take_detail) 盘点明细,
    (SELECT COUNT(*) FROM wms_move_order) 移库,(SELECT COUNT(*) FROM wms_wave) 波次`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
