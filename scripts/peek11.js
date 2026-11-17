const mysql = require('mysql2/promise');
(async () => {
  const conn = await mysql.createConnection({host:'127.0.0.1',user:'root',password:'ZBrj@2026',database:'ruoyi_ylsp',dateStrings:true});
  const q = async (s,p=[]) => { const [r]=await conn.query(s,p); return r; };
  const cols = async t => (await q(`SELECT column_name FROM information_schema.columns WHERE table_schema='ruoyi_ylsp' AND table_name=? ORDER BY ordinal_position`,[t])).map(x=>x.column_name).join(',');
  for (const t of ['pms_receive','pms_receive_detail','pms_purchase_order','pms_purchase_order_detail','pms_purchase_plan','pms_invoice_detail']) console.log(t+':\n  '+await cols(t));
  console.table(await q(`SELECT * FROM pms_receive LIMIT 2`));
  console.table(await q(`SELECT * FROM pms_purchase_plan LIMIT 3`));
  await conn.end();
})();
