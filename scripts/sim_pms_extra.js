/**
 * 采购模块补全：采购合同 / 合同变更 / 合同审批轨迹 / 发票付款 / 采购计划审批轨迹 / 供应商评价 / 评价审批轨迹
 * 依据：113 张采购订单、41 家供应商、12 张采购发票、6 张采购计划
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const addDays = (d, n) => new Date(new Date(d).getTime() + n * 86400000);
const MONEY = v => Math.round(Number(v) * 100) / 100;

const TODAY = '2026-11-16';
const PARTY_A = '云南赢隆食品加工有限公司';
const ADDR = '云南省曲靖市宣威市工业园区';

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  for (const t of ['pms_contract', 'pms_contract_audit_log', 'pms_contract_change', 'pms_invoice_payment',
    'pms_plan_audit_log', 'pms_supplier_eval', 'pms_supplier_eval_audit_log']) {
    await exec(`DELETE FROM \`${t}\``);
    await exec(`ALTER TABLE \`${t}\` AUTO_INCREMENT=1`);
  }

  // ================= 1. 采购合同 =================
  const sups = await q(`SELECT s.supplier_id, s.supplier_name, s.supplier_short_name, s.payment_days, s.payment_method,
      s.bank_name, s.bank_account, s.contact_person, s.contact_phone, s.delivery_cycle,
      COUNT(o.order_id) cnt, SUM(o.total_amount) amt, SUM(o.paid_amount) paid,
      MIN(o.order_date) first_date, MAX(o.order_date) last_date
    FROM wms_supplier s JOIN pms_purchase_order o ON o.supplier_id = s.supplier_id AND o.del_flag='0'
    GROUP BY s.supplier_id ORDER BY amt DESC`);

  const termOf = (days, i) => {
    if (days > 0) return `月结${days}天，货到验收合格并收到合规发票后电汇支付`;
    const opts = ['货到验收合格后 30 天内银行转账支付', '预付 30%，到货验收合格后付 65%，质保金 5% 一年后支付',
      '月结 45 天，银行转账支付', '货到付款，验收合格当日结清'];
    return opts[i % opts.length];
  };
  const delivOf = i => [
    `供方负责送货至需方${ADDR}仓库，运费及途中损耗由供方承担`,
    `供方按需方采购订单分批送货，接到订单后 ${3 + (i % 5)} 个工作日内到货，运费由供方承担`,
    `需方自提，供方负责装车并出具出厂检验报告`,
    `供方送货至需方指定仓库，随货提供检验合格证、检疫证明及送货单`
  ][i % 4];

  const contracts = [];   // {contract_id, contract_no, supplier_id, type, order_id, order_no, amount}
  let seq = 0;
  const insContract = async (o) => {
    seq += 1;
    const no = `CGHT2026${pad(seq)}`;
    const ins = await exec(`INSERT INTO pms_contract(contract_no,contract_name,order_id,order_no,supplier_id,supplier_name,
        status,contract_type,sign_date,effective_date,expire_date,contract_amount,paid_amount,payment_terms,delivery_terms,
        sign_by,sign_department,party_a,party_b,file_url,file_name,change_count,terminate_date,terminate_reason,
        del_flag,create_by,create_time,audit_by,audit_time,audit_opinion,dept_id,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','admin',?,?,?,?,104,?)`,
      [no, o.name, o.orderId || null, o.orderNo || null, o.supId, o.supName,
        o.status, o.type, o.signDate, o.effDate, o.expDate, MONEY(o.amount), MONEY(o.paid),
        o.terms, o.deliv, '李晨', '采购部', PARTY_A, o.supName,
        `/profile/contract/2026/${no}.pdf`, `${no}_${o.supName}采购合同.pdf`, o.changeCount || 0,
        o.terminateDate || null, o.terminateReason || null,
        fmt(new Date(o.signDate + 'T09:30:00')), o.auditBy || null, o.auditTime || null, o.auditOpinion || null,
        o.remark || null]);
    contracts.push({ contract_id: ins.insertId, contract_no: no, supplier_id: o.supId, type: o.type, order_id: o.orderId || null, order_no: o.orderNo || null, amount: MONEY(o.amount) });
    return ins.insertId;
  };

  // 1.1 年度框架协议（按采购额前 20 家）
  const topSups = sups.slice(0, 20);
  for (const [i, s] of topSups.entries()) {
    const amt = Number(s.amt);
    const paid = Math.min(Number(s.paid), amt);
    const signDate = s.first_date < '2026-01-05' ? dstr(addDays('2026-01-05', -(i % 12))) : dstr(addDays(s.first_date, -3));
    await insContract({
      name: `2026年度${s.supplier_short_name || s.supplier_name}采购框架协议`,
      supId: s.supplier_id, supName: s.supplier_name, type: '0',
      signDate, effDate: '2026-01-01', expDate: '2026-12-31',
      amount: amt, paid: paid || 0, terms: termOf(s.payment_days, i), deliv: delivOf(i),
      status: '1', auditBy: '采购经理', auditTime: fmt(new Date(signDate + 'T14:20:00')),
      auditOpinion: '供应商资质、报价及付款条件核验通过，同意签订',
      remark: `覆盖 2026 年度累计 ${s.cnt} 笔采购订单，含税金额 ${MONEY(amt).toFixed(2)} 元`
    });
  }

  // 1.2 单笔采购合同（订单量 ≥2 的供应商，取金额最大订单）
  const bigSups = sups.filter(s => Number(s.cnt) >= 2).slice(0, 14);
  for (const [i, s] of bigSups.entries()) {
    const ord = await q(`SELECT order_id, order_no, order_date, total_amount, paid_amount FROM pms_purchase_order
      WHERE supplier_id=? AND del_flag='0' ORDER BY total_amount DESC LIMIT 1`, [s.supplier_id]);
    if (!ord.length) continue;
    const o = ord[0];
    const signDate = dstr(addDays(o.order_date, -2));
    const expDate = dstr(addDays(signDate, 180 + (i % 4) * 30));
    const expired = expDate < TODAY;
    await insContract({
      name: `${s.supplier_short_name || s.supplier_name}采购合同（${o.order_no}）`,
      supId: s.supplier_id, supName: s.supplier_name, type: '1',
      orderId: o.order_id, orderNo: o.order_no,
      signDate, effDate: signDate, expDate,
      amount: Number(o.total_amount), paid: Number(o.paid_amount),
      terms: termOf(s.payment_days, i + 1), deliv: delivOf(i + 1),
      status: expired ? '3' : '1',
      auditBy: '采购经理', auditTime: fmt(new Date(signDate + 'T16:05:00')),
      auditOpinion: '合同金额与中标报价一致，条款无异议，同意签订',
      remark: `对应采购订单 ${o.order_no}，合同有效期 ${signDate} 至 ${expDate}`
    });
  }

  // 1.3 补充协议（供应商 35 追加年度采购量）
  const sup35 = sups.find(s => s.supplier_id === 35);
  if (sup35) {
    const parent = contracts.find(c => c.supplier_id === 35 && c.type === '0');
    const add = 68000;
    await insContract({
      name: `${sup35.supplier_short_name || sup35.supplier_name}框架协议补充协议（追加年度采购量）`,
      supId: 35, supName: sup35.supplier_name, type: '2',
      orderId: parent ? parent.order_id : null, orderNo: parent ? parent.order_no : null,
      signDate: '2026-09-18', effDate: '2026-09-18', expDate: '2026-12-31',
      amount: add, paid: 0,
      terms: '价格按原框架协议执行，追加部分账期同主合同',
      deliv: delivOf(2),
      status: '1', auditBy: '采购经理', auditTime: '2026-09-18 17:10:00',
      auditOpinion: '追加量在原框架 20% 幅度内，价格未上浮，同意',
      remark: `原合同 ${parent ? parent.contract_no : 'CGHT2026001'} 追加采购金额 ${add.toFixed(2)} 元`
    });
  }

  // 1.4 状态覆盖：草稿 / 变更审批中 / 已终止 / 待审核
  const patch = async (idx, fields) => {
    const c = contracts[idx];
    if (!c) return;
    const sets = Object.keys(fields).map(k => `${k}=?`).join(',');
    await exec(`UPDATE pms_contract SET ${sets} WHERE contract_id=?`, [...Object.values(fields), c.contract_id]);
  };
  let pick = 28;
  const pickIdx = (...skip) => { while (skip.includes(pick)) pick++; return pick++; };
  const iDraft = pickIdx();
  const iPending = pickIdx(iDraft);
  const iTerminated = pickIdx(iDraft, iPending);
  const iWaitAudit = pickIdx(iDraft, iPending, iTerminated);

  await patch(iDraft, { status: '0', audit_by: null, audit_time: null, audit_opinion: null, remark: '合同文本已拟，待用印' });
  await patch(iPending, { status: '2', remark: '合同金额变更审批中' });
  await patch(iTerminated, {
    status: '4', terminate_date: '2026-09-30', terminate_reason: '供应商食品生产许可证到期未完成续证，双方协商终止合作',
    remark: '资质到期终止，剩余订单已转其他合格供应商'
  });
  await patch(iWaitAudit, { status: '5', audit_by: null, audit_time: null, audit_opinion: null, remark: '已提交采购经理审核' });

  console.log(`采购合同 ${contracts.length} 张（框架 ${topSups.length} / 单笔 ${bigSups.length} / 补充 1）`);

  // ================= 2. 合同变更 =================
  const changePlans = [
    { idx: iPending, type: '0', field: 'contract_amount', orig: null, delta: 0.08, reason: '主要原料价格上涨，双方协商上调合同金额并锁定后两月供货价', status: '0' },
    { idx: 1, type: '0', field: 'contract_amount', orig: null, delta: 0.05, reason: '追加包装材料采购量，合同金额相应上调 5%', status: '1' },
    { idx: 2, type: '1', field: 'expire_date', orig: null, newVal: '2026-12-31', reason: '客户订单延后，交付期顺延至年底，合同有效期同步延长', status: '1' },
    { idx: 3, type: '1', field: 'delivery_terms', orig: '供方送货至需方仓库', newVal: '供方每 3 日送货一次，需方提前 2 天下单', reason: '生产节奏调整为小批量多频次，送货频次加密', status: '1' },
    { idx: 4, type: '2', field: 'payment_terms', orig: '月结30天', newVal: '月结45天', reason: '对方财务结算周期调整，账期由 30 天改为 45 天', status: '1' },
    { idx: 6, type: '9', field: 'remark', orig: '常规采购', newVal: '增加冷链运输温控要求（0~4℃）', reason: '产品升级，原料运输需全程冷链并留存温度记录', status: '2' }
  ];
  let chCnt = 0;
  for (const [i, c] of changePlans.entries()) {
    const ct = contracts[c.idx];
    if (!ct) continue;
    const no = `${ct.contract_no}-BG${pad(i + 1, 2)}`;
    const orig = c.orig || `原合同金额 ${ct.amount.toFixed(2)} 元`;
    const nv = c.newVal || `调整后合同金额 ${MONEY(ct.amount * (1 + c.delta)).toFixed(2)} 元`;
    const changeDate = dstr(addDays(i < 2 ? '2026-08-12' : '2026-10-09', i * 6));
    await exec(`INSERT INTO pms_contract_change(contract_id,change_no,change_type,field_name,new_value,change_content,
        original_content,change_reason,change_date,change_by,audit_by,audit_time,audit_status,audit_remark,
        del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','admin',?,?)`,
      [ct.contract_id, no, c.type, c.field, nv,
        `变更项：${c.field}；变更后：${nv}`,
        `变更前：${orig}`, c.reason, changeDate, '李晨',
        c.status === '0' ? null : '采购经理',
        c.status === '0' ? null : fmt(new Date(changeDate + 'T15:40:00')),
        c.status,
        c.status === '0' ? '待采购经理审核' : (c.status === '1' ? '变更理由充分，同意变更' : '冷链温控要求增加成本，需重新议价，暂不通过'),
        fmt(new Date(changeDate + 'T09:10:00')), c.status === '2' ? '驳回后已按原条款执行' : null]);
    if (c.status !== '2') {
      await exec(`UPDATE pms_contract SET change_count=change_count+1 WHERE contract_id=?`, [ct.contract_id]);
    }
    chCnt++;
  }
  console.log(`合同变更 ${chCnt} 条`);

  // ================= 3. 合同审批轨迹 =================
  let logCnt = 0;
  const addLog = async (cid, action, by, time, remark) => {
    await exec(`INSERT INTO pms_contract_audit_log(contract_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
      [cid, action, by, time, remark]);
    logCnt++;
  };
  for (const [i, c] of contracts.entries()) {
    const row = (await q(`SELECT status, sign_date, audit_time FROM pms_contract WHERE contract_id=?`, [c.contract_id]))[0];
    if (!row) continue;
    if (row.status === '0') continue;
    await addLog(c.contract_id, '1', '李晨', fmt(new Date(row.sign_date + 'T08:50:00')), '合同文本及供应商资质已齐备，提交审核');
    if (row.status === '5') continue;
    if (i === 5) {
      await addLog(c.contract_id, '3', '采购经理', fmt(new Date(row.sign_date + 'T10:20:00')), '付款条件与公司政策不符，退回修改');
      continue;
    }
    await addLog(c.contract_id, '2', row.audit_by || '采购经理',
      row.audit_time || fmt(new Date(row.sign_date + 'T14:20:00')),
      row.status === '4' ? '签订审核通过；后因资质到期已办理终止' : '价格、账期、交付条款核验通过，同意签订');
  }
  console.log(`合同审批轨迹 ${logCnt} 条`);

  // ================= 4. 发票付款记录 =================
  const invs = await q(`SELECT * FROM pms_invoice WHERE del_flag='0' ORDER BY invoice_id`);
  let payCnt = 0;
  const contractByOrder = new Map();
  for (const c of contracts) if (c.order_id) contractByOrder.set(c.order_id, c);
  const frameBySup = new Map();
  for (const c of contracts) if (c.type === '0') frameBySup.set(c.supplier_id, c);

  for (const [i, inv] of invs.entries()) {
    // 发票回填合同
    const link = contractByOrder.get(inv.order_id) || frameBySup.get(inv.supplier_id);
    if (link) {
      await exec(`UPDATE pms_invoice SET contract_id=?, contract_no=? WHERE invoice_id=?`, [link.contract_id, link.contract_no, inv.invoice_id]);
    }
    const total = Number(inv.total_amount);
    let payAmt = 0, times = 1;
    if (inv.status === '3') payAmt = total;
    else if (inv.status === '2') { payAmt = Number(inv.payment_amount) || MONEY(total / 2); times = 2; }
    if (payAmt <= 0) continue;

    for (let t = 0; t < times; t++) {
      payCnt++;
      const amt = times === 1 ? MONEY(payAmt) : MONEY(payAmt * (t === 0 ? 0.6 : 0.4));
      const pd = dstr(addDays(inv.invoice_date, 6 + t * 18));
      const no = `FK${pd.replace(/-/g, '')}${pad(payCnt)}`;
      const method = ['0', '0', '1', '0'][i % 4];
      await exec(`INSERT INTO pms_invoice_payment(payment_no,invoice_id,contract_id,order_id,supplier_id,supplier_name,
          payment_amount,payment_date,payment_method,bank_name,bank_account,payer,attachment,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?, '0','admin',?,?)`,
        [no, inv.invoice_id, link ? link.contract_id : null, inv.order_id, inv.supplier_id, inv.supplier_name,
          amt, pd, method,
          inv.bank_name || '中国农业银行宣威支行', inv.bank_account || '24-0000-0000-0001', '财务出纳 张丽',
          `/profile/payment/2026/${no}.pdf`, fmt(new Date(pd + 'T10:30:00')),
          method === '1' ? '银行承兑汇票支付，票据已交付供方' : '电汇支付，付款回单已归档']);
    }
  }
  // 追加：对未收票但已预付的订单生成预付货款记录
  const prepay = await q(`SELECT o.order_id, o.order_no, o.supplier_id, o.supplier_name, o.total_amount, o.order_date
    FROM pms_purchase_order o WHERE o.del_flag='0' AND o.total_amount > 5000
      AND NOT EXISTS(SELECT 1 FROM pms_invoice i WHERE i.order_id=o.order_id)
    ORDER BY o.total_amount DESC LIMIT 4`);
  for (const [i, o] of prepay.entries()) {
    payCnt++;
    const pd = dstr(addDays(o.order_date, 3));
    const no = `FK${pd.replace(/-/g, '')}${pad(payCnt)}`;
    const link = contractByOrder.get(o.order_id) || frameBySup.get(o.supplier_id);
    await exec(`INSERT INTO pms_invoice_payment(payment_no,invoice_id,contract_id,order_id,supplier_id,supplier_name,
        payment_amount,payment_date,payment_method,bank_name,bank_account,payer,attachment,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?, '0','admin',?,?)`,
      [no, 0, link ? link.contract_id : null, o.order_id, o.supplier_id, o.supplier_name,
        MONEY(Number(o.total_amount) * 0.3), pd, '0', '中国农业银行宣威支行', '24-0000-0000-0001', '财务出纳 张丽',
        `/profile/payment/2026/${no}.pdf`, fmt(new Date(pd + 'T15:00:00')),
        `订单 ${o.order_no} 预付 30% 货款，发票未到，挂预付账款`]);
  }
  console.log(`发票付款记录 ${payCnt} 条`);

  // 4.1 合同已付金额按实际付款记录回填
  await exec(`UPDATE pms_contract c SET c.paid_amount = (
      SELECT COALESCE(SUM(p.payment_amount),0) FROM pms_invoice_payment p WHERE p.contract_id = c.contract_id)`);
  await exec(`UPDATE pms_contract c SET c.paid_amount = (
      SELECT COALESCE(SUM(p.payment_amount),0) FROM pms_invoice_payment p
      WHERE p.supplier_id = c.supplier_id AND (p.contract_id IS NULL OR p.contract_id = 0))
    WHERE c.paid_amount = 0 AND c.contract_type='0'
      AND EXISTS(SELECT 1 FROM pms_invoice_payment p WHERE p.supplier_id=c.supplier_id AND (p.contract_id IS NULL OR p.contract_id=0))`);
  await exec(`UPDATE pms_contract SET paid_amount = contract_amount WHERE paid_amount > contract_amount`);

  // ================= 5. 采购计划审批轨迹 =================
  const plans = await q(`SELECT plan_id, plan_no, status, start_date, audit_by, audit_time, planner_name, create_time FROM pms_purchase_plan ORDER BY plan_id`);
  let planLog = 0;
  for (const p of plans) {
    if (p.status === '0') continue;
    const submit = fmt(new Date(new Date(p.create_time || p.start_date).getTime() + 3600000));
    await exec(`INSERT INTO pms_plan_audit_log(plan_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
      [p.plan_id, '1', p.planner_name || '李晨', submit, '采购计划编制完成，提交审批']);
    planLog++;
    const by = p.audit_by || '采购经理';
    const at = p.audit_time || fmt(addDays(p.start_date, -2));
    if (!p.audit_by) await exec(`UPDATE pms_purchase_plan SET audit_by=?, audit_time=? WHERE plan_id=?`, [by, at, p.plan_id]);
    await exec(`INSERT INTO pms_plan_audit_log(plan_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
      [p.plan_id, '2', by, at,
        p.status === '5' ? '计划执行完毕，实际采购额与预算偏差 2.5%，在允许范围内结案'
          : '预算与库存缺口核算一致，同意按计划执行']);
    planLog++;
  }
  console.log(`采购计划审批轨迹 ${planLog} 条`);

  // ================= 6. 供应商评价 =================
  const evalSups = sups.slice(0, 20);
  const grades = (sid, offset) => {
    const d = 78 + ((sid * 7 + offset) % 18);
    const q2 = 80 + ((sid * 5 + offset) % 16);
    const p = 75 + ((sid * 11 + offset) % 20);
    const s = 80 + ((sid * 3 + offset) % 17);
    const i1 = MONEY(d * 0.30), i2 = MONEY(q2 * 0.30), i3 = MONEY(p * 0.20), i4 = MONEY(s * 0.20);
    const total = MONEY(i1 + i2 + i3 + i4);
    const level = total >= 90 ? 'A' : total >= 80 ? 'B' : total >= 70 ? 'C' : 'D';
    return { d, q2, p, s, i1, i2, i3, i4, total, level };
  };
  const periods = [
    { code: '2026Q3', date: '2026-10-08', offset: 0 },
    { code: '2026Q4', date: '2026-11-10', offset: 3 }
  ];
  let evalCnt = 0, evalLog = 0, rejectIdx = 7;
  for (const [pi, per] of periods.entries()) {
    for (const [si, s] of evalSups.entries()) {
      const g = grades(s.supplier_id, per.offset);
      const status = per.code === '2026Q3' ? '1' : (si % 3 === 0 ? '1' : '0');
      const ins = await exec(`INSERT INTO pms_supplier_eval(supplier_id,supplier_name,eval_period,eval_date,
          delivery_score,delivery_weight,delivery_item_score,quality_score,quality_weight,quality_item_score,
          price_score,price_weight,price_item_score,service_score,service_weight,service_item_score,
          total_score,eval_level,status,audit_by,audit_time,audit_remark,evaluator_id,evaluator_name,
          del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,30,?,?,30,?,?,20,?,?,20,?,?,?,?,?,?,?,11,'李晨', '0','admin',?,?)`,
        [s.supplier_id, s.supplier_name, per.code, per.date,
          g.d, g.i1, g.q2, g.i2, g.p, g.i3, g.s, g.i4, g.total, g.level,
          status === '1' ? '1' : '0',
          status === '1' ? '采购经理' : null,
          status === '1' ? fmt(new Date(per.date + 'T16:30:00')) : null,
          status === '1' ? (g.level === 'D' ? '价格偏高且交付波动大，要求 30 日内提交整改计划' : '四维评分与台账数据一致，确认评级') : null,
          fmt(new Date(per.date + 'T09:00:00')),
          `交付及时率 ${(70 + (s.supplier_id * 3) % 28).toFixed(1)}%，来料批次合格率 ${(93 + (s.supplier_id % 7)).toFixed(1)}%`]);
      evalCnt++;

      // 审批轨迹
      await exec(`INSERT INTO pms_supplier_eval_audit_log(eval_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
        [ins.insertId, '1', '李晨', fmt(new Date(per.date + 'T09:20:00')), '季度评价表已填写，提交审核']);
      evalLog++;
      if (status === '1') {
        if (pi === 0 && si === rejectIdx) {
          await exec(`INSERT INTO pms_supplier_eval_audit_log(eval_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
            [ins.insertId, '3', '采购经理', fmt(new Date(per.date + 'T16:40:00')), '质量扣分依据不足，退回补充来料检验数据']);
          evalLog++;
        } else {
          await exec(`INSERT INTO pms_supplier_eval_audit_log(eval_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
            [ins.insertId, '2', '采购经理', fmt(new Date(per.date + 'T16:30:00')),
              g.level === 'A' ? '连续两季度 A 级，建议提高采购份额' : '评分与台账一致，同意评级']);
          evalLog++;
        }
      }
    }
  }
  console.log(`供应商评价 ${evalCnt} 份 / 评价审批轨迹 ${evalLog} 条`);

  const [c1] = await q(`SELECT COUNT(*) c FROM pms_contract`);
  const [c2] = await q(`SELECT COUNT(*) c FROM pms_invoice_payment`);
  console.log(`\n合计：合同 ${c1.c} / 变更 ${chCnt} / 合同轨迹 ${logCnt} / 付款 ${c2.c} / 评价 ${evalCnt}`);
  await conn.end();
})().catch(e => { console.error('ERR', e.message, e.sqlMessage || ''); process.exit(1); });
