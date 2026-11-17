// 扫描 ruoyi_ylsp 中各业务表的行数，找出空表
const mysql = require('mysql2/promise');

(async () => {
  const conn = await mysql.createConnection({
    host: '127.0.0.1', user: 'root', password: 'ZBrj@2026',
    database: 'ruoyi_ylsp', dateStrings: true, multipleStatements: true
  });
  const [tabs] = await conn.query(
    `SELECT table_name tn, table_rows tr FROM information_schema.tables
     WHERE table_schema='ruoyi_ylsp' ORDER BY table_name`);
  const biz = tabs.filter(t => /^(wms|mms|qms|pms|pdm|fin|mk|bi|hr|safety|bid|flowable|gen)_/.test(t.tn));
  const out = [];
  for (const t of biz) {
    const [[{ c }]] = await conn.query(`SELECT COUNT(*) c FROM \`${t.tn}\``);
    out.push({ tn: t.tn, c });
  }
  const empty = out.filter(o => o.c === 0);
  const small = out.filter(o => o.c > 0 && o.c < 20);
  console.log('=== 空表 (' + empty.length + ') ===');
  empty.forEach(o => console.log(o.tn));
  console.log('\n=== 数据很少 (1-19行) ===');
  small.forEach(o => console.log(o.tn, o.c));
  console.log('\n=== 有数据 (>=20) ===');
  out.filter(o => o.c >= 20).forEach(o => console.log(o.tn, o.c));
  await conn.end();
})();
