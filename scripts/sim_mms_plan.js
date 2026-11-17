/**
 * 生产计划链模拟：需求 → 主生产计划(MPS) → 排产 → 排产任务 → 产能负荷 → OEE 快照 → 外协
 * 数据来源：已有 75 张工单、332 条派工、18 条停机、308 条报工
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const rnd = (a, b) => a + Math.random() * (b - a);
const ri = (a, b) => Math.floor(rnd(a, b + 1));

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  for (const t of ['mms_demand', 'mms_mps', 'mms_schedule', 'mms_schedule_task', 'mms_capacity_load', 'mms_oee_snapshot', 'mms_outsource']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  const wos = await q(`SELECT * FROM mms_work_order WHERE del_flag='0' ORDER BY plan_start`);
  const custs = await q(`SELECT customer_id, customer_no, customer_name FROM mk_customer LIMIT 11`);

  // ---------- 1. 需求 ----------
  const seen = new Set();
  const demandList = [];
  let dCnt = 0;
  for (const wo of wos) {
    const key = (wo.source_order_no || '') + wo.product_code;
    if (wo.source_order_no && seen.has(key)) continue;
    seen.add(key);
    const c = custs[dCnt % custs.length];
    const req = new Date(wo.plan_finish);
    const no = 'XQ' + dstr(new Date(wo.plan_start)).replace(/-/g, '') + pad(dCnt + 1);
    const status = wo.status === 'finished' ? '2' : (wo.status === 'pending' ? '0' : '1');
    const ins = await exec(`INSERT INTO mms_demand(demand_no,demand_type,source_order_no,product_id,product_code,product_name,spec_model,unit,demand_qty,require_date,priority,customer_id,customer_no,customer_name,status,remark,del_flag,create_by,create_time,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,104)`,
      [no, wo.source_type === 'order' ? '0' : '2', wo.source_order_no || '', wo.product_id, wo.product_code, wo.product_name,
        wo.spec_model, wo.unit, wo.plan_qty, dstr(req), wo.priority === 'high' ? '0' : '1',
        c.customer_id, c.customer_no, c.customer_name, status, '来源工单 ' + wo.work_order_no, fmt(new Date(wo.create_time))]);
    demandList.push({ id: ins.insertId, no, wo, c });
    dCnt++;
    if (dCnt >= 40) break;
  }
  console.log(`需求 ${dCnt} 条`);

  // ---------- 2. 主生产计划 ----------
  let mCnt = 0;
  const mpsList = [];
  const res = await q(`SELECT resource_id, resource_name FROM mms_resource ORDER BY resource_id`);
  for (const [i, d] of demandList.slice(0, 24).entries()) {
    const ps = new Date(d.wo.plan_start);
    const pe = new Date(d.wo.plan_finish);
    const r = res[i % res.length];
    const no = 'MPS' + dstr(ps).replace(/-/g, '') + pad(i + 1);
    const status = d.wo.status === 'finished' ? '3' : (i % 5 === 0 ? '1' : '2');
    const ins = await exec(`INSERT INTO mms_mps(mps_no,demand_id,demand_no,product_id,product_code,product_name,spec_model,unit,plan_qty,period_start,period_end,resource_id,resource_name,priority,status,audit_by,audit_time,audit_opinion,remark,del_flag,create_by,create_time,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,104)`,
      [no, d.id, d.no, d.wo.product_id, d.wo.product_code, d.wo.product_name, d.wo.spec_model, d.wo.unit, d.wo.plan_qty,
        dstr(ps), dstr(pe), r.resource_id, r.resource_name, d.wo.priority === 'high' ? '0' : '1', status,
        status === '0' ? null : '生产经理', status === '0' ? null : fmt(new Date(ps.getTime() - 86400000)),
        status === '0' ? null : '产能与交期可满足，同意下达', '按需求自动生成 MPS', fmt(new Date(ps.getTime() - 2 * 86400000))]);
    mpsList.push({ id: ins.insertId, no, demand: d });
    mCnt++;
  }
  console.log(`主生产计划 ${mCnt} 条`);

  // ---------- 3. 排产 & 排产任务 ----------
  const dispatches = await q(`SELECT * FROM mms_dispatch WHERE del_flag='0' ORDER BY dispatch_id`);
  const dispByWo = new Map();
  dispatches.forEach(d => { if (!dispByWo.has(d.work_order_id)) dispByWo.set(d.work_order_id, []); dispByWo.get(d.work_order_id).push(d); });
  const woById = new Map(wos.map(w => [w.work_order_id, w]));
  let sCnt = 0, stCnt = 0;
  for (const [i, m] of mpsList.slice(0, 8).entries()) {
    const ps = new Date(m.demand.wo.plan_start);
    const no = 'PC' + dstr(ps).replace(/-/g, '') + pad(i + 1);
    const ins = await exec(`INSERT INTO mms_schedule(schedule_no,plan_id,strategy,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?, '0','system',?,'按产能正排，优先高优先级订单')`,
      [no, m.id, i % 3 === 0 ? '2' : '1', i < 6 ? '0' : '1', fmt(new Date(ps.getTime() - 86400000))]);
    sCnt++;
    // 取该需求对应工单的派工行作为排产任务
    const wo = m.demand.wo;
    const ds = (dispByWo.get(wo.work_order_id) || []).slice(0, 6);
    for (const d of ds) {
      if (!d.resource_id) continue;
      await exec(`INSERT INTO mms_schedule_task(schedule_no,schedule_id,work_order_id,work_order_no,product_id,product_code,product_name,resource_id,resource_name,op_seq,plan_start,plan_end,setup_minutes,strategy,locked,status,del_flag,create_by,create_time,remark,dept_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?,104)`,
        [no, ins.insertId, wo.work_order_id, wo.work_order_no, wo.product_id, wo.product_code, wo.product_name,
          d.resource_id, d.resource_name, d.op_seq, d.plan_start, d.plan_end, ri(15, 60), i % 3 === 0 ? '2' : '1',
          '1', '0', fmt(new Date(ps.getTime() - 86400000)), '排产锁定']);
      stCnt++;
    }
  }
  console.log(`排产单 ${sCnt} 张 / 排产任务 ${stCnt} 条`);

  // ---------- 4. 产能负荷 ----------
  let cCnt = 0;
  const base = new Date('2026-10-19T00:00:00');
  for (let k = 0; k < 14; k++) {
    const day = new Date(base.getTime() + k * 86400000);
    const dow = day.getDay();
    const calendarHours = (dow === 0 || dow === 6) ? 0 : 14;
    for (const r of res) {
      const load = calendarHours === 0 ? 0 : Number((calendarHours * rnd(0.55, 1.15)).toFixed(2));
      const rate = calendarHours === 0 ? 0 : Number((load / calendarHours).toFixed(4));
      await exec(`INSERT INTO mms_capacity_load(snapshot_date,resource_id,resource_name,calendar_hours,load_hours,load_rate,create_time)
        VALUES(?,?,?,?,?,?,NOW())`, [dstr(day), r.resource_id, r.resource_name, calendarHours, load, rate]);
      cCnt++;
    }
  }
  console.log(`产能负荷 ${cCnt} 行`);

  // ---------- 5. OEE 快照 ----------
  const dt = await q(`SELECT resource_id, SUM(hours) h FROM mms_downtime WHERE del_flag='0' GROUP BY resource_id`);
  const dtMap = new Map(dt.map(x => [x.resource_id, Number(x.h)]));
  let oCnt = 0;
  for (let k = 0; k < 20; k++) {
    const day = new Date('2026-10-12T00:00:00');
    day.setDate(day.getDate() + k);
    for (const r of res) {
      const planned = 14;
      const down = Number((dtMap.get(r.resource_id) || 0) / 20 + rnd(0, 0.8)).toFixed(2);
      const run = Number((planned - down).toFixed(2));
      const avail = Number((run / planned).toFixed(4));
      const perf = Number(rnd(0.78, 0.96).toFixed(4));
      const qual = Number(rnd(0.965, 0.998).toFixed(4));
      const oee = Number((avail * perf * qual).toFixed(4));
      const good = Number((run * 120 * rnd(0.6, 1.1)).toFixed(3));
      await exec(`INSERT INTO mms_oee_snapshot(snapshot_date,resource_id,resource_name,work_order_id,work_order_no,planned_hours,run_hours,downtime_hours,availability,performance,quality_rate,oee,good_qty,defect_qty,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())`,
        [dstr(day), r.resource_id, r.resource_name, null, '', planned, run, down, avail, perf, qual, oee, good,
          Number((good * (1 - qual) / qual).toFixed(3))]);
      oCnt++;
    }
  }
  console.log(`OEE 快照 ${oCnt} 行`);

  // ---------- 6. 外协 ----------
  const outWo = wos.filter(w => w.status === 'finished').slice(-4);
  let ouCnt = 0;
  const outDefs = [
    { p: '包装印刷', sup: '昆明华彩包装制品有限公司', supId: 4, qty: 3000 },
    { p: '肠衣代加工', sup: '富源永胤商贸有限公司', supId: 3, qty: 500 },
    { p: '冷链运输', sup: '云南顺捷冷链物流有限公司', supId: 5, qty: 200 },
    { p: '礼盒组装', sup: '曲靖优选食材供应链公司', supId: 8, qty: 800 }
  ];
  for (const [i, d] of outDefs.entries()) {
    const wo = outWo[i % outWo.length];
    const day = new Date(wo.plan_start);
    const price = Number((2 + Math.random() * 8).toFixed(2));
    await exec(`INSERT INTO mms_outsource(outsource_no,work_order_id,work_order_no,op_seq,process_name,product_id,product_code,product_name,spec_model,unit,supplier_id,supplier_name,plan_qty,out_qty,unit_price,total_amount,out_date,plan_back_date,actual_back_date,back_qty,loss_qty,status,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?,104)`,
      ['WX' + dstr(day).replace(/-/g, '') + pad(i + 1), wo.work_order_id, wo.work_order_no, i + 1, d.p,
        wo.product_id, wo.product_code, wo.product_name, wo.spec_model, wo.unit, d.supId, d.sup,
        d.qty, d.qty, price, (d.qty * price).toFixed(4), dstr(day), dstr(new Date(day.getTime() + 7 * 86400000)),
        dstr(new Date(day.getTime() + 6 * 86400000)), d.qty, 0, '2',
        fmt(day), '委外加工，回厂复检']);
    ouCnt++;
  }
  console.log(`外协 ${ouCnt} 条`);

  console.log(await q(`SELECT (SELECT COUNT(*) FROM mms_demand) 需求,(SELECT COUNT(*) FROM mms_mps) MPS,
    (SELECT COUNT(*) FROM mms_schedule) 排产,(SELECT COUNT(*) FROM mms_schedule_task) 排产任务,
    (SELECT COUNT(*) FROM mms_capacity_load) 产能,(SELECT COUNT(*) FROM mms_oee_snapshot) OEE,
    (SELECT COUNT(*) FROM mms_outsource) 外协`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
