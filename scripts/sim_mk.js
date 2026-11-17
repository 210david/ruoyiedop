/**
 * 营销/CRM 数据模拟：商机阶段配置 / 线索(含跟进日志) / 联系人 / 商机(含阶段日志) / 市场活动(含参与人) /
 * 订单退货 / 回款计划与回款记录
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

  for (const t of ['mk_opportunity_stage', 'mk_lead', 'mk_lead_log', 'mk_contact', 'mk_opportunity',
    'mk_opportunity_stage_log', 'mk_activity', 'mk_activity_participant', 'mk_order_return',
    'mk_order_return_detail', 'mk_payment_plan', 'mk_payment_record']) {
    await exec(`DELETE FROM \`${t}\``);
  }

  // ---------- 1. 商机阶段配置 ----------
  const stages = [
    ['stage1', '初步接洽', 1, 10, 15], ['stage2', '需求确认', 2, 30, 20], ['stage3', '方案报价', 3, 50, 25],
    ['stage4', '商务谈判', 4, 75, 30], ['stage5', '赢单', 5, 100, null]
  ];
  for (const s of stages) {
    await exec(`INSERT INTO mk_opportunity_stage(stage_code,stage_name,sort,win_rate,max_days,required_fields,status,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?, '0','system',NOW(),'标准销售流程')`, [s[0], s[1], s[2], s[3], s[4], 'expected_amount']);
  }

  // ---------- 2. 线索 ----------
  const leads = [
    ['云南滇西土特产连锁有限公司', '杨明', '13888651201', '采购总监', '昆明', 1, 85, 'A', '2', '寻找高端猪肉制品供应商，关注大河乌猪系列礼盒'],
    ['成都蜀味食品贸易有限公司', '李静', '13908012345', '采购经理', '成都', 1, 76, 'B', '1', '代理云贵川腊味产品，询问区域经销政策'],
    ['重庆火锅食材供应链公司', '王强', '13608312345', '总经理', '重庆', 1, 72, 'B', '3', '需要小酥肉/香肠类火锅食材，月需求量约 20 吨'],
    ['贵阳永辉超市生鲜部', '刘莉', '13985112345', '生鲜采购', '贵阳', 1, 68, 'C', '1', '关注真空包装香肠，需提供检验报告与生产许可'],
    ['广州粤味餐饮管理公司', '陈志', '13802012345', '研发主厨', '广州', 1, 58, 'C', '0', '寻求培根、腊肉原料，试样中'],
    ['昆明鲜生活社区团购', '赵敏', '13888112345', '运营负责人', '昆明', 5, 55, 'C', '1', '社区团购平台，愿意做本地专场'],
    ['上海长三角食品集采中心', '孙浩', '13611212345', '品类经理', '上海', 1, 90, 'A', '3', '年度采购框架，要求 ISO22000 与溯源能力'],
    ['深圳前海跨境电商', '周琳', '13510212345', '供应链经理', '深圳', 3, 62, 'C', '0', '咨询出口东南亚的腊味产品资质'],
    ['曲靖本地商超联采', '吴刚', '13887412345', '采购', '曲靖', 2, 70, 'B', '2', '本地商超联合采购，希望账期 30 天'],
    ['宣威火腿专卖（电商）', '何芳', '13887512345', '店主', '曲靖', 0, 48, 'D', '5', '咨询一件代发与贴牌'],
    ['北京京客隆生鲜采购部', '马超', '13601012345', '采购主管', '北京', 1, 88, 'A', '4', '已合作客户新增品类，转介绍'],
    ['杭州联华华商集团', '沈洁', '13857112345', '品类采购', '杭州', 1, 65, 'C', '1', '关注低温肉制品，需要冷链配送方案'],
    ['南京苏果超市', '许强', '13805112345', '采购', '南京', 1, 60, 'C', '1', '补充休闲肉制品品类'],
    ['昆明空港航空食品', '段勇', '13887612345', '采购经理', '昆明', 1, 74, 'B', '3', '航空配餐用切片火腿，需小规格包装'],
    ['大理文旅伴手礼渠道', '杨丽', '13887212345', '渠道经理', '大理', 5, 52, 'C', '0', '景区伴手礼定制礼盒'],
    ['西双版纳边贸商行', '岩温', '13887912345', '负责人', '西双版纳', 4, 45, 'D', '5', '边贸批发询价'],
    ['武汉中百仓储', '胡斌', '13802712345', '采购', '武汉', 1, 66, 'C', '1', '华中区域腊味品类补充'],
    ['长沙步步高生鲜', '彭宇', '13807312345', '采购主管', '长沙', 1, 64, 'C', '0', '关注小甜肠系列'],
    ['西安爱菊粮油食品', '张涛', '13802912345', '采购', '西安', 1, 56, 'D', '2', '西北市场代理意向'],
    ['昆明铁路客运餐饮', '李梅', '13887712345', '采购', '昆明', 1, 58, 'C', '1', '列车盒餐用肉制品']
  ];
  const custs = await q(`SELECT customer_id,customer_name FROM mk_customer ORDER BY customer_id`);
  let leadCnt = 0;
  const leadIds = [];
  for (const [i, l] of leads.entries()) {
    const d = new Date(2026, 8 + (i % 3), 1 + (i * 3) % 27, 9 + (i % 8), 30);
    const converted = l[8] === '4';
    const ins = await exec(`INSERT INTO mk_lead(lead_no,company_name,contact_name,contact_phone,contact_email,position,industry,company_size,province,city,lead_source,requirement_desc,lead_score,lead_grade,lead_status,is_public,user_id,dept_id,receive_time,last_follow_time,convert_customer_id,convert_time,receive_status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?)`,
      ['XS' + fmt(d).slice(0, 10).replace(/-/g, '') + pad(i + 1), l[0], l[1], l[2],
        'contact' + (i + 1) + '@example.com', l[3], '零售', '2', l[4].slice(0, 2) === '昆' ? '云南省' : '其他省', l[4],
        String(l[5]), l[9], l[6], l[7], l[8], l[8] === '0' ? '1' : '0', 7, 105,
        fmt(d), fmt(new Date(d.getTime() + ri(1, 20) * 86400000)),
        converted ? custs[i % custs.length].customer_id : null, converted ? fmt(new Date(d.getTime() + 15 * 86400000)) : null,
        '2', fmt(d), '线上/展会获客']);
    leadCnt++;
    leadIds.push({ id: ins.insertId, lead: l, date: d });
    // 跟进日志
    const logN = l[8] === '0' ? 1 : 3;
    const actions = ['新建线索', '电话沟通', '发送资料', '微信跟进', '现场拜访', '报价', '转商机'];
    for (let k = 0; k < logN; k++) {
      const t = new Date(d.getTime() + k * 3 * 86400000);
      await exec(`INSERT INTO mk_lead_log(lead_id,action_type,action_desc,operator_id,operator_name,operate_time)
        VALUES(?,?,?,?,?,?)`,
        [ins.insertId, String(ri(0, 4)), actions[Math.min(k + 1, actions.length - 1)] + '：' + (k === 0 ? '确认客户需求与采购量' : (k === 1 ? '客户索要样品与检验报告' : '推进至下一阶段')),
          7, '吴芳', fmt(t)]);
    }
  }
  console.log(`线索 ${leadCnt} 条`);

  // ---------- 3. 联系人 ----------
  let ctCnt = 0;
  const surnames = ['张', '王', '李', '赵', '刘', '陈', '杨', '黄', '周', '吴', '徐'];
  const positions = ['采购总监', '采购经理', '品类采购', '总经理', '质量负责人'];
  for (const c of custs) {
    for (let k = 0; k < 2; k++) {
      await exec(`INSERT INTO mk_contact(customer_id,name,gender,position,department,role_tag,phone,email,wechat,is_key,is_primary,owner_user_id,last_contact_time,next_contact_time,personal_trait,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
        [c.customer_id, surnames[(c.customer_id + k) % surnames.length] + (k === 0 ? '经理' : '主管'),
          k === 0 ? '0' : '1', positions[(c.customer_id + k) % positions.length], '采购部',
          k === 0 ? '决策人' : '影响者', '138' + (80000000 + c.customer_id * 111 + k),
          'c' + c.customer_id + '_' + k + '@example.com', 'wx_' + c.customer_id + '_' + k,
          k === 0 ? '1' : '0', k === 0 ? '1' : '0', 7,
          fmt(new Date(2026, 10, ri(1, 15))), fmt(new Date(2026, 11, ri(1, 20))), '沟通顺畅，注重品质与交期']);
      ctCnt++;
    }
  }
  console.log(`联系人 ${ctCnt} 人`);

  // ---------- 4. 商机 ----------
  const contacts = await q(`SELECT contact_id, customer_id FROM mk_contact WHERE is_primary='1'`);
  const cMap = new Map(contacts.map(c => [c.customer_id, c.contact_id]));
  const oppDefs = [
    { name: '上海联华生鲜 2027 年腊味礼盒年度框架', amt: 1860000, stage: 'stage4', status: '0', win: 75 },
    { name: '北京京客隆低温香肠品类引入', amt: 920000, stage: 'stage3', status: '0', win: 50 },
    { name: '南京苏果小甜肠系列上架', amt: 480000, stage: 'stage2', status: '0', win: 30 },
    { name: '杭州联华生鲜冷链配送合作', amt: 660000, stage: 'stage3', status: '0', win: 50 },
    { name: '苏州 foodie 餐饮定制培根', amt: 320000, stage: 'stage1', status: '0', win: 10 },
    { name: '昆明锦华批发商行春节备货', amt: 780000, stage: 'stage5', status: '1', win: 100 },
    { name: '曲靖优选食材供应链年度采购', amt: 540000, stage: 'stage5', status: '1', win: 100 },
    { name: '上海滇胤贸易经销商区域授权', amt: 1200000, stage: 'stage4', status: '0', win: 75 },
    { name: '上海宝山直营店渠道拓展', amt: 150000, stage: 'stage2', status: '0', win: 30 },
    { name: '新农垦集团团购福利订单', amt: 260000, stage: 'stage3', status: '0', win: 50 },
    { name: '成都蜀味食品区域代理（新增）', amt: 430000, stage: 'stage1', status: '2', win: 0 },
    { name: '重庆火锅食材供应链小酥肉', amt: 890000, stage: 'stage2', status: '0', win: 30 }
  ];
  let oCnt = 0, osCnt = 0;
  for (const [i, o] of oppDefs.entries()) {
    const c = custs[i % custs.length];
    const d = new Date(2026, 8 + (i % 3), 5 + (i % 20), 10, 0);
    const winRate = o.win;
    const wAmt = (o.amt * winRate / 100).toFixed(2);
    const ins = await exec(`INSERT INTO mk_opportunity(opportunity_no,opportunity_name,customer_id,contact_id,lead_id,opportunity_source,expected_amount,expected_date,stage_code,stage_name,win_rate,weighted_amount,opportunity_status,competitor,pain_point,solution,lost_reason,actual_amount,actual_date,user_id,dept_id,next_action,next_time,stage_enter_time,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?)`,
      ['SJ' + fmt(d).slice(0, 10).replace(/-/g, '') + pad(i + 1), o.name, c.customer_id, cMap.get(c.customer_id) || null,
        leadIds[i % leadIds.length].id, String(i % 4), o.amt, fmt(new Date(2026, 11, ri(10, 28))).slice(0, 10),
        o.stage, stages.find(s => s[0] === o.stage)[1], winRate, wAmt, o.status,
        i % 3 === 0 ? '本地同类肉制品企业' : '外省品牌',
        '客户希望降低采购成本并保证稳定供货', '提供定制规格 + 年度锁价 + 冷链直达',
        o.status === '2' ? '2' : null, o.status === '1' ? (o.amt * rnd(0.95, 1.05)).toFixed(2) : null,
        o.status === '1' ? fmt(new Date(2026, 9, ri(10, 28))).slice(0, 10) : null,
        7, 105, '安排下一步拜访与样品确认', fmt(new Date(2026, 10, ri(17, 28))), fmt(d), '按销售流程推进']);
    oCnt++;
    // 阶段日志
    const stageIdx = stages.findIndex(s => s[0] === o.stage);
    for (let k = 0; k <= Math.min(stageIdx, 3); k++) {
      await exec(`INSERT INTO mk_opportunity_stage_log(opportunity_id,from_stage_code,from_stage_name,to_stage_code,to_stage_name,action_type,operator_id,operator_name,operate_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?)`,
        [ins.insertId, k === 0 ? null : stages[k - 1][0], k === 0 ? null : stages[k - 1][1], stages[k][0], stages[k][1],
          'forward', 7, '吴芳', fmt(new Date(d.getTime() + k * 7 * 86400000)), '阶段推进']);
      osCnt++;
    }
  }
  console.log(`商机 ${oCnt} 个 / 阶段日志 ${osCnt} 条`);

  // ---------- 5. 市场活动 ----------
  const acts = [
    ['2026 中国（昆明）国际食品博览会', '1', '1', 120000, 800, 260, 32, 12, 3, 680000],
    ['大河乌猪品牌客户品鉴沙龙（上海站）', '3', '1', 45000, 120, 86, 24, 9, 2, 320000],
    ['抖音生鲜年货节专场直播', '0', '0', 60000, 20000, 3800, 120, 26, 5, 460000],
    ['经销商大会暨 2027 年政策发布会', '3', '1', 88000, 200, 168, 42, 18, 6, 1560000],
    ['预制菜行业峰会（成都）', '2', '1', 35000, 500, 210, 18, 6, 1, 180000]
  ];
  let aCnt = 0, pCnt = 0;
  for (const [i, a] of acts.entries()) {
    const d = new Date(2026, 7 + i, 12 + i * 3, 9, 0);
    const ins = await exec(`INSERT INTO mk_activity(activity_no,activity_name,activity_type,activity_form,activity_status,start_time,end_time,location,budget,cost,target_count,target_lead_count,user_id,dept_id,owner_phone,summary,content,actual_count,lead_count,mql_count,sql_count,opportunity_count,deal_amount,actual_roi,del_flag)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0')`,
      ['HD2026' + pad(i + 1), a[0], a[1], a[2], '3', fmt(d), fmt(new Date(d.getTime() + 8 * 3600000)),
        i % 2 === 0 ? '昆明滇池国际会展中心' : '上海市宝山区万豪酒店', a[3], Number((a[3] * rnd(0.85, 1.05)).toFixed(2)),
        a[4], a[5], 7, 105, '13888123456', '活动已完成，客户反馈良好，形成有效线索 ' + a[6] + ' 条',
        '围绕大河乌猪系列产品开展品牌推介与渠道洽谈', a[4], a[6], a[7], a[8], Math.max(1, Math.round(a[6] / 10)), a[9],
        (a[9] / a[3]).toFixed(2)]);
    aCnt++;
    const n = Math.min(10, a[4]);
    for (let k = 0; k < 10; k++) {
      const l = leadIds[(i * 4 + k) % leadIds.length];
      await exec(`INSERT INTO mk_activity_participant(activity_id,lead_id,company_name,contact_name,contact_phone,contact_email,position,participate_status,source,sign_time,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, 'system',NOW(),'')`,
        [ins.insertId, l.id, l.lead[0], l.lead[1], l.lead[2], 'p' + i + '_' + k + '@example.com', l.lead[3],
          k < 8 ? '1' : '2', '0', fmt(new Date(d.getTime() + 30 * 60000))]);
      pCnt++;
    }
  }
  console.log(`市场活动 ${aCnt} 场 / 参与人 ${pCnt} 条`);

  // ---------- 6. 订单退货 ----------
  const orders = await q(`SELECT o.order_id,o.order_no,o.customer_id,o.order_amount,o.create_time,c.customer_name
    FROM mk_order o LEFT JOIN mk_customer c ON c.customer_id=o.customer_id WHERE o.del_flag='0' AND o.order_amount>0
    ORDER BY o.order_id DESC LIMIT 3`);
  let rtCnt = 0, rtDCnt = 0;
  const reasons = ['客户反馈包装破损，要求退货', '产品临期，客户申请退货', '规格与订单不符退回'];
  for (const [i, o] of orders.entries()) {
    const items = await q(`SELECT * FROM mk_order_item WHERE order_id=? LIMIT 2`, [o.order_id]);
    if (!items.length) continue;
    const d = new Date(o.create_time);
    let qty = 0, amt = 0;
    for (const it of items) { const q2 = Math.max(1, Math.round(it.quantity * 0.05)); qty += q2; amt += q2 * Number(it.unit_price); }
    const ins = await exec(`INSERT INTO mk_order_return(return_no,order_id,order_no,customer_id,customer_name,warehouse_id,warehouse_name,return_amount,total_qty,return_reason,return_status,refund_amount,refund_date,approve_opinion,approve_by,approve_time,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),?)`,
      ['XT' + fmt(d).slice(0, 10).replace(/-/g, '') + pad(i + 1), o.order_id, o.order_no, o.customer_id, o.customer_name,
        2, '冷藏成品库', amt.toFixed(2), qty, reasons[i % reasons.length], i === 0 ? '3' : (i === 1 ? '2' : '1'),
        i < 2 ? amt.toFixed(2) : null, i < 2 ? fmt(new Date(d.getTime() + 5 * 86400000)).slice(0, 10) : null,
        i < 2 ? '同意退货，按原价退款' : null, i < 2 ? '销售经理' : null, i < 2 ? fmt(new Date(d.getTime() + 2 * 86400000)) : null,
        '客户申请退货']);
    rtCnt++;
    for (const it of items) {
      const q2 = Math.max(1, Math.round(it.quantity * 0.05));
      await exec(`INSERT INTO mk_order_return_detail(return_id,material_id,material_code,material_name,spec_model,unit,ship_qty,return_qty,unit_price,amount,return_reason,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?,?, '0','system',NOW(),'')`,
        [ins.insertId, it.material_id, it.material_code, it.product_name, it.product_spec, it.unit,
          it.quantity, q2, it.unit_price, (q2 * Number(it.unit_price)).toFixed(2), reasons[i % reasons.length]]);
      rtDCnt++;
    }
  }
  console.log(`销售退货 ${rtCnt} 张 / 明细 ${rtDCnt} 行`);

  // ---------- 7. 回款计划 + 回款记录 ----------
  const payOrders = await q(`SELECT o.order_id,o.order_no,o.customer_id,o.order_amount,o.create_time FROM mk_order o
    WHERE o.del_flag='0' AND o.order_amount>0 ORDER BY o.order_id DESC LIMIT 8`);
  let plCnt = 0, prCnt = 0;
  for (const [i, o] of payOrders.entries()) {
    const d = new Date(o.create_time);
    const amt = Number(o.order_amount);
    for (let k = 1; k <= 2; k++) {
      const part = k === 1 ? 0.5 : 0.5;
      const planAmt = Number((amt * part).toFixed(2));
      const paid = (i % 4 !== 3) || k === 1;
      const ins = await exec(`INSERT INTO mk_payment_plan(order_id,customer_id,period_no,plan_amount,plan_date,actual_amount,actual_date,payment_status,payment_method,overdue_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, 'system',NOW(),?)`,
        [o.order_id, o.customer_id, k, planAmt, fmt(new Date(d.getTime() + k * 30 * 86400000)).slice(0, 10),
          paid ? planAmt : 0, paid ? fmt(new Date(d.getTime() + k * 30 * 86400000 + 2 * 86400000)).slice(0, 10) : null,
          paid ? '2' : '0', paid ? '1' : null, !paid && k === 2 && i % 5 === 0 ? '1' : '0', '按期回款']);
      plCnt++;
      if (paid) {
        await exec(`INSERT INTO mk_payment_record(plan_id,customer_id,this_amount,payment_method,payment_date,confirm_status,confirm_by,confirm_time,create_by,create_time,remark)
          VALUES(?,?,?,?,?,?,?,?, 'system',NOW(),?)`,
          [ins.insertId, o.customer_id, planAmt, '1', fmt(new Date(d.getTime() + k * 30 * 86400000 + 2 * 86400000)).slice(0, 10),
            '1', '财务', fmt(new Date(d.getTime() + k * 30 * 86400000 + 3 * 86400000)), '银行转账回款']);
        prCnt++;
      }
    }
  }
  console.log(`回款计划 ${plCnt} 期 / 回款记录 ${prCnt} 笔`);

  console.log(await q(`SELECT (SELECT COUNT(*) FROM mk_lead) 线索,(SELECT COUNT(*) FROM mk_contact) 联系人,
    (SELECT COUNT(*) FROM mk_opportunity) 商机,(SELECT COUNT(*) FROM mk_activity) 活动,
    (SELECT COUNT(*) FROM mk_order_return) 退货,(SELECT COUNT(*) FROM mk_payment_plan) 回款计划`));
  await conn.end();
})().catch(e => { console.error(e); process.exit(1); });
