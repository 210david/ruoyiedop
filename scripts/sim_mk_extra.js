/**
 * 营销 CRM 补全：渠道 / 客户标签 / 互动记录 / 客户变更 / 合同审批与变更 / 销售发票 /
 * 退货明细 / 内容与素材 / 渠道投放包 / 投放数据 / 活动渠道与状态轨迹 / 经销商档案与流向 / 价格政策 / 返利
 * 依据：11 家客户、22 位联系人、12 个商机、5 场活动、50 张销售订单、3 张销售合同、3 张销售退货
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

(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  const tables = ['mk_channel', 'mk_customer_tag', 'mk_customer_tag_rel', 'mk_interaction', 'mk_customer_change_log',
    'mk_contract_approve_log', 'mk_contract_change_log', 'mk_invoice', 'mk_order_return_item',
    'mk_material', 'mk_content', 'mk_content_audit_log', 'mk_content_package', 'mk_promotion_data',
    'mk_activity_channel_link', 'mk_activity_status_log', 'mk_dealer_profile', 'mk_dealer_sell_flow',
    'mk_price_policy', 'mk_rebate_policy', 'mk_rebate_settle', 'mk_rebate_offset'];
  for (const t of tables) {
    await exec(`DELETE FROM \`${t}\``);
    await exec(`ALTER TABLE \`${t}\` AUTO_INCREMENT=1`);
  }

  // ================= 1. 营销渠道 =================
  const channels = [
    ['0', 'ylsp_food', '抖音号 ylsp_food', 'https://www.douyin.com/user/ylsp_food', 128600],
    ['1', '云南赢隆食品', '视频号 sph_ylsp', 'https://channels.weixin.qq.com/ylsp', 46200],
    ['2', '赢隆食品旗舰', '小红书号 6241830', 'https://www.xiaohongshu.com/user/6241830', 32800],
    ['3', '赢隆食品', 'gh_8f3c21a7b9d0', 'https://mp.weixin.qq.com/s/ylsp-home', 18500],
    ['4', '赢隆食品官网商城', 'site_ylsp', 'https://www.yinglong-food.cn', 0],
    ['5', '赢隆食品微博', 'weibo_ylsp', 'https://weibo.com/ylspfood', 9700]
  ];
  const chIds = [];
  for (const [i, c] of channels.entries()) {
    const ins = await exec(`INSERT INTO mk_channel(channel_type,account_name,account_id,account_url,fans_count,user_id,channel_status,
        del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,7,'0','0','admin',?,?)`,
      [c[0], c[1], c[2], c[3], c[4], fmt(new Date(2026, 0, 6 + i * 3, 10, 0, 0)), '品牌自运营账号']);
    chIds.push(ins.insertId);
  }
  console.log(`营销渠道 ${chIds.length} 个`);

  // ================= 2. 客户标签 =================
  const tagDefs = [
    ['高潜力', '0', '#409EFF'], ['价格敏感', '0', '#F56C6C'], ['连锁商超', '1', '#67C23A'],
    ['区域龙头', '0', '#E6A23C'], ['年度合约', '0', '#909399'], ['试单客户', '1', '#B37FEB'],
    ['餐饮渠道', '1', '#36CFC9'], ['电商渠道', '1', '#FF7A45'], ['冷链要求', '1', '#2F54EB'],
    ['回款及时', '1', '#52C41A'], ['需重点维护', '0', '#FA541C'], ['流失预警', '1', '#8C8C8C']
  ];
  const tagIds = [];
  for (const [i, t] of tagDefs.entries()) {
    const ins = await exec(`INSERT INTO mk_customer_tag(tag_name,tag_type,tag_color,sort,status,create_by,create_time,remark)
      VALUES(?,?,?,?, '0','admin',?,?)`,
      [t[0], t[1], t[2], i + 1, fmt(new Date(2026, 0, 8, 9, 30, 0)), t[1] === '1' ? '按规则自动打标' : '手工维护标签']);
    tagIds.push(ins.insertId);
  }
  const tagRel = {
    1: ['连锁商超', '高潜力', '年度合约', '回款及时'],
    2: ['连锁商超', '高潜力', '冷链要求'],
    3: ['连锁商超', '价格敏感', '冷链要求'],
    4: ['连锁商超', '需重点维护', '冷链要求'],
    5: ['餐饮渠道', '试单客户', '价格敏感'],
    6: ['区域龙头', '年度合约', '回款及时'],
    7: ['区域龙头', '价格敏感', '需重点维护'],
    8: ['区域龙头', '试单客户'],
    9: ['高潜力', '电商渠道', '年度合约', '回款及时'],
    10: ['试单客户', '流失预警'],
    11: ['区域龙头', '高潜力', '年度合约']
  };
  let relCnt = 0;
  for (const [cid, names] of Object.entries(tagRel)) {
    for (const n of names) {
      const idx = tagDefs.findIndex(t => t[0] === n);
      await exec(`INSERT INTO mk_customer_tag_rel(customer_id,tag_id,create_time) VALUES(?,?,?)`,
        [cid, tagIds[idx], fmt(new Date(2026, 1, 12, 10, 0, 0))]);
      relCnt++;
    }
  }
  console.log(`客户标签 ${tagIds.length} 个 / 标签关联 ${relCnt} 条`);

  // ================= 3. 客户互动记录 =================
  const customers = await q(`SELECT customer_id, customer_no, customer_name, customer_type, customer_level, total_amount FROM mk_customer ORDER BY customer_id`);
  const contacts = await q(`SELECT contact_id, customer_id, name, position FROM mk_contact ORDER BY contact_id`);
  const opps = await q(`SELECT opportunity_id, customer_id, opportunity_name FROM mk_opportunity`);
  const interTypes = ['0', '1', '2', '3', '4'];
  const contentOf = (type, c, con, opp) => {
    const nm = con ? con.name : '客户';
    switch (type) {
      case '0': return `电话联系${nm}（${con ? con.position : '采购'}），确认${c.customer_name} 12 月备货节奏与到货窗口，对方关注腊味礼盒供货能力`;
      case '1': return `上门拜访${c.customer_name}，与${nm}面谈年度框架条款，现场查看冷柜陈列与竞品价格带`;
      case '2': return `邮件发送${opp ? opp.opportunity_name : '产品报价单'}及检验报告，抄送${nm}，请其内部评审`;
      case '3': return `视频会议复盘${opp ? opp.opportunity_name : '合作进展'}，明确样品的送样时间与验收标准`;
      default: return `微信同步新品资料与促销政策，${nm}反馈终端动销良好，希望增加小规格包装`;
    }
  };
  const nextOf = i => [
    '寄送腊味礼盒样品并跟进试吃反馈', '安排品控人员到厂审核', '提交年度框架合同草稿', '确认首批订单数量与交期',
    '跟进回款并核对对账单', '邀请参加经销商大会'
  ][i % 6];
  let interCnt = 0;
  for (let i = 0; i < 62; i++) {
    const c = customers[i % customers.length];
    const cs = contacts.filter(x => x.customer_id === c.customer_id);
    const con = cs.length ? cs[i % cs.length] : null;
    const op = opps.filter(x => x.customer_id === c.customer_id);
    const opp = op.length && i % 3 === 0 ? op[i % op.length] : null;
    const type = interTypes[i % interTypes.length];
    const day = addDays('2026-06-03', i * 2 + (i % 5));
    const tm = fmt(new Date(day.getFullYear(), day.getMonth(), day.getDate(), 9 + (i % 8), (i * 7) % 60, 0));
    await exec(`INSERT INTO mk_interaction(customer_id,contact_id,opportunity_id,lead_id,interact_type,interact_time,content,
        user_id,next_time,next_content,attachment,del_flag,create_by,create_time)
      VALUES(?,?,?,NULL,?,?,?,7,?,?,?,'0','admin',?)`,
      [c.customer_id, con ? con.contact_id : null, opp ? opp.opportunity_id : null, type, tm,
        contentOf(type, c, con, opp), fmt(addDays(day, 5)), nextOf(i),
        type === '2' ? `/profile/marketing/mail/${2026000 + i}.pdf` : null, tm]);
    interCnt++;
  }
  console.log(`客户互动记录 ${interCnt} 条`);

  // ================= 4. 客户变更日志 =================
  const changeDefs = [
    [1, 'credit_limit', '300000.00', '500000.00', '年度框架签订，授信额度上调', '2026-03-12 10:20:00'],
    [2, 'credit_limit', '250000.00', '400000.00', '连续 6 个月回款及时，上调授信', '2026-04-08 09:40:00'],
    [3, 'customer_level', 'B', 'A', '年采购额突破 80 万，等级上调', '2026-07-02 14:10:00'],
    [4, 'credit_days', '15', '30', '客户要求延长账期，经审批同意', '2026-07-15 11:00:00'],
    [5, 'customer_status', '0', '1', '完成首单交付，转为正式合作客户', '2026-05-20 16:30:00'],
    [7, 'customer_level', 'B', 'C', '季度采购额下滑，等级下调', '2026-09-05 10:00:00'],
    [8, 'customer_level', 'A', 'B', '调整为区域直营，等级下调', '2026-08-19 15:20:00'],
    [9, 'credit_limit', '300000.00', '500000.00', '经销商年度任务达成，授信上调至 50 万', '2026-01-20 09:00:00'],
    [11, 'contact_phone', '13800000000', '13988886666', '客户联系人变更，更新联系方式', '2026-06-11 13:30:00'],
    [10, 'customer_status', '1', '2', '个人客户长期无成交，转入沉默池', '2026-10-09 17:00:00']
  ];
  for (const d of changeDefs) {
    await exec(`INSERT INTO mk_customer_change_log(customer_id,change_type,old_value,new_value,change_reason,
        create_by,create_time,remark)
      VALUES(?,?,?,?,?, 'admin',?,?)`,
      [d[0], d[1], d[2], d[3], d[4], d[5], '经销售经理审批通过']);
  }
  console.log(`客户变更记录 ${changeDefs.length} 条`);

  // ================= 5. 销售合同审批与变更 =================
  const mkContracts = await q(`SELECT contract_id, contract_no, contract_name, contract_amount, sign_date, customer_id FROM mk_contract ORDER BY contract_id`);
  let cLog = 0, cChg = 0;
  for (const [i, c] of mkContracts.entries()) {
    const submit = fmt(new Date(new Date(c.sign_date).getTime() - 3 * 86400000));
    await exec(`INSERT INTO mk_contract_approve_log(contract_id,action_type,approve_opinion,approve_by,approve_time)
      VALUES(?,?,?,?,?)`, [c.contract_id, 'submit', '合同文本与报价单已齐备，提交审批', '林晓', submit]);
    await exec(`INSERT INTO mk_contract_approve_log(contract_id,action_type,approve_opinion,approve_by,approve_time)
      VALUES(?,?,?,?,?)`, [c.contract_id, 'approve',
      i === 2 ? '金额与授信匹配，同意签订，30 天账期按合同执行' : '条款与年度框架一致，同意签订',
      '销售总监', fmt(new Date(new Date(c.sign_date).getTime() - 86400000 / 3))]);
    cLog += 2;
    if (i < 2) {
      const chgType = i === 0 ? '1' : '2';
      await exec(`INSERT INTO mk_contract_change_log(contract_id,contract_no,change_type,field_name,old_value,new_value,
          change_reason,change_status,approve_opinion,approve_by,approve_time,create_by,create_time)
        VALUES(?,?,?,?,?,?,?,?,?,?,?, 'admin',?)`,
        [c.contract_id, c.contract_no, chgType,
          i === 0 ? 'contract_amount' : 'expire_date',
          i === 0 ? MONEY(c.contract_amount) : '2026-12-31',
          i === 0 ? MONEY(Number(c.contract_amount) + 86000) : '2027-03-31',
          i === 0 ? '客户追加春节礼盒采购量，合同金额上调 8.6 万元' : '客户春节备货延后，合同有效期顺延至 2027 年 3 月 31 日',
          '1', '变更事由合理，金额在原授权范围内，同意变更', '销售总监',
          fmt(new Date('2026-09-20 10:30:00')), fmt(new Date('2026-09-18 15:00:00'))]);
      cChg++;
    }
  }
  console.log(`销售合同审批 ${cLog} 条 / 变更 ${cChg} 条`);

  // ================= 6. 销售发票 =================
  const orders = await q(`SELECT o.order_id, o.order_no, o.customer_id, o.order_amount, o.contract_id,
      c.customer_name, c.credit_code
    FROM mk_order o LEFT JOIN mk_customer c ON c.customer_id=o.customer_id
    WHERE o.del_flag='0' ORDER BY o.order_no`);
  let invCnt = 0;
  const invOf = new Map();
  for (const [i, o] of orders.entries()) {
    if (i % 5 === 4) continue;                        // 约 20% 订单尚未开票
    const od = o.order_no.slice(2, 10);               // XS20261105001 → 20261105
    const invDate = dstr(addDays(`${od.slice(0, 4)}-${od.slice(4, 6)}-${od.slice(6, 8)}`, 2 + (i % 6)));
    const amount = Number(o.order_amount);
    const tax = MONEY(amount / 1.13 * 0.13);
    const ct = mkContracts.find(x => x.customer_id === o.customer_id);
    invCnt++;
    const no = `XSFP${invDate.replace(/-/g, '')}${pad(invCnt)}`;
    const ins = await exec(`INSERT INTO mk_invoice(invoice_no,order_id,order_no,contract_id,contract_no,customer_id,customer_name,
        invoice_type,invoice_status,invoice_amount,tax_rate,tax_amount,invoice_title,tax_no,invoice_date,invoice_attachment,
        del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,13.00,?,?,?,?,?, '0','admin',?,?)`,
      [no, o.order_id, o.order_no, ct ? ct.contract_id : null, ct ? ct.contract_no : null,
        o.customer_id, o.customer_name, i % 7 === 0 ? '1' : '0', i % 11 === 10 ? '2' : '1',
        amount, tax, o.customer_name, o.credit_code || '91530100MA6K000000',
        invDate, `/profile/marketing/invoice/${no}.pdf`, fmt(new Date(invDate + 'T11:00:00')),
        i % 11 === 10 ? '客户信息有误，已作废重开' : '按订单金额开具增值税发票']);
    invOf.set(o.order_id, ins.insertId);
  }
  // 待开票订单
  for (const [i, o] of orders.entries()) {
    if (i % 5 !== 4) continue;
    invCnt++;
    const ins = await exec(`INSERT INTO mk_invoice(invoice_no,order_id,order_no,contract_id,contract_no,customer_id,customer_name,
        invoice_type,invoice_status,invoice_amount,tax_rate,tax_amount,invoice_title,tax_no,invoice_date,invoice_attachment,
        del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?, '0',?,13.00,?,?,?,NULL,NULL, '0','admin',?,?)`,
      [`XSFP${o.order_no.slice(2, 10)}${pad(invCnt)}`, o.order_id, o.order_no,
        null, null, o.customer_id, o.customer_name, '0', Number(o.order_amount),
        MONEY(Number(o.order_amount) / 1.13 * 0.13), o.customer_name, o.credit_code || '91530100MA6K000000',
        fmt(new Date()), '客户要求合并开票，待确认开票信息']);
    invOf.set(o.order_id, ins.insertId);
  }
  console.log(`销售发票 ${invCnt} 张`);

  // ================= 7. 销售退货明细 =================
  const returns = await q(`SELECT return_id, return_no, order_id, order_no, total_qty, return_amount, return_reason, customer_id FROM mk_order_return ORDER BY return_id`);
  let retItem = 0;
  for (const r of returns) {
    const items = await q(`SELECT item_id, product_name, product_spec, unit, quantity, shipped_qty, unit_price, subtotal
      FROM mk_order_item WHERE order_id=? ORDER BY line_no`, [r.order_id]);
    const ratio = Number(r.total_qty) / (items.reduce((a, b) => a + Number(b.quantity), 0) || 1);
    for (const [i, it] of items.entries()) {
      const rq = i === 0 ? Number(r.total_qty) : MONEY(Number(it.quantity) * ratio * 0.4);
      if (rq <= 0) continue;
      await exec(`INSERT INTO mk_order_return_item(return_id,order_item_id,product_name,product_spec,unit,order_qty,ship_qty,
          return_qty,unit_price,return_amount,return_reason,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?,?, '0','system',?,?)`,
        [r.return_id, it.item_id, it.product_name, it.product_spec || '', it.unit || '袋',
          Number(it.quantity), Number(it.shipped_qty || it.quantity), MONEY(rq),
          Number(it.unit_price), MONEY(rq * Number(it.unit_price)), r.return_reason,
          fmt(new Date()), i === 0 ? '退货主因商品' : '同批次连带有轻微瑕疵，一并退回']);
      retItem++;
    }
  }
  console.log(`销售退货明细 ${retItem} 行`);

  // ================= 8. 营销素材 =================
  const acts = await q(`SELECT activity_id, activity_no, activity_name, activity_status, start_time, end_time FROM mk_activity ORDER BY activity_id`);
  const matDefs = [
    ['0', '大河乌猪品牌故事文案（长图版）', '文案', '品牌故事,乌猪,非遗工艺'],
    ['1', '腊味礼盒主图组图（5 张）', '图片', '腊味礼盒,春节,主图'],
    ['2', '生产线实拍短视频（30s）', '视频', '工厂溯源,生产线,短视频'],
    ['0', '低温香肠产品卖点脚本', '文案', '低温香肠,卖点,直播'],
    ['1', '经销商大会主视觉', '图片', '经销商大会,主视觉,2027政策'],
    ['2', '客户证言采访视频（2min）', '视频', '客户证言,商超,口碑'],
    ['0', '预制菜行业峰会演讲稿', '文案', '行业峰会,预制菜,演讲'],
    ['3', '展会易拉宝设计稿', '其他', '展会,物料,易拉宝'],
    ['1', '抖音直播间贴片与话术卡', '图片', '直播,贴片,话术'],
    ['2', '产品溯源纪录片（5min）', '视频', '溯源,纪录片,品牌']
  ];
  const matIds = [];
  for (const [i, m] of matDefs.entries()) {
    const act = acts[i % acts.length];
    const ext = m[1] === '视频' ? 'mp4' : (m[1] === '图片' ? 'jpg' : 'pdf');
    const ins = await exec(`INSERT INTO mk_material(material_no,material_type,title,content,file_url,file_name,file_size,
        tags,activity_id,use_count,user_id,material_status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,7,'1','0','admin',?,?)`,
      [`SC2026${pad(i + 1)}`, m[0], m[1], `素材用途：${m[3].split(',')[0]}；已按平台规范完成尺寸与字幕适配。`,
        `/profile/marketing/material/SC2026${pad(i + 1)}.${ext}`, `${m[1]}.${ext}`,
        m[1] === '视频' ? 24 * 1024 * 1024 + i * 1024 * 512 : (m[1] === '图片' ? 1.8 * 1024 * 1024 : 640 * 1024),
        m[2], act.activity_id, 3 + (i * 5) % 40, fmt(new Date(2026, 6, 10 + i, 14, 0, 0)), '活动配套素材']);
    matIds.push(ins.insertId);
  }
  console.log(`营销素材 ${matIds.length} 份`);

  // ================= 9. 内容与审核 =================
  const contentDefs = [
    ['0', '2026 昆明食品博览会｜大河乌猪全系产品亮相', 1, '2'],
    ['1', '为什么高端商超都选乌猪做的腊味？', 0, '2'],
    ['2', '从牧场到餐桌：一块腊肉的 21 天', 3, '2'],
    ['3', '低温香肠怎么选？看这 4 个指标就够了', 2, '2'],
    ['0', '经销商大会剧透：2027 年政策三大变化', 4, '1'],
    ['4', '赢隆食品获评省级农业产业化重点龙头企业', 5, '2'],
    ['1', '春节礼盒开箱｜六款腊味一次尝遍', 3, '3'],
    ['3', '预制菜供应链的冷链断点在哪里', 5, '1'],
    ['2', '客户故事：一家区域商超如何把腊味做成爆款', 2, '0'],
    ['5', '企业开放日报名开启｜走进宣威工厂', 1, '0']
  ];
  const contIds = [];
  for (const [i, c] of contentDefs.entries()) {
    const act = acts[i % acts.length];
    const no = `NR2026${pad(i + 1)}`;
    const pdate = dstr(addDays(new Date(act.start_time), -5 + i));
    const ins = await exec(`INSERT INTO mk_content(content_no,title,content_type,theme,activity_id,plan_publish_date,
        audit_status,audit_user_id,audit_time,audit_remark,user_id,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,7,'0','admin',?,?)`,
      [no, c[1], c[0], c[1], act.activity_id, pdate, c[3],
        c[3] === '0' ? null : 7, c[3] === '0' ? null : fmt(new Date(addDays(pdate, -2))),
        c[3] === '0' ? null : (c[3] === '3' ? '卖点表述涉及绝对化用语，需修改后重新提审' : '内容合规，与产品资料一致，同意发布'),
        fmt(new Date(addDays(pdate, -4))), `围绕「${act.activity_name}」的内容排期`]);
    contIds.push({ id: ins.insertId, status: c[3], pdate, act: act.activity_id });
    if (c[3] !== '0') {
      await exec(`INSERT INTO mk_content_audit_log(content_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
        [ins.insertId, '1', '林晓', fmt(new Date(addDays(pdate, -3))), '内容已按品牌规范校对，提交审核']);
      await exec(`INSERT INTO mk_content_audit_log(content_id,audit_action,audit_by,audit_time,audit_remark) VALUES(?,?,?,?,?)`,
        [ins.insertId, c[3] === '3' ? '3' : '2', '品牌经理', fmt(new Date(addDays(pdate, -2))),
          c[3] === '3' ? '卖点表述涉及绝对化用语，需修改后重新提审' : '内容合规，与产品资料一致，同意发布']);
    }
  }
  console.log(`营销内容 ${contIds.length} 篇`);

  // ================= 10. 渠道投放包 + 投放数据 =================
  let pkgCnt = 0, promoCnt = 0;
  const platTag = ['#腊味', '#年货节', '#大河乌猪', '#云南特产', '#预制菜', '#工厂溯源'];
  for (const [i, c] of contIds.entries()) {
    const cnt = c.status === '2' ? 3 : (c.status === '1' ? 2 : 1);
    for (let k = 0; k < cnt; k++) {
      const ch = chIds[(i + k) % chIds.length];
      const chType = channels[(i + k) % channels.length][0];
      pkgCnt++;
      const published = c.status === '2' ? 1 : 0;
      const pubTime = published ? fmt(new Date(new Date(c.pdate + 'T10:00:00').getTime() + k * 3600000)) : null;
      const ins = await exec(`INSERT INTO mk_content_package(content_id,channel_id,channel_type,package_title,package_body,topic_tags,
          cover_url,material_ids,package_status,publish_url,publish_time,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?,?, '0','admin',?,?)`,
        [c.id, ch, chType,
          `${contentDefs[i][1]}（${['抖音', '视频号', '小红书', '公众号', '官网', '微博'][(i + k) % 6]}版）`,
          `标题：${contentDefs[i][1]}\n正文：${contentDefs[i][1].replace(/｜/g, '，')}。云南赢隆食品，从牧场到餐桌全程可追溯。\n行动号召：点击了解详情 / 私信咨询渠道合作。`,
          platTag[(i + k) % platTag.length] + ' ' + platTag[(i + k + 2) % platTag.length],
          `/profile/marketing/cover/${2026000 + pkgCnt}.jpg`,
          matIds.slice(i % 5, i % 5 + 2).join(','),
          published ? '1' : '0',
          published ? `https://www.yinglong-food.cn/p/${2026000 + pkgCnt}` : null,
          pubTime, fmt(new Date(addDays(c.pdate, -1))), '按渠道规范改写后的投放版本']);
      if (!published) continue;
      // 投放数据：发布后 10 天
      let base = 8000 + (pkgCnt * 1370) % 26000;
      for (let d = 0; d < 10; d++) {
        const exp = Math.round(base * (1 - d * 0.07) * (0.9 + ((d * 13) % 10) / 50));
        const read = Math.round(exp * (0.28 + ((d * 7) % 9) / 100));
        const like = Math.round(read * (0.05 + ((d * 3) % 5) / 100));
        const cmt = Math.round(like * 0.12);
        const lead = Math.round(read * 0.006) + (d % 4 === 0 ? 1 : 0);
        await exec(`INSERT INTO mk_promotion_data(package_id,data_date,exposure_count,read_count,like_count,comment_count,lead_count,
            del_flag,create_by,create_time,remark)
          VALUES(?,?,?,?,?,?,?, '0','system',?,?)`,
          [ins.insertId, dstr(addDays(pubTime.slice(0, 10), d)), exp, read, like, cmt, lead,
            fmt(new Date(addDays(pubTime.slice(0, 10), d + 1))), '平台后台数据每日同步']);
        promoCnt++;
      }
    }
  }
  console.log(`渠道投放包 ${pkgCnt} 个 / 投放数据 ${promoCnt} 条`);

  // ================= 11. 活动渠道链接 + 状态轨迹 =================
  const statusFlow = { '0': [], '1': [['0', '1', '活动立项完成，开放报名']], '2': [['0', '1', '开放报名'], ['1', '2', '活动开始，签到进行中']], '3': [['0', '1', '活动立项，开放报名'], ['1', '2', '活动开始'], ['2', '3', '活动结束，已归档复盘']] };
  let linkCnt = 0, statusCnt = 0;
  for (const [i, a] of acts.entries()) {
    const chN = 2 + (i % 2);
    for (let k = 0; k < chN; k++) {
      const ch = chIds[(i + k) % chIds.length];
      const click = 1200 + ((i + 1) * (k + 3) * 421) % 9000;
      const signup = Math.round(click * (0.08 + k * 0.02));
      const lead = Math.round(signup * (0.35 + k * 0.05));
      await exec(`INSERT INTO mk_activity_channel_link(activity_id,channel_id,channel_code,click_count,signup_count,lead_count,
          del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?, '0','admin',?,?)`,
        [a.activity_id, ch, `${a.activity_no}-C${k + 1}`, click, signup, lead,
          fmt(new Date(a.start_time)), `活动推广渠道 ${['抖音', '视频号', '小红书', '公众号', '官网', '微博'][(i + k) % 6]}`]);
      linkCnt++;
    }
    const flow = statusFlow[String(a.activity_status)] || [];
    for (const [f, t, rm] of flow) {
      await exec(`INSERT INTO mk_activity_status_log(activity_id,from_status,to_status,operator_id,operator_name,remark,create_time)
        VALUES(?,?,?,?,?,?,?)`,
        [a.activity_id, f, t, 7, '林晓', rm, fmt(new Date(new Date(a.start_time).getTime() - (t === '1' ? 20 : 0) * 86400000))]);
      statusCnt++;
    }
  }
  console.log(`活动渠道链接 ${linkCnt} 条 / 状态轨迹 ${statusCnt} 条`);

  // ================= 12. 经销商档案 =================
  const dealerDefs = [
    [9, '1', '上海市、江苏省', '腌腊制品、香肠制品', 200000, '2023-03-01', '2027-02-28'],
    [11, '1', '云南省曲靖市、昆明市', '全品类', 150000, '2024-06-01', '2027-05-31'],
    [7, '2', '云南省昆明市', '香肠制品、酱卤制品', 50000, '2025-01-10', '2027-01-09'],
    [8, '2', '云南省曲靖市', '腌腊制品', 30000, '2025-09-01', '2026-12-31'],
    [3, '2', '江苏省南京市', '低温香肠', 60000, '2026-02-20', '2027-02-19']
  ];
  for (const d of dealerDefs) {
    await exec(`INSERT INTO mk_dealer_profile(customer_id,dealer_level,auth_regions,auth_categories,deposit_amount,
        coop_start_date,coop_end_date,status,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?, '0','admin',?,?)`,
      [d[0], d[1], d[2], d[3], d[4], d[5], d[6], fmt(new Date(d[5] + 'T09:00:00')),
        d[1] === '1' ? '一级经销商，可发展下级分销' : '二级经销商，仅限授权区域零售终端']);
  }
  console.log(`经销商档案 ${dealerDefs.length} 家`);

  // ================= 13. 经销商流向 =================
  const batches = await q(`SELECT batch_no, product_id, product_code, product_name, finish_qty, finish_time
    FROM mms_finish_receipt WHERE batch_no IS NOT NULL AND batch_no<>'' AND del_flag='0'
    ORDER BY finish_time DESC LIMIT 40`);
  const dealerIds = dealerDefs.map(d => d[0]);
  const downstream = [['1', '上海联华生鲜配送中心'], ['1', '北京京客隆配送中心'], ['2', '南京苏果马群店'],
    ['2', '杭州世纪联华文一店'], ['2', '昆明锦华食品批发商行'], ['2', '曲靖优选食材前置仓'],
    ['2', '苏州 foodie 中央厨房'], ['2', '富源县农特产品直营店']];
  let flowCnt = 0;
  for (const [i, b] of batches.entries()) {
    if (i >= 32) break;
    const dealer = dealerIds[i % dealerIds.length];
    const mat = await q(`SELECT material_id, unit FROM wms_material WHERE material_code=? LIMIT 1`, [b.product_code]);
    if (!mat.length) continue;
    const ds = downstream[i % downstream.length];
    const day = dstr(addDays(b.finish_time || '2026-10-20', 1 + (i % 5)));
    await exec(`INSERT INTO mk_dealer_sell_flow(dealer_id,flow_date,material_id,batch_no,quantity,downstream_type,downstream_name,
        shipment_id,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,NULL,'admin',?,?)`,
      [dealer, day, mat[0].material_id, b.batch_no,
        MONEY(Number(b.finish_qty) * (0.3 + (i % 4) * 0.1)), ds[0], ds[1],
        fmt(new Date(day + 'T15:30:00')), '经销商上报流向，含批次追溯信息']);
    flowCnt++;
  }
  console.log(`经销商流向 ${flowCnt} 条`);

  // ================= 14. 价格政策 =================
  const prods = await q(`SELECT DISTINCT i.material_id, i.material_code, i.product_name, i.product_spec, i.unit_price
    FROM mk_order_item i WHERE i.material_id IS NOT NULL ORDER BY i.material_id LIMIT 12`);
  let priceCnt = 0;
  for (const [i, p] of prods.entries()) {
    const base = Number(p.unit_price);
    // 客户专价（A 级客户）
    await exec(`INSERT INTO mk_price_policy(policy_no,policy_name,price_type,customer_id,customer_level,material_id,unit_price,
        start_date,end_date,status,del_flag,create_by,create_time,remark)
      VALUES(?,?, '1',?,?,?,?,?,?, '0','0','admin',?,?)`,
      [`JG2026${pad(priceCnt + 1)}`, `${p.product_name} 上海滇胤专价`, 9, null, p.material_id,
        MONEY(base * 0.94), '2026-01-01', '2026-12-31', fmt(new Date('2026-01-02 09:00:00')), '战略客户专价，较目录价下浮 6%']);
    priceCnt++;
    if (i % 2 === 0) {
      await exec(`INSERT INTO mk_price_policy(policy_no,policy_name,price_type,customer_id,customer_level,material_id,unit_price,
          start_date,end_date,status,del_flag,create_by,create_time,remark)
        VALUES(?,?, '2',NULL,?,?,?,?,?, '0','0','admin',?,?)`,
        [`JG2026${pad(priceCnt + 1)}`, `${p.product_name} A/B 级客户等级价`, 'A', p.material_id,
          MONEY(base * 0.97), '2026-01-01', '2026-12-31', fmt(new Date('2026-01-02 09:10:00')), 'A 级客户下浮 3%，B 级按目录价']);
      priceCnt++;
    }
    if (i % 3 === 0) {
      await exec(`INSERT INTO mk_price_policy(policy_no,policy_name,price_type,customer_id,customer_level,material_id,unit_price,
          start_date,end_date,status,del_flag,create_by,create_time,remark)
        VALUES(?,?, '3',NULL,NULL,?,?,?,?, '0','0','admin',?,?)`,
        [`JG2026${pad(priceCnt + 1)}`, `${p.product_name} 经销商供货价`, p.material_id,
          MONEY(base * 0.86), '2026-01-01', '2026-12-31', fmt(new Date('2026-01-02 09:20:00')), '一级经销商供货价，含运费，账期 30 天']);
      priceCnt++;
    }
  }
  console.log(`价格政策 ${priceCnt} 条`);

  // ================= 15. 返利政策 / 结算 / 冲抵 =================
  const rebatePolicies = [
    { no: 'FL2026001', name: '上海滇胤年度销售返利', cust: 9, basis: '1', period: '3', start: '2026-01-01', end: '2026-12-31',
      tiers: [[0, 500000, 1.5], [500000, 1000000, 2.0], [1000000, null, 3.0]] },
    { no: 'FL2026002', name: '新农垦年度销售返利', cust: 11, basis: '1', period: '3', start: '2026-01-01', end: '2026-12-31',
      tiers: [[0, 300000, 1.0], [300000, 600000, 1.5], [600000, null, 2.0]] },
    { no: 'FL2026003', name: '雷景华年度销售返利', cust: 10, basis: '1', period: '3', start: '2026-01-01', end: '2026-12-31',
      tiers: [[0, 10000, 1.0], [10000, 50000, 1.5], [50000, null, 2.0]] }
  ];
  for (const p of rebatePolicies) {
    for (const [i, t] of p.tiers.entries()) {
      await exec(`INSERT INTO mk_rebate_policy(policy_no,policy_name,customer_id,basis,period_type,tier_min,tier_max,rebate_rate,
          start_date,end_date,status,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,?,?,?,?, '0','0','admin',?,?)`,
        [p.no, `${p.name}（第 ${i + 1} 档）`, p.cust, p.basis, p.period, t[0], t[1], t[2], p.start, p.end,
          fmt(new Date('2026-01-05 09:30:00')),
          t[1] === null ? `累计达成 ≥ ${t[0].toLocaleString()} 元部分按 ${t[2]}% 返利` : `累计达成 ${t[0].toLocaleString()}~${t[1].toLocaleString()} 元按 ${t[2]}% 返利`]);
    }
  }
  // 按月度达成金额结算
  const achieve = await q(`SELECT customer_id, SUBSTRING(order_no,3,6) ym, SUM(order_amount) amt, COUNT(*) c
    FROM mk_order WHERE del_flag='0' GROUP BY customer_id, ym HAVING amt > 0 ORDER BY ym`);
  let settleCnt = 0, offsetCnt = 0;
  for (const a of achieve) {
    const pol = rebatePolicies.find(p => p.cust === a.customer_id);
    if (!pol) continue;
    const amt = Number(a.amt);
    const tier = [...pol.tiers].reverse().find(t => amt >= t[0]) || pol.tiers[0];
    const rate = tier[2];
    const rebate = MONEY(amt * rate / 100);
    const settled = a.ym <= '202609';
    settleCnt++;
    const ins = await exec(`INSERT INTO mk_rebate_settle(settle_no,customer_id,customer_name,period,policy_no,policy_name,
        achieve_amount,rebate_rate,rebate_amount,status,redeem_type,redeem_date,redeem_by,del_flag,create_by,create_time,
        remark,offset_status,offset_amount)
      VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?, '0','admin',?,?,?,?)`,
      [`FLJS${a.ym}${pad(settleCnt, 2)}`, a.customer_id,
        (customers.find(c => c.customer_id === a.customer_id) || {}).customer_name || '',
        a.ym, pol.no, pol.name, amt, rate, rebate,
        settled ? '1' : '0',
        settled ? (settleCnt % 2 === 0 ? '货品冲抵' : '现金返还') : null,
        settled ? fmt(new Date(`${a.ym.slice(0, 4)}-${a.ym.slice(4, 6)}-28 10:00:00`)) : null,
        settled ? '财务主管' : null,
        fmt(new Date(`${a.ym.slice(0, 4)}-${a.ym.slice(4, 6)}-05 09:00:00`)),
        `${a.ym} 月达成 ${amt.toFixed(2)} 元，${a.c} 笔订单，适用 ${rate}% 返利率`,
        settled && settleCnt % 2 === 0 ? '1' : '0', settled && settleCnt % 2 === 0 ? rebate : 0]);
    if (settled && settleCnt % 2 === 0) {
      for (let k = 0; k < 1; k++) {
        await exec(`INSERT INTO mk_rebate_offset(settle_id,offset_amount,offset_date,audit_status,audit_by,audit_time,
            audit_opinion,msg_id,create_by,create_time,remark)
          VALUES(?,?,?, '1','财务主管',?,?,?, 'admin',?,?)`,
          [ins.insertId, rebate, dstr(addDays(`${a.ym.slice(0, 4)}-${a.ym.slice(4, 6)}-28`, 3)),
            fmt(new Date(`${a.ym.slice(0, 4)}-${a.ym.slice(4, 6)}-29 15:00:00`)),
            '返利以货品形式冲抵下期货款，已与客户确认', 800000 + settleCnt * 10 + k,
            fmt(new Date(`${a.ym.slice(0, 4)}-${a.ym.slice(4, 6)}-28 11:00:00`)), '冲抵当期应收']);
        offsetCnt++;
      }
    }
  }
  console.log(`返利政策 ${rebatePolicies.reduce((a, b) => a + b.tiers.length, 0)} 档 / 结算 ${settleCnt} 笔 / 冲抵 ${offsetCnt} 笔`);

  // ================= 16. 活动状态校正（未开始的活动不应为已结束） =================
  await exec(`UPDATE mk_activity SET activity_status='1', actual_count=0, lead_count=0, mql_count=0, sql_count=0,
      opportunity_count=0, deal_amount=0, cost=ROUND(budget*0.35,2),
      summary='报名中，物料与场地已确认，筹备执行阶段'
    WHERE start_time > '2026-11-16 00:00:00'`);
  const [fix] = await q(`SELECT COUNT(*) c FROM mk_activity WHERE start_time > '2026-11-16 00:00:00'`);
  console.log(`活动状态校正：${fix.c} 场未开始活动改为「报名中」`);

  const summary = await q(`SELECT 'mk_channel' t, COUNT(*) c FROM mk_channel UNION ALL
    SELECT 'mk_invoice', COUNT(*) FROM mk_invoice UNION ALL
    SELECT 'mk_interaction', COUNT(*) FROM mk_interaction UNION ALL
    SELECT 'mk_content_package', COUNT(*) FROM mk_content_package UNION ALL
    SELECT 'mk_promotion_data', COUNT(*) FROM mk_promotion_data`);
  console.table(summary);
  await conn.end();
})().catch(e => { console.error('ERR', e.message); process.exit(1); });
