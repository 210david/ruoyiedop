// 通用查询：node scripts/q.js "SELECT ..."
const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({ host: '127.0.0.1', user: 'root', password: 'ZBrj@2026', database: 'ruoyi_ylsp', dateStrings: true });
  const sql = process.argv.slice(2).join(' ');
  const [rows] = await conn.query(sql);
  if (Array.isArray(rows)) {
    console.log(`rows: ${rows.length}`);
    console.table(rows.slice(0, 40));
  } else {
    console.log(rows);
  }
  await conn.end();
})().catch(e => { console.error('ERR', e.message); process.exit(1); });
