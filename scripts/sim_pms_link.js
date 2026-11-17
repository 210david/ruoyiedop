/**
 * 采购订单关联补齐：合同（contract_id/contract_no）+ 采购申请单（request_id/request_no）
 * 依据：113 张已完工采购订单、308 行订单明细、49 份合同、3 张历史申请单
 * 策略：
 *   1) 对无生效合同的 14 家零星供应商补建「单笔采购合同」，使关联合同 100% 命中
 *   2) 订单优先绑定名称中含本单 PO 号的专项合同，否则绑定该供应商年度框架协议
 *   3) 按「月份 + 物料大类（原材料/辅料）」归集生成采购申请单，并回填订单与订单明细
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const p2 = n => String(n).padStart(2, '0');
const fmt = d => {
  const x = new Date(d);
  return `${x.getFullYear()}-${p2(x.getMonth() + 1)}-${p2(x.getDate())} ${p2(x.getHours())}:${p2(x.getMinutes())}:${p2(x.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const addDays = (d, n) => new Date(new Date(d + 'T00:00:00').getTime() + n * 86400000);
const MONEY = v => Math.round(Number(v) * 100) / 100;

const PARTY_A = '云南赢隆食品加工有限公司';
const TYPE_NAME = { '0': '原材料', '3': '辅料', '6': '包材', '2': '成品' };
const TYPE_TAG = { '0': 'YL', '3': 'FL', '6': 'BC', '2': 'CP' };

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true, multipleStatements: false,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  // ============ 0. 幂等清理（只清本脚本生成物） ============
  await exec(`DELETE FROM pms_contract_audit_log WHERE contract_id IN
    (SELECT contract_id FROM pms_contract WHERE contract_no >= 'CGHT2026036')`);
  const delC = await exec(`DELETE FROM pms_contract WHERE contract_no >= 'CGHT2026036'`);

  const oldReq = await q(`SELECT request_id FROM pms_purchase_request WHERE remark = '由月度采购订单归集生成'`);
  if (oldReq.length) {
    const ids = oldReq.map(r => r.request_id);
    await exec(`DELETE FROM pms_request_audit_log WHERE request_id IN (?)`, [ids]);
    await exec(`DELETE FROM pms_purchase_request_detail WHERE request_id IN (?)`, [ids]);
    await exec(`UPDATE pms_purchase_order SET request_id=NULL, request_no=NULL WHERE request_id IN (?)`, [ids]);
    await exec(`DELETE FROM pms_purchase_request WHERE request_id IN (?)`, [ids]);
  }
  console.log(`清理：合同 ${delC.affectedRows} 份，旧申请单 ${oldReq.length} 张`);

  // ============ 1. 为无生效合同的供应商补建零星采购合同 ============
  const bare = await q(`SELECT o.supplier_id, o.supplier_name,
      COUNT(*) cnt, SUM(o.total_amount) amt, SUM(o.paid_amount) paid,
      MIN(o.order_date) first_date, MAX(o.order_date) last_date,
      SUBSTRING_INDEX(GROUP_CONCAT(o.buyer_name ORDER BY o.order_date DESC), ',', 1) buyer
    FROM pms_purchase_order o
    WHERE o.del_flag='0' AND NOT EXISTS (
        SELECT 1 FROM pms_contract c
        WHERE c.supplier_id=o.supplier_id AND c.status='1' AND c.contract_type<>'2' AND c.del_flag='0')
    GROUP BY o.supplier_id, o.supplier_name ORDER BY amt DESC`);

  let cseq = 36;
  let cCnt = 0;
  for (const s of bare) {
    const no = 'CGHT2026' + pad(cseq++, 3);
    const sign = dstr(addDays(s.first_date, -2));
    const buyer = s.buyer || '张敏';
    const isOnline = /网购|淘宝/.test(s.supplier_name);
    const isSample = MONEY(s.amt) === 0;
    const cname = isSample
      ? `2026年度${s.supplier_name}样品采购合同`
      : `2026年度${s.supplier_name}${isOnline ? '渠道' : ''}零星采购合同`;
    const cRemark = isSample
      ? `样品采购，覆盖 ${s.cnt} 笔打样/赠品订单，不计价（含税金额 0 元）`
      : `零星采购，覆盖 2026 年度 ${s.cnt} 笔订单，含税金额 ${MONEY(s.amt)} 元`;
    const ins = await exec(`INSERT INTO pms_contract
      (contract_no,contract_name,order_id,order_no,supplier_id,supplier_name,status,contract_type,
       sign_date,effective_date,expire_date,contract_amount,paid_amount,payment_terms,delivery_terms,
       sign_by,sign_department,party_a,party_b,file_url,file_name,change_count,del_flag,
       create_by,create_time,remark,audit_by,audit_time,audit_opinion,dept_id)
      VALUES(?,?,NULL,NULL,?,?,'1','1',?,?,?,?,?,?,?,?,'采购部',?,?,?,?,0,'0','admin',?,?,'采购经理',?,'零星采购金额小、已三方比价，同意签订',104)`,
      [no, cname, s.supplier_id, s.supplier_name,
        sign, sign, '2026-12-31', MONEY(s.amt), MONEY(s.paid),
        isSample ? '样品免费提供，需方仅承担快递运费' : '货到验收合格后一次性现款结算，不留质保金',
        '供方送货上门至需方仓库或指定地点，运费由供方承担',
        buyer, PARTY_A, s.supplier_name,
        `/profile/contract/2026/${no}.pdf`, `${no}_${s.supplier_name}采购合同.pdf`,
        fmt(new Date(sign + 'T09:10:00')),
        cRemark,
        fmt(new Date(dstr(addDays(sign, 1)) + 'T14:30:00'))]);
    const cid = ins.insertId;
    await exec(`INSERT INTO pms_contract_audit_log(contract_id,audit_action,audit_by,audit_time,audit_remark)
      VALUES(?,'1',?,?,'零星采购合同已拟，附三方比价单，提交审核'),(?,'2','采购经理',?,'金额小、条款清晰，同意签订')`,
      [cid, buyer, fmt(new Date(sign + 'T09:10:00')),
        cid, fmt(new Date(dstr(addDays(sign, 1)) + 'T14:30:00'))]);
    cCnt++;
  }
  console.log(`补建零星采购合同 ${cCnt} 份（CGHT2026036 ~ CGHT2026${pad(cseq - 1, 3)}）`);

  // ============ 2. 订单 → 合同 绑定 ============
  const contracts = await q(`SELECT contract_id, contract_no, contract_name, supplier_id, contract_type, sign_date
    FROM pms_contract
    WHERE status='1' AND contract_type<>'2' AND del_flag='0'
    ORDER BY sign_date DESC, contract_id DESC`);

  const byOrderNo = new Map();
  for (const c of contracts) {
    const m = /PO\d{10}/.exec(c.contract_name || '');
    if (m) byOrderNo.set(m[0], c);
  }
  const bySup = new Map();
  for (const c of contracts) {
    if (!bySup.has(c.supplier_id)) bySup.set(c.supplier_id, []);
    bySup.get(c.supplier_id).push(c);
  }

  const orders = await q(`SELECT o.order_id, o.order_no, o.supplier_id, o.supplier_name, o.order_date,
      o.total_amount, o.buyer_name, o.dept_id, o.status
    FROM pms_purchase_order o WHERE o.del_flag='0' ORDER BY o.order_date, o.order_id`);

  let hitExact = 0, hitFrame = 0, cMiss = 0;
  for (const o of orders) {
    let c = byOrderNo.get(o.order_no);
    if (c) hitExact++;
    if (!c) {
      const arr = bySup.get(o.supplier_id) || [];
      c = arr.find(x => x.contract_type === '0') || arr[0] || null;
      if (c) hitFrame++;
    }
    if (!c) { cMiss++; continue; }
    await exec(`UPDATE pms_purchase_order SET contract_id=?, contract_no=? WHERE order_id=?`,
      [c.contract_id, c.contract_no, o.order_id]);
  }
  console.log(`合同绑定：专项合同 ${hitExact} 单，框架协议 ${hitFrame} 单，未命中 ${cMiss} 单`);

  // ============ 3. 采购申请单（月份 + 物料大类归集） ============
  const details = await q(`SELECT d.detail_id, d.order_id, d.material_id, d.material_code, d.material_name,
      d.spec_model, d.unit, d.qty, d.unit_price, d.amount, m.material_type
    FROM pms_purchase_order_detail d
    LEFT JOIN wms_material m ON m.material_id = d.material_id`);

  // 每单主类别 = 明细金额最大的物料类型
  const mainType = new Map();
  const tmpAmt = new Map();
  for (const d of details) {
    const t = d.material_type || '0';
    tmpAmt.set(d.order_id + '#' + t, (tmpAmt.get(d.order_id + '#' + t) || 0) + Number(d.amount || 0));
  }
  for (const [k, v] of tmpAmt) {
    const [oid, t] = k.split('#');
    const cur = mainType.get(oid);
    if (!cur || v > cur.amt) mainType.set(oid, { type: t, amt: v });
  }

  // 分组
  const groups = new Map();
  for (const o of orders) {
    const t = (mainType.get(String(o.order_id)) || { type: '0' }).type;
    const ym = o.order_date.slice(0, 7);
    const key = `${ym}#${t}`;
    if (!groups.has(key)) groups.set(key, { ym, type: t, orders: [], amount: 0 });
    const g = groups.get(key);
    g.orders.push(o);
    g.amount += Number(o.total_amount || 0);
  }

  const used = new Set((await q(`SELECT request_no FROM pms_purchase_request`)).map(r => r.request_no));

  let rCnt = 0, dCnt = 0;
  const groupList = [...groups.values()].sort((a, b) => a.ym.localeCompare(b.ym) || a.type.localeCompare(b.type));

  for (const g of groupList) {
    const first = g.orders[0].order_date;
    const last = g.orders[g.orders.length - 1].order_date;
    const reqDate = dstr(addDays(first, -2));
    const ymd = reqDate.replace(/-/g, '');
    let idx = 1;
    while (used.has(`CGSQ${ymd}${pad(idx, 3)}`)) idx++;
    const reqNo = `CGSQ${ymd}${pad(idx, 3)}`;
    used.add(reqNo);

    // 主要采购员
    const buyerStat = new Map();
    for (const o of g.orders) buyerStat.set(o.buyer_name, (buyerStat.get(o.buyer_name) || 0) + 1);
    const buyer = [...buyerStat.entries()].sort((a, b) => b[1] - a[1])[0][0] || '张敏';

    const tname = TYPE_NAME[g.type] || '物资';
    const title = `${g.ym.slice(0, 4)}年${Number(g.ym.slice(5, 7))}月${tname}采购申请`;
    const purpose = g.type === '0'
      ? '生产计划投料需求，结合安全库存与在途量汇总申报'
      : '生产辅料及包装耗用需求，按月度消耗定额与库存缺口汇总申报';

    const createTime = fmt(new Date(reqDate + 'T09:20:00'));
    const auditTime = fmt(new Date(dstr(addDays(reqDate, 1)) + 'T11:00:00'));
    const expectDate = dstr(addDays(first, 7));

    const ins = await exec(`INSERT INTO pms_purchase_request
      (request_no,title,status,priority,expect_date,purpose,total_amount,requester_id,requester_name,
       dept_id,dept_name,audit_by,audit_time,audit_opinion,plan_id,plan_no,del_flag,create_by,create_time,
       update_by,update_time,remark)
      VALUES(?,?,'2',?,?,?,?,NULL,?,103,'采购供应部','采购经理',?,'需求与库存缺口核对无误，同意采购',NULL,NULL,'0','admin',?,'',NULL,'由月度采购订单归集生成')`,
      [reqNo, title, g.type === '0' ? '1' : '0', expectDate, purpose, MONEY(g.amount), buyer, auditTime, createTime]);
    const rid = ins.insertId;

    await exec(`INSERT INTO pms_request_audit_log(request_id,audit_action,audit_by,audit_time,audit_remark)
      VALUES(?,'1',?,?,'月度需求汇总完毕，提交审批'),(?,'2','采购经理',?,'需求合理，同意采购')`,
      [rid, buyer, createTime, rid, auditTime]);

    // 明细：按物料汇总
    const oids = g.orders.map(o => o.order_id);
    const dts = details.filter(d => oids.includes(d.order_id));
    const matMap = new Map();
    for (const d of dts) {
      const k = String(d.material_id);
      if (!matMap.has(k)) matMap.set(k, { ...d, qty: 0, amount: 0, detailIds: [] });
      const m = matMap.get(k);
      m.qty += Number(d.qty || 0);
      m.amount += Number(d.amount || 0);
      m.detailIds.push(d.detail_id);
    }

    for (const m of matMap.values()) {
      const qty = MONEY(m.qty);
      const amt = MONEY(m.amount);
      const price = qty > 0 ? MONEY(amt / qty) : MONEY(m.unit_price || 0);
      const di = await exec(`INSERT INTO pms_purchase_request_detail
        (request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,
         del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,'0','admin',?,NULL)`,
        [rid, m.material_id, m.material_code, m.material_name, m.spec_model, m.unit, qty, price, amt, createTime]);
      dCnt++;
      // 回填订单明细
      await exec(`UPDATE pms_purchase_order_detail SET request_detail_id=? WHERE detail_id IN (?)`,
        [di.insertId, m.detailIds]);
    }

    await exec(`UPDATE pms_purchase_order SET request_id=?, request_no=? WHERE order_id IN (?)`,
      [rid, reqNo, oids]);
    rCnt++;

    console.log(`  ${reqNo}  ${title}  ${g.orders.length} 单 / ${matMap.size} 项 / ¥${MONEY(g.amount)}  [${first} ~ ${last}]`);
  }

  console.log(`\n采购申请单 ${rCnt} 张，申请明细 ${dCnt} 行，回填订单 ${orders.length} 单`);

  // ============ 4. 校验 ============
  const chk = await q(`SELECT
      SUM(contract_id IS NULL OR contract_no IS NULL) no_contract,
      SUM(request_id IS NULL OR request_no IS NULL) no_request,
      COUNT(*) total FROM pms_purchase_order WHERE del_flag='0'`);
  console.log(`校验：订单 ${chk[0].total} 单，缺合同 ${chk[0].no_contract}，缺申请单 ${chk[0].no_request}`);

  const chk2 = await q(`SELECT COUNT(*) c FROM pms_purchase_order_detail WHERE request_detail_id IS NULL`);
  console.log(`校验：订单明细缺申请明细 ${chk2[0].c} 行`);

  const chk3 = await q(`SELECT SUM(total_amount) ord_amt,
      (SELECT SUM(total_amount) FROM pms_purchase_request WHERE remark='由月度采购订单归集生成') req_amt
    FROM pms_purchase_order WHERE del_flag='0'`);
  console.log(`校验：订单合计 ¥${chk3[0].ord_amt}，申请单合计 ¥${chk3[0].req_amt}`);

  await conn.end();
})().catch(e => { console.error('ERR', e.message); process.exit(1); });
