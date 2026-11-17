/**
 * 招投标 → 资质资料库：清理旧模拟数据，录入真实证照（带原件附件）
 * 数据来源：赢隆资质（营业执照+生产许可）.pdf（4 页扫描件）
 * 幂等：按 cert_no 判重，可重复执行
 */
const mysql = require('mysql2/promise');

const DB = {
  host: '127.0.0.1', port: 3306, user: 'root', password: 'ZBrj@2026',
  database: 'ruoyi_ylsp', dateStrings: true, charset: 'utf8mb4'
};

const FILE_URL = '/profile/upload/2026/11/17/赢隆资质（营业执照、生产许可）.pdf';
const DEPT_ID = 103;

const ROWS = [
  {
    qualType: '1',
    name: '营业执照',
    certNo: '91530325MA7K3QA92L',
    qualLevel: '有限责任公司',
    issueDate: '2024-04-03',
    expireDate: null,
    warnDays: 60,
    remark: '登记机关：富源县行政审批局；注册资本壹仟万元整；成立日期 2022-03-10；'
      + '住所：云南省曲靖市富源县胜境街道后煤矿社区煤炭湾生活区；法定代表人：丰孟。'
      + '原件为《赢隆资质（营业执照、生产许可）.pdf》第 1 页。'
  },
  {
    qualType: '1',
    name: '食品生产许可证（SC）',
    certNo: 'SC10453032500205',
    qualLevel: '调味品、肉制品',
    issueDate: '2025-07-18',
    expireDate: '2027-10-30',
    warnDays: 90,
    remark: '发证机关：曲靖市市场监督管理局。许可品种：'
      + '0305 半固态(酱)调味料[复合调味酱]；0401 酱卤肉制品[酱卤肉类]、热加工肉灌制品[灌肠类]；'
      + '0402 发酵灌制品；0404 腌腊肉灌制品、腊肉制品、火腿制品。'
      + '原件为《赢隆资质（营业执照、生产许可）.pdf》第 2-4 页。'
  }
];

(async () => {
  const conn = await mysql.createConnection(DB);
  const q = async (sql, p = []) => (await conn.query(sql, p))[0];
  const x = async (sql, p = []) => (await conn.query(sql, p))[0];

  // 1) 备份旧数据
  const old = await q('SELECT * FROM bid_qualification');
  if (old.length) {
    await x('DROP TABLE IF EXISTS bid_qualification_bak20261117');
    await x('CREATE TABLE bid_qualification_bak20261117 LIKE bid_qualification');
    await x('INSERT INTO bid_qualification_bak20261117 SELECT * FROM bid_qualification');
    console.log(`旧资质已备份 ${old.length} 条 → bid_qualification_bak20261117`);
    old.forEach(r => console.log(`  - [${r.qual_type}] ${r.name} / ${r.cert_no || '无编号'}`));
  }

  // 2) 清空
  await x('DELETE FROM bid_qualification');
  console.log('已清空 bid_qualification');

  // 3) 录入真实证照
  let n = 0;
  for (const r of ROWS) {
    const dup = await q('SELECT qualification_id FROM bid_qualification WHERE cert_no = ?', [r.certNo]);
    if (dup.length) { console.log(`跳过（已存在）：${r.name}`); continue; }
    await x(`INSERT INTO bid_qualification
      (qual_type, name, cert_no, qual_level, owner_type, owner_user_id, owner_name,
       issue_date, expire_date, warn_days, file_url, dept_id, del_flag, create_by, create_time, remark)
      VALUES (?,?,?,?,'1',NULL,NULL,?,?,?,?,?,'0','admin',NOW(),?)`,
      [r.qualType, r.name, r.certNo, r.qualLevel, r.issueDate, r.expireDate,
        r.warnDays, FILE_URL, DEPT_ID, r.remark]);
    n++;
    console.log(`+ ${r.name} | ${r.certNo} | ${r.issueDate} → ${r.expireDate || '长期'}`);
  }
  console.log(`新增 ${n} 条`);

  // 4) 校验
  const chk = await q(`SELECT qualification_id, qual_type, name, cert_no, qual_level,
      owner_type, issue_date, expire_date, warn_days, file_url, dept_id, del_flag
    FROM bid_qualification ORDER BY qualification_id`);
  console.log('\n当前资质库：');
  chk.forEach(r => console.log(`  #${r.qualification_id} [${r.qual_type}] ${r.name} | ${r.cert_no} | `
    + `${r.issue_date} → ${r.expire_date || '长期'} | warn ${r.warn_days} | ${r.file_url}`));

  await conn.end();
})().catch(e => { console.error('FAILED:', e.message); process.exit(1); });
