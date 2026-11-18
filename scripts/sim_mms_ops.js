/**
 * 生产执行补充：异常 / 停机 / 齐套 / 批次追溯 / 生产日报 + 领料批次号回填
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

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const wos = await q(`SELECT * FROM mms_work_order WHERE del_flag='0' ORDER BY plan_start`);
  const woById = new Map(wos.map(w => [w.work_order_id, w]));
  const dispatches = await q(`SELECT * FROM mms_dispatch WHERE del_flag='0' ORDER BY dispatch_id`);
  const dispByWo = new Map();
  dispatches.forEach(d => { if (!dispByWo.has(d.work_order_id)) dispByWo.set(d.work_order_id, []); dispByWo.get(d.work_order_id).push(d); });

  for (const t of ['mms_abnormal', 'mms_downtime', 'mms_kit_check', 'mms_kit_check_detail', 'mms_batch_trace', 'mms_stat_daily']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ============ 1. 生产异常 ============
  const finished = wos.filter(w => w.status === 'finished' && Number(w.finished_qty) > 0);
  const pick = [];
  for (let i = 0; i < 14; i++) pick.push(finished[Math.floor(i * finished.length / 14)]);
  const ABN = [
    { type: '0', res: 4, desc: '灌装机封口刀座磨损，封口出现虚封，停机更换刀座', sev: '1', method: '更换零件', cause: '易损件未按周期更换', prevent: '将封口刀座纳入月度点检强制更换清单' },
    { type: '0', res: 5, desc: '蒸煮锅压力表读数漂移，无法确认中心温度，暂停蒸煮', sev: '1', method: '更换零件', cause: '压力表超期未校准', prevent: '压力表纳入年度校准计划并挂牌标识' },
    { type: '1', res: 1, desc: '到货原料中心温度 -8℃，未达冷冻原料 -18℃ 要求，暂停领用', sev: '2', method: '补充物料', cause: '冷链运输保温不足', prevent: '对供应商冷链车辆加装温度记录仪并到货查验' },
    { type: '2', res: 8, desc: '真空包装后发现少量袋体针孔漏气，判定为包装工序不良', sev: '1', method: '返工处理', cause: '包装袋热封温度偏低', prevent: '热封温度纳入首件确认参数' },
    { type: '0', res: 9, desc: '金属探测仪试块测试报警异常，停线校验', sev: '2', method: '调整参数', cause: '探头灵敏度漂移', prevent: '每班开机前做 Fe/SUS 试块验证' },
    { type: '1', res: 3, desc: '腌制用食盐批次到货延迟，腌制工序待料 4 小时', sev: '0', method: '人员调配', cause: '供应商发货延误', prevent: '关键辅料设置安全库存与二供方' },
    { type: '0', res: 3, desc: '滚揉机减速箱异响，降速运行', sev: '1', method: '设备维修', cause: '润滑不足', prevent: '增加润滑周期点检' },
    { type: '2', res: 4, desc: '灌装计量偏差超 ±3%，单袋重量不稳定', sev: '1', method: '调整参数', cause: '计量泵密封圈磨损', prevent: '每班首件称重并记录' },
    { type: '0', res: 8, desc: '真空泵抽真空时间延长，真空度不足', sev: '0', method: '设备维修', cause: '真空泵油乳化', prevent: '按 500h 更换真空泵油' },
    { type: '1', res: 1, desc: '分割间环境温度升至 18℃，超出 12℃ 控制要求', sev: '1', method: '停线整改', cause: '制冷机组高压保护跳停', prevent: '加装温控报警并联动停机' },
    { type: '2', res: 5, desc: '烟熏上色不均，局部发白', sev: '0', method: '返工处理', cause: '烟熏炉风道积垢', prevent: '每周清理风道' },
    { type: '0', res: 7, desc: '卤制锅夹层压力不足，升温缓慢', sev: '0', method: '设备维修', cause: '蒸汽疏水阀堵塞', prevent: '疏水阀季度拆检' },
    { type: '2', res: 9, desc: '金检机剔除装置动作迟缓，存在漏剔风险', sev: '2', method: '停线整改', cause: '气缸气压不足', prevent: '气路压力纳入班前点检' },
    { type: '1', res: 1, desc: '包材版面印刷与最新版标签不一致，暂停使用', sev: '1', method: '补充物料', cause: '供应商使用旧版文件', prevent: '包材到货比对现行版本并留样' }
  ];
  const aSeqByDay = new Map();
  const abnIds = [];
  const users = ['张伟', '刘洋', '陈静', '孙丽', '赵雷'];
  for (let i = 0; i < pick.length; i++) {
    const wo = pick[i], a = ABN[i % ABN.length];
    const ds = dispByWo.get(wo.work_order_id) || [];
    const d = ds[Math.min(ds.length - 1, ri(0, Math.max(0, ds.length - 1)))] || null;
    const t = d && d.actual_start ? new Date(d.actual_start) : new Date(wo.plan_start);
    const rt = addMin(t, ri(30, 240));
    const key = ymd(rt);
    aSeqByDay.set(key, (aSeqByDay.get(key) || 0) + 1);
    const no = 'YC' + key + pad(aSeqByDay.get(key));
    const closed = i % 4 !== 0;
    const resp = addMin(rt, ri(10, 45));
    const end = addMin(resp, ri(60, 300));
    const hrs = ((end - rt) / 3600000).toFixed(2);
    const ins = await exec(`INSERT INTO mms_abnormal(abnormal_no,work_order_id,work_order_no,resource_id,resource_name,product_code,product_name,spec_model,unit,abnormal_type,severity,description,report_by,report_time,response_by,response_time,response_measure,impact_scope,estimated_restore_time,response_remark,handle_by,handle_time,close_time,handle_result,handle_method,root_cause,preventive_measure,downtime_hours,need_pursuit,status,remark,del_flag,create_by,create_time,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,104)`,
      [no, wo.work_order_id, wo.work_order_no, a.res, d ? d.resource_name : '', wo.product_code, wo.product_name, wo.spec_model, wo.unit,
        a.type, a.sev, a.desc, users[i % users.length], fmt(rt), '车间主任', fmt(resp),
        '现场确认后按异常处置流程响应，隔离在制品并通知相关部门', '当班在制工单', fmt(addMin(resp, 120)), '已通知设备/质量/仓储到场',
        closed ? '车间主任' : null, closed ? fmt(addMin(resp, 90)) : null, closed ? fmt(end) : null,
        closed ? '处置完成，复检合格恢复生产' : '', a.method, a.cause, a.prevent, hrs, a.sev === '2' ? '1' : '0',
        closed ? '2' : '1', '', fmt(rt)]);
    abnIds.push({ id: ins.insertId, no, wo, d, rt, end, type: a.type, hours: hrs });
  }
  console.log(`生产异常 ${abnIds.length} 条`);

  // ============ 2. 停机记录 ============
  const dSeqByDay = new Map();
  let dtCount = 0;
  for (const a of abnIds) {
    if (a.type !== '0' && a.type !== '1') continue;
    const key = ymd(a.rt);
    dSeqByDay.set(key, (dSeqByDay.get(key) || 0) + 1);
    const no = 'TJ' + key + pad(dSeqByDay.get(key));
    await exec(`INSERT INTO mms_downtime(downtime_no,dispatch_id,abnormal_id,abnormal_no,work_order_id,resource_id,resource_name,start_time,end_time,dt_type,dt_category,dt_level,reason,hours,report_by,handle_by,handle_result,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?,104)`,
      [no, a.d ? a.d.dispatch_id : null, a.id, a.no, a.wo.work_order_id, a.d ? a.d.resource_id : null, a.d ? a.d.resource_name : '',
        fmt(a.rt), fmt(a.end), a.type === '0' ? '1' : '3', '1', a.hours > 3 ? '1' : '0',
        a.type === '0' ? '设备故障停机' : '物料短缺停机', a.hours, '车间主任', '设备主管', '修复后试机正常', fmt(a.rt), '', 104]);
    dtCount++;
  }
  // 补充换型停机（无异常单）
  const changeOvers = dispatches.filter(d => d.status === '2' && d.actual_end).slice(0, 8);
  for (const d of changeOvers) {
    const st = addMin(new Date(d.actual_end), ri(5, 40));
    const en = addMin(st, ri(25, 75));
    const key = ymd(st);
    dSeqByDay.set(key, (dSeqByDay.get(key) || 0) + 1);
    const no = 'TJ' + key + pad(dSeqByDay.get(key));
    await exec(`INSERT INTO mms_downtime(downtime_no,dispatch_id,work_order_id,resource_id,resource_name,start_time,end_time,dt_type,dt_category,dt_level,reason,hours,report_by,handle_by,handle_result,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?, '2','0','0',?,?,?,?,?, '0','system',?,?,104)`,
      [no, d.dispatch_id, d.work_order_id, d.resource_id, d.resource_name, fmt(st), fmt(en),
        '规格切换，更换模具/调整设备参数', ((en - st) / 3600000).toFixed(2), '班组长', '班组长', '换型完成，首件确认合格', fmt(st), '计划内换型', 104]);
    dtCount++;
  }
  console.log(`停机记录 ${dtCount} 条`);

  // ============ 3. 齐套检查 ============
  const inv = await q(`SELECT material_id, SUM(qty - lock_qty) qty FROM wms_inventory WHERE del_flag='0' GROUP BY material_id`);
  const invMap = new Map(inv.map(i => [i.material_id, Number(i.qty)]));
  const recent = [...wos].sort((a, b) => new Date(b.create_time) - new Date(a.create_time)).slice(0, 12);
  let kitCnt = 0, kitDetail = 0;
  const kSeqByDay = new Map();
  for (const wo of recent) {
    const lines = await q(`SELECT * FROM mms_work_order_bom WHERE work_order_id=?`, [wo.work_order_id]);
    if (!lines.length) continue;
    const t = new Date(wo.create_time);
    const key = ymd(t);
    kSeqByDay.set(key, (kSeqByDay.get(key) || 0) + 1);
    const no = 'QT' + key + pad(kSeqByDay.get(key));
    const ins = await exec(`INSERT INTO mms_kit_check(kit_no,work_order_id,work_order_no,bom_id,check_time,kit_rate,is_complete,status,remark,del_flag,create_by,create_time,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?, '0','system',?,104)`,
      [no, wo.work_order_id, wo.work_order_no, wo.bom_id, fmt(t), 0, '0', '1', '下达前齐套检查', fmt(t)]);
    let req = 0, ok = 0, allOk = true;
    for (const l of lines) {
      const need = Number(l.usage_qty);
      const avail = Number((invMap.get(l.material_id) || 0).toFixed(3));
      const short = Math.max(0, Number((need - avail).toFixed(3)));
      req++; if (short === 0) ok++; else allOk = false;
      await exec(`INSERT INTO mms_kit_check_detail(kit_id,material_id,material_code,material_name,spec_model,unit,required_qty,available_qty,shortage_qty,is_complete,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0','system',?,?)`,
        [ins.insertId, l.material_id, l.material_code, l.material_name, l.spec_model, l.unit, need, avail, short, short === 0 ? '1' : '0', fmt(t), short === 0 ? '' : '库存不足，需紧急采购']);
      kitDetail++;
    }
    const rate = (ok / req * 100).toFixed(2);
    await exec(`UPDATE mms_kit_check SET kit_rate=?, is_complete=?, status=? WHERE kit_id=?`,
      [rate, allOk ? '1' : '0', allOk ? '1' : '2', ins.insertId]);
    kitCnt++;
  }
  console.log(`齐套检查 ${kitCnt} 张 / ${kitDetail} 行`);

  // ============ 4. 领料明细批次号回填 + 批次追溯 ============
  const issues = await q(`SELECT i.*, i.issue_time FROM mms_issue i WHERE i.del_flag='0' ORDER BY issue_id`);
  const details = await q(`SELECT * FROM mms_issue_detail ORDER BY detail_id`);
  const detByIssue = new Map();
  details.forEach(d => { if (!detByIssue.has(d.issue_id)) detByIssue.set(d.issue_id, []); detByIssue.get(d.issue_id).push(d); });
  const ySeqByDay = new Map();
  const rawBatchByDetail = new Map();
  for (const is of issues) {
    const day = ymd(new Date(is.issue_time));
    for (const d of detByIssue.get(is.issue_id) || []) {
      ySeqByDay.set(day, (ySeqByDay.get(day) || 0) + 1);
      const b = 'YL' + day + pad(ySeqByDay.get(day));
      rawBatchByDetail.set(d.detail_id, b);
      if (!d.batch_no) await exec(`UPDATE mms_issue_detail SET batch_no=? WHERE detail_id=?`, [b, d.detail_id]);
    }
  }
  const receipts = await q(`SELECT * FROM mms_finish_receipt WHERE del_flag='0' AND batch_no IS NOT NULL AND batch_no<>''`);
  const batchByWo = new Map(receipts.map(r => [r.work_order_id, r.batch_no]));
  let trCount = 0;
  for (const is of issues) {
    const out = batchByWo.get(is.work_order_id);
    if (!out) continue;
    for (const d of detByIssue.get(is.issue_id) || []) {
      await exec(`INSERT INTO mms_batch_trace(work_order_id,op_seq,batch_out,batch_in,material_in,material_code,qty_used,create_time,create_by)
        VALUES(?,1,?,?,?,?,?,?,'system')`,
        [is.work_order_id, out, rawBatchByDetail.get(d.detail_id) || '', d.material_id, d.material_code, d.issue_qty, fmt(new Date(is.issue_time + ' '))]);
      trCount++;
    }
  }
  console.log(`批次追溯 ${trCount} 条`);

  // ============ 5. 生产日报 ============
  const rows = await q(`SELECT DATE(report_time) d,
      COUNT(DISTINCT work_order_id) wo_cnt, COUNT(*) rep_cnt, SUM(good_qty) good, SUM(defect_qty) defect
    FROM mms_work_report WHERE del_flag='0' GROUP BY DATE(report_time) ORDER BY d`);
  let sCount = 0;
  for (const r of rows) {
    const ab = await q(`SELECT COUNT(*) c FROM mms_abnormal WHERE DATE(report_time)=? `, [r.d]);
    const finishedC = await q(`SELECT COUNT(*) c FROM mms_work_order WHERE status='finished' AND DATE(actual_finish)=?`, [r.d]);
    const planSum = await q(`SELECT SUM(plan_qty) s FROM mms_work_order WHERE DATE(plan_start)<=? AND DATE(plan_finish)>=?`, [r.d, r.d]);
    const oee = (0.72 + Math.random() * 0.16).toFixed(4);
    await exec(`INSERT INTO mms_stat_daily(stat_date,work_order_count,completed_count,in_progress_count,abnormal_count,total_plan_qty,total_good_qty,total_defect_qty,avg_oee,report_count,create_time)
      VALUES(?,?,?,?,?,?,?,?,?,?,NOW())`,
      [r.d, r.wo_cnt, Number(finishedC[0].c) || 0, Math.max(0, r.wo_cnt - (Number(finishedC[0].c) || 0)), ab[0].c,
        planSum[0].s || 0, r.good, r.defect, oee, r.rep_cnt]);
    sCount++;
  }
  console.log(`生产日报 ${sCount} 天`);

  console.log(await q(`SELECT
    (SELECT COUNT(*) FROM mms_work_order_bom) 工单BOM,
    (SELECT COUNT(*) FROM mms_work_order_routing) 工单工艺,
    (SELECT COUNT(*) FROM mms_dispatch) 派工,
    (SELECT COUNT(*) FROM mms_work_report) 报工,
    (SELECT COUNT(*) FROM mms_qc) 质检,
    (SELECT COUNT(*) FROM mms_abnormal) 异常,
    (SELECT COUNT(*) FROM mms_downtime) 停机,
    (SELECT COUNT(*) FROM mms_kit_check) 齐套,
    (SELECT COUNT(*) FROM mms_batch_trace) 追溯,
    (SELECT COUNT(*) FROM mms_stat_daily) 日报`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
