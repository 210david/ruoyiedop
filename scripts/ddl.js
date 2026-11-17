const mysql = require('mysql2/promise');
const tabs = process.argv.slice(2);
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  for (const t of tabs) {
    const [c] = await conn.query(
      `SELECT column_name n, column_type t, is_nullable nl, column_default d, column_key k, extra e
       FROM information_schema.columns WHERE table_schema='ruoyi_ylsp' AND table_name=? ORDER BY ordinal_position`, [t]);
    console.log('### '+t);
    console.log(c.map(x=>`  ${x.n} ${x.t}${x.nl==='NO'?' NOT NULL':''}${x.d!=null?' DEF='+x.d:''}${x.e.includes('auto_increment')?' AI':''}`).join('\n'));
  }
  await conn.end();
})();
