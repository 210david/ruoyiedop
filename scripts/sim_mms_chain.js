/**
 * 依据现有 75 张工单 / 21 条工艺路线 / 21 个 BOM / 42 张领料单，
 * 反向模拟生产执行链：工艺快照 → BOM 快照 → 派工 → 报工 → 工序质检 → 异常/停机 → 齐套 → 批次追溯 → 生产日报
 */
const mysql = require('mysql2/promise');

const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const ymd = d => fmt(d).slice(0, 10).replace(/-/g, '');
const addMin = (d, m) => new Date(d.getTime() + m * 60000);
const rnd = (a, b) => a + Math.random() * (b - a);
const ri = (a, b) => Math.floor(rnd(a, b + 1));

function unitKg(spec, unit) {
  const s = String(spec || '');
  const m = s.match(/(\d+(?:\.\d+)?)\s*(kg|千克|g|克)\s*\/\s*[^\s/]+/i);
  if (m) { let v = parseFloat(m[1]); if (/^g$|克/i.test(m[2])) v /= 1000; return v; }
  if (unit === 'kg' || unit === '公斤' || unit === '千克') return 1;
  if (unit === '吨') return 1000;
  return 0.5;
}

const PROCESS_TEAM = { '分割分拣': 'TM01', '腌制': 'TM04', '灌装': 'TM03', '蒸煮熏制': 'TM04', '包装': 'TM06', '检验': 'TM06' };
const PROCESS_RES = { '分割分拣': 1, '腌制': 3, '灌装': 4, '蒸煮熏制': 5, '包装': 8, '检验': 9 };

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const routes = await q(`SELECT * FROM mms_route`);
  const routeById = new Map(routes.map(r => [r.route_id, r]));
  const routeByProd = new Map(routes.map(r => [r.product_code, r]));
  const procs = await q(`SELECT * FROM mms_route_process ORDER BY route_id, step_seq`);
  const procsByRoute = new Map();
  procs.forEach(p => { if (!procsByRoute.has(p.route_id)) procsByRoute.set(p.route_id, []); procsByRoute.get(p.route_id).push(p); });

  const boms = await q(`SELECT * FROM mms_bom`);
  const bomById = new Map(boms.map(b => [b.bom_id, b]));
  const bomByProd = new Map(boms.map(b => [b.product_code, b]));
  const bomDetail = await q(`SELECT * FROM mms_bom_detail ORDER BY bom_id, seq`);
  const detailByBom = new Map();
  bomDetail.forEach(d => { if (!detailByBom.has(d.bom_id)) detailByBom.set(d.bom_id, []); detailByBom.get(d.bom_id).push(d); });

  const wos = await q(`SELECT * FROM mms_work_order WHERE del_flag='0' ORDER BY work_order_id`);

  // 产品 -> 路线/BOM 兜底映射
  const fallbackRoute = name => {
    const n = String(name || '');
    if (/肠/.test(n)) return routeByProd.get('WL20260003').route_id;          // 广味香肠 6 道
    if (/火腿|腿|腊|培根|酱香猪脸|琵琶/.test(n)) return routeByProd.get('WL20260006').route_id; // 4 道
    if (/酱|鸡枞/.test(n)) return routeByProd.get('WL20260006').route_id;
    return routeByProd.get('WL20260228').route_id;                            // 分割肉 3 道
  };
  const fallbackBom = name => {
    const n = String(name || '');
    if (/肠/.test(n)) return bomByProd.get('WL20260003').bom_id;
    if (/火腿|腿|腊|培根|酱香猪脸|琵琶|酱/.test(n)) return bomByProd.get('WL20260006').bom_id;
    return bomByProd.get('WL20260228').bom_id;
  };

  const resByProc = { '分割分拣': ['分割台组', 1], '腌制': ['滚揉机', 3], '灌装': ['真空灌装机', 4], '蒸煮熏制': ['蒸煮锅', 5], '包装': ['真空包装机', 8], '检验': ['金属探测仪', 9] };
  const teamNames = Object.fromEntries((await q(`SELECT team_code, team_name FROM mms_team`)).map(t => [t.team_code, t.team_name]));

  // ============ 清空待生成表 ============
  for (const t of ['mms_wo_route_snapshot', 'mms_work_order_routing', 'mms_wo_bom_snapshot', 'mms_work_order_bom',
    'mms_dispatch', 'mms_work_report', 'mms_qc', 'mms_abnormal', 'mms_downtime', 'mms_kit_check',
    'mms_kit_check_detail', 'mms_batch_trace', 'mms_stat_daily']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ============ 1. 工艺快照 + BOM 快照 ============
  let rSnap = 0, bSnap = 0;
  const woPlan = new Map(); // work_order_id -> {routeId, steps}
  for (const wo of wos) {
    const route = routeByProd.get(wo.product_code) || routeById.get(fallbackRoute(wo.product_name));
    const steps = procsByRoute.get(route.route_id) || [];
    woPlan.set(wo.work_order_id, { route, steps });
    for (const s of steps) {
      await exec(`INSERT INTO mms_wo_route_snapshot(work_order_id,route_id,route_no,route_version,step_seq,process_id,process_code,process_name,resource_id,resource_name,std_time,prep_time,is_key_process,is_outsource,del_flag,create_by,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?)`,
        [wo.work_order_id, route.route_id, route.route_no, route.version, s.step_seq, s.process_id, s.process_code, s.process_name,
          resByProc[s.process_name] ? resByProc[s.process_name][1] : null, resByProc[s.process_name] ? resByProc[s.process_name][0] : '',
          s.std_time, s.prep_time, s.is_key_process, s.is_outsource, fmt(new Date(wo.create_time))]);
      await exec(`INSERT INTO mms_work_order_routing(work_order_id,route_id,op_seq,process_id,process_code,process_name,resource_id,resource_name,std_time,prep_time,is_key_process,is_outsource,report_mode,is_danger,qc_flag,snapshot_version,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?, '1','0',?, '1.0',?)`,
        [wo.work_order_id, route.route_id, s.step_seq, s.process_id, s.process_code, s.process_name,
          resByProc[s.process_name] ? resByProc[s.process_name][1] : null, resByProc[s.process_name] ? resByProc[s.process_name][0] : '',
          s.std_time, s.prep_time, s.is_key_process, s.is_outsource, s.is_key_process === '1' ? '1' : '0', fmt(new Date(wo.create_time))]);
      rSnap++;
    }

    const bom = bomByProd.get(wo.product_code) || bomById.get(fallbackBom(wo.product_name));
    const lines = detailByBom.get(bom.bom_id) || [];
    const kgPer = unitKg(wo.spec_model, wo.unit);
    const totalKg = Number(wo.plan_qty) * kgPer;
    let seq = 0;
    for (const l of lines) {
      seq++;
      const loss = Number(l.loss_rate) || 0;
      const usage = Number((totalKg * Number(l.usage_qty) / 100 * (1 + loss / 100)).toFixed(3));
      await exec(`INSERT INTO mms_wo_bom_snapshot(work_order_id,bom_id,bom_no,bom_version,seq,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,is_key_material,supply_type,pick_store_id,pick_store_name,is_phantom,del_flag,create_by,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','0','system',?)`,
        [wo.work_order_id, bom.bom_id, bom.bom_no, bom.version, seq, l.material_id, l.material_code, l.material_name, l.spec_model, l.unit,
          usage, l.loss_rate, l.is_key_material, l.supply_type, 1, '冷冻原料库', fmt(new Date(wo.create_time))]);
      await exec(`INSERT INTO mms_work_order_bom(work_order_id,bom_id,bom_no,material_id,material_code,material_name,spec_model,unit,usage_qty,loss_rate,supply_type,is_key_material,pick_store_id,snapshot_version,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?, '1.0',?)`,
        [wo.work_order_id, bom.bom_id, bom.bom_no, l.material_id, l.material_code, l.material_name, l.spec_model, l.unit,
          usage, l.loss_rate, l.supply_type, l.is_key_material, 1, fmt(new Date(wo.create_time))]);
      bSnap++;
    }

    await exec(`UPDATE mms_work_order SET route_id=?, route_no=?, bom_id=?, bom_no=? WHERE work_order_id=?`,
      [route.route_id, route.route_no, bom.bom_id, bom.bom_no, wo.work_order_id]);
  }
  console.log(`工艺快照 ${rSnap} 条，BOM 快照 ${bSnap} 行`);

  // ============ 2. 派工 ============
  const dispatchMap = new Map(); // workOrderId -> [{dispatchId, step, start, end, status}]
  let dSeqByDay = new Map(), dCount = 0;
  for (const wo of wos) {
    const { steps } = woPlan.get(wo.work_order_id);
    const ps = new Date(wo.plan_start), pf = new Date(wo.plan_finish);
    const totalW = steps.reduce((a, s) => a + (Number(s.std_time) || 60), 0) || 1;
    let cursor = new Date(ps);
    const spanMin = Math.max(120, (pf - ps) / 60000);
    const arr = [];
    const done = wo.status === 'finished';
    const running = wo.status === 'processing';
    for (let i = 0; i < steps.length; i++) {
      const s = steps[i];
      const dur = Math.max(30, spanMin * (Number(s.std_time) || 60) / totalW);
      const st = new Date(cursor), en = addMin(st, dur);
      cursor = en;
      let status = '0', actualStart = null, actualEnd = null;
      if (done) { status = '2'; actualStart = addMin(st, ri(-20, 40)); actualEnd = addMin(en, ri(-30, 60)); }
      else if (running && i < steps.length - 1) { status = '2'; actualStart = addMin(st, ri(-20, 30)); actualEnd = addMin(en, ri(-20, 40)); }
      else if (running && i === steps.length - 1) { status = '1'; actualStart = addMin(st, ri(0, 30)); }
      const key = ymd(st);
      dSeqByDay.set(key, (dSeqByDay.get(key) || 0) + 1);
      const dno = 'PG' + key + pad(dSeqByDay.get(key));
      const team = PROCESS_TEAM[s.process_name] || 'TM01';
      const res = resByProc[s.process_name] || [null, null];
      const ins = await exec(`INSERT INTO mms_dispatch(dispatch_no,work_order_id,work_order_no,product_code,product_name,spec_model,unit,op_seq,process_id,process_name,resource_id,resource_name,team_id,team_name,user_ids,plan_qty,good_qty,defect_qty,plan_start,plan_end,actual_start,actual_end,status,del_flag,create_by,create_time,remark,dept_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,'按工艺路线自动派工',104)`,
        [dno, wo.work_order_id, wo.work_order_no, wo.product_code, wo.product_name, wo.spec_model, wo.unit, s.step_seq, s.process_id, s.process_name,
          res[1], res[0] || '', teamNames[team] ? Number(String(team).replace('TM', '')) : null, teamNames[team] || '',
          team === 'TM03' ? '刘洋,王雪姣' : (teamNames[team] || ''), wo.plan_qty,
          status === '2' ? wo.finished_qty : 0, 0, fmt(st), fmt(en), actualStart ? fmt(actualStart) : null, actualEnd ? fmt(actualEnd) : null,
          status, fmt(new Date(wo.create_time))]);
      arr.push({ id: ins.insertId, step: s, start: st, end: en, status, actualStart, actualEnd });
      dCount++;
    }
    dispatchMap.set(wo.work_order_id, arr);

    if (done) {
      const first = arr[0], last = arr[arr.length - 1];
      await exec(`UPDATE mms_work_order SET actual_start=?, actual_finish=? WHERE work_order_id=?`,
        [first.actualStart ? fmt(first.actualStart) : null, last.actualEnd ? fmt(last.actualEnd) : null, wo.work_order_id]);
    }
  }
  console.log(`派工 ${dCount} 条`);

  // ============ 3. 成品批次号（回填完工入库单） ============
  const batchByWo = new Map();
  const seqByDay = new Map();
  const receipts = await q(`SELECT * FROM mms_finish_receipt WHERE del_flag='0' ORDER BY finish_id`);
  for (const r of receipts) {
    const d = new Date(r.finish_time || r.create_time);
    const k = ymd(d);
    seqByDay.set(k, (seqByDay.get(k) || 0) + 1);
    const batch = 'CP' + k + pad(seqByDay.get(k));
    if (!batchByWo.has(r.work_order_id)) batchByWo.set(r.work_order_id, batch);
    if (!r.batch_no) await exec(`UPDATE mms_finish_receipt SET batch_no=? WHERE finish_id=?`, [batch, r.finish_id]);
  }
  console.log(`成品批次 ${batchByWo.size} 个，回填完工入库单 ${receipts.filter(r => !r.batch_no).length} 张`);

  // ============ 4. 报工 ============
  let wCount = 0;
  const wSeqByDay = new Map();
  const reportRows = [];
  for (const wo of wos) {
    const arr = dispatchMap.get(wo.work_order_id);
    const n = arr.length;
    const batch = batchByWo.get(wo.work_order_id) || null;
    const fin = Number(wo.finished_qty) || 0;
    for (let i = 0; i < n; i++) {
      const d = arr[i];
      if (d.status === '0') continue;
      const isLast = i === n - 1;
      const good = d.status === '2' ? Number((fin * (1 + 0.004 * (n - 1 - i))).toFixed(3)) : Number((fin * rnd(0.35, 0.6)).toFixed(3));
      if (good <= 0) continue;
      const defect = (wo.status === 'finished' && !isLast && /包装|灌装/.test(d.step.process_name))
        ? Number((Number(wo.defect_qty) * rnd(0.3, 0.7)).toFixed(3)) : 0;
      const rt = d.actualEnd ? addMin(d.actualEnd, ri(-10, 20)) : d.end;
      const key = ymd(rt);
      wSeqByDay.set(key, (wSeqByDay.get(key) || 0) + 1);
      const rno = 'BG' + key + pad(wSeqByDay.get(key));
      const shift = /腌制/.test(d.step.process_name) ? (rt.getHours() >= 16 ? 'S02' : 'S01') : 'S01';
      const hours = Math.max(0.5, ((d.actualEnd || d.end) - (d.actualStart || d.start)) / 3600000).toFixed(2);
      await exec(`INSERT INTO mms_work_report(report_no,work_order_id,work_order_no,batch_id,batch_code,process_id,process_name,resource_id,resource_name,good_qty,defect_qty,work_hours,report_time,report_by,team_id,team_name,shift_id,shift_name,is_first_piece,status,audit_by,audit_time,audit_remark,source,remark,del_flag,create_by,create_time,dept_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '1',?,?, '正常报工', '1',?, '0','system',?,104)`,
        [rno, wo.work_order_id, wo.work_order_no, null, batch, d.step.process_id, d.step.process_name,
          PROCESS_RES[d.step.process_name] || null, resByProc[d.step.process_name] ? resByProc[d.step.process_name][0] : '',
          good, defect, hours, fmt(rt), teamNames[PROCESS_TEAM[d.step.process_name]] ? 'system' : 'system',
          Number(String(PROCESS_TEAM[d.step.process_name]).replace('TM', '')), teamNames[PROCESS_TEAM[d.step.process_name]] || '',
          shift === 'S01' ? 1 : 2, shift === 'S01' ? '白班' : '中班', i === 0 ? '1' : '0',
          d.status === '2' ? '质检员' : null, d.status === '2' ? fmt(addMin(rt, ri(30, 180))) : null,
          isLast ? (batch ? '批次:' + batch : '') : '', fmt(new Date(wo.create_time))]);
      wCount++;
      reportRows.push({ wo, d, good, defect, rt, isLast, batch });
    }
  }
  console.log(`报工 ${wCount} 条`);

  // ============ 5. 工序质检 ============
  let qCount = 0, qSeqByDay = new Map();
  const DEFECTS = ['0', '1', '2', '3', '4'];
  for (const wo of wos) {
    if (wo.status !== 'finished' && wo.status !== 'processing') continue;
    const arr = dispatchMap.get(wo.work_order_id);
    const last = arr[arr.length - 1];
    const first = arr[0];
    const plan = [
      { d: first, type: '0', n: 1, defect: 0, result: '0' },
      { d: arr[Math.min(1, arr.length - 1)], type: '1', n: 1, defect: Number(wo.defect_qty) > 0 ? Math.min(3, Number(wo.defect_qty)) : 0, result: Number(wo.defect_qty) > 0 ? '1' : '0' },
      { d: last, type: '2', n: 1, defect: 0, result: '0' }
    ];
    for (const p of plan) {
      if (p.d.status === '0') continue;
      const t = p.d.actualEnd ? addMin(p.d.actualEnd, ri(5, 30)) : p.d.end;
      const key = ymd(t);
      qSeqByDay.set(key, (qSeqByDay.get(key) || 0) + 1);
      const qno = 'ZJ' + key + pad(qSeqByDay.get(key));
      const inspectQty = Math.max(1, Math.min(30, Math.round(Number(wo.plan_qty) * 0.02)));
      await exec(`INSERT INTO mms_qc(qc_no,work_order_id,work_order_no,product_code,product_name,spec_model,unit,plan_qty,process_id,process_name,qc_type,inspect_qty,defect_qty,scrap_qty,defect_type,qc_result,qc_by,qc_time,remark,del_flag,create_by,create_time,dept_id)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,104)`,
        [qno, wo.work_order_id, wo.work_order_no, wo.product_code, wo.product_name, wo.spec_model, wo.unit, wo.plan_qty,
          p.d.step.process_id, p.d.step.process_name, p.type, inspectQty, p.defect, p.defect > 0 ? 1 : 0,
          p.defect > 0 ? DEFECTS[ri(0, 4)] : null, p.result, '质检员', fmt(t),
          p.defect > 0 ? '检出少量外观不良，已隔离挑拣' : '按抽样方案检验，符合要求', fmt(new Date(wo.create_time))]);
      qCount++;
    }
  }
  console.log(`工序质检 ${qCount} 条`);

  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
