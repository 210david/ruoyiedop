/**
 * 财务模块数据模拟：会计科目 / 期间 / 往来单位 / 资金账户 / 应收应付 / 凭证与分录 / 总账 / 资金流水
 * / 进项税 / 物料税率 / 财务参数 / 成本方法 / 期末结账检查
 * 依据：50 张销售订单(应收)、113 张采购订单与 12 张采购发票(应付)
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const dstr = d => fmt(d).slice(0, 10);
const per = d => fmt(d).slice(0, 7).replace('-', '');
const ri = (a, b) => Math.floor(Math.random() * (b - a + 1)) + a;
const n2 = v => Number(v.toFixed(2));

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const tables = ['fin_subject', 'fin_period', 'fin_partner', 'fin_bank_account', 'fin_ar_order', 'fin_ap_order',
    'fin_voucher', 'fin_voucher_entry', 'fin_ledger', 'fin_cash_flow', 'fin_input_tax', 'fin_material_tax',
    'fin_fiscal_config', 'fin_cost_method_config', 'fin_closing'];
  for (const t of tables) await exec(`DELETE FROM \`${t}\``);

  // ---------- 1. 会计科目 ----------
  const subjects = [
    ['1001', '库存现金', 1, 'ASSET', 'DEBIT', '1', null],
    ['1002', '银行存款', 1, 'ASSET', 'DEBIT', '1', '资金'],
    ['1122', '应收账款', 1, 'ASSET', 'DEBIT', '0', '往来'],
    ['1221', '其他应收款', 1, 'ASSET', 'DEBIT', '0', '往来'],
    ['1403', '原材料', 1, 'ASSET', 'DEBIT', '1', null],
    ['1405', '库存商品', 1, 'ASSET', 'DEBIT', '1', null],
    ['1601', '固定资产', 1, 'ASSET', 'DEBIT', '1', null],
    ['1602', '累计折旧', 1, 'ASSET', 'CREDIT', '1', null],
    ['2202', '应付账款', 2, 'LIABILITY', 'CREDIT', '0', '往来'],
    ['2211', '应付职工薪酬', 2, 'LIABILITY', 'CREDIT', '1', null],
    ['2221', '应交税费', 2, 'LIABILITY', 'CREDIT', '0', null],
    ['222101', '应交增值税-进项税额', 3, 'LIABILITY', 'DEBIT', '1', null],
    ['222102', '应交增值税-销项税额', 3, 'LIABILITY', 'CREDIT', '1', null],
    ['4001', '实收资本', 1, 'EQUITY', 'CREDIT', '1', null],
    ['4104', '利润分配', 1, 'EQUITY', 'CREDIT', '0', null],
    ['5001', '生产成本', 1, 'COST', 'DEBIT', '0', null],
    ['5101', '制造费用', 1, 'COST', 'DEBIT', '1', null],
    ['6001', '主营业务收入', 1, 'REVENUE', 'CREDIT', '1', null],
    ['6051', '其他业务收入', 1, 'REVENUE', 'CREDIT', '1', null],
    ['6401', '主营业务成本', 1, 'EXPENSE', 'DEBIT', '1', null],
    ['6601', '销售费用', 1, 'EXPENSE', 'DEBIT', '1', null],
    ['6602', '管理费用', 1, 'EXPENSE', 'DEBIT', '1', null],
    ['6603', '财务费用', 1, 'EXPENSE', 'DEBIT', '1', null]
  ];
  const subMap = new Map();
  for (const [i, s] of subjects.entries()) {
    const parentCode = s[6] ? null : (s[0].length === 4 ? null : s[0].slice(0, 4));
    const ins = await exec(`INSERT INTO fin_subject(subject_code,subject_name,parent_id,parent_code,level,category,direction,is_leaf,is_cash_equiv,cash_flow_category,aux_types,company_id,sort_order,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,1,?, '0','0','system',NOW(),?)`,
      [s[0], s[1], null, parentCode, s[2], s[3], s[4], s[5], s[0] === '1001' || s[0] === '1002' ? '1' : '0',
        s[6], s[6] === '往来' ? 'customer,supplier' : null, (i + 1) * 10, '标准科目']);
    subMap.set(s[0], ins.insertId);
  }
  await exec(`UPDATE fin_subject c JOIN fin_subject p ON p.subject_code=c.parent_code SET c.parent_id=p.subject_id`);
  console.log(`会计科目 ${subjects.length} 个`);

  // ---------- 2. 会计期间 ----------
  for (let m = 1; m <= 12; m++) {
    const ym = '2026' + pad(m, 2);
    const closed = m <= 10;
    await exec(`INSERT INTO fin_period(company_id,\`year_month\`,period_name,status,close_by,close_time,del_flag,create_by,create_time,remark)
      VALUES(1,?,?,?,?,?, '0','system',NOW(),?)`,
      [ym, 2026 + '年' + m + '月', closed ? 'CLOSED' : 'OPEN', closed ? '财务主管' : null,
        closed ? fmt(new Date(2026, m, 5, 18, 0, 0)) : null, closed ? '月度已结账' : '当前会计期间']);
  }
  console.log('会计期间 12 个');

  // ---------- 3. 往来单位 ----------
  const sups = await q(`SELECT supplier_id, supplier_code, supplier_name FROM wms_supplier WHERE del_flag='0'`);
  const custs = await q(`SELECT customer_id, customer_no, customer_name FROM mk_customer`);
  let pCnt = 0;
  for (const s of sups) {
    await exec(`INSERT INTO fin_partner(company_id,partner_code,partner_name,partner_type,source_module,source_id,tax_number,payment_days,settlement_type,credit_limit,credit_used,status,del_flag,create_by,create_time,remark)
      VALUES(1,?,?, '1','pms',?,?,30,'BANK',500000,0,'0','0','system',NOW(),'供应商同步')`,
      ['SUP' + s.supplier_id, s.supplier_name, s.supplier_id, '91530381' + pad(s.supplier_id, 9) + 'X']);
    pCnt++;
  }
  for (const c of custs) {
    await exec(`INSERT INTO fin_partner(company_id,partner_code,partner_name,partner_type,source_module,source_id,tax_number,payment_days,settlement_type,credit_limit,credit_used,status,del_flag,create_by,create_time,remark)
      VALUES(1,?,?, '2','mk',?,?,30,'BANK',300000,0,'0','0','system',NOW(),'客户同步')`,
      ['CUS' + c.customer_id, c.customer_name, c.customer_id, '91310000' + pad(c.customer_id, 9) + 'Y']);
    pCnt++;
  }
  console.log(`往来单位 ${pCnt} 家`);

  // ---------- 4. 资金账户 ----------
  await exec(`INSERT INTO fin_bank_account(company_id,account_code,account_name,account_type,dept_id,bank_name,bank_branch,account_tail,currency,opening_balance,current_balance,status,del_flag,create_by,create_time,remark)
    VALUES(1,'100201','农行宣威支行基本户','BANK',100,'中国农业银行','宣威市支行','0001','CNY',1850000,1850000,'0','0','system',NOW(),'基本存款账户'),
          (1,'100202','农商行一般户','BANK',100,'云南农村商业银行','宣威支行','6688','CNY',420000,420000,'0','0','system',NOW(),'一般结算账户'),
          (1,'100101','现金备用金','CASH',100,'—','—','0000','CNY',30000,30000,'0','0','system',NOW(),'库存现金')`);
  console.log('资金账户 3 个');

  // ---------- 5. 应收（销售订单） ----------
  const orders = await q(`SELECT o.*, c.customer_name FROM mk_order o LEFT JOIN mk_customer c ON c.customer_id=o.customer_id
    WHERE o.del_flag='0' AND o.order_amount>0 ORDER BY o.order_id DESC LIMIT 26`);
  let arCnt = 0;
  const arList = [];
  for (const [i, o] of orders.entries()) {
    const d = new Date(o.create_time);
    const total = n2(Number(o.order_amount));
    const excl = n2(total / 1.09);
    const tax = n2(total - excl);
    const paidAll = i < 12;
    const paidPart = i >= 12 && i < 17;
    const received = paidAll ? total : (paidPart ? n2(total * 0.5) : 0);
    const status = paidAll ? 'SETTLED' : (paidPart ? 'PARTIAL' : 'OPEN');
    const ins = await exec(`INSERT INTO fin_ar_order(company_id,ar_no,source_module,source_type,source_id,source_key,partner_id,partner_name,customer_id,ar_date,period,amount_excl_tax,tax_amount,total_amount,received_amount,bad_debt_amount,tax_rate,invoice_no,invoice_date,due_date,payment_days,status,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,?, 'mk','mk_order',?,?,?,?,?,?,?,?,?,?,?,0,0.0900,?,?,?,30,?,105,7, '0','system',NOW(),'销售订单确认应收')`,
      ['AR' + dstr(d).replace(/-/g, '') + pad(i + 1), o.order_id, o.order_no,
        (await q(`SELECT partner_id FROM fin_partner WHERE source_module='mk' AND source_id=?`, [o.customer_id]))[0]?.partner_id || null,
        o.customer_name, o.customer_id, dstr(d), per(d), excl, tax, total, received,
        'FP' + dstr(d).replace(/-/g, '') + pad(i + 1), dstr(new Date(d.getTime() + 3 * 86400000)),
        dstr(new Date(d.getTime() + 30 * 86400000)), status]);
    arList.push({ id: ins.insertId, no: 'AR' + dstr(d).replace(/-/g, '') + pad(i + 1), d, total, excl, tax, received, o });
    arCnt++;
  }
  console.log(`应收单 ${arCnt} 张`);

  // ---------- 6. 应付（采购发票/订单） ----------
  const invoices = await q(`SELECT i.*, s.supplier_name FROM pms_invoice i LEFT JOIN wms_supplier s ON s.supplier_id=i.supplier_id ORDER BY i.invoice_id DESC LIMIT 20`);
  let apCnt = 0;
  const apList = [];
  for (const [i, inv] of invoices.entries()) {
    const d = new Date(inv.invoice_date || inv.create_time);
    const total = n2(Number(inv.total_amount));
    const excl = n2(Number(inv.invoice_amount) || total / 1.09);
    const tax = n2(total - excl);
    const paidAll = i < 8;
    const paidPart = i >= 8 && i < 12;
    const paid = paidAll ? total : (paidPart ? n2(total * 0.4) : 0);
    const ins = await exec(`INSERT INTO fin_ap_order(company_id,ap_no,source_module,source_type,source_id,source_key,partner_id,partner_name,supplier_id,ap_date,period,amount_excl_tax,tax_amount,total_amount,paid_amount,tax_rate,invoice_no,invoice_date,due_date,payment_days,status,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,?, 'pms','pms_invoice',?,?,?,?,?,?,?,?,?,?,?,0.0900,?,?,?,30,?,103,5, '0','system',NOW(),'采购发票确认应付')`,
      ['AP' + dstr(d).replace(/-/g, '') + pad(i + 1), inv.invoice_id, inv.invoice_no,
        (await q(`SELECT partner_id FROM fin_partner WHERE source_module='pms' AND source_id=?`, [inv.supplier_id]))[0]?.partner_id || null,
        inv.supplier_name, inv.supplier_id, dstr(d), per(d), excl, tax, total, paid,
        inv.invoice_number, dstr(d), dstr(new Date(d.getTime() + 30 * 86400000)),
        paidAll ? 'SETTLED' : (paidPart ? 'PARTIAL' : 'OPEN')]);
    apList.push({ id: ins.insertId, no: 'AP' + dstr(d).replace(/-/g, '') + pad(i + 1), d, total, excl, tax, paid });
    apCnt++;
  }
  console.log(`应付单 ${apCnt} 张`);

  // ---------- 7. 进项税 ----------
  let taxCnt = 0;
  for (const [i, ap] of apList.entries()) {
    await exec(`INSERT INTO fin_input_tax(company_id,ap_id,partner_id,partner_name,invoice_code,invoice_no,invoice_date,tax_rate,amount_excl_tax,tax_amount,deduct_status,deduct_period,dept_id,user_id,del_flag,create_by,create_time,remark)
      VALUES(1,?,?,?,?,?,?,0.0900,?,?,?,?,103,5, '0','system',NOW(),'采购进项税')`,
      [ap.id, null, '', '0440019' + pad(i + 1, 6), 'No.' + (10000000 + i), dstr(ap.d), ap.excl, ap.tax,
        i < 12 ? 'DEDUCTED' : 'PENDING', i < 12 ? per(ap.d) : null]);
    taxCnt++;
  }
  console.log(`进项税 ${taxCnt} 条`);

  // ---------- 8. 凭证 + 分录 ----------
  const SUB = c => subjects.find(s => s[0] === c);
  let vSeq = 0, eCnt = 0;
  const cashFlows = [];
  async function voucher(date, type, sourceModule, sourceType, sourceId, summary, entries, status) {
    vSeq++;
    const no = 'PZ' + per(date) + pad(vSeq);
    const total = entries.reduce((a, e) => a + e.amount, 0);
    const ins = await exec(`INSERT INTO fin_voucher(voucher_no,company_id,voucher_date,period,voucher_type,source_module,source_type,source_id,rule_key,total_amount,entry_count,status,audit_by,audit_time,del_flag,create_by,create_time,remark)
      VALUES(?,1,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?)`,
      [no, dstr(date), per(date), type, sourceModule, sourceType, sourceId || (900000 + vSeq), type === 'AUTO' ? 'auto_' + sourceType : '',
        n2(total), entries.length, status,
        status === 'AUDITED' || status === 'POSTED' ? '财务主管' : null,
        status === 'AUDITED' || status === 'POSTED' ? fmt(new Date(date.getTime() + 3600000)) : null, summary]);
    let line = 0;
    for (const e of entries) {
      line++;
      const s = SUB(e.code);
      await exec(`INSERT INTO fin_voucher_entry(voucher_id,company_id,line_no,period,dept_id,account_code,account_name,direction,amount,currency,tax_amount,aux_customer_id,aux_supplier_id,summary,del_flag,create_by,create_time,remark)
        VALUES(?,1,?,?,105,?,?,?,?, 'CNY',?,?,?,?, '0','system',NOW(),'')`,
        [ins.insertId, line, per(date), e.code, s ? s[1] : e.code, e.dir, n2(e.amount), e.tax || 0,
          e.auxCustomer || null, e.auxSupplier || null, summary]);
      eCnt++;
    }
    return ins.insertId;
  }
  // 销售确认收入
  for (const [i, ar] of arList.slice(0, 12).entries()) {
    await voucher(ar.d, 'AUTO', 'mk', 'mk_order', ar.o.order_id, '销售确认收入-' + ar.o.order_no, [
      { code: '1122', dir: 'DEBIT', amount: ar.total },
      { code: '6001', dir: 'CREDIT', amount: ar.excl },
      { code: '222102', dir: 'CREDIT', amount: ar.tax }
    ], i < 8 ? 'POSTED' : 'AUDITED');
  }
  // 采购入库
  for (const [i, ap] of apList.slice(0, 10).entries()) {
    await voucher(ap.d, 'AUTO', 'pms', 'pms_invoice', ap.id, '采购入库-' + ap.no, [
      { code: '1403', dir: 'DEBIT', amount: ap.excl },
      { code: '222101', dir: 'DEBIT', amount: ap.tax },
      { code: '2202', dir: 'CREDIT', amount: ap.total }
    ], i < 6 ? 'POSTED' : 'AUDITED');
  }
  // 收款
  for (const [i, ar] of arList.filter(a => a.received > 0).slice(0, 8).entries()) {
    const d = new Date(ar.d.getTime() + 20 * 86400000);
    await voucher(d, 'AUTO', 'mk', 'mk_payment', ar.id, '收到货款-' + ar.no, [
      { code: '1002', dir: 'DEBIT', amount: ar.received },
      { code: '1122', dir: 'CREDIT', amount: ar.received }
    ], 'POSTED');
    cashFlows.push({ d, dir: 'IN', biz: 'SALE_RECEIPT', amt: ar.received, summary: '客户回款-' + ar.no });
  }
  // 付款
  for (const [i, ap] of apList.filter(a => a.paid > 0).slice(0, 6).entries()) {
    const d = new Date(ap.d.getTime() + 15 * 86400000);
    await voucher(d, 'AUTO', 'pms', 'pms_payment', ap.id, '支付货款-' + ap.no, [
      { code: '2202', dir: 'DEBIT', amount: ap.paid },
      { code: '1002', dir: 'CREDIT', amount: ap.paid }
    ], 'POSTED');
    cashFlows.push({ d, dir: 'OUT', biz: 'PURCHASE_PAY', amt: ap.paid, summary: '供应商付款-' + ap.no });
  }
  // 计提工资 / 折旧 / 结转成本
  for (let m = 9; m <= 10; m++) {
    const d1 = new Date(2026, m - 1, 28);
    await voucher(d1, 'MANUAL', 'manual', 'salary', null, m + '月计提工资', [
      { code: '5001', dir: 'DEBIT', amount: 186000 },
      { code: '2211', dir: 'CREDIT', amount: 186000 }
    ], 'POSTED');
    await voucher(new Date(2026, m - 1, 28), 'MANUAL', 'manual', 'depreciation', null, m + '月计提折旧', [
      { code: '5101', dir: 'DEBIT', amount: 42000 },
      { code: '1602', dir: 'CREDIT', amount: 42000 }
    ], 'POSTED');
    await voucher(new Date(2026, m - 1, 28), 'MANUAL', 'manual', 'cost_carry', null, m + '月结转销售成本', [
      { code: '6401', dir: 'DEBIT', amount: 968000 },
      { code: '1405', dir: 'CREDIT', amount: 968000 }
    ], 'POSTED');
  }
  // 期初余额 + 月度销售/费用（使报表完整可读）
  await voucher(new Date('2026-09-01T09:00:00'), 'MANUAL', 'manual', 'opening', null, '2026年9月初余额结转', [
    { code: '1002', dir: 'DEBIT', amount: 2270000 },
    { code: '1403', dir: 'DEBIT', amount: 1200000 },
    { code: '1405', dir: 'DEBIT', amount: 2800000 },
    { code: '1122', dir: 'DEBIT', amount: 1500000 },
    { code: '1601', dir: 'DEBIT', amount: 1500000 },
    { code: '1602', dir: 'CREDIT', amount: 200000 },
    { code: '2202', dir: 'CREDIT', amount: 900000 },
    { code: '4001', dir: 'CREDIT', amount: 8170000 }
  ], 'POSTED');
  for (const [m, rev] of [[9, 1706422], [10, 1879817]]) {
    const tax = Number((rev * 0.09).toFixed(2));
    await voucher(new Date(2026, m - 1, 30), 'MANUAL', 'manual', 'monthly_sales_' + m, null, m + '月销售收入汇总', [
      { code: '1122', dir: 'DEBIT', amount: rev + tax },
      { code: '6001', dir: 'CREDIT', amount: rev },
      { code: '222102', dir: 'CREDIT', amount: tax }
    ], 'POSTED');
    await voucher(new Date(2026, m - 1, 30), 'MANUAL', 'manual', 'expense_' + m, null, m + '月管理费用结转', [
      { code: '6602', dir: 'DEBIT', amount: 148000 },
      { code: '6601', dir: 'DEBIT', amount: 62000 },
      { code: '1002', dir: 'CREDIT', amount: 210000 }
    ], 'POSTED');
  }
  // 月末结转损益至利润分配
  const plRows = await q(`SELECT e.account_code, e.period, e.direction, SUM(e.amount) amt
    FROM fin_voucher_entry e JOIN fin_voucher v ON v.voucher_id=e.voucher_id
    WHERE v.status IN ('AUDITED','POSTED') AND (e.account_code LIKE '5%' OR e.account_code LIKE '6%')
    GROUP BY e.account_code, e.period, e.direction`);
  const plByPeriod = new Map();
  for (const r of plRows) {
    if (!plByPeriod.has(r.period)) plByPeriod.set(r.period, new Map());
    const m = plByPeriod.get(r.period);
    m.set(r.account_code, (m.get(r.account_code) || 0) + (r.direction === 'DEBIT' ? Number(r.amt) : -Number(r.amt)));
  }
  for (const [period, m] of [...plByPeriod.entries()].sort()) {
    const entries = [];
    let profit = 0;
    for (const [code, net] of m) {
      if (Math.abs(net) < 0.01) continue;
      if (net > 0) { entries.push({ code, dir: 'CREDIT', amount: net }); profit += net; }
      else { entries.push({ code, dir: 'DEBIT', amount: -net }); profit += net; }
    }
    if (!entries.length) continue;
    const realProfit = -profit;
    entries.push({ code: '4104', dir: realProfit >= 0 ? 'CREDIT' : 'DEBIT', amount: Math.abs(realProfit) });
    const y = Number(period.slice(0, 4)), mo = Number(period.slice(4, 6));
    await voucher(new Date(y, mo - 1, 30, 18, 0, 0), 'MANUAL', 'manual', 'pl_close_' + period, null,
      y + '年' + mo + '月结转本期损益', entries, 'POSTED');
  }
  console.log(`凭证 ${vSeq} 张 / 分录 ${eCnt} 条`);

  // ---------- 9. 总账 ----------
  const rows = await q(`SELECT e.account_code, e.account_name, e.period, e.direction, SUM(e.amount) amt
    FROM fin_voucher_entry e JOIN fin_voucher v ON v.voucher_id=e.voucher_id
    WHERE v.status IN ('AUDITED','POSTED') AND e.del_flag='0' GROUP BY e.account_code,e.account_name,e.period,e.direction`);
  const acc = new Map();
  for (const r of rows) {
    const k = r.account_code;
    if (!acc.has(k)) acc.set(k, { name: r.account_name, byPeriod: new Map() });
    const a = acc.get(k);
    if (!a.byPeriod.has(r.period)) a.byPeriod.set(r.period, { d: 0, c: 0 });
    const t = a.byPeriod.get(r.period);
    if (r.direction === 'DEBIT') t.d += Number(r.amt); else t.c += Number(r.amt);
  }
  let lCnt = 0;
  const allPeriods = [...new Set([...acc.values()].flatMap(a => [...a.byPeriod.keys()]))].sort();
  for (const [code, a] of acc) {
    let od = 0, oc = 0;
    for (const p of allPeriods) {
      const t = a.byPeriod.get(p) || { d: 0, c: 0 };
      const cd = od + t.d, cc = oc + t.c;
      await exec(`INSERT INTO fin_ledger(company_id,account_code,period,dept_id,aux_type,aux_id,opening_debit,opening_credit,period_debit,period_credit,closing_debit,closing_credit,del_flag,create_by,create_time,remark)
        VALUES(1,?,?,0,'',0,?,?,?,?,?,?, '0','system',NOW(),'')`,
        [code, p, n2(od), n2(oc), n2(t.d), n2(t.c), n2(cd), n2(cc)]);
      lCnt++;
      od = cd; oc = cc;
    }
  }
  console.log(`总账 ${lCnt} 行`);

  // ---------- 10. 资金流水 ----------
  cashFlows.sort((a, b) => a.d - b.d);
  let bal = 2270000;
  for (const [i, f] of cashFlows.entries()) {
    bal += f.dir === 'IN' ? f.amt : -f.amt;
    await exec(`INSERT INTO fin_cash_flow(flow_no,company_id,account_id,flow_date,period,dept_id,direction,biz_type,amount,balance_after,source_module,source_type,source_id,del_flag,create_by,create_time,remark)
      VALUES(?,1,1,?,?,105,?,?,?,?, 'fin','manual',?, '0','system',NOW(),?)`,
      ['LS' + per(f.d) + pad(i + 1), dstr(f.d), per(f.d), f.dir, f.biz, n2(f.amt), n2(bal), i + 1, f.summary]);
  }
  await exec(`UPDATE fin_bank_account SET current_balance=? WHERE account_id=1`, [n2(bal)]);
  console.log(`资金流水 ${cashFlows.length} 条`);

  // ---------- 11. 物料税率 ----------
  const mats = await q(`SELECT material_id, material_code, material_type FROM wms_material WHERE del_flag='0'`);
  let mtCnt = 0;
  for (const m of mats) {
    const rate = m.material_type === '0' ? 0.09 : (m.material_type === '4' ? 0.06 : 0.13);
    await exec(`INSERT INTO fin_material_tax(material_id,material_code,tax_rate,tax_included,effective_date,company_id,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?, '2026-01-01',1,'0','0','system',NOW(),?)`,
      [m.material_id, m.material_code, rate, '1',
        m.material_type === '0' ? '农产品原料适用 9%' : (m.material_type === '4' ? '服务类 6%' : '一般货物 13%')]);
    mtCnt++;
  }
  console.log(`物料税率 ${mtCnt} 条`);

  // ---------- 12. 财务参数 / 成本方法 / 结账检查 ----------
  const cfgs = [
    ['company_name', '云南赢隆食品加工有限公司', '公司名称', 'base'],
    ['taxpayer_type', 'GENERAL', '纳税人类型', 'base'],
    ['base_currency', 'CNY', '本位币', 'base'],
    ['fiscal_year_start', '01-01', '会计年度起始', 'base'],
    ['voucher_rule_auto', '1', '业务单据自动生成凭证', 'voucher'],
    ['voucher_audit_required', '1', '凭证必须审核后入账', 'voucher'],
    ['ar_default_days', '30', '应收账期(天)', 'ar_ap'],
    ['ap_default_days', '30', '应付账期(天)', 'ar_ap'],
    ['negative_stock_allowed', '0', '允许负库存', 'inventory'],
    ['cost_method', 'WEIGHTED_AVG', '存货计价方法', 'inventory']
  ];
  for (const c of cfgs) {
    await exec(`INSERT INTO fin_fiscal_config(config_key,config_value,config_name,config_group,company_id,del_flag,create_by,create_time)
      VALUES(?,?,?,?,1, '0','system',NOW())`, c);
  }
  await exec(`INSERT INTO fin_cost_method_config(material_id,cost_method,tax_included,effective_date,company_id,status,del_flag,create_by,create_time,remark)
    VALUES(NULL,'WEIGHTED_AVG','1','2026-01-01',1,'0','0','system',NOW(),'全公司统一移动加权平均法')`);
  const checks = [
    ['CHK001', '凭证借贷平衡检查', '1', '全部 42 张凭证借贷平衡'],
    ['CHK002', '现金银行日记账与总账核对', '1', '银行存款总账与资金流水一致'],
    ['CHK003', '应收应付与往来对账', '1', '应收 26 笔、应付 20 笔无异常'],
    ['CHK004', '存货成本结转检查', '1', '9-10 月成本已结转'],
    ['CHK005', '未记账凭证检查', '0', '存在 4 张已审核未记账凭证']
  ];
  for (const c of checks) {
    await exec(`INSERT INTO fin_closing(company_id,\`year_month\`,dept_id,check_code,check_name,check_result,check_detail,check_by,check_time,del_flag,create_by,create_time)
      VALUES(1,'202610',0,?,?,?,?, '财务主管',NOW(), '0','system',NOW())`, c);
  }
  console.log('财务参数/成本方法/结账检查 已生成');

  console.log(await q(`SELECT (SELECT COUNT(*) FROM fin_subject) 科目,(SELECT COUNT(*) FROM fin_period) 期间,
    (SELECT COUNT(*) FROM fin_partner) 往来,(SELECT COUNT(*) FROM fin_ar_order) 应收,
    (SELECT COUNT(*) FROM fin_ap_order) 应付,(SELECT COUNT(*) FROM fin_voucher) 凭证,
    (SELECT COUNT(*) FROM fin_voucher_entry) 分录,(SELECT COUNT(*) FROM fin_ledger) 总账,
    (SELECT COUNT(*) FROM fin_cash_flow) 流水,(SELECT COUNT(*) FROM fin_input_tax) 进项税,
    (SELECT COUNT(*) FROM fin_material_tax) 物料税率`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
