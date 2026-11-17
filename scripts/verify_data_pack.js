/**
 * 校验数据包：对比源库 ruoyi_ylsp 与验证库 ruoyi_ylsp_verify 中
 * 7 个业务模块前缀表的精确行数（COUNT(*)）。
 */
const mysql = require('mysql2/promise');

const PREFIXES = [
  ['wms', '仓储管理', /^wms_/],
  ['dms', '设备管理', /^dms_/],
  ['mk', '营销管理', /^mk_/],
  ['pms', '采购管理', /^pms_/],
  ['safety', '安全生产', /^biz_safety_/],
  ['qms', '质量管理', /^qms_/],
  ['mms', '生产管控', /^mms_/],
];

const isBak = (t) => /_bak\d{8}[a-z]*$/.test(t) || /_copy\d*$/.test(t) || /_backup\d*$/i.test(t);

(async () => {
  const cfg = { host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', charset: 'utf8mb4' };
  const src = await mysql.createConnection({ ...cfg, database: 'ruoyi_ylsp' });
  const ver = await mysql.createConnection({ ...cfg, database: 'ruoyi_ylsp_verify' });

  const [rows] = await src.query(
    "SELECT table_name AS t FROM information_schema.tables WHERE table_schema='ruoyi_ylsp' AND table_type='BASE TABLE' ORDER BY table_name"
  );
  const allTables = rows.map((r) => r.t);

  let grand = { tables: 0, rows: 0, bad: 0 };
  const problems = [];

  for (const [key, label, re] of PREFIXES) {
    const tables = allTables.filter((t) => re.test(t) && !isBak(t));
    let sum = 0,
      bad = 0;
    for (const t of tables) {
      const [[s]] = await src.query(`SELECT COUNT(*) AS c FROM \`${t}\``);
      const [[v]] = await ver.query(`SELECT COUNT(*) AS c FROM \`${t}\``).catch(() => [[{ c: -1 }]]);
      sum += Number(s.c);
      if (Number(s.c) !== Number(v.c)) {
        bad++;
        problems.push(`  [${label}] ${t}: 源=${s.c} 包=${v.c}`);
      }
    }
    grand.tables += tables.length;
    grand.rows += sum;
    grand.bad += bad;
    console.log(
      `${label.padEnd(8, ' ')} 表 ${String(tables.length).padStart(3)} 张   行 ${String(sum).padStart(6)}   ${
        bad === 0 ? 'OK 全部一致' : `差异 ${bad} 张`
      }`
    );
  }

  console.log('-'.repeat(56));
  console.log(`合计       表 ${grand.tables} 张   行 ${grand.rows}   ${grand.bad === 0 ? '全部一致' : `差异 ${grand.bad} 张`}`);

  if (problems.length) {
    console.log('\n差异明细：');
    problems.slice(0, 40).forEach((p) => console.log(p));
    if (problems.length > 40) console.log(`  ... 另有 ${problems.length - 40} 条`);
  }

  await src.end();
  await ver.end();
})();
