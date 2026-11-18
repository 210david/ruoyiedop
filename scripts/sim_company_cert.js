/**
 * 企业档案 + 证照管理 录入（依据《营业执照》《食品生产许可证》PDF）
 * 表：biz_safety_enterprise / biz_safety_cert
 * 附件：/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf
 */
const mysql = require('mysql2/promise');

const PDF = '/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf';

const ENTERPRISE = {
  enterprise_name: '云南赢隆食品加工有限公司',
  credit_code: '91530325MA7K3QA92L',
  industry_type: '5',            // 高原特色食品加工
  is_high_risk: '0',
  legal_person: '丰孟',
  safety_manager_id: 20,         // 邓楠（安全生产办公室）
  safety_manager: '邓楠',
  safety_org: '安全生产办公室',
  supervise_dept: '富源县应急管理局',
  responsibility: '总经理（法定代表人）为本单位安全生产与食品安全第一责任人；设安全生产办公室并配备专职安全管理人员，实行“一岗双责”，各车间负责人为本区域直接责任人，逐级签订安全生产责任书。',
  remark: '统一社会信用代码 91530325MA7K3QA92L；类型：有限责任公司；注册资本壹仟万元整；成立日期 2022-03-10。' +
    '经营范围：许可项目—食品生产、食品销售、牲畜饲养；一般项目—鲜肉批发，农产品的生产、销售、加工、运输、贮藏及其他相关服务，食用农产品批发，牲畜销售，畜禽收购。' +
    '资料来源：《营业执照》（2024-04-03 换发）、食品生产许可证 SC10453032500205。',
};

const CERTS = [
  {
    cert_name: '营业执照',
    cert_no: '91530325MA7K3QA92L',
    cert_type: '4',
    issue_org: '富源县行政审批局',
    issue_date: '2024-04-03',
    expire_date: '2099-12-31',
    remark: '类型：有限责任公司；注册资本壹仟万元整；成立日期 2022-03-10；' +
      '住所：云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区；营业期限：长期（证照副本未标注终止日期）。',
  },
  {
    cert_name: '食品生产许可证',
    cert_no: 'SC10453032500205',
    cert_type: '7',
    issue_org: '曲靖市市场监督管理局',
    issue_date: '2025-07-18',
    expire_date: '2027-10-30',
    remark: '食品类别：调味品、肉制品。品种明细：1. 调味品 0305 调味料—半固态(酱)调味料[复合调味酱]；' +
      '2. 肉制品 0401 热加工熟肉制品—酱卤肉制品[酱卤肉类]、热加工肉灌制品[灌肠类]；' +
      '3. 肉制品 0402 发酵肉制品—发酵灌制品；4. 肉制品 0404 腌腊肉制品—腌腊肉灌制品、腊肉制品、火腿制品。',
  },
];

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true,
  });
  const q = (s, p = []) => conn.query(s, p).then(r => r[0]);
  const exec = (s, p = []) => conn.query(s, p).then(r => r[0]);

  // ---------- 1) 证照类型字典补「食品生产许可证」 ----------
  const [d] = await q(`SELECT dict_code FROM sys_dict_data WHERE dict_type='safety_license_type' AND dict_value='7'`);
  if (!d) {
    await exec(`INSERT INTO sys_dict_data(dict_sort,dict_label,dict_value,dict_type,css_class,list_class,is_default,status,create_by,create_time,remark)
      VALUES(5,'食品生产许可证','7','safety_license_type','','warning','N','0','admin',NOW(),'食品生产企业 SC 证')`);
    console.log('字典新增：safety_license_type / 7 / 食品生产许可证');
  } else {
    console.log('字典已存在：safety_license_type / 7');
  }

  // ---------- 2) 企业档案 ----------
  let [ent] = await q(`SELECT enterprise_id FROM biz_safety_enterprise WHERE credit_code=? AND del_flag='0'`, [ENTERPRISE.credit_code]);
  let entId;
  if (!ent) {
    const r = await exec(`INSERT INTO biz_safety_enterprise(enterprise_name,credit_code,industry_type,is_high_risk,
        legal_person,safety_manager_id,safety_manager,safety_org,supervise_dept,responsibility,
        status,del_flag,create_by,create_time,remark)
      VALUES(?,?,?,?,?,?,?,?,?,?,'0','0','admin',NOW(),?)`,
      [ENTERPRISE.enterprise_name, ENTERPRISE.credit_code, ENTERPRISE.industry_type, ENTERPRISE.is_high_risk,
        ENTERPRISE.legal_person, ENTERPRISE.safety_manager_id, ENTERPRISE.safety_manager, ENTERPRISE.safety_org,
        ENTERPRISE.supervise_dept, ENTERPRISE.responsibility, ENTERPRISE.remark]);
    entId = r.insertId;
    console.log(`企业档案新增：${ENTERPRISE.enterprise_name}（id=${entId}）`);
  } else {
    entId = ent.enterprise_id;
    console.log(`企业档案已存在：id=${entId}，跳过`);
  }

  // ---------- 3) 证照 ----------
  for (const c of CERTS) {
    const [old] = await q(`SELECT cert_id FROM biz_safety_cert WHERE cert_no=? AND del_flag='0'`, [c.cert_no]);
    if (old) {
      await exec(`UPDATE biz_safety_cert SET cert_name=?,cert_type=?,issue_org=?,issue_date=?,expire_date=?,
          enterprise_id=?,attachment=?,remark=?,status='0',update_by='admin',update_time=NOW()
        WHERE cert_id=?`,
        [c.cert_name, c.cert_type, c.issue_org, c.issue_date, c.expire_date, entId, PDF, c.remark, old.cert_id]);
      console.log(`证照更新：${c.cert_name}（id=${old.cert_id}）`);
    } else {
      const r = await exec(`INSERT INTO biz_safety_cert(cert_name,cert_no,cert_type,issue_org,issue_date,expire_date,
          remind_status,enterprise_id,attachment,status,del_flag,create_by,create_time,remark)
        VALUES(?,?,?,?,?,?,'0',?,?,'0','0','admin',NOW(),?)`,
        [c.cert_name, c.cert_no, c.cert_type, c.issue_org, c.issue_date, c.expire_date, entId, PDF, c.remark]);
      console.log(`证照新增：${c.cert_name} / ${c.cert_no}（id=${r.insertId}，有效期至 ${c.expire_date}）`);
    }
  }

  // ---------- 4) 主数据工厂地址与执照保持一致 ----------
  const r4 = await exec(`UPDATE mms_factory SET address=?, update_by='admin', update_time=NOW()
    WHERE factory_name='云南赢隆食品加工有限公司' AND address<>?`,
    ['云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区', '云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区']);
  if (r4.affectedRows) console.log(`mms_factory 地址已校正为执照地址（${r4.affectedRows} 行）`);

  console.log('== 企业档案：', JSON.stringify(await q(`SELECT enterprise_id,enterprise_name,credit_code,industry_type,legal_person,safety_manager,supervise_dept FROM biz_safety_enterprise`)));
  console.log('== 证照：', JSON.stringify(await q(`SELECT cert_id,cert_name,cert_no,cert_type,issue_org,issue_date,expire_date,attachment FROM biz_safety_cert`)));

  await conn.end();
})();
