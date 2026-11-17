/**
 * 采购模块数据模拟：采购退货 / 供应商资质 / 供货清单 / 采购申请 / 计划明细 / 询比价 / 采购发票
 * 全部基于已有 113 张采购订单、113 张收货单、6 份采购计划、41 家供应商
 */
const mysql = require('mysql2/promise');
const pad = (n, w = 3) => String(n).padStart(w, '0');
const fmt = d => {
  const p = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const rnd = (a, b) => a + Math.random() * (b - a);
const ri = (a, b) => Math.floor(rnd(a, b + 1));

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  for (const t of ['pms_return', 'pms_return_detail', 'pms_supplier_qualification', 'pms_supplier_catalog',
    'pms_purchase_request', 'pms_purchase_request_detail', 'pms_purchase_plan_detail',
    'pms_inquiry', 'pms_inquiry_detail', 'pms_quotation', 'pms_quotation_detail',
    'pms_invoice', 'pms_invoice_detail']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ---------- 1. 采购退货（来源于收货单不合格品） ----------
  const badReceives = await q(`SELECT r.* FROM pms_receive r WHERE r.del_flag='0' AND r.status IN ('2','3') ORDER BY r.receive_date DESC LIMIT 6`);
  let rtCnt = 0, rtDCnt = 0;
  const reasons = ['到货外观破损，不符合验收标准', '中心温度不达标，判定不合格', '规格与订单不符，予以退回', '超过保质期三分之一，拒收'];
  for (const [i, r] of badReceives.entries()) {
    let dets = await q(`SELECT * FROM pms_receive_detail WHERE receive_id=? AND receive_qty>0 LIMIT 2`, [r.receive_id]);
    dets = dets.map(d => ({ ...d, unqualified_qty: Math.max(1, Math.round(Number(d.receive_qty) * 0.05 * 100) / 100) }));
    if (!dets.length) continue;
    const d0 = new Date(r.receive_date);
    const no = 'TH' + fmt(d0).slice(0, 10).replace(/-/g, '') + pad(i + 1);
    let totalQty = 0, totalAmt = 0;
    const rows = [];
    for (const d of dets) {
      const qty = Number(d.unqualified_qty) || 1;
      const price = Number(d.unit_price) || 10;
      totalQty += qty; totalAmt += qty * price;
      rows.push([d, qty, price]);
    }
    const status = i < 4 ? '3' : '2';
    const ins = await exec(`INSERT INTO pms_return(return_no,order_id,order_no,receive_id,receive_no,supplier_id,supplier_name,status,return_type,return_date,total_qty,total_amount,reason,handler_id,handler_name,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?,103)`,
      [no, r.order_id, r.order_no, r.receive_id, r.receive_no, r.supplier_id, r.supplier_name, status,
        i % 3 === 0 ? '0' : (i % 3 === 1 ? '1' : '2'), fmt(new Date(d0.getTime() + 86400000)).slice(0, 10),
        totalQty.toFixed(2), totalAmt.toFixed(2), reasons[i % reasons.length], 5, '李晨', '收货验收不合格转退货', 103]);
    for (const [d, qty, price] of rows) {
      await exec(`INSERT INTO pms_return_detail(return_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,return_reason,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
        [ins.insertId, d.material_id, d.material_code, d.material_name, d.spec_model, d.unit, qty, price, (qty * price).toFixed(2),
          reasons[i % reasons.length]]);
      rtDCnt++;
    }
    rtCnt++;
  }
  console.log(`采购退货 ${rtCnt} 张 / 明细 ${rtDCnt} 行`);

  // ---------- 2. 供应商资质 ----------
  const sups = await q(`SELECT supplier_id,supplier_name FROM wms_supplier WHERE del_flag='0' ORDER BY supplier_id LIMIT 20`);
  let qCnt = 0;
  const qualTpl = [
    ['营业执照', '0', 2024], ['食品生产许可证', '1', 2025], ['食品经营许可证', '2', 2024], ['ISO22000 食品安全管理体系认证', '3', 2024]
  ];
  for (const s of sups) {
    const n = 2 + (s.supplier_id % 3);
    for (let i = 0; i < n; i++) {
      const t = qualTpl[i % qualTpl.length];
      const issue = new Date(t[2], (s.supplier_id + i) % 12, 10);
      const expire = new Date(t[2] + 3, (s.supplier_id + i) % 12, 9);
      const expired = expire < new Date('2026-11-16');
      await exec(`INSERT INTO pms_supplier_qualification(supplier_id,supplier_name,qual_name,qual_type,qual_code,issue_date,expire_date,status,audit_by,audit_time,audit_remark,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?)`,
        [s.supplier_id, s.supplier_name, t[0], t[1], 'SC' + pad(s.supplier_id) + pad(i + 1, 4), fmt(issue).slice(0, 10), fmt(expire).slice(0, 10),
          expired ? '3' : '1', '李晨', fmt(new Date(2026, 0, 15)), expired ? '已过期，要求补办' : '审核通过',
          expired ? '到期提醒：请供应商更新' : '']);
      qCnt++;
    }
  }
  console.log(`供应商资质 ${qCnt} 份`);

  // ---------- 3. 供货清单（价格库） ----------
  const priceRows = await q(`SELECT d.material_id,d.material_code,d.material_name,d.spec_model,d.unit,
      AVG(d.unit_price) price, MIN(d.unit_price) minp, MAX(d.unit_price) maxp, COUNT(*) cnt,
      SUBSTRING_INDEX(GROUP_CONCAT(o.supplier_id ORDER BY d.detail_id DESC),',',1) sup_id,
      SUBSTRING_INDEX(GROUP_CONCAT(o.supplier_name ORDER BY d.detail_id DESC),',',1) sup_name
    FROM pms_purchase_order_detail d JOIN pms_purchase_order o ON o.order_id=d.order_id
    WHERE o.del_flag='0' AND d.unit_price>0 GROUP BY d.material_id,d.material_code,d.material_name,d.spec_model,d.unit`);
  let catCnt = 0;
  for (const p of priceRows) {
    await exec(`INSERT INTO pms_supplier_catalog(supplier_id,supplier_name,material_id,material_code,material_name,spec_model,unit,unit_price,tax_rate,min_order_qty,delivery_cycle,currency,price_effective_date,price_expire_date,status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?, 'CNY',?,?, '1','0','system',NOW(),?)`,
      [p.sup_id, p.sup_name, p.material_id, p.material_code, p.material_name, p.spec_model, p.unit,
        Number(p.price).toFixed(4), p.material_type === '2' ? 13 : 9, 1, ri(5, 20),
        '2026-01-01', '2026-12-31', '由历史采购价自动归集（' + p.cnt + ' 次成交）']);
    catCnt++;
  }
  console.log(`供货清单 ${catCnt} 条`);

  // ---------- 4. 采购申请 ----------
  const lowStock = await q(`SELECT m.material_id,m.material_code,m.material_name,m.spec_model,m.unit,m.safety_stock_min,
      COALESCE(SUM(i.qty),0) qty FROM wms_material m LEFT JOIN wms_inventory i ON i.material_id=m.material_id AND i.del_flag='0'
      WHERE m.del_flag='0' AND m.safety_stock_min>0 GROUP BY m.material_id HAVING qty < m.safety_stock_min ORDER BY (m.safety_stock_min-qty) DESC LIMIT 12`);
  let reqCnt = 0, reqDCnt = 0;
  const reqGroups = [lowStock.slice(0, 5), lowStock.slice(5, 9), lowStock.slice(9, 12)];
  for (const [i, g] of reqGroups.entries()) {
    if (!g.length) continue;
    const d = new Date(2026, 10, 3 + i * 3);
    const no = 'CGSQ' + fmt(d).slice(0, 10).replace(/-/g, '') + pad(i + 1);
    let total = 0; const rows = [];
    for (const m of g) {
      const qty = Math.max(1, Math.ceil((Number(m.safety_stock_min) - Number(m.qty)) * 1.2));
      const price = Number((10 + Math.random() * 90).toFixed(2));
      total += qty * price;
      rows.push([m, qty, price]);
    }
    const ins = await exec(`INSERT INTO pms_purchase_request(request_no,title,status,priority,expect_date,purpose,total_amount,requester_id,requester_name,dept_id,dept_name,audit_by,audit_time,audit_opinion,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
      [no, '2026年11月' + (i === 0 ? '原料' : (i === 1 ? '辅料' : '包材')) + '补库采购申请',
        i === 0 ? '2' : (i === 1 ? '1' : '0'), i === 0 ? '1' : '0', fmt(new Date(d.getTime() + 15 * 86400000)).slice(0, 10),
        '低于安全库存，需补库以保障生产', total.toFixed(2), 5, '李晨', 103, '采购供应部',
        i === 0 ? 'admin' : null, i === 0 ? fmt(d) : null, i === 0 ? '同意，转入采购计划' : null]);
    for (const [m, qty, price] of rows) {
      await exec(`INSERT INTO pms_purchase_request_detail(request_id,material_id,material_code,material_name,spec_model,unit,qty,estimated_price,estimated_amount,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?, '0','system',NOW(),?)`,
        [ins.insertId, m.material_id, m.material_code, m.material_name, m.spec_model, m.unit, qty, price, (qty * price).toFixed(2), '安全库存:' + m.safety_stock_min]);
      reqDCnt++;
    }
    reqCnt++;
  }
  console.log(`采购申请 ${reqCnt} 张 / 明细 ${reqDCnt} 行`);

  // ---------- 5. 采购计划明细 ----------
  const plans = await q(`SELECT * FROM pms_purchase_plan WHERE del_flag='0' ORDER BY plan_id`);
  let planDCnt = 0;
  for (const p of plans) {
    const mats = await q(`SELECT material_id,material_code,material_name,spec_model,unit FROM wms_material
      WHERE del_flag='0' AND material_type IN ('0','3','6') ORDER BY RAND() LIMIT 4`);
    const budgetTotal = Number(p.budget_amount) || 100000;
    for (const m of mats) {
      const price = Number((budgetTotal / 4 / ri(200, 2000)).toFixed(2));
      const qty = Math.max(10, Math.round(budgetTotal / 4 / Math.max(1, price)));
      await exec(`INSERT INTO pms_purchase_plan_detail(plan_id,material_id,material_code,material_name,specification,unit,plan_quantity,budget_price,budget_amount,actual_quantity,actual_amount,require_date,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,0,0,?, '0','system',NOW(),?)`,
        [p.plan_id, m.material_id, m.material_code, m.material_name, m.spec_model, m.unit, qty, price, (qty * price).toFixed(2),
          fmt(new Date(p.end_date)).slice(0, 10), '计划分解']);
      planDCnt++;
    }
  }
  console.log(`采购计划明细 ${planDCnt} 行`);

  // ---------- 6. 询比价 ----------
  const inquiryDefs = [
    { title: '2026年12月真空包装袋询价', mats: ['真空袋', '包装袋', '彩袋'], type: '1', status: '4', deadline: '2026-11-10' },
    { title: '2026年12月食盐及调味料询价', mats: ['食盐', '调味', '香辛'], type: '0', status: '3', deadline: '2026-11-18' },
    { title: '冷链运输服务年度询价', mats: [], type: '0', status: '1', deadline: '2026-11-25' }
  ];
  let inqCnt = 0, inqDCnt = 0, quoCnt = 0, quoDCnt = 0;
  for (const [i, def] of inquiryDefs.entries()) {
    const d = new Date(2026, 10, 5 + i * 6);
    const no = 'XJ' + fmt(d).slice(0, 10).replace(/-/g, '') + pad(i + 1);
    const ins = await exec(`INSERT INTO pms_inquiry(inquiry_no,title,status,inquiry_type,purchase_type,deadline,estimate_amount,award_amount,award_supplier_id,award_supplier_name,description,inquirer_id,inquirer_name,dept_id,dept_name,del_flag,create_by,create_time,audit_by,audit_time,audit_opinion)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?,?,?)`,
      [no, def.title, def.status, def.type, '0', fmt(new Date(def.deadline + 'T18:00:00')),
        (30000 + i * 20000).toFixed(2), def.status === '4' ? (28000 + i * 18000).toFixed(2) : '0.00',
        def.status === '4' ? 4 : null, def.status === '4' ? '昆明华彩包装制品有限公司' : null,
        '按年度采购计划组织的询价，邀请合格供应商参与报价', 5, '李晨', 103, '采购供应部',
        def.status === '4' ? 'admin' : null, def.status === '4' ? fmt(new Date(2026, 10, 11)) : null,
        def.status === '4' ? '定标，按最低价成交' : null]);
    inqCnt++;
    // 明细
    let mats;
    if (def.mats.length) {
      mats = await q(`SELECT material_id,material_code,material_name,spec_model,unit FROM wms_material WHERE del_flag='0' AND (${def.mats.map(() => 'material_name LIKE ?').join(' OR ')}) LIMIT 5`, def.mats.map(x => '%' + x + '%'));
    } else {
      mats = await q(`SELECT material_id,material_code,material_name,spec_model,unit FROM wms_material WHERE del_flag='0' AND material_type='4' LIMIT 2`);
    }
    if (!mats.length) mats = await q(`SELECT material_id,material_code,material_name,spec_model,unit FROM wms_material WHERE del_flag='0' LIMIT 3`);
    const inqDets = [];
    for (const m of mats) {
      const qty = ri(1000, 20000);
      const maxp = Number((0.5 + Math.random() * 20).toFixed(4));
      const insD = await exec(`INSERT INTO pms_inquiry_detail(inquiry_id,material_id,material_code,material_name,specification,unit,quantity,max_price,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
        [ins.insertId, m.material_id, m.material_code, m.material_name, m.spec_model, m.unit, qty, maxp]);
      inqDets.push({ id: insD.insertId, m, qty, maxp });
      inqDCnt++;
    }
    // 报价（已截止/已定标才有）
    if (def.status === '3' || def.status === '4') {
      const suppliers = await q(`SELECT supplier_id,supplier_name FROM wms_supplier WHERE del_flag='0' LIMIT 2`);
      for (const s of suppliers) {
        let total = 0; const qds = [];
        for (const x of inqDets) {
          const price = Number((x.maxp * rnd(0.82, 0.99)).toFixed(4));
          total += price * x.qty;
          qds.push([x, price]);
        }
        const qi = await exec(`INSERT INTO pms_quotation(inquiry_id,supplier_id,supplier_name,contact_name,contact_phone,total_amount,tax_rate,delivery_days,payment_terms,valid_date,status,del_flag,create_by,create_time,remark)
          VALUES(?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
          [ins.insertId, s.supplier_id, s.supplier_name, '业务经理', '138' + ri(10000000, 99999999),
            total.toFixed(2), 13, ri(7, 25), '月结30天', fmt(new Date(def.deadline)).slice(0, 10), '1']);
        quoCnt++;
        for (const [x, price] of qds) {
          await exec(`INSERT INTO pms_quotation_detail(quotation_id,inquiry_detail_id,material_id,material_code,material_name,specification,unit,quantity,price,amount,tax_rate,tax_price,tax_amount,delivery_date,del_flag,create_by,create_time,remark)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
            [qi.insertId, x.id, x.m.material_id, x.m.material_code, x.m.material_name, x.m.spec_model, x.m.unit, x.qty,
              price, (price * x.qty).toFixed(2), 13, price, (price * x.qty * 0.13).toFixed(2), def.deadline]);
          quoDCnt++;
        }
      }
    }
  }
  console.log(`询价 ${inqCnt} 单 / 明细 ${inqDCnt} 行，报价 ${quoCnt} 份 / 明细 ${quoDCnt} 行`);

  // ---------- 7. 采购发票 ----------
  const orders = await q(`SELECT o.order_id,o.order_no,o.supplier_id,o.supplier_name,o.total_amount,o.order_date,o.received_amount
    FROM pms_purchase_order o WHERE o.del_flag='0' AND o.total_amount>0 ORDER BY o.order_id DESC LIMIT 12`);
  let invCnt = 0, invDCnt = 0;
  for (const [i, o] of orders.entries()) {
    const d = new Date(o.order_date);
    const dets = await q(`SELECT * FROM pms_purchase_order_detail WHERE order_id=? AND qty>0 LIMIT 3`, [o.order_id]);
    if (!dets.length) continue;
    const amt = Number(o.total_amount);
    const taxRate = 9;
    const total = Number((amt * 1.09).toFixed(2));
    const status = i < 6 ? '3' : (i < 9 ? '2' : '0');
    const invNo = 'FP' + fmt(d).slice(0, 10).replace(/-/g, '') + pad(i + 1);
    const ins = await exec(`INSERT INTO pms_invoice(invoice_no,order_id,order_no,supplier_id,supplier_name,status,invoice_type,invoice_number,invoice_date,invoice_title,tax_number,invoice_amount,tax_amount,tax_rate,total_amount,payment_amount,pay_amount,payment_date,payment_method,bank_name,bank_account,payee_name,payment_remark,del_flag,create_by,create_time,remark,dept_id)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?,103)`,
      [invNo, o.order_id, o.order_no, o.supplier_id, o.supplier_name, status, i % 2 === 0 ? '0' : '1',
        'No.' + ri(10000000, 99999999), fmt(new Date(d.getTime() + 5 * 86400000)).slice(0, 10),
        '云南赢隆食品加工有限公司', '91530381MA6KXXXXXX', amt.toFixed(2), (total - amt).toFixed(2), taxRate, total.toFixed(2),
        status === '3' ? total.toFixed(2) : (status === '2' ? (total * 0.5).toFixed(2) : '0.00'),
        status === '3' ? total.toFixed(2) : (status === '2' ? (total * 0.5).toFixed(2) : '0.00'),
        status === '3' ? fmt(new Date(d.getTime() + 30 * 86400000)).slice(0, 10) : null,
        '1', '中国农业银行宣威支行', '24-0000-0000-0001', o.supplier_name, status === '3' ? '已付清' : '按期支付', '采购订单对应发票']);
    invCnt++;
    for (const x of dets) {
      const qty = Number(x.qty);
      const up = Number(x.unit_price);
      await exec(`INSERT INTO pms_invoice_detail(invoice_id,material_id,material_code,material_name,spec_model,unit,qty,unit_price,amount,tax_rate,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
        [ins.insertId, x.material_id, x.material_code, x.material_name, x.spec_model, x.unit, qty, up, (qty * up).toFixed(2), taxRate]);
      invDCnt++;
    }
  }
  console.log(`采购发票 ${invCnt} 张 / 明细 ${invDCnt} 行`);

  console.log(await q(`SELECT (SELECT COUNT(*) FROM pms_return) 退货,(SELECT COUNT(*) FROM pms_supplier_qualification) 资质,
    (SELECT COUNT(*) FROM pms_supplier_catalog) 供货清单,(SELECT COUNT(*) FROM pms_purchase_request) 申请,
    (SELECT COUNT(*) FROM pms_purchase_plan_detail) 计划明细,(SELECT COUNT(*) FROM pms_inquiry) 询价,
    (SELECT COUNT(*) FROM pms_invoice) 发票`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
