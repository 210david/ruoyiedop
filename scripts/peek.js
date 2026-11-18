const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  console.log('--- work_order columns ---');
  console.log((await q(`SELECT column_name,column_type FROM information_schema.columns WHERE table_schema='ruoyi_ylsp' AND table_name='mms_work_order' ORDER BY ordinal_position`)).map(c=>c.column_name+':'+c.column_type).join(', '));
  console.log('\n--- work_order 样例 ---');
  console.table(await q(`SELECT * FROM mms_work_order LIMIT 3`));
  console.log('\n--- work_order 状态分布 ---');
  console.log(await q(`SELECT status,COUNT(*) c FROM mms_work_order GROUP BY status`));
  console.log('\n--- route_process 样例 ---');
  console.table(await q(`SELECT * FROM mms_route_process LIMIT 3`));
  console.log('\n--- mms_route 样例 ---');
  console.table(await q(`SELECT route_id,route_no,product_code,product_name,version,status FROM mms_route LIMIT 3`));
  await conn.end();
})();
