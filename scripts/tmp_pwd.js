const mysql = require('mysql2/promise');
const mode = process.argv[2];
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp'});
  const ORIG = '$2a$10$BWb2O.SxvYS90abIBtOUbuD5cAqzHbhJjJm7m2axBW..24M8JhGZm';
  const TEMP = '$2a$10$124zB/ba2klS/hMGuUGFueltVYDhIjrljl8i0PWEQuvg204QXpcmG';
  await conn.query('UPDATE sys_user SET password=? WHERE user_name=?', [mode==='temp'?TEMP:ORIG, 'admin']);
  console.log('password set to', mode);
  await conn.end();
})();
