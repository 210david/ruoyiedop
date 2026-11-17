/**
 * 审批轨迹与过程记录补充：生产/采购/质量/营销各类审核日志、文控版本与分发、电子签名、
 * 内审检查模板、供应商现场审核、SQM 推送日志、管理评审措施跟踪
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const ri = (a, b) => Math.floor(Math.random() * (b - a + 1)) + a;

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const tables = ['mms_route_audit_log', 'mms_work_order_audit_log', 'mms_mps_audit_log',
    'pms_order_audit_log', 'pms_request_audit_log', 'pms_receive_audit_log', 'pms_return_audit_log',
    'pms_invoice_audit_log', 'pms_inquiry_audit_log', 'pms_plan_audit_log',
    'qms_ncr_approve_log', 'qms_doc_version', 'qms_doc_distribute', 'qms_esig_record', 'qms_mr_action',
    'qms_audit_check_template', 'qms_audit_check_template_item', 'qms_supplier_audit', 'qms_sqm_push_log',
    'mk_order_audit_log', 'mk_order_return_approve_log', 'mk_order_return_audit_log'];
  for (const t of tables) await exec(`DELETE FROM \`${t}\``);

  const log = async (table, cols, vals) => exec(`INSERT INTO \`${table}\`(${cols.join(',')}) VALUES(${cols.map(() => '?').join(',')})`, vals);

  // ---------- 1. 工艺路线审核 ----------
  const routes = await q(`SELECT route_id, route_no, status, create_time FROM mms_route`);
  let n1 = 0;
  for (const r of routes) {
    const d = new Date(r.create_time);
    await log('mms_route_audit_log', ['route_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [r.route_id, '1', '生产经理', fmt(new Date(d.getTime() + 3600000)), '提交审核']);
    await log('mms_route_audit_log', ['route_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [r.route_id, '2', '技术总监', fmt(new Date(d.getTime() + 7200000)), '工艺参数与工序顺序符合要求，审核通过']);
    n1 += 2;
  }
  console.log(`工艺审核日志 ${n1} 条`);

  // ---------- 2. 工单 / MPS 审核 ----------
  const wos = await q(`SELECT work_order_id, work_order_no, status, create_time, release_time FROM mms_work_order`);
  let n2 = 0;
  for (const w of wos) {
    const d = new Date(w.create_time);
    await log('mms_work_order_audit_log', ['work_order_id', 'audit_by', 'audit_action', 'audit_remark', 'audit_time'],
      [w.work_order_id, '生产经理', 'release', '工单已下达车间', fmt(new Date(d.getTime() + 1800000))]);
    if (w.status === 'finished') {
      await log('mms_work_order_audit_log', ['work_order_id', 'audit_by', 'audit_action', 'audit_remark', 'audit_time'],
        [w.work_order_id, '生产经理', 'finish', '完工入库，工单关闭', fmt(new Date(d.getTime() + 5 * 86400000))]);
      n2 += 2;
    } else n2++;
  }
  console.log(`工单审核日志 ${n2} 条`);

  const mps = await q(`SELECT mps_id, mps_no, status, create_time FROM mms_mps`);
  let n3 = 0;
  for (const m of mps) {
    if (m.status === '0') continue;
    await log('mms_mps_audit_log', ['mps_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [m.mps_id, 'approve', '生产经理', fmt(new Date(new Date(m.create_time).getTime() + 3600000)), '产能与交期确认，审核通过']);
    n3++;
  }
  console.log(`MPS 审核日志 ${n3} 条`);

  // ---------- 3. 采购审核日志 ----------
  const po = await q(`SELECT order_id, order_no, status, order_date, create_time FROM pms_purchase_order WHERE del_flag='0'`);
  let n4 = 0;
  for (const o of po) {
    if (!['2', '3', '4', '5'].includes(String(o.status))) continue;
    await log('pms_order_audit_log', ['order_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [o.order_id, '2', '采购经理', fmt(new Date(new Date(o.create_time).getTime() + 3600000)), '价格与供应商资质核验通过']);
    n4++;
  }
  console.log(`采购订单审核 ${n4} 条`);

  const reqs = await q(`SELECT request_id, status, create_time FROM pms_purchase_request`);
  for (const r of reqs) {
    if (r.status !== '2') continue;
    await log('pms_request_audit_log', ['request_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [r.request_id, '2', '采购经理', fmt(new Date(new Date(r.create_time).getTime() + 7200000)), '需求合理，同意采购']);
  }
  const recvs = await q(`SELECT receive_id, status, receive_date, create_time FROM pms_receive WHERE del_flag='0' LIMIT 60`);
  for (const r of recvs) {
    await log('pms_receive_audit_log', ['receive_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [r.receive_id, '2', '质检员', fmt(new Date(new Date(r.receive_date).getTime() + 3600000)), '验收合格，准予入库']);
  }
  const rets = await q(`SELECT return_id, status, create_time FROM pms_return`);
  for (const r of rets) {
    await log('pms_return_audit_log', ['return_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [r.return_id, r.status === '3' ? '2' : '1', '采购经理', fmt(new Date(new Date(r.create_time).getTime() + 3600000)),
        r.status === '3' ? '同意退货，已通知供应商' : '提交审批，待供应商确认']);
  }
  const invs = await q(`SELECT invoice_id, status, create_time FROM pms_invoice`);
  for (const i of invs) {
    if (i.status === '0') continue;
    await log('pms_invoice_audit_log', ['invoice_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [i.invoice_id, '2', '财务主管', fmt(new Date(new Date(i.create_time).getTime() + 3600000)), '发票查验通过，金额与订单一致']);
  }
  const inqs = await q(`SELECT inquiry_id, status, create_time FROM pms_inquiry`);
  for (const i of inqs) {
    if (i.status === '0' || i.status === '1') continue;
    await log('pms_inquiry_audit_log', ['inquiry_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [i.inquiry_id, '2', '采购经理', fmt(new Date(new Date(i.create_time).getTime() + 2 * 86400000)), '询价结果审核，按最低价定标']);
  }
  console.log('采购类审核日志 已生成');

  // ---------- 4. NCR 审批轨迹 ----------
  const ncrs = await q(`SELECT * FROM qms_ncr`);
  let n5 = 0;
  const nodes = ['质量部评审', '责任部门确认', '处置方案审批', '效果验证'];
  for (const n of ncrs) {
    const base = new Date(n.discover_time);
    const cnt = n.ncr_status === '5' ? 4 : 2;
    for (let k = 0; k < cnt; k++) {
      await log('qms_ncr_approve_log', ['ncr_id', 'node_seq', 'node_name', 'approver_role', 'action_type', 'approve_opinion', 'approver_id', 'approver_name', 'approve_time', 'create_by', 'create_time'],
        [n.ncr_id, k + 1, nodes[k], k === 0 ? '质量主管' : '部门负责人', '2',
          k === 0 ? '不符合事实清楚，转入处置' : (k === 3 ? '整改有效，同意关闭' : '同意处置方案'),
          10, '陈静', fmt(new Date(base.getTime() + (k + 1) * 86400000)), 'system', fmt(new Date(base.getTime() + (k + 1) * 86400000))]);
      n5++;
    }
  }
  console.log(`NCR 审批日志 ${n5} 条`);

  // ---------- 5. 文控：版本 / 分发 / 电子签名 ----------
  const docs = await q(`SELECT * FROM qms_doc`);
  let n6 = 0, n7 = 0;
  for (const d of docs) {
    const t = new Date(d.publish_date);
    await log('qms_doc_version', ['doc_id', 'doc_no', 'version_no', 'change_log', 'version_status', 'publish_by', 'publish_time', 'esig_user', 'esig_meaning', 'esig_time', 'create_by', 'create_time'],
      [d.doc_id, d.doc_no, d.version_no, '首次发布' === '' ? '' : '按年度文件评审结果修订，更新职责与记录表单', '1', '质量负责人', fmt(t), '陈静', '批准发布', fmt(t), 'system', fmt(t)]);
    n6++;
    for (const dept of [['质量管理部', 104], ['生产部', 104], ['仓储部', 100]]) {
      await log('qms_doc_distribute', ['doc_id', 'doc_no', 'version_no', 'distribute_type', 'dept_id', 'dept_name', 'receive_time', 'dist_status', 'watermark', 'create_by', 'create_time'],
        [d.doc_id, d.doc_no, d.version_no, 'distribute', dept[1], dept[0], fmt(new Date(t.getTime() + 86400000)), '1', '受控-' + d.doc_no, 'system', fmt(t)]);
      n7++;
    }
  }
  // 电子签名（沿用真实业务对象）
  let n8 = 0;
  for (const n of ncrs) {
    await log('qms_esig_record', ['biz_type', 'biz_id', 'biz_no', 'user_id', 'user_name', 'nick_name', 'sig_meaning', 'sig_opinion', 'sig_result', 'sig_ip', 'sig_time', 'create_by', 'create_time'],
      ['ncr_approve', n.ncr_id, n.ncr_no, 10, 'chenjing', '陈静', 'NCR 处置审批', '同意处置方案', 'PASS', '192.168.1.' + (20 + n.ncr_id), fmt(new Date(n.discover_time)), 'system', fmt(new Date(n.discover_time))]);
    n8++;
  }
  const tasks = await q(`SELECT task_id, task_no, inspect_result, judge_time FROM qms_insp_task WHERE inspect_result IS NOT NULL AND inspect_result<>'' LIMIT 20`);
  for (const t of tasks) {
    await log('qms_esig_record', ['biz_type', 'biz_id', 'biz_no', 'user_id', 'user_name', 'nick_name', 'sig_meaning', 'sig_opinion', 'sig_result', 'sig_ip', 'sig_time', 'create_by', 'create_time'],
      ['insp_judge', t.task_id, t.task_no, 10, 'chenjing', '陈静', '检验判定签署', '判定合格，准予放行', 'PASS', '192.168.1.35', fmt(new Date(t.judge_time || new Date())), 'system', fmt(new Date(t.judge_time || new Date()))]);
    n8++;
  }
  console.log(`文档版本 ${n6} 条 / 分发 ${n7} 条 / 电子签名 ${n8} 条`);

  // ---------- 6. 内审检查模板 ----------
  const tpl = await exec(`INSERT INTO qms_audit_check_template(template_name,standard,remark,del_flag,create_by,create_time)
    VALUES('ISO9001 内审检查表（通用）','ISO9001','覆盖 4-10 章条款','0','system',NOW())`);
  const tplItems = [
    ['4.1', '是否识别与体系相关的内外部因素并定期评审'],
    ['5.2', '质量方针是否形成文件并沟通至相关人员'],
    ['6.1', '是否识别风险与机遇并采取措施'],
    ['7.1.5', '监视测量资源是否校准并标识'],
    ['8.4', '外部供方是否按准则评价与监控'],
    ['8.5.1', '生产是否在受控条件下进行（参数、SOP、人员资质）'],
    ['8.7', '不合格输出是否标识、隔离并处置'],
    ['9.2', '是否按计划实施内审并跟踪不符合项'],
    ['10.2', '纠正措施是否验证有效性']
  ];
  let n9 = 0;
  for (const [i, it] of tplItems.entries()) {
    await exec(`INSERT INTO qms_audit_check_template_item(template_id,clause_ref,check_content,sort_no,del_flag)
      VALUES(?,?,?,?, '0')`, [tpl.insertId, it[0], it[1], i + 1]);
    n9++;
  }
  console.log(`内审模板 1 套 / ${n9} 项`);

  // ---------- 7. 供应商现场审核 ----------
  const sups = await q(`SELECT supplier_id, supplier_name FROM wms_supplier WHERE del_flag='0' LIMIT 6`);
  for (const [i, s] of sups.entries()) {
    const d = new Date(2026, 5 + (i % 4), 10 + i, 9, 0, 0);
    await exec(`INSERT INTO qms_supplier_audit(audit_no,supplier_id,supplier_name,audit_type,audit_date,auditor,audit_scope,audit_result,nonconformity,audit_status,del_flag,status,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?, '1','0','0','system',NOW(),'年度现场审核')`,
      ['SA' + dstr(d).replace(/-/g, '') + pad(i + 1), s.supplier_id, s.supplier_name, i % 2 === 0 ? 'onsite' : 'document',
        dstr(d), '陈静', '生产环境、原料控制、检验能力、追溯体系',
        i % 3 === 2 ? '2' : '1', i % 3 === 2 ? '原料库温控记录缺失，要求 30 天内整改' : '未发现严重不符合项']);
  }
  console.log(`供应商现场审核 ${sups.length} 次`);

  // ---------- 8. SQM 推送日志 ----------
  const evals = await q(`SELECT * FROM qms_supplier_eval`);
  for (const e of evals) {
    await exec(`INSERT INTO qms_sqm_push_log(supplier_id,supplier_name,eval_id,eval_period,quality_score,grade,pass_rate,ppm,push_status,push_time,retry_count,error_msg,del_flag,create_by,create_time)
      VALUES(?,?,?,?,?,?,?,?,?,?,0,'', '0','system',NOW())`,
      [e.supplier_id, e.supplier_name, e.eval_id, e.eval_period, e.quality_score, e.grade, e.pass_rate, e.ppm, '1', fmt(new Date())]);
  }
  console.log(`SQM 推送日志 ${evals.length} 条`);

  // ---------- 9. 管理评审措施 ----------
  const mr = await q(`SELECT * FROM qms_mr LIMIT 1`);
  if (mr.length) {
    const actions = [
      ['腌制间增设 2 台冷风机并加装温度自动记录', '设备改造', '设备部', '赵雷', '2026-11-30', '0'],
      ['供应商冷链温度记录全覆盖', '供应商管理', '采购部', '李晨', '2026-12-31', '1'],
      ['修订《真空包装作业指导书》等 3 份文件', '文件修订', '质量管理部', '陈静', '2026-11-15', '2'],
      ['制定 2027 年质量目标与考核方案', '目标管理', '质量管理部', '陈静', '2026-12-20', '0']
    ];
    for (const a of actions) {
      await exec(`INSERT INTO qms_mr_action(mr_id,mr_no,action_desc,action_type,resp_dept_name,responsible_person,plan_close_date,actual_close_date,action_result,action_status,del_flag,create_by,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0','system',NOW())`,
        [mr[0].mr_id, mr[0].mr_no, a[0], a[1], a[2], a[3], a[4], a[5] === '2' ? a[4] : null,
          a[5] === '2' ? '已完成并评审通过' : '', a[5]]);
    }
    console.log('管理评审措施 4 项');
  }

  // ---------- 10. 营销审批日志 ----------
  const orders = await q(`SELECT order_id, order_status, create_time FROM mk_order WHERE del_flag='0' LIMIT 40`);
  for (const o of orders) {
    if (!['2', '3', '4'].includes(String(o.order_status))) continue;
    await log('mk_order_audit_log', ['order_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark', 'create_time'],
      [o.order_id, '1', '销售经理', fmt(new Date(new Date(o.create_time).getTime() + 3600000)), '订单审核通过，安排发货', fmt(new Date(o.create_time))]);
  }
  const rets2 = await q(`SELECT return_id, return_status, create_time FROM mk_order_return`);
  for (const r of rets2) {
    await log('mk_order_return_approve_log', ['return_id', 'action_type', 'approve_opinion', 'approve_by', 'approve_time'],
      [r.return_id, 'approve', '同意退货，按原价退款', '销售经理', fmt(new Date(new Date(r.create_time).getTime() + 86400000))]);
    await log('mk_order_return_audit_log', ['return_id', 'audit_action', 'audit_by', 'audit_time', 'audit_remark'],
      [r.return_id, '1', '财务主管', fmt(new Date(new Date(r.create_time).getTime() + 2 * 86400000)), '退款金额核对无误']);
  }
  console.log('营销审批日志 已生成');

  console.log(await q(`SELECT
    (SELECT COUNT(*) FROM mms_route_audit_log) 工艺日志,(SELECT COUNT(*) FROM mms_work_order_audit_log) 工单日志,
    (SELECT COUNT(*) FROM pms_order_audit_log) 采购日志,(SELECT COUNT(*) FROM qms_ncr_approve_log) NCR日志,
    (SELECT COUNT(*) FROM qms_doc_version) 文档版本,(SELECT COUNT(*) FROM qms_esig_record) 电子签名,
    (SELECT COUNT(*) FROM qms_supplier_audit) 供应商审核,(SELECT COUNT(*) FROM qms_sqm_push_log) SQM推送`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
