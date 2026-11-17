const mysql = require('mysql2/promise');
const suffix = process.argv[2] || 'p';
const tabs = process.argv.slice(3);
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp'});
  for (const t of tabs) {
    const [[{c}]] = await conn.query(`SELECT COUNT(*) c FROM \`${t}\``);
    if (c === 0) { console.log(t, '空表，跳过备份'); continue; }
    await conn.query(`DROP TABLE IF EXISTS \`${t}_bak20261116${suffix}\``);
    await conn.query(`CREATE TABLE \`${t}_bak20261116${suffix}\` LIKE \`${t}\``);
    await conn.query(`INSERT INTO \`${t}_bak20261116${suffix}\` SELECT * FROM \`${t}\``);
    console.log('备份', t, c);
  }
  await conn.end();
})();
