const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp'});
  const [r] = await conn.query(`SELECT user_id,user_name,password FROM sys_user WHERE user_name='admin'`);
  console.log(r);
  await conn.end();
})();
