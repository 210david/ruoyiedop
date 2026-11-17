/**
 * 财务补充：凭证规则 / 收付款记录 / 应收应付核销 / 成本结转 / 存货成本与跌价 / 银行对账单 / 往来银行账户 / 坏账准备
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const per = d => fmt(d).slice(0, 7).replace('-', '');
const n2 = v => Number(Number(v).toFixed(2));

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  for (const t of ['fin_voucher_rule', 'fin_payment_record', 'fin_ar_writeoff', 'fin_ap_writeoff',
    'fin_cost_carry_forward', 'fin_inventory_cost', 'fin_inventory_valuation', 'fin_bank_statement',
    'fin_bank_statement_item', 'fin_partner_bank', 'fin_bad_debt']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ---------- 1. 凭证规则 ----------
  const rules = [
    ['sale_revenue', '销售发货-确认收入', 'mk', 'mk_shipment', 'AUDITED', '6001', 'CREDIT', 'AMOUNT_EXCL_TAX', null, '销售确认收入', 10],
    ['sale_ar', '销售发货-应收挂账', 'mk', 'mk_shipment', 'AUDITED', '1122', 'DEBIT', 'TOTAL_AMOUNT', 'customer', '应收账款-客户', 20],
    ['sale_tax', '销售发货-销项税', 'mk', 'mk_shipment', 'AUDITED', '222102', 'CREDIT', 'TAX_AMOUNT', null, '销项税额', 30],
    ['purchase_inv', '采购发票-原材料', 'pms', 'pms_invoice', 'AUDITED', '1403', 'DEBIT', 'AMOUNT_EXCL_TAX', null, '采购入库', 10],
    ['purchase_tax', '采购发票-进项税', 'pms', 'pms_invoice', 'AUDITED', '222101', 'DEBIT', 'TAX_AMOUNT', null, '进项税额', 20],
    ['purchase_ap', '采购发票-应付挂账', 'pms', 'pms_invoice', 'AUDITED', '2202', 'CREDIT', 'TOTAL_AMOUNT', 'supplier', '应付账款-供应商', 30],
    ['receipt', '收款单-银行收款', 'fin', 'receipt', 'CONFIRMED', '1002', 'DEBIT', 'TOTAL_AMOUNT', null, '收到货款', 10],
    ['receipt_ar', '收款单-冲减应收', 'fin', 'receipt', 'CONFIRMED', '1122', 'CREDIT', 'TOTAL_AMOUNT', 'customer', '应收账款核销', 20],
    ['payment', '付款单-银行付款', 'fin', 'payment', 'CONFIRMED', '2202', 'DEBIT', 'TOTAL_AMOUNT', 'supplier', '支付货款', 10],
    ['payment_bank', '付款单-银行付款', 'fin', 'payment', 'CONFIRMED', '1002', 'CREDIT', 'TOTAL_AMOUNT', null, '银行存款减少', 20]
  ];
  for (const r of rules) {
    await exec(`INSERT INTO fin_voucher_rule(rule_key,rule_name,source_module,source_type,trigger_status,account_code,direction,amount_expr,aux_type,summary_tpl,priority,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?, '0','0','system',NOW(),'系统预置规则')`, r);
  }
  console.log(`凭证规则 ${rules.length} 条`);

  // ---------- 2. 收付款记录 ----------
  const ars = await q(`SELECT * FROM fin_ar_order WHERE received_amount>0 ORDER BY ar_id`);
  const aps = await q(`SELECT * FROM fin_ap_order WHERE paid_amount>0 ORDER BY ap_id`);
  let payCnt = 0;
  for (const [i, a] of ars.entries()) {
    const d = new Date(new Date(a.ar_date).getTime() + 20 * 86400000);
    await exec(`INSERT INTO fin_payment_record(company_id,payment_no,source_module,source_id,source_key,pay_type,partner_id,partner_name,ar_id,amount,payment_date,period,payment_method,bank_name,bank_account_tail,status,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,?, 'mk',?,?, 'RECEIPT',?,?,?,?,?,?, '1','中国农业银行','0001','CONFIRMED',105,7, '0','system',NOW(),'客户回款')`,
      ['SK' + per(d) + pad(i + 1), a.ar_id, a.ar_no, a.partner_id, a.partner_name, a.ar_id, a.received_amount, dstr(d), per(d)]);
    payCnt++;
  }
  for (const [i, a] of aps.entries()) {
    const d = new Date(new Date(a.ap_date).getTime() + 15 * 86400000);
    await exec(`INSERT INTO fin_payment_record(company_id,payment_no,source_module,source_id,source_key,pay_type,partner_id,partner_name,ap_id,amount,payment_date,period,payment_method,bank_name,bank_account_tail,status,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,?, 'pms',?,?, 'PAY',?,?,?,?,?,?, '1','中国农业银行','0001','CONFIRMED',103,5, '0','system',NOW(),'供应商付款')`,
      ['FK' + per(d) + pad(i + 1), a.ap_id, a.ap_no, a.partner_id, a.partner_name, a.ap_id, a.paid_amount, dstr(d), per(d)]);
    payCnt++;
  }
  console.log(`收付款记录 ${payCnt} 条`);

  // ---------- 3. 应收/应付核销 ----------
  let wCnt = 0;
  for (const a of ars) {
    const d = new Date(new Date(a.ar_date).getTime() + 20 * 86400000);
    const rec = await q(`SELECT payment_id FROM fin_payment_record WHERE ar_id=?`, [a.ar_id]);
    await exec(`INSERT INTO fin_ar_writeoff(ar_id,receipt_id,writeoff_amount,writeoff_date,period,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,105,7, '0','system',NOW(),'按回款核销')`,
      [a.ar_id, rec[0] ? rec[0].payment_id : null, a.received_amount, dstr(d), per(d)]);
    wCnt++;
  }
  for (const a of aps) {
    const d = new Date(new Date(a.ap_date).getTime() + 15 * 86400000);
    const pm = await q(`SELECT payment_id FROM fin_payment_record WHERE ap_id=?`, [a.ap_id]);
    await exec(`INSERT INTO fin_ap_writeoff(ap_id,payment_id,writeoff_amount,writeoff_date,period,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,103,5, '0','system',NOW(),'按付款核销')`,
      [a.ap_id, pm[0] ? pm[0].payment_id : null, a.paid_amount, dstr(d), per(d)]);
    wCnt++;
  }
  console.log(`核销记录 ${wCnt} 条`);

  // ---------- 4. 成本结转 ----------
  const costs = await q(`SELECT i.material_id, m.material_code, m.material_name, i.qty, i.warehouse_id
    FROM wms_inventory i LEFT JOIN wms_material m ON m.material_id=i.material_id
    WHERE i.del_flag='0' AND i.qty>0 AND m.material_type='2' ORDER BY i.qty DESC LIMIT 12`);
  let cfCnt = 0;
  for (const [i, c] of costs.entries()) {
    const month = i % 2 === 0 ? 10 : 9;
    const d = new Date(2026, month - 1, 28);
    const qty = n2(Number(c.qty) * 0.35);
    const unit = n2(18 + (i * 3.7));
    await exec(`INSERT INTO fin_cost_carry_forward(cf_no,company_id,cost_date,period,dept_id,source_module,source_type,source_id,material_id,material_code,qty,unit_cost,total_cost,target_type,target_key,del_flag,create_by,create_time,remark)
      VALUES(?,1,?,?,105,'mk','mk_shipment',?,?,?,?,?,?, 'COST_OF_SALES',?, '0','system',NOW(),'销售出库成本结转')`,
      ['CB' + per(d) + pad(i + 1), dstr(d), per(d), 1000 + i, c.material_id, c.material_code, qty, unit, n2(qty * unit), 'COST-' + per(d)]);
    cfCnt++;
  }
  console.log(`成本结转 ${cfCnt} 条`);

  // ---------- 5. 存货成本 & 跌价准备 ----------
  const inv = await q(`SELECT i.material_id, m.material_code, m.material_name, i.warehouse_id, i.location_id, i.batch_no, i.qty
    FROM wms_inventory i LEFT JOIN wms_material m ON m.material_id=i.material_id WHERE i.del_flag='0' ORDER BY i.qty DESC LIMIT 120`);
  const priceRows = await q(`SELECT material_id, AVG(unit_price) p FROM pms_purchase_order_detail GROUP BY material_id`);
  const priceMap = new Map(priceRows.map(p => [p.material_id, Number(p.p)]));
  let icCnt = 0;
  const valAgg = new Map();
  for (const r of inv) {
    const unit = priceMap.get(r.material_id) ? n2(priceMap.get(r.material_id)) : n2(8 + Math.random() * 40);
    const total = n2(Number(r.qty) * unit);
    await exec(`INSERT INTO fin_inventory_cost(company_id,material_id,material_code,material_name,warehouse_id,location_id,batch_no,qty,unit_cost,total_cost,cost_method,source_module,source_type,last_in_date,version,del_flag,create_by,create_time,remark)
      VALUES(1,?,?,?,?,?,?,?,?,?, 'WEIGHTED_AVG','wms','inbound','2026-10-31',0, '0','system',NOW(),'移动加权平均')`,
      [r.material_id, r.material_code, r.material_name, r.warehouse_id, r.location_id, r.batch_no || '', r.qty, unit, total]);
    icCnt++;
    const k = r.material_id + '|' + r.warehouse_id;
    if (!valAgg.has(k)) valAgg.set(k, { material_id: r.material_id, code: r.material_code, wh: r.warehouse_id, qty: 0, value: 0, unit });
    const v = valAgg.get(k);
    v.qty += Number(r.qty); v.value += total;
  }
  let vCnt = 0;
  for (const v of valAgg.values()) {
    const provision = n2(v.value * (Math.random() < 0.15 ? 0.05 : 0));
    await exec(`INSERT INTO fin_inventory_valuation(company_id,period,material_id,material_code,warehouse_id,qty,unit_cost,total_value,provision_amount,del_flag,create_by,create_time,remark)
      VALUES(1,'202610',?,?,?,?,?,?,?, '0','system',NOW(),?)`,
      [v.material_id, v.code, v.wh, n2(v.qty), n2(v.value / Math.max(1, v.qty)), n2(v.value), provision,
        provision > 0 ? '临期计提跌价准备' : '正常']);
    vCnt++;
  }
  console.log(`存货成本 ${icCnt} 行 / 存货估值 ${vCnt} 行`);

  // ---------- 6. 银行对账单 ----------
  const flows = await q(`SELECT * FROM fin_cash_flow ORDER BY flow_id`);
  let stCnt = 0, itemCnt = 0;
  const months = [...new Set(flows.map(f => f.period))].sort();
  let bal = 2270000;
  for (const [mi, m] of months.entries()) {
    const list = flows.filter(f => f.period === m);
    const begin = bal;
    let end = bal;
    list.forEach(f => { end += f.direction === 'IN' ? Number(f.amount) : -Number(f.amount); });
    const ins = await exec(`INSERT INTO fin_bank_statement(company_id,account_id,account_name,statement_date,period,begin_balance,bank_begin_balance,bank_end_balance,book_end_balance,diff_amount,status,file_name,import_by,import_time,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,1,'农行宣威支行基本户',?,?,?,?,?,?,0, 'MATCHED',?, '财务', NOW(),100,7, '0','system',NOW(),'月度银行对账单导入')`,
      ['2026-' + m.slice(4, 6) + '-30', m, n2(begin), n2(begin), n2(end), n2(end), 'ICBC_' + m + '.xlsx']);
    stCnt++;
    let b = begin;
    for (const [i, f] of list.entries()) {
      b += f.direction === 'IN' ? Number(f.amount) : -Number(f.amount);
      await exec(`INSERT INTO fin_bank_statement_item(statement_id,trans_date,trans_type,trans_amount,balance_after,counterparty,summary,voucher_no,flow_id,match_status,del_flag,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0',NOW())`,
        [ins.insertId, f.flow_date, f.direction === 'IN' ? 'CREDIT_IN' : 'DEBIT_OUT', f.amount, n2(b),
          f.direction === 'IN' ? '客户回款' : '供应商付款', f.remark, '', f.flow_id, '1']);
      itemCnt++;
    }
    bal = end;
  }
  console.log(`银行对账单 ${stCnt} 份 / 明细 ${itemCnt} 条`);

  // ---------- 7. 往来银行账户 ----------
  const partners = await q(`SELECT partner_id, partner_name, partner_type FROM fin_partner ORDER BY partner_id LIMIT 16`);
  for (const [i, p] of partners.entries()) {
    await exec(`INSERT INTO fin_partner_bank(partner_id,dept_id,company_id,account_name,bank_name,bank_branch,account_tail,is_default,status,del_flag,create_by,create_time,remark)
      VALUES(?,105,1,?,?,?,?,?, '0','0','system',NOW(),'')`,
      [p.partner_id, p.partner_name, p.partner_type === '1' ? '中国农业银行' : '中国工商银行',
        p.partner_type === '1' ? '云南省分行' : '昆明市分行', pad(1000 + i, 4), i === 0 ? '1' : '0']);
  }
  console.log(`往来银行账户 ${partners.length} 个`);

  // ---------- 8. 坏账准备 ----------
  const openAr = await q(`SELECT ar_id, partner_id, partner_name, total_amount, ar_date FROM fin_ar_order WHERE status<>'SETTLED' ORDER BY ar_date LIMIT 6`);
  for (const [i, a] of openAr.entries()) {
    const ratio = [0.05, 0.1, 0.3][i % 3];
    await exec(`INSERT INTO fin_bad_debt(company_id,provision_no,period,ar_id,partner_id,partner_name,receivable_amount,provision_ratio,provision_amount,provision_type,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,?,?,?,?,?,?,?,?, 'PERIOD',105,7, '0','system',NOW(),'按账龄计提坏账准备')`,
      ['HZ' + pad(i + 1), '202610', a.ar_id, a.partner_id, a.partner_name, a.total_amount, ratio, n2(Number(a.total_amount) * ratio)]);
  }
  console.log(`坏账准备 ${openAr.length} 条`);

  console.log(await q(`SELECT (SELECT COUNT(*) FROM fin_voucher_rule) 规则,(SELECT COUNT(*) FROM fin_payment_record) 收付款,
    (SELECT COUNT(*) FROM fin_ar_writeoff) 应收核销,(SELECT COUNT(*) FROM fin_ap_writeoff) 应付核销,
    (SELECT COUNT(*) FROM fin_cost_carry_forward) 成本结转,(SELECT COUNT(*) FROM fin_inventory_cost) 存货成本,
    (SELECT COUNT(*) FROM fin_inventory_valuation) 存货估值,(SELECT COUNT(*) FROM fin_bank_statement) 对账单,
    (SELECT COUNT(*) FROM fin_bank_statement_item) 对账明细,(SELECT COUNT(*) FROM fin_bad_debt) 坏账`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
